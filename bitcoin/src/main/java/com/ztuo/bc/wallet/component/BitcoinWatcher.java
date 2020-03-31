package com.ztuo.bc.wallet.component;

import com.alibaba.fastjson.JSON;
import com.spark.blockchain.rpcclient.Bitcoin;
import com.ztuo.bc.wallet.constants.SystemId;
import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.entity.Deposit;
import com.ztuo.bc.wallet.mapper.BlockRecordBtcMapper;
import com.ztuo.bc.wallet.mapperextend.AddressBtcMapperExtend;
import com.ztuo.bc.wallet.model.BlockRecordBtc;
import com.ztuo.bc.wallet.service.BitcoinRPCClientExtend;
import com.ztuo.bc.wallet.service.BitcoinUtilExtend;
import com.ztuo.bc.wallet.service.TxidService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class BitcoinWatcher extends Watcher {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private BitcoinRPCClientExtend rpcClient;
    @Autowired
    private AddressBtcMapperExtend addressBtcMapper;
    @Autowired
    private BlockRecordBtcMapper blockRecordBtcMapper;
    @Autowired
    private BitcoinUtilExtend bitcoinUtil;
    @Autowired
    private Coin coin;
    private Logger logger = LoggerFactory.getLogger(BitcoinWatcher.class);
    
    @Override
    public List<Deposit> replayBlock(Long startBlockNumber, Long endBlockNumber) {
        List<Deposit> deposits = new ArrayList<Deposit>();
        Set<String> needUpdateAddresses = new HashSet<>();
        try {
            //查询数据库地址池
            List<String> allAddress = this.addressBtcMapper.getAllAddress();
            for (Long blockHeight = startBlockNumber; blockHeight <= endBlockNumber; blockHeight++) {
                logger.info("开始查看：" + blockHeight);
                String blockHash = rpcClient.getBlockHash(blockHeight.intValue());
                //获取区块
                Bitcoin.Block block = rpcClient.getBlock(blockHash);
//                Bitcoin.Block block = rpcClient.getBlock("000000000000021c0b38de03d2936ffb8a9b2fe036c6643d8badcd64d5fdc7ba");
                List<String> txids = block.tx();
//              logger.info(blockHeight + "区块内txid：" + JSON.stringify(txids));
                //遍历区块中的交易
                for (String txid : txids) {
                    Bitcoin.RawTransaction transaction = rpcClient.getRawTransaction(txid);
//                    Object getblock = rpcClient.query("getblock", "000000000000021c0b38de03d2936ffb8a9b2fe036c6643d8badcd64d5fdc7ba", 2);
                    //遍历vOut
                    List<Bitcoin.RawTransaction.Out> outs = transaction.vOut();
                    for (Bitcoin.RawTransaction.Out out : outs) {
                        if (out.scriptPubKey() != null && out.scriptPubKey().addresses() != null) {
                            String outAddress = out.scriptPubKey().addresses().get(0);
                            if (outAddress != null && allAddress.contains(outAddress)) {
//                            if (outAddress != null && !allAddress.contains(outAddress)) {
                                //发送链上余额变动信息到kafka
                                Deposit deposit = new Deposit();
                                deposit.setTxid(transaction.txId());
                                deposit.setBlockHeight((long) block.height());
                                deposit.setBlockHash(transaction.blockHash());
                                BigDecimal amount = BigDecimal.valueOf(out.value());
                                deposit.setAmount(amount);
                                deposit.setAddress(outAddress);
                                deposit.setTime(transaction.time());
                                //如果是找零则不发送kafka消息
//                                if (!chargeAddress.equals(outAddress)&&coin.getMinSpentAmount().compareTo(amount)<0&&!txidService.isTxIdExist(deposit.getTxid())) {
                                if (coin.getMinSpentAmount().compareTo(amount)<0) {
                                    deposits.add(deposit);
                                }
                                logger.info("BTC本系统交易数据：" + JSON.toJSONString(deposit));
                                //生成用户余额表需更新list
                                needUpdateAddresses.add(outAddress);
                                //插入扫描记录
                                this.insertBlockRecord((long) block.height(),transaction,outAddress,amount,false);
                            }
                        }
                    }
                    //遍历vIn
//                    List<Bitcoin.RawTransaction.In> ins = transaction.vIn();
//                    for (Bitcoin.RawTransaction.In in : ins) {
//                        //  为空说明该交易是铸币交易，区块奖励,结束本次循环
//                        if (in.txid()!=null&&in.getTransactionOutput() != null
//                                && in.getTransactionOutput().scriptPubKey() != null
//                                && in.getTransactionOutput().scriptPubKey().addresses() != null) {
//                            String inAddress = in.getTransactionOutput().scriptPubKey().addresses().get(0);
//                            if (inAddress != null && allAddress.contains(inAddress)) {
//                                needUpdateAddresses.add(inAddress);
//                                //插入扫描记录
//                                this.insertBlockRecord((long) block.height(),transaction,inAddress,new BigDecimal(in.getTransactionOutput().value()),true);
//                            }
//                        }
//                    }
                }
            }
            //更新余额表
            logger.info("需更新的余额地址列表为：" + needUpdateAddresses.toString());
            for (String address : needUpdateAddresses) {
                this.bitcoinUtil.syncAddressBalance(address);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deposits;
    }
    
    /*插入区块扫描记录*/
    public void insertBlockRecord(Long height,Bitcoin.RawTransaction transaction, String address, BigDecimal amount, Boolean isInt) {
        BlockRecordBtc blockRecordBtc = new BlockRecordBtc();
        blockRecordBtc.setBlockHash(transaction.blockHash());
        blockRecordBtc.setBlockHeight(height.toString());
        blockRecordBtc.setCreateTime(new Date());
        blockRecordBtc.setTxid(transaction.txId());
        blockRecordBtc.setCurrency(coin.getName());
        blockRecordBtc.setAmount(amount);
        if (isInt) {
            blockRecordBtc.setFromAddress(address);
        } else {
            blockRecordBtc.setToAddress(address);
        }
        blockRecordBtc.setSysId(SystemId.APP.getName());
        blockRecordBtc.setStatus("0");
        this.blockRecordBtcMapper.insert(blockRecordBtc);
    }
    
    @Override
    public Long getNetworkBlockHeight() {
        try {
            return Long.valueOf(rpcClient.getBlockCount());
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }
}
