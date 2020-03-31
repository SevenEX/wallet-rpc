package com.ztuo.bc.wallet.config;

import com.spark.blockchain.rpcclient.BitcoinException;
import com.ztuo.bc.wallet.service.BitcoinRPCClientExtend;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;

/**
 * 初始化RPC客户端
 */
@Configuration
public class RpcClientConfig {
    private Logger logger = LoggerFactory.getLogger(RpcClientConfig.class);
    
 
    @Bean
    public BitcoinRPCClientExtend setClient(@Value("${coin.rpc}") String uri){
        try {
            logger.info("uri={}",uri);
            BitcoinRPCClientExtend client =  new BitcoinRPCClientExtend(uri);
            int blockCount = client.getBlockCount();
            logger.info("blockHeight={}",blockCount);
            return client;
        } catch (MalformedURLException e) {
            logger.info("init wallet failed");
            e.printStackTrace();
            return null;
        } catch (BitcoinException e) {
            logger.info("BitcoinException");
            e.printStackTrace();
            return null;
        }
    }
    @Bean
    public NetworkParameters setParameters(@Value("${rpc.environment.istest}") String isTest){
        try {
            logger.info("isTest={}",isTest);
            NetworkParameters parameters = "true".equals(isTest) ? TestNet3Params.get() : MainNetParams.get();
            return parameters;
        } catch (Exception e) {
            logger.info("init parameters failed");
            e.printStackTrace();
            return null;
        }
    }
}
