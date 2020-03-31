/**
 * Copyright (C), 2016-2019, XXX有限公司
 * FileName: TransactionJob
 * Author:   Administrator
 * Date:     2019/12/13/013 13:34
 * Description: 自动交易job
 * History:
 * <author>          <time>          <version>          <desc>
 * simon          修改时间           版本号              描述
 */
package com.ztuo.bc.wallet.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ztuo.bc.wallet.entity.BtcTransactionDto;
import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.interceptor.SignFailException;
import com.ztuo.bc.wallet.service.BitcoinUtilExtend;
import com.ztuo.bc.wallet.service.TxidService;
import com.ztuo.bc.wallet.service.impl.BtcTransactionServiceImpl;
import com.ztuo.bc.wallet.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br>
 * 〈自动交易job〉
 *
 * @author Administrator
 * @create 2019/12/13/013
 * @since 1.0.0
 */
@Component
public class TransactionJob {
    Logger logger = LoggerFactory.getLogger(TransactionJob.class);
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private BitcoinUtilExtend bitcoinUtil;
    @Autowired
    private BtcTransactionServiceImpl btcTransactionServiceImpl;
    @Autowired
    private Coin coin;

    @Scheduled(cron = "0 0/3 * * * ?")
    public void doSendAsyncTransaction() {
        String ridisQueue = coin.getName() + "_transaction";
        //redis消息队列
        List<Object> objects = new ArrayList<>();
        //待发送交易的map
        Map<String, BigDecimal> toMap = new HashMap<>();
        //待发送交易的队列
        List<BtcTransactionDto> dtos = new ArrayList<>();
//		List<Object> validPopList = new ArrayList<>();
        //此次不送发送的队列
        List<Object> invalidPopList = new ArrayList<>();
        try {
            long size = redisUtil.lGetListSize(ridisQueue);
            logger.info("发送多对多交易任务----开始");
            if (size > 0) {
                logger.info("当前队列名{}，长度{}", ridisQueue, size);
                for (int i = 0; i < (size > 3 ? 3 : size); i++) {
                    objects.add(redisUtil.lLeftPop(ridisQueue));
                }
                for (Object object : objects) {
                    BtcTransactionDto dto = JSONObject.parseObject((String) object, BtcTransactionDto.class);
                    //移除队列
                    logger.info("当前Withdraw信息;{}", object.toString());
                    if (StringUtils.isNotBlank(dto.getToAddress()) && dto.getAmount() != null
                            && dto.getAmount().compareTo(BigDecimal.ZERO) > 0 && dto.getSendNum() < 3) {
                        //一个地址只能在一笔交易中发送一次
                        if (toMap.containsKey(dto.getToAddress())) {
                            invalidPopList.add(JSON.toJSONString(dto));
                            continue;
                        }
                        toMap.put(dto.getToAddress(), dto.getAmount());
                        dtos.add(dto);
                    } else {
                        //发送失败消息
                        btcTransactionServiceImpl.notify(dto.getWithdrawId(), "", 0);
                    }
                }
                try {
                    String txid = bitcoinUtil.sendSignTransaction(toMap);
                    if (StringUtils.isNotBlank(txid)) {
                        for (BtcTransactionDto dto : dtos) {
                            logger.info("---交易{}发送成功---{}", dto.getWithdrawId(), txid);
//						//如果该交易是手续费转账
//						if (StringUtils.equals(dto.getWithdrawId(), "charegefee")) {
//							txidService.save(txid);
//						}else {
                            //发送成功消息
                            btcTransactionServiceImpl.notify(dto.getWithdrawId(), txid, 1);
//						}
                        }
                    } else {
                        //未成功的交易加入此次不送发送的队列 ,次数+1
                        for (BtcTransactionDto dto : dtos) {
                            dto.setSendNum(dto.getSendNum() + 1);
                            invalidPopList.add(JSON.toJSONString(dto));
                        }
                    }
                } catch (SignFailException e) {
                    e.printStackTrace();
                    //未成功的交易加入此次不送发送的队列,次数不变
                    for (BtcTransactionDto dto : dtos) {
                        invalidPopList.add(JSON.toJSONString(dto));
                    }
                } catch (Exception e) {
					//未成功的交易加入此次不送发送的队列,次数+1
                    for (BtcTransactionDto dto : dtos) {
                        dto.setSendNum(dto.getSendNum() + 1);
                        invalidPopList.add(JSON.toJSONString(dto));
                    }
                }
                if (invalidPopList.size() > 0) {
                    //此次未送发送的队列的交易重新加入到队尾
                    redisUtil.lSet(ridisQueue, invalidPopList);
                }
                logger.info("多对多交易发送结束");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("-------{}----------多对多交易发送失败-----------", coin.getName());
        }
        logger.info("发送多对多交易任务----结束");
    }

}
