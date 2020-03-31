package com.ztuo.bc.wallet.component;


import com.alibaba.fastjson.JSON;
import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.entity.Deposit;
import com.ztuo.bc.wallet.event.DepositEvent;
import com.ztuo.bc.wallet.mapper.BlockRecordEthMapper;
import com.ztuo.bc.wallet.model.BlockRecordEth;
import com.ztuo.bc.wallet.model.BlockRecordEthExample;
import com.ztuo.bc.wallet.service.AccountService;
import com.ztuo.bc.wallet.service.EthService;
import com.ztuo.bc.wallet.service.TxidService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthSyncing;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class EthWatcher extends Watcher {
    private Logger logger = LoggerFactory.getLogger(EthWatcher.class);
    @Autowired
    private Web3j web3j;
    @Autowired
    private EthService ethService;
    @Autowired
    private BlockRecordEthMapper blockRecordEthMapper;
    @Autowired
    private Coin coin;

    @Autowired
    private DepositEvent depositEvent;
    @Autowired
    private TxidService txidService;

    @Override
    public List<Deposit> replayBlock(Long startBlockNumber, Long endBlockNumber) {
        List<Deposit> deposits = new ArrayList<>();
        try {
            for (Long i = startBlockNumber; i <= endBlockNumber; i++) {
                EthBlock block = web3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(i), true).send();
                block.getBlock().getTransactions().stream().forEach(transactionResult -> {
                    EthBlock.TransactionObject transactionObject = (EthBlock.TransactionObject) transactionResult;
                    Transaction transaction = transactionObject.get();
                    // 如果是手续费交易，只更新余额即可
                    if(txidService.isTxIdExist(transaction.getHash())){
                        try {
                            ethService.syncAddressBalance(transaction.getTo(),coin.getName());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else if (StringUtils.isNotEmpty(transaction.getTo())
                            && ethService.isAddressExist(transaction.getTo())
                            && !transaction.getFrom().equalsIgnoreCase(getCoin().getIgnoreFromAddress())) {
                        Deposit deposit = new Deposit();
                        deposit.setTxid(transaction.getHash());
                        deposit.setBlockHeight(transaction.getBlockNumber().longValue());
                        deposit.setBlockHash(transaction.getBlockHash());
                        deposit.setAmount(Convert.fromWei(transaction.getValue().toString(), Convert.Unit.ETHER));
                        deposit.setAddress(transaction.getTo());
                        deposits.add(deposit);
                        logger.info("received coin {} at height {}", transaction.getValue(), transaction.getBlockNumber());
                        //同步余额
                        try {
                            ethService.syncAddressBalance(deposit.getAddress(),coin.getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    //如果是地址簿里转出去的地址，需要同步余额
                    if (StringUtils.isNotEmpty(transaction.getFrom()) && ethService.isAddressExist(transaction.getFrom())) {
                        logger.info("sync address:{} balance", transaction.getFrom());
                        try {
                            ethService.syncAddressBalance(transaction.getFrom(),coin.getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    // 保存交易记录
                    if ((StringUtils.isNotEmpty(transaction.getFrom()) && ethService.isAddressExist(transaction.getFrom())) ||
                            (StringUtils.isNotEmpty(transaction.getTo())
                                    && ethService.isAddressExist(transaction.getTo()))
                    ) {
                        BlockRecordEth blockRecordEth = new BlockRecordEth();
                        blockRecordEth.setSysId("app");
                        blockRecordEth.setToAddress(transaction.getTo());
                        blockRecordEth.setAmount(Convert.fromWei(transaction.getValue().toString(), Convert.Unit.ETHER));
                        blockRecordEth.setCurrency("ETH");
                        blockRecordEth.setIsCallback("1");
                        blockRecordEth.setIsDelete("1");
                        blockRecordEth.setPayType(SysCode.PayType.CHARGE_MONEY);
                        blockRecordEth.setFromAddress(transaction.getFrom());
                        blockRecordEth.setTrueFromAddress(transaction.getFrom());
                        blockRecordEth.setTxid(transaction.getHash());
                        blockRecordEth.setNonceStr(transaction.getNonce().toString());
                        Date date = new Date();
                        date.setTime(block.getBlock().getTimestamp().longValue() * 1000);
                        blockRecordEth.setCreateTime(date);
                        blockRecordEth.setStatus("03");
                        BigDecimal gasPrice = Convert.fromWei(transaction.getGasPrice().toString(), Convert.Unit.ETHER);
                        BigDecimal gas = BigDecimal.valueOf(transaction.getGas().longValue());

                        blockRecordEth.setBlockHash(transaction.getBlockHash());
                        blockRecordEth.setTradeBlockNo(transaction.getBlockNumber().toString());

                        blockRecordEth.setGasLimit(gas);
                        blockRecordEth.setGasPrice(gasPrice);
                        blockRecordEth.setInputData(transaction.getInput());
                        this.blockRecordEthMapper.insert(blockRecordEth);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deposits;
    }


    public synchronized int replayBlockInit(Long startBlockNumber, Long endBlockNumber) throws IOException {
        int count = 0;
        for (Long i = startBlockNumber; i <= endBlockNumber; i++) {
            EthBlock block = web3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(i), true).send();

            block.getBlock().getTransactions().stream().forEach(transactionResult -> {
                EthBlock.TransactionObject transactionObject = (EthBlock.TransactionObject) transactionResult;
                Transaction transaction = transactionObject.get();
                if (StringUtils.isNotEmpty(transaction.getTo())
                        && ethService.isAddressExist(transaction.getTo())
                        && !transaction.getFrom().equalsIgnoreCase(getCoin().getIgnoreFromAddress())) {
                    Deposit deposit = new Deposit();
                    deposit.setTxid(transaction.getHash());
                    deposit.setBlockHeight(transaction.getBlockNumber().longValue());
                    deposit.setBlockHash(transaction.getBlockHash());
                    deposit.setAmount(Convert.fromWei(transaction.getValue().toString(), Convert.Unit.ETHER));
                    deposit.setAddress(transaction.getTo());
                    logger.info("received coin {} at height {}", transaction.getValue(), transaction.getBlockNumber());
                    depositEvent.onConfirmed(deposit);
                    //同步余额
                    try {
                        ethService.syncAddressBalance(deposit.getAddress(), coin.getName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //如果是地址簿里转出去的地址，需要同步余额
                if (StringUtils.isNotEmpty(transaction.getFrom()) && ethService.isAddressExist(transaction.getFrom())) {
                    logger.info("sync address:{} balance", transaction.getFrom());
                    try {
                        ethService.syncAddressBalance(transaction.getFrom(), coin.getName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        return count;
    }

    @Override
    public Long getNetworkBlockHeight() {
        try {
//            EthSyncing ethSyncing = web3j.ethSyncing().send();
//            Numeric.toBigInt(JSON.parseObject(JSON.toJSONString(ethSyncing.getResult())).getString("currentBlock"));
            EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
            return blockNumber.getBlockNumber().longValue();
//            return Numeric.toBigInt(JSON.parseObject(JSON.toJSONString(ethSyncing.getResult())).getString("currentBlock")).longValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }
}
