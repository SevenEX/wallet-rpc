package com.ztuo.bc.wallet.entity;


import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class Coin {
    private String name;
    private String unit;
    private String rpc;
    private String keystorePath;
    private BigDecimal defaultMinerFee;
    private String withdrawAddress;
    private String withdrawWallet;
    private String withdrawWalletPassword;
    private BigDecimal minCollectAmount;
    /*
    低于微尘交易，不使用此uxto
    */
    private BigDecimal minSpentAmount;
    private BigInteger gasLimit;
    private BigDecimal gasSpeedUp = BigDecimal.ONE;
    private BigDecimal rechargeMinerFee;
    private String ignoreFromAddress;
    private String masterAddress;
    private String token;
}
