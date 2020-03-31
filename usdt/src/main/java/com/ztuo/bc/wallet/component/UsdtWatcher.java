package com.ztuo.bc.wallet.component;

import com.ztuo.bc.wallet.config.JsonrpcClient;
import com.ztuo.bc.wallet.constants.SystemId;
import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.entity.Deposit;
import com.ztuo.bc.wallet.mapper.BlockRecordBtcMapper;
import com.ztuo.bc.wallet.mapperextend.AddressUsdtOmniMapperExtend;
import com.ztuo.bc.wallet.model.BlockRecordBtc;
import com.ztuo.bc.wallet.service.UsdtServiceimpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Component
public class UsdtWatcher extends Watcher {
    private Logger logger = LoggerFactory.getLogger(UsdtWatcher.class);
    @Autowired
    private JsonrpcClient jsonrpcClient;
    @Autowired
    private AddressUsdtOmniMapperExtend addressUsdtOmniMapper;
    @Autowired
    private BlockRecordBtcMapper blockRecordBtcMapper;
    @Autowired
    private Coin coin;
    @Autowired
    private UsdtServiceimpl usdtServiceimpl;
    @Value("${omni.currency.usdt.propertyid}")
    private BigInteger propertyid;
    
    @Override
    public List<Deposit> replayBlock(Long startBlockNumber, Long endBlockNumber) {
        List<Deposit> deposits = new ArrayList<>();
        Set<String> needUpdateAddresses = new HashSet<>();
        try {
            //查询数据库地址池
            List<String> allAddress = this.addressUsdtOmniMapper.getAllAddress();
            for (Long blockHeight = startBlockNumber; blockHeight <= endBlockNumber; blockHeight++) {
                List<String> list = jsonrpcClient.omniListBlockTransactions(blockHeight);
                for (String txid : list) {
                    Map<String, Object> map = jsonrpcClient.omniGetTransactions(txid);
                    //判断是否是omni交易
                    if (map.get("propertyid") == null) {
                        continue;
                    }
//                    logger.info(map.toString());
                    String propertyid = map.get("propertyid").toString();
                    String txId = map.get("txid").toString();
                    Boolean valid = Boolean.parseBoolean(map.get("valid").toString());
                    if (propertyid.equals(this.propertyid.toString()) && valid) {
                        //发送方地址
                        String sendingaddress = String.valueOf(map.get("sendingaddress"));
                        //接收方地址
                        String referenceaddress = String.valueOf(map.get("referenceaddress"));
                        //是否插入记录
                        Boolean isInsertBlockRecord = false;
                        //如果接受方地址属于系统
                        if (allAddress.contains(referenceaddress)) {
                            logger.info("propertyid: {},txId:{},sendingaddressL{},referenceaddress:{}", propertyid, txId, sendingaddress, referenceaddress);
                            Deposit deposit = new Deposit();
                            deposit.setTxid(txId);
                            deposit.setBlockHash(String.valueOf(map.get("blockhash")));
                            deposit.setAmount(new BigDecimal(map.get("amount").toString()));
                            deposit.setAddress(referenceaddress);
                            logger.info("receive usdt {}", String.valueOf(map.get("referenceaddress")));
                            deposit.setBlockHeight(Long.valueOf(String.valueOf(map.get("block"))));
//                            if (!coin.getWithdrawWallet().equals(referenceaddress)) {
                                deposits.add(deposit);
//                            }
                            //生成用户余额表需更新list
                            needUpdateAddresses.add(referenceaddress);
                            isInsertBlockRecord = true;
                        }
                        //如果发送方地址属于系统
                        if (allAddress.contains(sendingaddress)) {
                            //生成用户余额表需更新list
                            needUpdateAddresses.add(sendingaddress);
                            isInsertBlockRecord = true;
                        }
                        if (isInsertBlockRecord) {
                            //插入扫描记录
                            this.insertBlockRecord(map);
                        }
                    }
                }
            }
            //更新余额表
            logger.info("需更新的余额地址列表为：" + needUpdateAddresses.toString());
            for (String address : needUpdateAddresses) {
                this.usdtServiceimpl.syncAddressBalance(address);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deposits;
    }
    
    @Override
    public Long getNetworkBlockHeight() {
        try {
            return Long.valueOf(jsonrpcClient.getBlockCount());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }
    
    /*插入区块扫描记录*/
    public void insertBlockRecord(Map<String, Object> map) {
        BlockRecordBtc blockRecordBtc = new BlockRecordBtc();
        blockRecordBtc.setBlockHash(map.get("blockhash").toString());
        blockRecordBtc.setBlockHeight(map.get("block").toString());
        blockRecordBtc.setCreateTime(new Date());
        blockRecordBtc.setTxid(map.get("txid").toString());
        blockRecordBtc.setCurrency(coin.getName());
        blockRecordBtc.setAmount(new BigDecimal(map.get("amount").toString()));
        blockRecordBtc.setFromAddress(map.get("sendingaddress").toString());
        blockRecordBtc.setToAddress(map.get("referenceaddress").toString());
        blockRecordBtc.setSysId(SystemId.APP.getName());
        blockRecordBtc.setStatus("0");
        this.blockRecordBtcMapper.insert(blockRecordBtc);
    }
}
