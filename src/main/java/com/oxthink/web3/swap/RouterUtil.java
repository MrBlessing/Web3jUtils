package com.oxthink.web3.swap;

import com.oxthink.constant.RouterInfo;
import com.oxthink.constant.TokenInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.utils.Convert;
import com.oxthink.tool.StringUtil;
import com.oxthink.web3.PairUtil;
import com.oxthink.web3.Web3jUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
public class RouterUtil {

    /**
     * 路由地址
     */
    final RouterInfo routerInfo;

    /**
     * web3对象
     */
    final Web3jUtil web3;

    /**
     * 常用用于组lp的代币信息
     */
    public List<String> commonPairToken = new ArrayList<>();

    public RouterUtil(Web3jUtil web3, RouterInfo routerInfo) {
        this.web3 = web3;
        this.routerInfo = routerInfo;
    }

    public RouterUtil(String privateKey, RouterInfo routerInfo) {
        this.web3 = new Web3jUtil(routerInfo.getChainInfo(), privateKey);
        this.routerInfo = routerInfo;
    }

    public RouterUtil(RouterInfo routerInfo) {
        this.web3 = new Web3jUtil(routerInfo.getChainInfo());
        this.routerInfo = routerInfo;
    }

    /**
     * 获取代币兑换的输出
     *
     * @param amountIn 输入需要兑换的代币数量
     * @param paths    兑换路径
     * @return 代币兑换出的数量：单位ether
     * @throws Exception 与节点交互失败会抛出异常
     */
    public String getAmountOut(String amountIn, List<String> paths) throws Exception {
        // 对象转换
        List<Address> addressPath = new ArrayList<>();
        for (String path : paths) {
            addressPath.add(new Address(path));
        }
        List<Type> input = Arrays.asList(
                new Uint256(Convert.toWei(amountIn, Convert.Unit.ETHER).toBigInteger()),
                new DynamicArray<>(addressPath));
        List<TypeReference<?>> output = Collections.singletonList(new TypeReference<DynamicArray<Uint256>>() {
        });
        List<Type> res = web3.readContract(routerInfo.getRouterAddress(), "getAmountsOut", input, output);
        List<Uint256> amounts = ((DynamicArray<Uint256>) res.get(0)).getValue();
        // 拿到合约返回的结果(返回的最后结果，才是最终要输出的)
        BigInteger amountOut = amounts.get(amounts.size() - 1).getValue();
        return Convert.fromWei(amountOut.toString(), Convert.Unit.ETHER).toString();
    }

    /**
     * Token兑换token(输入token数量确认)
     *
     * @param amountIn     输入的代币数量
     * @param amountOutMin 输出的最小代币数量
     * @param paths        兑换路径
     * @param slippage     滑点
     * @return 交易hash
     * @throws Exception 与节点交互失败会抛出异常
     */
    public String swapExactTokensForTokens(String amountIn, String amountOutMin, List<String> paths, double slippage) throws Exception {
        // 计算滑点
        amountOutMin = new BigDecimal(amountOutMin).multiply(new BigDecimal(1 - slippage)).toBigInteger().toString();
        // 对象转换
        List<Address> addressPath = new ArrayList<>();
        for (String path : paths) {
            addressPath.add(new Address(path));
        }
        // 持续时间（20min）
        long time = (System.currentTimeMillis()) / 1000 + 1200;
        Uint256 deadline = new Uint256(new BigInteger(String.valueOf(time), 10));
        List<Type> input = Arrays.asList(
                new Uint256(Convert.toWei(amountIn, Convert.Unit.ETHER).toBigInteger()),
                new Uint256(Convert.toWei(amountOutMin, Convert.Unit.ETHER).toBigInteger()),
                new DynamicArray<>(addressPath),
                new Address(web3.getOwnerAddress()),
                deadline);
        List<TypeReference<?>> output = Collections.singletonList(new TypeReference<Bool>() {
        });
        return web3.writeContract(routerInfo.getRouterAddress(), "swapExactTokensForTokens", input, output);
    }

