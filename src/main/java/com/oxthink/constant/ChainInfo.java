package com.oxthink.constant;

import lombok.Getter;

@Getter
public enum ChainInfo {

    /**
     * ETH主网
     */
//    ETH_MAIN("https://mainnet.infura.io/v3/", "1", "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2", "ETH", "https://etherscan.io/"),

    /**
     * BSC主网
     */
    BSC_MAIN("https://bsc-dataseed1.binance.org/", "56", "0xbb4cdb9cbd36b01bd1cbaebf2de08d9173bc095c", "BSC", "https://bscscan.com/"),

    /**
     * BSC测试网
     */
    BSC_TEST("https://data-seed-prebsc-1-s1.binance.org:8545/", "97", "0xae13d989daC2f0dEbFf460aC112a837C89BAa7cd", "BSCTest", "https://testnet.bscscan.com/"),

    /**
     * Polygon主网
     */
    MATIC_MAIN("https://polygon-rpc.com/", "137", "0x0d500b1d8e8ef31e21c99d1db9a6444d3adf1270", "Polygon", "https://polygonscan.com/"),

    /**
     * FTM主网
     */
    FTM_MAIN("https://rpcapi.fantom.network", "250", "", "FTM", "https://ftmscan.com/"),

    /**
     * Okex主网
     */
    OEC_MAIN("https://exchainrpc.okex.org", "66", "0x8f8526dbfd6e38e3d8307702ca8469bae6c56c15", "oec", "https://www.oklink.com/okexchain/"),

    /**
     * AAAVE主网
     */
    AAVE_MAIN("https://api.avax.network/ext/bc/C/rpc", "", "", "", ""),

    /**
     * CELO主网
     */
    CELO_MAIN("https://forno.celo.org", "", "", "", ""),

    /**
     * CRO主网
     */
    CRO_MAIN("https://evm-cronos.crypto.org", "", "", "", ""),
    DOGE_EVM("https://rpc02-sg.dogechain.dog/","2000","0xB7ddC6414bf4F5515b52D8BdD69973Ae205ff101","dogechain" ,"https://explorer.dogechain.dog");
    /**
     * 节点链接
     */
    private final String nodeUrl;
    /**
     * 网络id
     */
    private final String chainId;
    /**
     * 网络weth的地址
     */
    private final String WETHAddress;
    /**
     * 网络名称
     */
    private final String chainName;
    /**
     * 网络浏览器地址
     */
    private final String chainBrowser;

    ChainInfo(String nodeUrl, String chainId, String WETHAddress, String chainName, String chainBrowser) {
        this.nodeUrl = nodeUrl;
        this.chainId = chainId;
        this.WETHAddress = WETHAddress;
        this.chainName = chainName;
        this.chainBrowser = chainBrowser;
    }

    public ChainInfo getChainInfoByName(String name) {
        for (ChainInfo chainInfo : ChainInfo.values()) {
            if (chainInfo.getChainName().equalsIgnoreCase(name)) {
                return chainInfo;
            }
        }
        return null;
    }
}
