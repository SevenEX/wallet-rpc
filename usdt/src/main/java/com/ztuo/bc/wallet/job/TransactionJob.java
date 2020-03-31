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
import com.ztuo.bc.wallet.service.UsdtServiceimpl;
import com.ztuo.bc.wallet.service.UsdtTransactionService;
import com.ztuo.bc.wallet.service.impl.BtcTransactionServiceImpl;
import com.ztuo.bc.wallet.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
	private KafkaTemplate<String, String> kafkaTemplate;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	private UsdtServiceimpl usdtServiceimpl;
	@Autowired
	private UsdtTransactionService usdtTransactionService;

	@Autowired
	private Coin coin;

	@Scheduled(cron = "0 0/2 * * * ?")
	public void doSendAsyncTransaction() {
		String ridisQueue = coin.getName() + "_transaction";
		logger.info("发送异步交易任务----开始");
		try {
			long size = redisUtil.lGetListSize(ridisQueue);
			if (size > 0) {
				String txid = null;
				logger.info("当前队列名{}，长度{}", ridisQueue, size);
				Object object = redisUtil.lLeftPop(ridisQueue);
				logger.info("当前Withdraw信息;{}", object.toString());
				BtcTransactionDto dto = JSONObject.parseObject((String) object, BtcTransactionDto.class);
				if (StringUtils.isNotBlank(dto.getToAddress()) && dto.getAmount() != null
						&& dto.getAmount().compareTo(BigDecimal.ZERO) > 0 && dto.getSendNum() < 3) {
					try{
						txid = usdtServiceimpl.sendSignTransaction(dto.getFromAddress(), dto.getToAddress(), dto.getAmount());
						if (StringUtils.isNotBlank(txid)) {
							logger.info("---交易{}发送成功---{}", dto.getWithdrawId(), txid);
							//发送成功消息
							usdtTransactionService.notify(dto.getWithdrawId(), txid, 1);
						} else {
							//重新插入到队尾
							dto.setSendNum(dto.getSendNum() + 1);
							redisUtil.lSet(ridisQueue, JSON.toJSONString(dto));
						}
					}catch (SignFailException e){
						e.printStackTrace();
						redisUtil.lSet(ridisQueue, JSON.toJSONString(dto));
					}
				} else {
					//发送失败消息
					usdtTransactionService.notify(dto.getWithdrawId(), "", 0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("-------{}----------异步交易发送失败-----------", coin.getName());
		}
		logger.info("发送异步交易任务----结束");
	}
}