    /**
     * 用Token兑换ETH（输入TOKEN数量确定）
     *
     * @param amountIn     输入的代币数量
     * @param amountOutMin 输出的最小代币数量
     * @param paths        兑换路径
     * @param slippage     滑点
     * @return 交易hash
     * @throws Exception 与节点交互失败会抛出异常
     */
    public String swapExactTokensForETH(String amountIn, String amountOutMin, List<String> paths, double slippage) throws Exception {
        // 计算滑点
        amountOutMin = new BigDecimal(amountOutMin).multiply(new BigDecimal(1 - slippage)).toBigInteger().toString();
        // 对象转换
        List<Address> addressPath = new ArrayList<>();
        for (String path : paths) {
            addressPath.add(new Address(path));
        }
        // 持续时间（20min）
        long time = (System.currentTimeMillis()) / 1000 + 1200;
        Uint256 deadline = new Uint256(new BigInteger(String.valueOf(time), 10));
        List<Type> input = Arrays.asList(
                new Uint256(Convert.toWei(amountIn, Convert.Unit.ETHER).toBigInteger()),
                new Uint256(Convert.toWei(amountOutMin, Convert.Unit.ETHER).toBigInteger()),
                new DynamicArray<>(addressPath),
                new Address(web3.getOwnerAddress()),
                deadline);
        List<TypeReference<?>> output = Collections.singletonList(new TypeReference<Bool>() {
        });
        return web3.writeContract(routerInfo.getRouterAddress(), "swapExactTokensForETH", input, output);
    }

    /**
     * 用ETH兑换TOKEN（ETH输入数量确定）
     *
     * @param amountIn     输入的代币数量
     * @param amountOutMin 输出的最小代币数量
     * @param paths        兑换路径
     * @param slippage     滑点
     * @return 交易hash
     * @throws Exception 与节点交互失败会抛出异常
     */
    public String swapExactETHForTokens(String amountIn, String amountOutMin, List<String> paths, double slippage) throws Exception {
        // 计算滑点
        amountOutMin = new BigDecimal(amountOutMin).multiply(new BigDecimal(1 - slippage)).toBigInteger().toString();
        // 对象转换
        List<Address> addressPath = new ArrayList<>();
        for (String path : paths) {
            addressPath.add(new Address(path));
        }
        // 持续时间（20min）
        long time = (System.currentTimeMillis()) / 1000 + 1200;
        Uint256 deadline = new Uint256(new BigInteger(String.valueOf(time), 10));
        List<Type> input = Arrays.asList(
                new Uint256(Convert.toWei(amountOutMin, Convert.Unit.ETHER).toBigInteger()),
                new DynamicArray<>(addressPath),
                new Address(web3.getOwnerAddress()),
                deadline);
        List<TypeReference<?>> output = Collections.singletonList(new TypeReference<Bool>() {
        });
        return web3.writeContract(routerInfo.getRouterAddress(), "swapExactETHForTokens", amountIn, input, output);
    }

    /**
     * 获取使用的web3j对象
     *
     * @return web3j对象
     */
    public Web3jUtil getInnerWeb3j() {
        return web3;
    }

    /**
     * 卖出土狗
     *
     * @param tokenAddress    合约地址
     * @param gasPrice        gas价格
     * @param amountIn        卖出数量
     * @param minEthAmountOut 能接受的最小eth数量(不计算滑点)
     * @param slippage        卖出滑点
     * @param tryCount        卖出尝试次数
     * @return 是否成功
     * @throws Exception 与节点交互失败会抛出异常
     */
    public boolean sellTuGou(String tokenAddress, String gasPrice, String amountIn, String minEthAmountOut, double slippage, int tryCount) throws Exception {
        web3.setGasPriceLimit(gasPrice, gasPrice);
        int count = 0;
        List<String> path = new ArrayList<>();
        path.add(tokenAddress);
        path.add(web3.getChainInfo().getWETHAddress());
        while (count < tryCount) {
            count++;
            String out = getAmountOut(amountIn, path);
            // 兑换出的数量小于最小的心理预期，重试
            if (Double.parseDouble(out) < Double.parseDouble(minEthAmountOut)) {
                log.info("当前可获得ETH数量: " + Double.parseDouble(out) + ",小于期待最小值，正在重试");
                continue;
            }
            String hash = swapExactTokensForETH(amountIn, out, path, slippage);
            boolean status = web3.blockTransactionUtilComplete(hash, 30 * 1000);
            if (status) {
                log.info("买入土狗成功，交易hash: " + hash);
                return true;
            } else {
                log.info("买入土狗失败，正在重试，交易hash: " + hash);
            }
        }
        return false;
    }

