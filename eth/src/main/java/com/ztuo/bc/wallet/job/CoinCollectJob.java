package com.ztuo.bc.wallet.job;


import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.mapper.AddressEthMapper;
import com.ztuo.bc.wallet.service.AccountService;
import com.ztuo.bc.wallet.service.EthService;
import com.ztuo.bc.wallet.util.AccountReplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CoinCollectJob {
    private Logger logger = LoggerFactory.getLogger(CoinCollectJob.class);
    @Autowired
    private AccountService accountService;
    @Autowired
    private EthService ethService;
    @Autowired
    private Coin coin;

    /**
     * 同步ETH地址余额
     */
//    @Scheduled(cron = "0 0 */2 * * *")
//    @Scheduled(cron = "0 0 17 * * ?")
    public void rechargeMinerFee(){
        AccountReplay accountReplay = new AccountReplay(accountService,100);
        accountReplay.run(account -> {
            logger.info("process account:{}",account);
            try {
                //查询余额
                BigDecimal ethBalance = ethService.getBalance(account.getAddress());
                //同步账户余额
//                accountService.updateBalance(account.getAddress(),ethBalance);
                ethService.syncAddressBalance(account.getAddress(), coin.getName());
            }
            catch (Exception e){
                e.printStackTrace();
            }
        });
    }
}
