
package com.oxthink.web3;

import com.oxthink.constant.ChainInfo;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TokenUtil {

    private final Web3jUtil web3;

    public TokenUtil(Web3jUtil web3jUtil) {
        web3 = web3jUtil;
    }

    public TokenUtil(String url) {
        web3 = new Web3jUtil(url);
    }

    public TokenUtil(ChainInfo chainInfo) {
        web3 = new Web3jUtil(chainInfo);
    }

    /**
     * 获取Token名称
     *
     * @param tokenAddress token地址
     * @return Token名称
     * @throws Exception 与合约交互出现异常
     */
    public String getName(String tokenAddress) throws Exception {
        List<TypeReference<?>> output = Collections.singletonList(new TypeReference<Utf8String>() {
        });
        List<Type> result = web3.readContract(tokenAddress, "name", new ArrayList<>(), output);
        return result.get(0).toString();
    }

    /**
     * 获取Token标志
     *
     * @param tokenAddress token地址
     * @return Token标志
     * @throws Exception 与合约交互出现异常
     */
    public String getSymbol(String tokenAddress) throws Exception {
        List<TypeReference<?>> output = Collections.singletonList(new TypeReference<Utf8String>() {
        });
        List<Type> result = web3.readContract(tokenAddress, "symbol", new ArrayList<>(), output);
        return result.get(0).toString();
    }

    /**
     * 获取Token精度
     *
     * @param tokenAddress token地址
     * @return Token精度
     * @throws Exception 与合约交互出现异常
     */
    public String getDecimals(String tokenAddress) throws Exception {
        List<TypeReference<?>> output = Collections.singletonList(new TypeReference<Uint8>() {
        });
        List<Type> result = web3.readContract(tokenAddress, "decimals", new ArrayList<>(), output);
        BigInteger decimals = (BigInteger) result.get(0).getValue();
        return decimals.toString();
    }

    /**
     * 获取代币总供应量
     *
     * @param tokenAddress token地址
     * @return token总供应量, 单位Wei
     * @throws Exception 与合约交互出现异常
     */
    public BigInteger getTotalSupply(String tokenAddress) throws Exception {
        List<TypeReference<?>> output = Collections.singletonList(new TypeReference<Uint256>() {
        });
        List<Type> result = web3.readContract(tokenAddress, "totalSupply", new ArrayList<>(), output);
        return (BigInteger) result.get(0).getValue();
    }

    /**
     * 获取某个代币的余额
     *
     * @param tokenAddress 代币合约地址
     * @param address      查询地址
     * @return 某种代币的余额，单位wei
     * @throws Exception 与合约交互出现异常
     */
    public String balanceOf(String tokenAddress, String address) throws Exception {
        List input = Collections.singletonList(new Address(address));
        List output = Collections.singletonList(new TypeReference<Uint256>() {
        });
        List o = web3.readContract(tokenAddress, "balanceOf", input, output);
        Uint256 balance = (Uint256) o.get(0);
        return balance.getValue().toString();
    }


}
