package com.oxthink.web3;

import com.oxthink.constant.ChainInfo;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint112;
import org.web3j.abi.datatypes.generated.Uint32;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;

public class PairUtil {

    private final Web3jUtil web3;

    private final TokenUtil tokenUtil;

    public PairUtil(Web3jUtil web3jUtil) {
        web3 = web3jUtil;
        tokenUtil = new TokenUtil(web3);
    }

    public PairUtil(String url) {
        web3 = new Web3jUtil(url);
        tokenUtil = new TokenUtil(web3);
    }

    public PairUtil(ChainInfo chainInfo) {
        web3 = new Web3jUtil(chainInfo);
        tokenUtil = new TokenUtil(web3);
    }

    /**
     * 获取LPPair中两种代币的存储量
     *
     * @param pairAddress Pair地址
     * @return token0: token0数量，单位wei
     * token1: token1数量，单位wei
     * @throws Exception 与节点交互失败会抛出异常
     */
    public Map<String, BigInteger> getPairReserves(String pairAddress) throws Exception {
        List<TypeReference<?>> output = Arrays.asList(new TypeReference<Uint112>() {
        }, new TypeReference<Uint112>() {
        }, new TypeReference<Uint32>() {
        });
        List<Type> result = web3.readContract(pairAddress, "getReserves", new ArrayList<>(), output);
        Map<String, BigInteger> info = new HashMap<>(4);
        info.put("token0", (BigInteger) result.get(0).getValue());
        info.put("token1", (BigInteger) result.get(1).getValue());
        return info;
    }

    /**
     * 获取Token0地址
     *
     * @param pairAddress Pair地址
     * @return Token0地址
     * @throws Exception 与节点交互失败会抛出异常
     */
    public String getToken0(String pairAddress) throws Exception {
        List<TypeReference<?>> output = Collections.singletonList(new TypeReference<Address>() {
        });
        List<Type> result = web3.readContract(pairAddress, "token0", new ArrayList<>(), output);
        Address token0 = (Address) result.get(0);
        return token0.getValue();
    }

    /**
     * 获取Token1地址
     *
     * @param pairAddress Pair地址
     * @return Token1地址
     * @throws Exception 与节点交互失败会抛出异常
     */
    public String getToken1(String pairAddress) throws Exception {
        List<TypeReference<?>> output = Collections.singletonList(new TypeReference<Address>() {
        });
        List<Type> result = web3.readContract(pairAddress, "token1", new ArrayList<>(), output);
        Address token0 = (Address) result.get(0);
        return token0.getValue();
    }


    /**
     * Pair数量转化为两种代币的数量
     *
     * @param pairAddress pair地址
     * @param pairAmount  pair数量
     * @return 两种代币的数量, 单位wei
     * @throws Exception 与节点交互失败会抛出异常
     */
    public Map<String, BigInteger> getTwoTokenAmount(String pairAddress, BigInteger pairAmount) throws Exception {
        Map<String, BigInteger> map = getPairReserves(pairAddress);
        BigDecimal factor = new BigDecimal(pairAmount).divide(new BigDecimal(tokenUtil.getTotalSupply(pairAddress)), 18, RoundingMode.FLOOR);
        BigInteger token0Amount = factor.multiply(new BigDecimal(map.get("token0"))).toBigInteger();
        BigInteger token1Amount = factor.multiply(new BigDecimal(map.get("token1"))).toBigInteger();
        Map<String, BigInteger> info = new HashMap<>(4);
        info.put("token0Amount", token0Amount);
        info.put("token1Amount", token1Amount);
        return info;
    }

    /**
     * 获取pair的工厂合约
     *
     * @param pairAddress pair地址
     * @return 工厂合约
     * @throws Exception 与节点交互失败会抛出异常
     */
    public String getPairFactory(String pairAddress) throws Exception {
        List<TypeReference<?>> output = Collections.singletonList(new TypeReference<Address>() {
        });
        List<Type> result = web3.readContract(pairAddress, "factory", new ArrayList<>(), output);
        Address token0 = (Address) result.get(0);
        return token0.getValue();
    }

    /**
     * 获取两种Token组合成的Pair地址
     * @param factory pair工厂地址
     * @param token0 token0
     * @param token1 token0
     * @return Pair地址
     * @throws Exception 与节点交互失败
     */
    public String getPair(String factory, String token0, String token1) throws Exception {
        List<Type> input = Arrays.asList(new Address(token0), new Address(token1));
        List<TypeReference<?>> output = Collections.singletonList(new TypeReference<Address>() {
        });
        List<Type> result = web3.readContract(factory, "getPair", input, output);
        Address lp = (Address) result.get(0);
        return lp.getValue();
    }

}
