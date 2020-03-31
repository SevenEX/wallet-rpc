package com.ztuo.bc.wallet.entity;

import lombok.Data;

@Data
public class WatcherSetting {
    /**
     * latest，默认从节点最新区块开始扫描
     */
    private String initBlockHeight = "latest";
    private Long interval = 5000L;
    private int step = 5;
    private int confirmation = 1;
}
