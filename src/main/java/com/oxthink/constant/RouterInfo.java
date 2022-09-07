package com.oxthink.constant;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum RouterInfo {
    // bsc
    PANCAKE_BSC_MAIN("0x10ED43C718714eb63d5aA57B78B54704E256024E", "Pancake", ChainInfo.BSC_MAIN),
    BISWAP_BSC_MAIN("0x3a6d8cA21D1CF76F653A67577FA0D27453350dD8", "Biswap", ChainInfo.BSC_MAIN),

    // oec
    CHERRY_OEC_MAIN("0x865bfde337C8aFBffF144Ff4C29f9404EBb22b15", "Cherry", ChainInfo.OEC_MAIN),
    JSWAP_OEC_MAIN("0x069A306A638ac9d3a68a6BD8BE898774C073DCb3", "Jswap", ChainInfo.OEC_MAIN),

    // matic
    QUICK_MATIC_MAIN("0xa5e0829caced8ffdd4de3c43696c57f7d7a678ff", "Quick", ChainInfo.MATIC_MAIN),
    UNI_MATIC_MAIN("", "uni", ChainInfo.MATIC_MAIN),

    // DOGE
    DOGESWAP_DOGE_EVM("0xa4ee06ce40cb7e8c04e127c1f7d3dfb7f7039c81","DogeSwap",ChainInfo.DOGE_EVM);

    /**
     * router地址
     */
    private final String routerAddress;

    /**
     * router名称
     */
    private final String routerName;

    /**
     * router所属的链信息
     */
    private final ChainInfo chainInfo;

    RouterInfo(String routerAddress, String routerName, ChainInfo chainInfo) {
        this.chainInfo = chainInfo;
        this.routerAddress = routerAddress;
        this.routerName = routerName;
    }

    /**
     * 根据token地址，查询对应的代币名称
     *
     * @param chainName 链名称
     * @return 该条链上所有的router地址
     */
    public static List<RouterInfo> getChainAllRouter(String chainName) {
        List<RouterInfo> list = new ArrayList<>();
        for (RouterInfo routerInfo : RouterInfo.values()) {
            if (routerInfo.getChainInfo().getChainName().equalsIgnoreCase(chainName)) {
                list.add(routerInfo);
            }
        }
        return list;
    }
}
