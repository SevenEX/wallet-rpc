package com.ztuo.bc.wallet.job;


import com.alibaba.fastjson.JSONObject;
import com.ztuo.bc.wallet.component.Watcher;
import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.entity.Contract;
import com.ztuo.bc.wallet.entity.Deposit;
import com.ztuo.bc.wallet.event.DepositEvent;
import com.ztuo.bc.wallet.event.Erc20TokenWrapper;
import com.ztuo.bc.wallet.service.AccountService;
import com.ztuo.bc.wallet.service.EnventService;
import com.ztuo.bc.wallet.service.EthService;
import com.ztuo.bc.wallet.util.EthConvert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@Component
public class RechargeJob {
    private Logger logger = LoggerFactory.getLogger(RechargeJob.class);
    @Autowired
    private AccountService accountService;
    @Autowired
    private EthService ethService;
    @Autowired
    private Coin coin;
    @Autowired
    private Contract contract;
    @Autowired
    private DepositEvent depositEvent;
    @Autowired
    private EnventService enventService;
    @Autowired
    private Web3j web3j;
    @Autowired
    private Watcher watcher;
    /**
     * 判断属于本系统的交易是否成功了
     */
//    @Scheduled(cron = "0 0 */1 * * *")
    @Scheduled(cron = "0/30 * * * * ?")
    public void recharge(){
        logger.info("recharge开始");
        List<Erc20TokenWrapper.TransferEventResponse> list = enventService.findByStatus(0);
        list.stream().forEach(transferEventResponse -> {
            try {
                Optional<TransactionReceipt> receipt =  web3j.ethGetTransactionReceipt(transferEventResponse.log.getTransactionHash()).send().getTransactionReceipt();
                BigInteger blockNumber = web3j.ethBlockNumber().send().getBlockNumber();
                if(receipt.isPresent()){
                    if(receipt.get().getStatus().equalsIgnoreCase("0x1") && (blockNumber.subtract(transferEventResponse.log.getBlockNumber())).compareTo(BigInteger.valueOf(watcher.getConfirmation())) > 0){
                        Deposit deposit = new Deposit();
                        deposit.setTxid(transferEventResponse.log.getTransactionHash());
                        deposit.setBlockHash(transferEventResponse.log.getBlockHash());
                        logger.info(contract.getUnit().toString());
                        logger.info("recharge:" + EthConvert.fromWei(transferEventResponse.value.toString(), contract.getUnit()));
                        deposit.setAmount(EthConvert.fromWei(transferEventResponse.value.toString(), contract.getUnit()));
                        deposit.setAddress(transferEventResponse.to);
                        deposit.setTime(Calendar.getInstance().getTime());
                        deposit.setBlockHeight(transferEventResponse.log.getBlockNumber().longValue());
                        logger.info("recharge:" + JSONObject.toJSONString(deposit));
                        depositEvent.onConfirmed(deposit);
                        // 更新status，后面不再推送
                        enventService.updateStatus(transferEventResponse.log.getTransactionHash());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
