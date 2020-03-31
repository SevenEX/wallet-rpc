//package com.ztuo.bc.wallet.component;
//
//import com.ztuo.bc.wallet.entity.Coin;
//import com.ztuo.bc.wallet.event.DepositEvent;
//import com.ztuo.bc.wallet.event.Erc20TokenWrapper;
//import com.ztuo.bc.wallet.service.AccountService;
//import com.ztuo.bc.wallet.service.EnventService;
//import com.ztuo.bc.wallet.service.EthService;
////import com.ztuo.bc.wallet.service.EtherscanApi;
//import lombok.Data;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.stereotype.Component;
//import org.web3j.protocol.Web3j;
//import org.web3j.protocol.core.DefaultBlockParameter;
//import org.web3j.protocol.core.DefaultBlockParameterName;
//import org.web3j.tx.ClientTransactionManager;
//import org.web3j.tx.Contract;
//
//import java.math.BigDecimal;
//import java.math.BigInteger;
//
///**
// * @author y
// */
//@Component
//@Data
//public class EnventWatcher implements ApplicationRunner {
//    private Logger logger = LoggerFactory.getLogger(EnventWatcher.class);
//    @Autowired
//    private Web3j web3j;
//    @Autowired
//    private AccountService accountService;
//    @Autowired
//    private EthService ethService;
////    @Autowired(required = false)
////    private EtherscanApi etherscanApi;
//
//    @Autowired
//    private DepositEvent depositEvent;
//    @Autowired
//    private Coin coin;
//    @Autowired
//    private com.ztuo.bc.wallet.entity.Contract contract;
//    @Autowired
//    private MongoTemplate mongoTemplate;
//    @Autowired
//    private EnventService enventService;
//    @Value("${rpc.environment.istest}")
//    private String isTest;
//    /**
//     * 会在服务启动完成后立即执行
//     */
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        ClientTransactionManager transactionManager = new ClientTransactionManager(web3j,
//                contract.getAddress());
//        Erc20TokenWrapper token = Erc20TokenWrapper.load(contract.getAddress(), web3j, transactionManager,
//                Contract.GAS_PRICE, Contract.GAS_LIMIT);
////        token.transferEventFlowable(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST)
//        token.transferEventFlowable(DefaultBlockParameter.valueOf(contract.getStartBlockNo()), DefaultBlockParameterName.LATEST)
////        token.transferEventFlowable(DefaultBlockParameter.valueOf(BigInteger.valueOf(9582000)), DefaultBlockParameter.valueOf(BigInteger.valueOf(9590000)))
//                .subscribe(tx -> {
////                    System.out.println("tx: " + JSON.toJSONString(tx));
//                    String toAddress = tx.to;
//                    String fromAddress = tx.from;
//                    String value = String.valueOf(tx.value);
//                    String txHash = String.valueOf(tx.log.getTransactionHash());
//                    tx.txId=txHash;
//                    tx.status =0;
//                    logger.info("toAddress: " +toAddress + "=fromAddress: " + fromAddress +"=txHash: " +txHash + tx.log.getBlockNumber());
//                    // 本系统账户充值并且交易是第一次保存
//                    if(ethService.isAddressExist(toAddress) && !enventService.isTxIdExist(txHash)) {
//                        enventService.save(tx);
//                        //同步余额
//                        try {
//                            ethService.syncAddressBalance(toAddress,coin.getName());
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    // 本系统账户充值并且交易是第一次保存
//                    if(ethService.isAddressExist(fromAddress) && !enventService.isTxIdExist(txHash)) {
//                        //同步余额
//                        try {
//                            ethService.syncAddressBalance(fromAddress,coin.getName());
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                },throwable -> {
//                    throwable.printStackTrace();
//                });
//    }
//}
