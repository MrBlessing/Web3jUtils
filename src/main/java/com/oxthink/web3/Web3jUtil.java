package com.oxthink.web3;

import com.oxthink.constant.ChainInfo;
import io.github.novacrypto.bip39.MnemonicGenerator;
import io.github.novacrypto.bip39.SeedCalculator;
import io.github.novacrypto.bip39.Words;
import io.github.novacrypto.bip39.wordlists.English;
import io.github.novacrypto.hashing.Sha256;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.wallet.DeterministicSeed;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.*;

@Slf4j
public class Web3jUtil {

    /**
     * provide对象
     */
    private final Web3j web3;
    /**
     * 凭证对象
     */
    private Credentials credentials;
    /**
     * 操作的账户
     */
    private String ownerAddress;

    /**
     * 最大能容忍的gasPrice
     */
    private BigInteger maxGasPrice;
    /**
     * 最小的gasPrice
     */
    private BigInteger minGasPrice;
    /**
     * 本地维护的nonce
     */
    private BigInteger nonce = new BigInteger("-1");
    /**
     * gasLimit
     */
    private BigInteger gasLimit = new BigInteger("0");

    /**
     * 关联的网络信息
     */
    private ChainInfo chainInfo;

    public Web3j getWeb3() {
        return web3;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    /**
     * 初始化Web3j变量和凭证
     *
     * @param chainInfo  网络信息
     * @param privateKey 托管私钥
     */
    public Web3jUtil(ChainInfo chainInfo, String privateKey) {
        this.chainInfo = chainInfo;
        // 根据RPCurl生成web3j对象
        this.web3 = Web3j.build(new HttpService(chainInfo.getNodeUrl()));
        // 根据私钥创建凭证对象
        this.credentials = Credentials.create(privateKey);
        // 私钥对应的地址
        this.ownerAddress = credentials.getAddress();
        // 默认gasPrice的范围
        setGasPriceLimit("1", "10");
    }

    /**
     * 初始化Web3j变量和凭证
     *
     * @param rpcUrl     RPC服务器链接
     * @param privateKey 托管私钥
     */
    public Web3jUtil(String rpcUrl, String privateKey) {
        // 根据RPCurl生成web3j对象
        this.web3 = Web3j.build(new HttpService(rpcUrl));
        // 根据私钥创建凭证对象
        this.credentials = Credentials.create(privateKey);
        // 私钥对应的地址
        this.ownerAddress = credentials.getAddress();
        // 默认gasPrice的范围
        setGasPriceLimit("1", "10");
    }

    /**
     * 初始化Web3j变量
     *
     * @param RPCurl RPC服务器链接
     */
    public Web3jUtil(String RPCurl) {
        // 根据RPCurl生成web3j对象
        web3 = Web3j.build(new HttpService(RPCurl));

    }

    public Web3jUtil(ChainInfo chainInfo) {
        this.chainInfo = chainInfo;
        // 根据RPCurl生成web3j对象
        this.web3 = Web3j.build(new HttpService(chainInfo.getNodeUrl()));
    }

    /**
     * 获取主网代币余额
     *
     * @param address 某个地址
     * @return 主网余额
     * @throws IOException 与节点交互出现异常
     */
    public String getBalance(String address) throws IOException {
        EthGetBalance ethGetBalance = web3.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
        // 格式转换 WEI(币种单位) --> ETHER
        return Convert.fromWei(new BigDecimal(ethGetBalance.getBalance()), Convert.Unit.ETHER).toPlainString();
    }

    /**
     * 获取代理账户的主网代币余额
     *
     * @return 主网余额
     * @throws IOException 与节点交互出现异常
     */
    public String getBalance() throws IOException {
        return getBalance(getOwnerAddress());
    }

    /**
     * 发送主网代币
     *
     * @param address 发送的地址
     * @param amount  数量 单位：ether
     * @return 交易hash
     * @throws Exception 与节点交互出现异常
     */
    public String sendEther(String address, String amount) throws Exception {
        // 估算gasLimit
        BigInteger gasLimit = estimateGasLimit(address, "");
        // 获取gasPrice
        BigInteger gasPrice = getGasPriceWithLimit();
        // 获取chainId
        long chainId = web3.ethChainId().send().getChainId().longValue();
        // 组建请求的参数
        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(getNonce(),
                gasPrice,
                gasLimit,
                address,
                Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger());
        // 签名数据
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
        String hexValue = Numeric.toHexString(signedMessage);
        // 发送数据
        EthSendTransaction response = web3.ethSendRawTransaction(hexValue).send();
        // 查看是否有错误
        if (response.hasError()) {
            throw new Exception("trade hash: " + response.getTransactionHash() +
                    "\nerror: " + response.getError().getMessage());
        }
        log.info("Gas fee: {} ETH", Convert.fromWei(String.valueOf(gasLimit.multiply(gasPrice)), Convert.Unit.ETHER));
        log.info("Trade Hash: {}", response.getTransactionHash());
        return response.getTransactionHash();
    }

    /**
     * 发送账户所有的主网代币
     *
     * @param address 发送的地址
     * @return 交易hash
     * @throws Exception 与节点交互出现异常
     */
    public String sendAllEther(String address) throws Exception {
        // 估算gasLimit
        BigInteger gasLimit = estimateGasLimit(address, "");
        // 获取gasPrice
        BigInteger gasPrice = getGasPriceWithLimit();
        // 计算消耗的gas(gasPrice的单位是GWEI),单位转换为ether
        BigDecimal costEth = new BigDecimal(gasPrice.multiply(gasLimit)).divide(BigDecimal.valueOf(Math.pow(10, 18)), 18, RoundingMode.HALF_UP);
        // 地址的eth余额
        String balance = getBalance();
        // 计算需要发送的ether
        String amount = new BigDecimal(balance).subtract(costEth).toString();
        return sendEther(address, amount);
    }

    /**
     * 与合约交互
     *
     * @param contractAddress 交互合约地址
     * @param functionName    交互函数名称
     * @param input           输入参数 eg:Arrays.asList(new Address("0x6dF655480F465DC36347a5616E875D155804F0c5"), new Uint256(10000000));
     * @param output          输出参数类型 eg: Arrays.asList(new TypeReference&lt;Bool&gt;(){});
     *                        类型映射关系
     *                        boolean - bool
     *                        BigInteger - uint/int
     *                        byte[] - string and address types
     *                        *                        Listbytes
     *                        String -  - dynamic/static array
     *                        T - struct/tuple types
     * @return 交易hash
     * @throws Exception 与节点交互出现异常
     */
    public String writeContract(String contractAddress, String functionName, List<Type> input, List<TypeReference<?>> output) throws Exception {
        return writeContract(contractAddress, functionName, "0", input, output);
    }

    /**
     * 与合约交互
     *
     * @param contractAddress 交互合约地址
     * @param functionName    交互函数名称
     * @param value           携带的eth数量(单位Ether)
     * @param input           输入参数 eg:Arrays.asList(new Address("0x6dF655480F465DC36347a5616E875D155804F0c5"), new Uint256(10000000));
     * @param output          输出参数类型 eg: Arrays.asList(new TypeReference&lt;Bool&gt;(){});
     *                        类型映射关系
     *                        boolean - bool
     *                        BigInteger - uint/int
     *                        byte[] - bytes
     *                        String - string and address types
     *                        List - dynamic/static array
     *                        T - struct/tuple types
     * @return 交易hash
     * @throws Exception 与节点交互出现异常
     */
    public String writeContract(String contractAddress, String functionName, String value, List<Type> input, List<TypeReference<?>> output) throws Exception {
        // 转换value的单位
        BigInteger valueWei = Convert.toWei(value, Convert.Unit.ETHER).toBigInteger();
        // 生成需要调用函数的data
        Function function = new Function(functionName, input, output);
        String data = FunctionEncoder.encode(function);
        // 估算gasLimit
        BigInteger gasLimit = estimateGasLimit(contractAddress, data, valueWei);
        // 获取gasPrice
        BigInteger gasPrice = getGasPriceWithLimit();
        // 获取chainId
        long chainId = web3.ethChainId().send().getChainId().longValue();
        // 正式请求
        RawTransaction rawTransaction = RawTransaction.createTransaction(getNonce(), gasPrice, gasLimit, contractAddress, valueWei, data);
        // 签名数据
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
        String hexValue = Numeric.toHexString(signedMessage);
        // 发送数据
        EthSendTransaction response = web3.ethSendRawTransaction(hexValue).send();
        // 查看是否有错误
        if (response.hasError()) {
            throw new Exception("trade hash: " + response.getTransactionHash() +
                    "\nerror: " + response.getError().getMessage());
        }
        log.info("function: {} data: {}", functionName, data);
        log.info("Gas fee: {} ETH", Convert.fromWei(String.valueOf(gasLimit.multiply(gasPrice)), Convert.Unit.ETHER));
        log.info("Trade Hash: {}", response.getTransactionHash());
        return response.getTransactionHash();
    }


    /**
     * 直接发送data模拟请求
     *
     * @param contractAddress 交互合约地址
     * @param data            交互数据
     * @return 交易hash
     * @throws Exception 与节点交互出现异常
     */
    public String writeContract(String contractAddress, String data) throws Exception {
        // 估算gasLimit
        BigInteger gasLimit = estimateGasLimit(contractAddress, data);
        // 获取gasPrice
        BigInteger gasPrice = getGasPriceWithLimit();
        // 获取chainId
        long chainId = web3.ethChainId().send().getChainId().longValue();
        // 正式请求
        RawTransaction rawTransaction = RawTransaction.createTransaction(getNonce(), gasPrice, gasLimit, contractAddress, data);
        // 签名数据
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
        String hexValue = Numeric.toHexString(signedMessage);
        // 发送数据
        EthSendTransaction response = web3.ethSendRawTransaction(hexValue).send();
        // 查看是否有错误
        if (response.hasError()) {
            throw new Exception("trade hash: " + response.getTransactionHash() +
                    "\nerror: " + response.getError().getMessage());
        }
        log.info("data: {}", data);
        log.info("Gas fee: {} ETH", Convert.fromWei(String.valueOf(gasLimit.multiply(gasPrice)), Convert.Unit.ETHER));
        log.info("Trade Hash: {}", response.getTransactionHash());
        return response.getTransactionHash();
    }


    /**
     * 查看交易状态
     *
     * @param hash 交易hash
     * @return -1表示交易失败 0表示交易未打包 1表示交易成功
     * @throws IOException 与节点交互出现异常
     */
    public int getTransactionStatus(String hash) throws Exception {
        Optional<TransactionReceipt> optional = web3.ethGetTransactionReceipt(hash).send().getTransactionReceipt();
        if (optional.isPresent()) {
            TransactionReceipt transactionReceipt = optional.get();
            if (transactionReceipt.getStatus() == null) {
                return 0;
            }
            if ("0x1".equals(transactionReceipt.getStatus())) {
                return 1;
            }
            if ("0x0".equals(transactionReceipt.getStatus())) {
                return -1;
            }
        }
        return 0;
    }

    /**
     * 等待交易完成
     *
     * @param hash     交易hash
     * @param waitTime 最大等待时间  单位:ms
     * @return 交易是否成功
     * @throws Exception 与节点交互出现异常
     */
    public boolean blockTransactionUtilComplete(String hash, int waitTime) throws Exception {
        long endTime = System.currentTimeMillis() + waitTime;
        while (System.currentTimeMillis() <= endTime) {
            int status = getTransactionStatus(hash);
            if (status == 1) {
                return true;
            }
            if (status == -1) {
                return false;
            }
            // 0.5s检查一次交易是否完成
            Thread.sleep(500);
        }
        return false;
    }

    /**
     * 获取当期的gasPrice,如果超过最大的限制，取最大限制
     *
     * @return 在区间内的gasPrice
     * @throws IOException 与节点交互出现异常
     */
    public BigInteger getGasPriceWithLimit() throws IOException {
        // 获取近几个区块的gasPrice,得到的gasPrice偏高
        BigInteger gasPrice = web3.ethGasPrice().send().getGasPrice();
        log.info("Gas price: {} Gwei, Min gas price: {} Gwei, Max gas price: {} Gwei",
                Convert.fromWei(String.valueOf(gasPrice), Convert.Unit.GWEI),
                Convert.fromWei(String.valueOf(minGasPrice), Convert.Unit.GWEI),
                Convert.fromWei(String.valueOf(maxGasPrice), Convert.Unit.GWEI));
        // 超过最大限制返回最大的gasPrice
        if (maxGasPrice.compareTo(gasPrice) < 0) {
            return maxGasPrice;
        }
        // 小于最小的限制返回最小的gasPrice
        if (minGasPrice.compareTo(gasPrice) > 0) {
            return minGasPrice;
        }
        return gasPrice;
    }

    /**
     * 设置gasPrice的限制，默认最大为5，最小为1
     *
     * @param minGwei 最小的gasLimit
     * @param maxGwei 最大的gasLimit
     */
    public void setGasPriceLimit(String minGwei, String maxGwei) {
        minGasPrice = Convert.toWei(minGwei, Convert.Unit.GWEI).toBigInteger();
        maxGasPrice = Convert.toWei(maxGwei, Convert.Unit.GWEI).toBigInteger();
    }

    /**
     * 设置交易所需的gasLimit，该值为0则自动估计gasLimit
     *
     * @param gasLimit gasLimit
     */
    public void setGasLimit(String gasLimit) {
        this.gasLimit = new BigInteger(gasLimit);
    }

    /**
     * 设置交易所需的nonce,r若nonce没有交易笔数大则取交易笔数作为nonce
     *
     * @param nonce gasLimit
     */
    public void setNonce(String nonce) {
        this.nonce = new BigInteger(nonce);
    }

    /**
     * 获取交易数量
     *
     * @return 账户交易次数
     * @throws IOException 与节点交互失败
     */
    public BigInteger getTransactionCount() throws IOException {
        EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(
                ownerAddress, DefaultBlockParameterName.LATEST).send();
        return ethGetTransactionCount.getTransactionCount();
    }

    /**
     * 估算GasLimit
     *
     * @param to   发送的地址
     * @param data 发送的数据
     * @return GasLimit
     * @throws IOException 与节点交互失败
     */
    public BigInteger estimateGasLimit(String to, String data) throws Exception {
        if (gasLimit.intValue() != 0) {
            return gasLimit;
        }
        Transaction testTransaction =
                Transaction.createEthCallTransaction(ownerAddress, to, data);
        EthEstimateGas response = web3.ethEstimateGas(testTransaction).send();
        // 查看是否有错误
        if (response.hasError()) {
            throw new Exception("error: " + response.getError().getMessage());
        }
        return response.getAmountUsed();
    }

    /**
     * 估算GasLimit
     *
     * @param to    发送的地址
     * @param data  发送的数据
     * @param value 携带的eth数量(单位wei)
     * @return GasLimit
     * @throws Exception 与节点交互失败
     */
    public BigInteger estimateGasLimit(String to, String data, BigInteger value) throws Exception {
        if (gasLimit.intValue() != 0) {
            return gasLimit;
        }
        Transaction testTransaction = Transaction.createFunctionCallTransaction(ownerAddress, null, null, null, to, value, data);
        EthEstimateGas response = web3.ethEstimateGas(testTransaction).send();
        // 查看是否有错误
        if (response.hasError()) {
            throw new Exception("error: " + response.getError().getMessage());
        }
        return response.getAmountUsed();
    }

    /**
     * 获取本地维护的nonce
     *
     * @return nonce
     * @throws IOException 与节点交互失败
     */
    public BigInteger getNonce() throws IOException {
        nonce = nonce.add(BigInteger.valueOf(1));
        // 查询交易笔数
        BigInteger count = getTransactionCount();
        // 如果交易笔数大于本地维护的nonce，则同步nonce
        if (count.compareTo(nonce) > 0) {
            nonce = count;
        }
        return nonce;
    }

    /**
     * 获取owner地址
     *
     * @return web3对象维护的地址
     */
    public String getOwnerAddress() {
        return ownerAddress;
    }

    /**
     * 读取合约状态
     *
     * @param contractAddress 合约地址
     * @param functionName    合约函数名称
     * @param input           输入参数和类型
     * @param output          输出类型
     * @return 合约函数返回值
     * @throws Exception 与节点交互失败
     */
    public List<Type> readContract(String contractAddress, String functionName, List<Type> input, List<TypeReference<?>> output) throws Exception {
        // 生成需要调用函数的data
        Function function = new Function(functionName, input, output);
        String data = FunctionEncoder.encode(function);
        // 组建请求的参数
        EthCall response = web3.ethCall(
                        Transaction.createEthCallTransaction(ownerAddress, contractAddress, data),
                        DefaultBlockParameterName.LATEST)
                .send();
        return FunctionReturnDecoder.decode(
                response.getValue(), function.getOutputParameters());
    }

    /**
     * 授权操作(授权数量为2^256-1wei)
     *
     * @param contractAddress 交互合约地址
     * @param spender         被授权的合约地址
     * @return 交易hash
     * @throws Exception 与节点交互失败
     */
    public String approve(String contractAddress, String spender) throws Exception {
        // 授权最大数量
        List input = Arrays.asList(new Address(spender)
                , new Uint256(new BigInteger("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16)));
        List output = Arrays.asList(new TypeReference<Bool>() {
        });
        return writeContract(contractAddress, "approve", input, output);
    }

    /**
     * 转账操作
     *
     * @param contractAddress 交互合约地址
     * @param recipient       接收转账地址
     * @param amount          转账数量
     * @return 交易hash
     * @throws Exception 与节点交互失败
     */
    public String transfer(String contractAddress, String recipient, String amount) throws Exception {
        List input = Arrays.asList(new Address(recipient)
                , new Uint256(new BigInteger(amount, 10)));
        List output = Arrays.asList(new TypeReference<Bool>() {
        });
        return writeContract(contractAddress, "transfer", input, output);
    }

    /**
     * 获取某个代币的余额
     *
     * @param contractAddress 代币合约地址
     * @param address         查询地址
     * @return 余额：单位ether
     * @throws Exception 与节点交互失败
     */
    public String balanceOf(String contractAddress, String address) throws Exception {
        List input = Arrays.asList(new Address(address));
        List output = Arrays.asList(new TypeReference<Uint256>() {
        });
        List<Type> o = readContract(contractAddress, "balanceOf", input, output);
        Uint256 balance = (Uint256) o.get(0);
        return Convert.fromWei(balance.getValue().toString(), Convert.Unit.ETHER).toString();
    }

    /**
     * 获取自己某个代币的余额
     *
     * @param contractAddress 代币合约地址
     * @return 代币的余额
     * @throws Exception 与节点交互失败
     */
    public String balanceOf(String contractAddress) throws Exception {
        List input = Arrays.asList(new Address(getOwnerAddress()));
        List output = Arrays.asList(new TypeReference<Uint256>() {
        });
        List<Type> o = readContract(contractAddress, "balanceOf", input, output);
        Uint256 balance = (Uint256) o.get(0);
        return Convert.fromWei(balance.getValue().toString(), Convert.Unit.ETHER).toString();
    }

    /**
     * 通过0.14.7版本的bitcoinj创建钱包
     *
     * @return 钱包相关信息
     * @throws MnemonicException.MnemonicLengthException 钱包创建失败
     */
    public static Map<String, String> createWalletV1() throws MnemonicException.MnemonicLengthException {
        SecureRandom secureRandom = new SecureRandom();
        byte[] entropy = new byte[DeterministicSeed.DEFAULT_SEED_ENTROPY_BITS / 8];
        secureRandom.nextBytes(entropy);

        //生成12位助记词
        List<String> str = MnemonicCode.INSTANCE.toMnemonic(entropy);

        //使用助记词生成钱包种子
        byte[] seed = MnemonicCode.toSeed(str, "");
        ECKeyPair keyPair = ECKeyPair.create(Sha256.sha256(seed));
        //通过公钥生成钱包地址
        String address = Keys.getAddress(keyPair.getPublicKey());

        Map<String, String> res = new HashMap<>(8);
        res.put("address", "0x" + address);
        res.put("wordHelper", str.toString());
        res.put("privateKey", keyPair.getPrivateKey().toString(16));
        res.put("publicKey", keyPair.getPublicKey().toString(16));
        return res;
    }

    /**
     * 通过novacrypto创建钱包
     *
     * @return 钱包相关信息
     */
    public static Map<String, String> createWalletV2() {
        // 生成助记词
        StringBuilder sb = new StringBuilder();
        byte[] entropy = new byte[Words.TWELVE.byteLength()];
        new SecureRandom().nextBytes(entropy);
        new MnemonicGenerator(English.INSTANCE).createMnemonic(entropy, sb::append);
        // 根据助记词生成种子
        byte[] seed = new SeedCalculator().calculateSeed(sb.toString(), "");
        // 根据种子生成私钥和公钥
        ECKeyPair keyPair = ECKeyPair.create(Sha256.sha256(seed));
        //根据公钥或者ECKeyPair获取钱包地址
        String address = Keys.getAddress(keyPair);

        Map<String, String> res = new HashMap<>(4);
        res.put("address", "0x" + address);
        res.put("wordHelper", sb.toString());
        res.put("privateKey", keyPair.getPrivateKey().toString(16));
        res.put("publicKey", keyPair.getPublicKey().toString(16));
        return res;
    }

    /**
     * 等待某个币数量小于或等于amount
     *
     * @param tokenAddress token地址
     * @param amount       小于的数量
     * @param waitTime     等待时间  单位:ms
     * @param ownerAddress 等待地址
     * @return 在限定时间内某个币数量小于或等于amount
     * @throws Exception 与节点交互失败
     */
    public boolean waitForTokenLTAmount(String tokenAddress, double amount, long waitTime, String ownerAddress) throws Exception {
        long endTime = System.currentTimeMillis() + waitTime;
        while (endTime >= System.currentTimeMillis()) {
            double leftAmount = Double.parseDouble(balanceOf(tokenAddress, ownerAddress));
            if (leftAmount < amount) {
                return true;
            }
            Thread.sleep(500);
        }
        // 超时
        return false;
    }

    /**
     * 等待某个币数量大于amount
     *
     * @param tokenAddress token地址
     * @param amount       大于的数量
     * @param waitTime     等待时间  单位:ms
     * @param ownerAddress 等待地址
     * @return 在限定时间内某个币数量大于amount
     * @throws Exception 与节点交互失败
     */
    public boolean waitForTokenGTAmount(String tokenAddress, double amount, long waitTime, String ownerAddress) throws Exception {
        long endTime = System.currentTimeMillis() + waitTime;
        while (endTime >= System.currentTimeMillis()) {
            double leftAmount = Double.parseDouble(balanceOf(tokenAddress, ownerAddress));
            if (leftAmount > amount) {
                return true;
            }
            Thread.sleep(500);
        }
        // 超时
        return false;
    }

    /**
     * 返回绑定网络的信息
     *
     * @return ChainInfo枚举类
     */
    public ChainInfo getChainInfo() {
        return chainInfo;
    }

    /**
     * 通过私钥获取公钥
     *
     * @param privateKey 私钥
     * @return 公钥地址
     */
    public static String getPublicAddress(String privateKey) {
        Credentials credentials = Credentials.create(privateKey);
        return credentials.getAddress();
    }
}
