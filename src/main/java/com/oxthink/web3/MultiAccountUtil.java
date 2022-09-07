package com.oxthink.web3;

import com.oxthink.constant.ChainInfo;
import com.oxthink.tool.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MultiAccountUtil {

    private final List<String> privateKeyList;

    private final ChainInfo chainInfo;

    private final Web3jUtil web3jMainAccount;

    private final String gasPrice;

    /**
     * 构造函数
     *
     * @param chainInfo      网络信息
     * @param privateKeyList 批量账户私钥列表
     * @param mainAccount    主账户私钥
     * @param gasPrice       设定的gasPrice价格
     */
    public MultiAccountUtil(ChainInfo chainInfo, List<String> privateKeyList, String mainAccount, String gasPrice) {
        this.privateKeyList = privateKeyList;
        this.chainInfo = chainInfo;
        this.web3jMainAccount = new Web3jUtil(chainInfo, mainAccount);
        this.gasPrice = gasPrice;
        web3jMainAccount.setGasPriceLimit(gasPrice, gasPrice);
    }

    /**
     * 多账户批量完成操作
     *
     * @param listener 操作具体内容
     */
    public void todo(AccountListener listener) {
        for (String privateKey : privateKeyList) {
            Web3jUtil web3j = new Web3jUtil(chainInfo, privateKey);
            // 设置gasPrice
            web3j.setGasPriceLimit(gasPrice, gasPrice);
            log.info(web3j.getOwnerAddress() + " 正在执行批量操作");
            // 执行具体的操作
            listener.doSameThing(web3j);
            log.info(web3j.getOwnerAddress() + " 完成批量操作");
        }
    }

    /**
     * 主账户向批量账户发送gas
     *
     * @param gasPerAccount gas数量 单位：ether
     * @throws Exception 与节点交互失败
     */
    public void distributeGas(String gasPerAccount) throws Exception {
        // 检查主账户的gas是否够用
        String mainBalance = web3jMainAccount.getBalance();
        String gasDistribute = String.valueOf(Double.parseDouble(gasPerAccount) * privateKeyList.size());
        if (StringUtil.greatThan(gasDistribute, mainBalance)) {
            throw new Exception(String.format("需花费 %s ether Gas,主账户Gas不足!", gasDistribute));
        }

        for (String privateKey : privateKeyList) {
            String address = Web3jUtil.getPublicAddress(privateKey);
            // 向钱包发送gas
            try {
                String hash = web3jMainAccount.sendEther(address, gasPerAccount);
                if (web3jMainAccount.blockTransactionUtilComplete(hash, 60000)) {
                    log.info(String.format("主账户向%s成功发送%s ether gas , 交易hash: %s", address, gasPerAccount, hash));
                } else {
                    log.info(String.format("主账户向%s发送gas失败 , 交易hash: %s", address, hash));
                }
            } catch (Exception e) {
                log.info(String.format("主账户向%s发送gas失败", address));
                e.printStackTrace();
            }
        }
    }

    /**
     * 向主账户发送GAS
     *
     * @throws Exception 与节点交互失败
     */
    public void collectGas() throws Exception {
        for (String privateKey : privateKeyList) {
            Web3jUtil web3j = new Web3jUtil(chainInfo, privateKey);
            web3j.setGasPriceLimit(gasPrice, gasPrice);
            String balance = web3j.getBalance();
            // 向主钱包发送gas
            try {
                String hash = web3j.sendAllEther(web3jMainAccount.getOwnerAddress());
                if (web3j.blockTransactionUtilComplete(hash, 60000)) {
                    log.info(String.format("%s向主账户成功发送%s ether gas , 交易hash: %s", web3j.getOwnerAddress(), balance, hash));
                } else {
                    log.info(String.format("%s向主账户发送gas失败 , 交易hash: %s", web3j.getOwnerAddress(), hash));
                }
            } catch (Exception e) {
                log.info(String.format("%s向主账户发送gas失败", web3j.getOwnerAddress()));
                e.printStackTrace();
            }
        }
    }

    interface AccountListener {
        void doSameThing(Web3jUtil web3j);
    }
}
