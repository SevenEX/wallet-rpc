package com.ztuo.bc.wallet.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author GuoShuai
 * @date 2017年12月19日
 */
@AllArgsConstructor
@Getter
public enum AdminModule {
    CMS("CMS"),
    COMMON("COMMON"),
    EXCHANGE("EXCHANGE"),
    FINANCE("FINANCE"),
    MEMBER("MEMBER"),
    OTC("OTC"),
    SYSTEM("SYSTEM"),
    PROMOTION("PROMOTION"),
    INDEX("INDEX"),
    IEO("IEO"),
    GIFT("GIFT"),
    MARGIN("MARGIN");
    @Setter
    private String title;
}