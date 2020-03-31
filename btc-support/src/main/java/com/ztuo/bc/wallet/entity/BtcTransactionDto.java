/**
 * Copyright (C), 2016-2019, XXX有限公司
 * FileName: BtcTransactionDto
 * Author:   Administrator
 * Date:     2019/12/12/012 15:27
 * Description: 交易类
 * History:
 * <author>          <time>          <version>          <desc>
 * simon          修改时间           版本号              描述
 */
package com.ztuo.bc.wallet.entity;

import lombok.*;

import java.math.BigDecimal;


/**
 * 〈一句话功能简述〉<br>
 * 〈交易类〉
 *
 * @author Administrator
 * @create 2019/12/12/012
 * @since 1.0.0
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BtcTransactionDto {

    private String withdrawId;
    private String txid;
    private String fromAddress;
    private String toAddress;
    private BigDecimal amount;
    private String coinName;
    private Integer sendNum;
}

