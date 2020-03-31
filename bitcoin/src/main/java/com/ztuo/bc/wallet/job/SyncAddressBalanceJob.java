package com.ztuo.bc.wallet.job;

import com.ztuo.bc.wallet.mapper.AddressBtcMapper;
import com.ztuo.bc.wallet.mapperextend.AddressBtcMapperExtend;
import com.ztuo.bc.wallet.model.AddressBtc;
import com.ztuo.bc.wallet.model.AddressBtcExample;
import com.ztuo.bc.wallet.service.BitcoinUtilExtend;
import com.ztuo.bc.wallet.service.BtcAccountService;
import org.bitcoinj.core.*;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author nz
 * @Description: 更新余额定时任务
 * @ClassName: CreateGethAddressJob
 * @date 2018年4月29日 上午10:28:14
 */
@Component
public class SyncAddressBalanceJob {

    private static final Logger logger = LoggerFactory.getLogger(SyncAddressBalanceJob.class);

    @Autowired
    private AddressBtcMapperExtend addressBtcMapper;
    @Autowired
    private BitcoinUtilExtend bitcoinUtil;

    /**
     * 更新余额定时任务
     */
    @Scheduled(cron = "0 0/15 * * * ?")
//    @Scheduled(fixedDelayString = "3600000")//间隔1小时
    public void syncAddressBalanceJob() throws Exception {
        //查询数据库地址池
        List<String> allInUseAddress = this.addressBtcMapper.getInUseAddress();
        for (String inUseAddress : allInUseAddress) {
            this.bitcoinUtil.syncAddressBalance(inUseAddress);
        }
    }
}
