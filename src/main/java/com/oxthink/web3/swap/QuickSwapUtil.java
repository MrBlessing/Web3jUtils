package com.oxthink.web3.swap;


import com.oxthink.constant.ChainInfo;
import com.oxthink.constant.RouterInfo;
import com.oxthink.constant.TokenInfo;
import com.oxthink.web3.Web3jUtil;

public class QuickSwapUtil extends RouterUtil {

    {
        // 常用用于组lp的代币信息
        commonPairToken.add(TokenInfo.WMATIC_MATIC_MAIN.getAddress());
        commonPairToken.add(TokenInfo.USDT_MATIC_MAIN.getAddress());
        commonPairToken.add(TokenInfo.USDC_MATIC_MAIN.getAddress());
    }


    public QuickSwapUtil() {
        super(new Web3jUtil(ChainInfo.MATIC_MAIN), RouterInfo.QUICK_MATIC_MAIN);
    }

    public QuickSwapUtil(String privateKey) {
        super(new Web3jUtil(ChainInfo.MATIC_MAIN, privateKey), RouterInfo.QUICK_MATIC_MAIN);
    }

    public QuickSwapUtil(Web3jUtil web3) {
        super(web3, RouterInfo.QUICK_MATIC_MAIN);
    }
}
