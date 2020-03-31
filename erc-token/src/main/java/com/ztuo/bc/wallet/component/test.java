package com.ztuo.bc.wallet.component;

import com.alibaba.fastjson.JSON;
import com.ztuo.bc.wallet.event.Erc20TokenWrapper;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.Contract;

public class test {

    public static void main(String[] args) {
        Web3j web3j = Web3j.build(new HttpService("http://36.153.147.94:8545"));
        ClientTransactionManager transactionManager = new ClientTransactionManager(web3j,
                "0xF9770386c9af335EE94B71f8616F156503BdD4FE");
        Erc20TokenWrapper token = Erc20TokenWrapper.load("0xF9770386c9af335EE94B71f8616F156503BdD4FE", web3j, transactionManager,
                Contract.GAS_PRICE, Contract.GAS_LIMIT);
        token.transferEventFlowable(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST)
                .subscribe(tx -> {
                    System.out.println("tx: " + JSON.toJSONString(tx));
                    String toAddress = tx.to;
                    String fromAddress = tx.from;
                    String value = String.valueOf(tx.value);
                    String txHash = String.valueOf(tx.log.getTransactionHash());
                    System.out.println("toAddress: " +toAddress + "=fromAddress: " + fromAddress +"=txHash: " +txHash + tx.log.getBlockNumber());
                },throwable -> {
                         throwable.printStackTrace();
                });
    }


}
