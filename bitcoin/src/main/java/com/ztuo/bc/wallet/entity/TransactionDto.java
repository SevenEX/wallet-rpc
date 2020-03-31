/**
 * Copyright (C), 2016-2019, XXX有限公司
 * FileName: TransactionDto
 * Author:   Administrator
 * Date:     2019/12/6/006 13:25
 * Description: 交易类
 * History:
 * <author>          <time>          <version>          <desc>
 * simon          修改时间           版本号              描述
 */
package com.ztuo.bc.wallet.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br> 
 * 〈交易类〉
 *
 * @author Administrator
 * @create 2019/12/6/006
 * @since 1.0.0
 */
@Data
public class TransactionDto {
    /**
     * 输出地址
     * */
    @ApiModelProperty(value = "输出地址", required = true)
    List<String> fromAddress;
    /**
     * 输入地址
     * */
    @ApiModelProperty(value = "输入地址和金额", required = true)
    Map<String,BigDecimal> targetAddressesMap;
}
