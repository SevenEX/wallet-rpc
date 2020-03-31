/**
 * Copyright (C), 2016-2019, XXX有限公司
 * FileName: TransactionService
 * Author:   Administrator
 * Date:     2019/12/11/011 19:28
 * Description: 交易service
 * History:
 * <author>          <time>          <version>          <desc>
 * simon           修改时间           版本号              描述
 */
package com.ztuo.bc.wallet.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ztuo.bc.wallet.entity.BtcTransactionDto;
import com.ztuo.bc.wallet.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 〈一句话功能简述〉<br>
 * 〈交易service〉
 *
 * @author Administrator
 * @create 2019/12/11/011
 * @since 1.0.0
 */
@Service
public class UsdtTransactionService {
    Logger logger = LoggerFactory.getLogger(UsdtTransactionService.class);
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private com.ztuo.bc.wallet.entity.Coin coin;
    
    public void notify(String withdrawId, String txid, int status) {
        if (StringUtils.isNotBlank(withdrawId)) {
            JSONObject json = new JSONObject();
            json.put("withdrawId", withdrawId);
            json.put("txid", txid);
            json.put("status", status);
            kafkaTemplate.send("withdraw-notify", coin.getName(), JSON.toJSONString(json));
        }
    }
    
    public void sendAsyncTransaction(String name, String fromAddress, String toAddress, BigDecimal amount, String withdrawId) {
        BtcTransactionDto dto = BtcTransactionDto.builder()
                .coinName(name).fromAddress(fromAddress).toAddress(toAddress).amount(amount).withdrawId(withdrawId).sendNum(0).build();
        redisUtil.lSet(name + "_transaction", JSON.toJSONString(dto));
    }
}