    /**
     * 通用兑换函数
     *
     * @param tokenIn      输入的token地址
     * @param amountIn     输入的token数量
     * @param tokenOut     输出的token地址
     * @param amountOutMin 最小可接受的输出数量
     * @param slippage     滑点（在amountOutMin的基础上）
     * @return 交易hash
     * @throws Exception 与节点交互失败会抛出异常
     */
    public String swapExactIn(String tokenIn, String amountIn, String tokenOut, String amountOutMin, double slippage) throws Exception {
        log.info("自动选择最优路径");
        // 自动选择最优路径
        List<String> path = getBestPath(tokenIn, amountIn, tokenOut);
        if (path == null || path.isEmpty()) {
            throw new Exception("自动寻找路径失败");
        }
        return swapExactIn(tokenIn, amountIn, tokenOut, amountOutMin, path, slippage);
    }

    /**
     * 通用兑换函数
     *
     * @param tokenIn      输入的token地址
     * @param amountIn     输入的token数量
     * @param tokenOut     输出的token地址
     * @param amountOutMin 最小可接受的输出数量
     * @param path         兑换路径
     * @param slippage     滑点（在amountOutMin的基础上）
     * @return 交易hash
     * @throws Exception 与节点交互失败会抛出异常
     */
    public String swapExactIn(String tokenIn, String amountIn, String tokenOut, String amountOutMin, List<String> path, double slippage) throws Exception {
        String wEth = web3.getChainInfo().getWETHAddress();
        // 判断兑换出的数量能否满足要求
        log.info("判断兑换出的数量能否满足要求");
        String amountOut = getAmountOut(amountIn, path);
        if (StringUtil.greatThan(amountOutMin, amountOut)) {
            throw new Exception(String.format("当前输出数量为: %s,无法满足最小输出", amountOut));
        }
        log.info("正在兑换");
        // 用eth兑换
        if (tokenIn.equals(wEth)) {
            return swapExactETHForTokens(amountIn, amountOutMin, path, slippage);
        }
        // 兑换成eth
        if (tokenOut.equals(wEth)) {
            return swapExactTokensForETH(amountIn, amountOutMin, path, slippage);
        }
        // token兑换成token
        return swapExactTokensForTokens(amountIn, amountOutMin, path, slippage);
    }


    /**
     * 自动寻找最优兑换路径，路径最大为三层
     *
     * @param tokenIn       输入代币
     * @param tokenAmountIn 输出代币
     * @param tokenOut      输出代币
     * @return 最优路径
     */
    public List<String> getBestPath(String tokenIn, String tokenAmountIn, String tokenOut) {
        if (tokenAmountIn.isEmpty()) {
            log.warn("常用组LP代币未配置");
        }
        // 最优兑换路径
        List<String> resPaths = null;
        // 最优兑换数量
        String maxAmountOut = "0";
        // 直接兑换
        try {
            List<String> path = new ArrayList<>();
            path.add(tokenIn);
            path.add(tokenOut);
            String amountOut = getAmountOut(tokenAmountIn, path);
            log.info(String.format("tokenA -> tokenB \namountOut: %s", amountOut));
            if (StringUtil.greatThan(amountOut, maxAmountOut)) {
                maxAmountOut = amountOut;
                resPaths = path;
            }
        } catch (Exception e) {
            log.info(String.format("无此交易对: %s -> %s", tokenIn, tokenOut));
        }
        // 走常见的中间兑换币进行兑换
        for (String pairToken : commonPairToken) {
            if (pairToken.equals(tokenIn) || pairToken.equals(tokenOut)) {
                continue;
            }
            try {
                List<String> path = new ArrayList<>();
                path.add(tokenIn);
                path.add(pairToken);
                path.add(tokenOut);
                String amountOut = getAmountOut(tokenAmountIn, path);
                log.info(String.format("tokenA -> %s -> tokenB \namountOut: %s", TokenInfo.getNameFormAddress(pairToken, web3.getChainInfo().getChainName()), amountOut));
                if (StringUtil.greatThan(amountOut, maxAmountOut)) {
                    maxAmountOut = amountOut;
                    resPaths = path;
                }
            } catch (Exception e) {
                log.info(String.format("寻找交易对失败: tokenA -> %s -> tokenB", TokenInfo.getNameFormAddress(pairToken, web3.getChainInfo().getChainName())));
            }
        }
        log.info(String.format("最优兑换路径: %s 最优兑换数量: %s", resPaths, maxAmountOut));
        return resPaths;
    }

    /**
     * 自动寻找最优兑换路径，路径最大为三层
     *
     * @param tokenIn       输入代币
     * @param tokenAmountIn 输出代币
     * @param tokenOut      输出代币
     * @return 最优路径
     */
    public List<Address> getPathWithAddress(String tokenIn, String tokenAmountIn, String tokenOut) {
        List<Address> result = new ArrayList<>();
        List<String> paths = getBestPath(tokenIn, tokenAmountIn, tokenOut);
        paths.forEach((p) -> result.add(new Address(p)));
        return result;
    }

