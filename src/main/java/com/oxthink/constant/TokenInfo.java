package com.oxthink.constant;

import lombok.Getter;

@Getter
public enum TokenInfo {

    /**
     * BSC主网
     */
    LOWB_BSC_MAIN("Lowb", "0x843d4a358471547f51534e3e51fae91cb4dc3f28", 18, ChainInfo.BSC_MAIN),
    USDT_BSC_MAIN("USDT", "0x55d398326f99059ff775485246999027b3197955", 18, ChainInfo.BSC_MAIN),
    WBNB_BSC_MAIN("WBNB", "0xbb4cdb9cbd36b01bd1cbaebf2de08d9173bc095c", 18, ChainInfo.BSC_MAIN),
    CAKE_BSC_MAIN("CAKE", "0x0E09FaBB73Bd3Ade0a17ECC321fD13a19e81cE82", 18, ChainInfo.BSC_MAIN),
    ALPACA_BSC_MAIN("Alpaca", "0x8F0528cE5eF7B51152A59745bEfDD91D97091d2F", 18, ChainInfo.BSC_MAIN),
    USDC_BSC_MAIN("USDC", "0x8AC76a51cc950d9822D68b83fE1Ad97B32Cd580d", 18, ChainInfo.BSC_MAIN),
    BUSD_BSC_MAIN("BUSD", "0xe9e7CEA3DedcA5984780Bafc599bD69ADd087D56", 18, ChainInfo.BSC_MAIN),
    DUET_BSC_MAIN("DUET", "0x95EE03e1e2C5c4877f9A298F1C0D6c98698FAB7B", 18, ChainInfo.BSC_MAIN),
    LOSERKING_BSC_MAIN("LoserKing", "0x711878f5472DdAFb4045FA9175586787C91b39B7", 18, ChainInfo.BSC_MAIN),
    LUSD_BSC_MAIN("LUSD", "0x308Fa584F35690E8Fae8B18814d67C6402417928", 18, ChainInfo.BSC_MAIN),

    /**
     * OEC主网
     */
    WOKT_OEC_MAIN("WOKT", "0x8f8526dbfd6e38e3d8307702ca8469bae6c56c15", 18, ChainInfo.OEC_MAIN),
    LOWB_OEC_MAIN("Lowb", "0x08963Db742Ab159F27518D1D12188f69AA7387FB", 18, ChainInfo.OEC_MAIN),
    USDT_OEC_MAIN("USDT", "0x382bb369d343125bfb2117af9c149795c6c65c50", 18, ChainInfo.OEC_MAIN),
    USDC_OEC_MAIN("USDC", "0xc946daf81b08146b1c7a8da2a851ddf2b3eaaf85", 18, ChainInfo.OEC_MAIN),

    /**
     * Polygon主网
     */
    LOWB_MATIC_MAIN("Lowb", "0x1C0a798B5a5273a9e54028eb1524fD337B24145F", 18, ChainInfo.MATIC_MAIN),
    USDT_MATIC_MAIN("USDT", "0xc2132D05D31c914a87C6611C10748AEb04B58e8F", 6, ChainInfo.MATIC_MAIN),
    USDC_MATIC_MAIN("USDC", "0x2791bca1f2de4661ed88a30c99a7a9449aa84174", 18, ChainInfo.MATIC_MAIN),
    WMATIC_MATIC_MAIN("WMATIC", "0x0d500b1d8e8ef31e21c99d1db9a6444d3adf1270", 18, ChainInfo.MATIC_MAIN),

    /**
     * dogeChain主网
     */
    WDOGE_DOGECHAIN_MAIN("WDOGE", "0xB7ddC6414bf4F5515b52D8BdD69973Ae205ff101", 18, ChainInfo.DOGE_EVM);


    /**
     * 代币名称
     */
    private final String name;

    /**
     * 代币地址
     */
    private final String address;

    /**
     * 代币精度
     */
    private final int decimals;

    /**
     * 代币所属网络名称
     */
    private final ChainInfo chainInfo;


    TokenInfo(String name, String address, int decimals, ChainInfo chainInfo) {
        this.name = name;
        this.address = address;
        this.decimals = decimals;
        this.chainInfo = chainInfo;
    }

    /**
     * 根据token地址，查询对应的代币名称
     *
     * @param address   地址
     * @param chainName 链名称
     * @return 代币名称，未查询到返回地址
     */
    public static String getNameFormAddress(String address, String chainName) {
        for (TokenInfo tokenInfo : TokenInfo.values()) {
            if (tokenInfo.getAddress().equalsIgnoreCase(address) && tokenInfo.getChainInfo().getChainName().equalsIgnoreCase(chainName)) {
                return tokenInfo.name;
            }
        }
        return address;
    }

    /**
     * 根据token地址，查询对应的代币名称
     *
     * @param name      代币名称
     * @param chainName 链名称
     * @return 代币地址，未查询到返回name
     */
    public static String getAddressFormName(String name, String chainName) {
        for (TokenInfo tokenInfo : TokenInfo.values()) {
            if (tokenInfo.getName().equalsIgnoreCase(name) && tokenInfo.getChainInfo().getChainName().equalsIgnoreCase(chainName)) {
                return tokenInfo.address;
            }
        }
        return null;
    }
}
