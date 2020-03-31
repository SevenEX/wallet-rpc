package com.ztuo.bc.wallet.job;

import com.alibaba.fastjson.JSONObject;
import com.ztuo.bc.wallet.config.JsonrpcClient;
import com.ztuo.bc.wallet.entity.BtcTransactionDto;
import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.mapperextend.AddressBtcMapperExtend;
import com.ztuo.bc.wallet.mapperextend.BalanceBtcMapperExtend;
import com.ztuo.bc.wallet.mapperextend.BalanceUsdtOmniMapperExtend;
import com.ztuo.bc.wallet.service.AccountService;
import com.ztuo.bc.wallet.service.UsdtServiceimpl;
import com.ztuo.bc.wallet.service.UsdtTransactionService;
import com.ztuo.bc.wallet.service.impl.BtcTransactionServiceImpl;
import com.ztuo.bc.wallet.util.AccountReplay;
import com.spark.blockchain.rpcclient.BitcoinUtil;
import com.ztuo.bc.wallet.config.JsonrpcClient;
import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.service.AccountService;
import com.ztuo.bc.wallet.util.AccountReplay;
import com.ztuo.bc.wallet.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CoinCollectJob {
    private Logger logger = LoggerFactory.getLogger(CoinCollectJob.class);
    //    @Autowired
//    private AccountService accountService;
    @Autowired
    private JsonrpcClient rpcClient;
    @Autowired
    private UsdtTransactionService usdtTransactionService;
    @Autowired
    private BalanceUsdtOmniMapperExtend balanceUsdtOmniMapperExtend;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private Coin coin;
    
    //    @Scheduled(cron = "0 0 15 * * *")
//    public void rechargeMinerFee(){
//        try {
//            AccountReplay accountReplay = new AccountReplay(accountService, 100);
//            accountReplay.run(account -> {
//                BigDecimal btcBalance = rpcClient.getAddressBalance(account.getAddress());
//                if(btcBalance.compareTo(coin.getRechargeMinerFee()) < 0) {
//                    BigDecimal usdtBalance = rpcClient.omniGetBalance(account.getAddress());
//                    if(usdtBalance.compareTo(coin.getMinCollectAmount()) >= 0) {
//                        try {
//                            String txid = BitcoinUtil.sendTransaction(rpcClient, coin.getWithdrawAddress(), account.getAddress(), coin.getRechargeMinerFee(), coin.getDefaultMinerFee());
//                            logger.info("BitcoinUtil.sendTransaction:address={},txid={}", account.getAddress(), txid);
//                        }
//                        catch (Exception e){
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            });
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
//    }
    @Scheduled(cron = "0 0/30 * * * *")
    public void rechargeMinerFee() {
        logger.info("给usdt地址自动充手续费任务---开始");
        try {
            //查询usdt地址池
            List<String> usdtAddress = this.balanceUsdtOmniMapperExtend.getUsdtAddress(coin.getName(), coin.getMinCollectAmount());
            List<Object> bitcoinTransaction = redisUtil.lGet("Bitcoin_transaction", 0, -1);
            for (String address : usdtAddress) {
                BigDecimal btcBalance = rpcClient.getAddressBalance(address);
                //btc余额低于最小充值金额
                if (btcBalance.compareTo(coin.getRechargeMinerFee()) < 0) {
                    //判断交易池是否已经有了充值手续费的交易
                    for (Object object : bitcoinTransaction) {
                        BtcTransactionDto dto = JSONObject.parseObject((String) object, BtcTransactionDto.class);
                        if (dto.getToAddress().equals(address)) {
                            continue;
                        }
                    }
                    BigDecimal usdtBalance = rpcClient.omniGetBalance(address);
                    //usdt余额大于等于最小提币金额
                    if (usdtBalance.compareTo(coin.getMinCollectAmount()) >= 0) {
                        try {
                            usdtTransactionService.sendAsyncTransaction("Bitcoin", "", address, coin.getRechargeMinerFee().subtract(btcBalance), "charegefee");
                            logger.info("自动充手续费的address={}，充值金额={}", address, coin.getRechargeMinerFee().subtract(btcBalance));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("给usdt地址自动充手续费任务---结束");
    }
}