    /**
     * 增加中间兑换pair
     *
     * @param tokenAddress 代币地址
     */
    public void addCommonPairToken(String tokenAddress) {
        commonPairToken.add(tokenAddress);
    }

    /**
     * 获取routerInfo
     *
     * @return 对应的router信息
     */
    public RouterInfo getRouterInfo() {
        return routerInfo;
    }

    /**
     * 获取lp流动对地址
     *
     * @param token0 用于组流动性的token
     * @param token1 用于组流动性的token
     * @return lp流动对地址
     * @throws Exception 与节点交互出现异常
     */
    public String getPairAddress(String token0, String token1) throws Exception {
        String factory = getPairFactoryAddress();
        PairUtil pairUtil = new PairUtil(web3);
        return pairUtil.getPair(factory, token0, token1);
    }

    /**
     * 获取LP pair工厂地址
     *
     * @return LP pair工厂地址
     * @throws Exception 与节点交互失败
     */
    public String getPairFactoryAddress() throws Exception {
        List<TypeReference<?>> output = Collections.singletonList(new TypeReference<Address>() {
        });
        List<Type> result = web3.readContract(routerInfo.getRouterAddress(), "factory", new ArrayList<>(), output);
        Address a = (Address) result.get(0);
        return a.getValue();
    }

    /**
     * 将代币授权给当前router合约
     *
     * @param tokenAddress 代币地址
     * @return 交易hash
     * @throws Exception 与节点交互失败
     */
    public String approve(String tokenAddress) throws Exception {
        return web3.approve(tokenAddress, routerInfo.getRouterAddress());
    }

    /**
     * 检测池子，抢购代币
     *
     * @param ethAmountIn  参与抢购的eth数量
     * @param tokenAddress 参与抢购的代币地址
     * @param tryCount     代币抢购次数
     * @throws Exception 与节点交互失败
     */
    public void rushToPurchase(String ethAmountIn, String tokenAddress, int tryCount) throws Exception {
        // 监控池子
        log.info("正在监控池子");
        List<String> path = null;
        while (true) {
            path = getBestPath(routerInfo.getChainInfo().getWETHAddress(), ethAmountIn, tokenAddress);
            if (path != null) {
                break;
            }
            log.info("未找到可用流动性，正在重试。。。");
        }
        log.info("正在兑换代币");
        int count = 0;
        while (true) {
            String hash = swapExactETHForTokens(ethAmountIn, "0", path, 0.5);
            boolean status = web3.blockTransactionUtilComplete(hash, 60 * 1000);
            if (status) {
                log.info("买入代币成功，交易hash: " + hash);
                break;
            } else {
                log.info("买入土狗失败，正在重试，交易hash: " + hash);
            }
            count++;
            if (count > tryCount) {
                log.info("尝试次数超过限制，抢购失败！");
                break;
            }
        }
    }

    /**
     * 获取某条链上最优的router对象
     *
     * @param chainName 链名称
     * @param tokenIn   输入的token地址
     * @param amtIn     输入的token数量
     * @param tokenOut  输出的token地址
     * @return 最优兑换数量的router对象
     * @throws Exception 与节点交互失败
     */
    public static RouterUtil getBestRouter(String chainName, String tokenIn, String amtIn, String tokenOut) throws Exception {
        String maxAmtOut = "0";
        RouterUtil resRouter = null;
        List<RouterInfo> allRouter = RouterInfo.getChainAllRouter(chainName);
        for (RouterInfo routerInfo : allRouter) {
            RouterUtil routerUtil = new RouterUtil(routerInfo);
            // 获取最佳的兑换路径
            List<String> path = routerUtil.getBestPath(tokenIn, amtIn, tokenOut);
            if (path != null) {
                String amtOut = routerUtil.getAmountOut(amtIn, path);
                log.info(String.format("%s最优兑换数量: %s", routerInfo.getRouterName(), amtOut));
                if (StringUtil.greatThan(amtOut, maxAmtOut)) {
                    maxAmtOut = amtOut;
                    resRouter = routerUtil;
                }
            }
        }
        if (resRouter != null) {
            log.info(String.format("最优兑换Router: %s 最优兑换数量: %s", resRouter.getRouterInfo().getRouterName(), maxAmtOut));
        }
        return resRouter;
    }
}
