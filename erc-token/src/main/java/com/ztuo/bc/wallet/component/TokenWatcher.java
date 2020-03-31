package com.ztuo.bc.wallet.component;

import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.entity.Contract;
import com.ztuo.bc.wallet.entity.Deposit;
import com.ztuo.bc.wallet.event.DepositEvent;
import com.ztuo.bc.wallet.event.Erc20TokenWrapper;
import com.ztuo.bc.wallet.service.*;
import com.ztuo.bc.wallet.util.EthConvert;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.tx.ClientTransactionManager;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

@Component
public class TokenWatcher extends Watcher {
    private Logger logger = LoggerFactory.getLogger(TokenWatcher.class);
    @Autowired
    private Web3j web3j;
    @Autowired
    private Contract contract;
    @Autowired
    private AccountService accountService;
    @Autowired
    private EthService ethService;
    @Autowired(required = false)
    private EtherscanApi etherscanApi;
    @Autowired
    private EnventService enventService;
    @Autowired
    private DepositEvent depositEvent;
    @Autowired
    private Coin coin;
    long saveBlockNum = 0;

    @Override
    public List<Deposit> replayBlock(Long startBlockNumber, Long endBlockNumber) {
        List<Deposit> deposits = new ArrayList<>();
        ClientTransactionManager transactionManager = new ClientTransactionManager(web3j,
                contract.getAddress());
        Erc20TokenWrapper token = Erc20TokenWrapper.load(contract.getAddress(), web3j, transactionManager,
                org.web3j.tx.Contract.GAS_PRICE, org.web3j.tx.Contract.GAS_LIMIT);
        DefaultBlockParameter defaultBlockParameter;
        boolean flag = endBlockNumber - startBlockNumber < getStep() - 1;
        if (flag) {
            defaultBlockParameter = DefaultBlockParameterName.LATEST;
            setStop(true);
        } else {
            defaultBlockParameter = DefaultBlockParameter.valueOf(BigInteger.valueOf(endBlockNumber));
        }

//        for(Long blockHeight = startBlockNumber;blockHeight<=endBlockNumber;blockHeight++) {
        token.transferEventFlowable(DefaultBlockParameter.valueOf(BigInteger.valueOf(startBlockNumber)), defaultBlockParameter)
//        token.transferEventFlowable(DefaultBlockParameter.valueOf(BigInteger.valueOf(9582000)), DefaultBlockParameter.valueOf(BigInteger.valueOf(9590000)))
                .subscribe(tx -> {
//                    System.out.println("tx: " + JSON.toJSONString(tx));
                    String toAddress = tx.to;
                    String fromAddress = tx.from;
                    String value = String.valueOf(tx.value);
                    String txHash = String.valueOf(tx.log.getTransactionHash());
                    tx.txId = txHash;
                    tx.status = 0;
                    long blockNumber = tx.log.getBlockNumber().longValue();
//                    logger.info("toAddress:{}=fromAddress:{}=txHash:{}=blockNumber:{}=contractAddress:{}",
//                            toAddress, fromAddress, txHash, blockNumber, contract.getAddress());
                    // 本系统账户充值并且交易是第一次保存
                    if (ethService.isAddressExist(toAddress) && !enventService.isTxIdExist(txHash)) {
                        enventService.save(tx);
                        //同步余额
                        try {
                            ethService.syncAddressBalance(toAddress, coin.getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // 本系统账户充值并且交易是第一次保存
                    if (ethService.isAddressExist(fromAddress) && !enventService.isTxIdExist(txHash)) {
                        //同步余额
                        try {
                            ethService.syncAddressBalance(fromAddress, coin.getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (flag&&blockNumber != saveBlockNum) {
                            //记录日志
                            this.saveBlockNum = blockNumber;
                            getWatcherLogService().update(coin.getName(), saveBlockNum);
                    }

                }, throwable -> {
                    throwable.printStackTrace();
                    setStop(false);
                });
//        }
        return deposits;
    }

    public synchronized void replayBlockInit(Long startBlockNumber, Long endBlockNumber) {
        for (long i = startBlockNumber; i <= endBlockNumber; i++) {
            EthBlock block = null;
            try {
                logger.info("ethGetBlockByNumber {}", i);
                block = web3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(i), true).send();
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<EthBlock.TransactionResult> transactionResults = block.getBlock().getTransactions();
            logger.info("transactionCount {}", transactionResults.size());
            transactionResults.forEach(transactionResult -> {

                EthBlock.TransactionObject transactionObject = (EthBlock.TransactionObject) transactionResult;
                Transaction transaction = transactionObject.get();
                try {
                    EthGetTransactionReceipt receipt = web3j.ethGetTransactionReceipt(transaction.getHash()).send();
                    if (receipt.getTransactionReceipt().get().getStatus().equalsIgnoreCase("0x1")) {
                        String input = transaction.getInput();
                        String cAddress = transaction.getTo();
                        if (StringUtils.isNotEmpty(input) && input.length() >= 138 && contract.getAddress().equalsIgnoreCase(cAddress)) {
                            String data = input.substring(0, 9);
                            data = data + input.substring(17, input.length());
                            Function function = new Function("transfer", Arrays.asList(), Arrays.asList(new TypeReference<Address>() {
                            }, new TypeReference<Uint256>() {
                            }));

                            List<Type> params = FunctionReturnDecoder.decode(data, function.getOutputParameters());
                            // 充币地址
                            String toAddress = params.get(0).getValue().toString();
                            String amount = params.get(1).getValue().toString();
                            logger.info("################{}###################{}", toAddress, amount);
                            if (ethService.isAddressExist(toAddress)) {
                                if (StringUtils.isNotEmpty(amount)) {
                                    Deposit deposit = new Deposit();
                                    deposit.setTxid(transaction.getHash());
                                    deposit.setBlockHash(transaction.getBlockHash());
                                    deposit.setAmount(EthConvert.fromWei(amount, contract.getUnit()));
                                    deposit.setAddress(toAddress);
                                    deposit.setTime(Calendar.getInstance().getTime());
                                    deposit.setBlockHeight(transaction.getBlockNumber().longValue());
//                                    depositEvent.onConfirmed(deposit);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
        }
    }

    @Override
    public Long getNetworkBlockHeight() {
        try {
            EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
            return blockNumber.getBlockNumber().longValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

}
