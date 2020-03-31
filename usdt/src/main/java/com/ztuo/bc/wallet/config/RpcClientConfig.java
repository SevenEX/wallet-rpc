package com.ztuo.bc.wallet.config;

import com.alibaba.fastjson.JSONObject;
import com.spark.blockchain.rpcclient.BitcoinRPCClient;
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
    public JsonrpcClient setClient(@Value("${coin.rpc}") String uri){
        try {
            logger.info("uri={}",uri);
            JsonrpcClient client =  new JsonrpcClient(uri);
            logger.info("=============================");
            logger.info("client={}",client);
            logger.info("=============================");
            return client;
        } catch (MalformedURLException e) {
            logger.info("init wallet failed");
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
