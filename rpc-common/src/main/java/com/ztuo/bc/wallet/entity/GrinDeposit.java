package com.ztuo.bc.wallet.entity;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class GrinDeposit {
    // 交易TXID
    private String txid;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date time;
    private BigDecimal amount;
    private String uid;
    private int status = 0;
    // 交易序号id
    private String id;
}
