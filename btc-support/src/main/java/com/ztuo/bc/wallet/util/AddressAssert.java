package com.ztuo.bc.wallet.util;

import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;

public class AddressAssert {

    //目前只支持base58编码的地址
    public static void checkAddress(NetworkParameters params, String... address) {
        try {
            if (address != null) {
                for (int i = 0; i < address.length; i++) {
                    LegacyAddress legacyAddress = LegacyAddress.fromBase58(params, address[i]);
                    if (legacyAddress == null) {
                        throw new RuntimeException("地址不合法");
                    }
                }
            } else {
                throw new RuntimeException("地址不合法");
            }
        } catch (Exception e) {
            throw new RuntimeException("地址不合法");
        }
    }
}
