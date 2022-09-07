package com.oxthink.web3.swap;


import com.oxthink.constant.ChainInfo;
import com.oxthink.constant.RouterInfo;
import com.oxthink.constant.TokenInfo;
import com.oxthink.web3.Web3jUtil;

public class JswapUtil extends RouterUtil {

    {
        // 常用用于组lp的代币信息
        commonPairToken.add(TokenInfo.USDT_OEC_MAIN.getAddress());
        commonPairToken.add(TokenInfo.WOKT_OEC_MAIN.getAddress());
        commonPairToken.add(TokenInfo.USDC_OEC_MAIN.getAddress());
    }

    public JswapUtil() {
        super(new Web3jUtil(ChainInfo.OEC_MAIN), RouterInfo.JSWAP_OEC_MAIN);
    }

    public JswapUtil(String privateKey) {
        super(new Web3jUtil(ChainInfo.OEC_MAIN, privateKey), RouterInfo.JSWAP_OEC_MAIN);
    }

    public JswapUtil(Web3jUtil web3) {
        super(web3, RouterInfo.JSWAP_OEC_MAIN);
    }
}
