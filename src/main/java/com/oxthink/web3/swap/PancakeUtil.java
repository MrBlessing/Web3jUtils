package com.oxthink.web3.swap;

import com.oxthink.constant.ChainInfo;
import com.oxthink.constant.ContractList;
import com.oxthink.constant.RouterInfo;
import com.oxthink.constant.TokenInfo;
import com.oxthink.web3.Web3jUtil;
import lombok.extern.slf4j.Slf4j;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.utils.Convert;

import java.math.BigInteger;
import java.util.*;

@Slf4j
public class PancakeUtil extends RouterUtil {

    {
        // 常用用于组lp的代币信息
        commonPairToken.add(TokenInfo.WBNB_BSC_MAIN.getAddress());
        commonPairToken.add(TokenInfo.USDT_BSC_MAIN.getAddress());
        commonPairToken.add(TokenInfo.CAKE_BSC_MAIN.getAddress());
        commonPairToken.add(TokenInfo.BUSD_BSC_MAIN.getAddress());
    }


    public PancakeUtil() {
        super(new Web3jUtil(ChainInfo.BSC_MAIN), RouterInfo.PANCAKE_BSC_MAIN);
    }

    public PancakeUtil(String privateKey) {
        super(new Web3jUtil(ChainInfo.BSC_MAIN, privateKey), RouterInfo.PANCAKE_BSC_MAIN);
    }

    public PancakeUtil(Web3jUtil web3) {
        super(web3, RouterInfo.PANCAKE_BSC_MAIN);
    }

    /**
     * 获取糖浆池奖励
     *
     * @param syrupAddress 糖浆池地址
     * @param userAddress  查询地址
     * @return 糖浆池奖励CAKE数量
     * @throws Exception 与节点交互失败会抛出异常
     */
    public BigInteger getSyrupPoolReward(String syrupAddress, String userAddress) throws Exception {
        List<Type> input = Collections.singletonList(new Address(userAddress));
        List<TypeReference<?>> output = Collections.singletonList(new TypeReference<Uint256>() {
        });
        List<Type> result = web3.readContract(syrupAddress, "pendingReward", input, output);
        return (BigInteger) result.get(0).getValue();
    }

    /**
     * 获取糖浆池奖励Token
     *
     * @param syrupAddress 糖浆池地址
     * @return 糖浆池奖励Token地址
     * @throws Exception 与节点交互失败会抛出异常
     */
    public String getSyrupRewardToken(String syrupAddress) throws Exception {
        List<TypeReference<?>> output = Collections.singletonList(new TypeReference<Address>() {
        });
        List<Type> result = web3.readContract(syrupAddress, "rewardToken", new ArrayList<>(), output);
        Address rewardToken = (Address) result.get(0);
        return rewardToken.getValue();
    }

    /**
     * 获取用户在糖浆池的信息
     *
     * @param syrupAddress 糖浆池地址
     * @param userAddress  查询地址
     * @return amount：用户在该糖浆池中存储的CAKE数量
     * @throws Exception 与节点交互失败会抛出异常
     */
    public Map<String, BigInteger> getSyrupUserInfo(String syrupAddress, String userAddress) throws Exception {
        List<Type> input = Collections.singletonList(new Address(userAddress));
        List<TypeReference<?>> output = Arrays.asList(new TypeReference<Uint256>() {
        }, new TypeReference<Uint256>() {
        });
        List<Type> result = web3.readContract(syrupAddress, "userInfo", input, output);
        BigInteger amount = (BigInteger) result.get(0).getValue();
        BigInteger rewardDebt = (BigInteger) result.get(1).getValue();
        Map<String, BigInteger> map = new HashMap<>(4);
        map.put("amount", amount);
        map.put("rewardDebt", rewardDebt);
        return map;
    }

    /**
     * 测试某个token是否为貔貅币
     * web3对象需要与一个至少有0.01bnb余额的私钥对应
     *
     * @param tokenAddress token地址
     * @return 是否是貔貅
     * @throws Exception 无法取得pair地址会抛出异常
     */
    public boolean piXiuCheck(String tokenAddress) throws Exception {
        // 设置token买入路径 默认bnb -> token
        List<Address> pathIn = getPathWithAddress(TokenInfo.WBNB_BSC_MAIN.getAddress(), "0.01", tokenAddress);
        // 获取token和bnb组成的pair地址
        String pairAddress = getPairAddress(pathIn.get(pathIn.size() - 2).getValue(), tokenAddress);
//        String pairAddress = pairUtil.getPair(ContractList.PANCAKE_PAIR_FACTORY_BSC_MAIN, pathIn.get(pathIn.size() - 2).getValue(), tokenAddress);
        List<Type> input = Arrays.asList(new Address(tokenAddress), new DynamicArray<>(pathIn), new Address(pairAddress));
        // 生成需要调用函数的data
        Function function = new Function("checkToken", input, new ArrayList<>());
        String data = FunctionEncoder.encode(function);
        // 携带的bnb Value
        BigInteger value = Convert.toWei("0.01", Convert.Unit.ETHER).toBigInteger();
        try {
            BigInteger limit = web3.estimateGasLimit(ContractList.PIXIU_CHECK_BSC_MAIN, data, value);
            log.info(String.format("买入卖出测试成功，花费gas %s wei，非貔貅币", limit));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
