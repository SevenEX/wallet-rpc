/**
 * Copyright (C), 2016-2019, XXX有限公司
 * FileName: AccountServiceIml
 * Author:   Administrator
 * Date:     2019/11/25/025 10:58
 * Description: 账户
 * History:
 * <author>          <time>          <version>          <desc>
 * simon          修改时间           版本号              描述
 */
package com.ztuo.bc.wallet.service.impl;

import com.alibaba.fastjson.JSON;
import com.spark.blockchain.rpcclient.BitcoinException;
import com.spark.blockchain.rpcclient.BitcoinRPCClient;
import com.ztuo.bc.wallet.aes.AESUtils;
import com.ztuo.bc.wallet.component.SysCode;
import com.ztuo.bc.wallet.config.RpcEnvironmentConfig;
import com.ztuo.bc.wallet.constants.SystemId;
import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.mapperextend.AddressBtcMapperExtend;
import com.ztuo.bc.wallet.mapperextend.BalanceBtcMapperExtend;
import com.ztuo.bc.wallet.model.AddressBtc;
import com.ztuo.bc.wallet.model.AddressBtcExample;
import com.ztuo.bc.wallet.service.BtcAccountService;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.Date;

/**
 * BtcAccountService<br>
 * @create 2019/11/25/025
 */
@Service
public class BtcAccountServiceImpl implements BtcAccountService {
    
    Logger logger = LoggerFactory.getLogger(BtcAccountServiceImpl.class);
    
    @Autowired
    private AddressBtcMapperExtend addressBtcMapper;
    @Autowired
    private BalanceBtcMapperExtend balanceBtcMapper;
    @Autowired
    private BitcoinRPCClient bitcoinRPCClient;
    @Autowired
    private Coin coin;
    
    
    /*创建10个地址*/
    @Override
    public void createTenAddress() throws BitcoinException {
        for (int i = 0; i < 10; i++) {
            AddressBtc address = this.createAddress(false);
            if (address != null) {
                //todo 如果没有正确导入的异常未处理
                 this.bitcoinRPCClient.query("importaddress", address, "watch-only", false);
            }
        }
    }
    
    
    /*创建地址*/
    @Override
    public AddressBtc createAddress(boolean flag) {
        NetworkParameters params = RpcEnvironmentConfig.isTest ? TestNet3Params.get() : MainNetParams.get();
        ECKey key = new ECKey();
        // 旧地址
        String legacyAddress = LegacyAddress.fromKey(params, key).toBase58();
        // 脚本地址（支持隔离见证）
        Script script = ScriptBuilder.createP2SHOutputScript(ScriptBuilder.createP2WPKHOutputScript(key));
        String scriptAddress = LegacyAddress.fromScriptHash(params, ScriptPattern.extractHashFromP2SH(script)).toString();
        // 账户私钥
        String priKey = key.getPrivateKeyAsHex();
//        String password = PassPhraseUtility.getPassPhrase(8);
        // 获取帐户AES密钥
        String aesKey = AESUtils.generateAESSecretKey();
        AddressBtc addressBtc = new AddressBtc();
        addressBtc.setAddress(legacyAddress);
        addressBtc.setScriptAddress(scriptAddress);
        // 是否删除
        addressBtc.setIsDelete(SysCode.IS_DELETE.NO);
        // 是否启用
        addressBtc.setIsEnable(SysCode.IS_ENABLE.YES);
        // 用账户AES密钥加密账户私钥
        addressBtc.setPriKey(AESUtils.encryptForCoupons(priKey, aesKey));
        // 业务系统ID
        addressBtc.setSysId(SystemId.APP.getName());
        // 更新时间
        addressBtc.setUpdateTime(new Date());
        // 随机密码
//        addressBtc.setPassword(AESUtils.encryptForCoupons(password, aesKey));
        // 钱包创建时间
        addressBtc.setCreateTime(new Date());
        // 用系统AES密钥加密账户AES密钥
        String enCodeAesKey = AESUtils.enCode(aesKey, RpcEnvironmentConfig.aesKeySecret);
        // 加密后账户AES密钥
        addressBtc.setAesKey(enCodeAesKey);
        if(flag){
            addressBtc.setUserId("1");
            addressBtc.setMasterFlg("1");
        }else {
            //业务系统，大钱包标识 0：否 1:是"
            addressBtc.setMasterFlg("0");
        }
        // 保存结果
        this.addressBtcMapper.insert(addressBtc);
        return addressBtc;
    }
    
    /**
     * 绑定账户
     *
     * @param account 业务层账户id
     * @return MessageResult
     */
    @Override
    public String bindAddress(String account) throws Exception {
        // 查询该账户是否绑定地址
        AddressBtcExample addressBtcExample = new AddressBtcExample();
        AddressBtcExample.Criteria criteria = addressBtcExample.createCriteria();
        criteria.andUserIdEqualTo(account);
        AddressBtc addressBtc = this.addressBtcMapper.selectOneByExample(addressBtcExample);
        logger.info(account + "查询数据库=" + JSON.toJSONString(addressBtc));
        if (addressBtc == null) {
            for (int j = 0; j < 2; j++) {
                // 绑定账户
                int i = this.addressBtcMapper.bindAccount(account);
                // 如果更新成功检索出绑定的地址
                if (i == 1) {
                    addressBtc = this.addressBtcMapper.selectOneByExample(addressBtcExample);
                    break;
                } else {
                    // 无地址绑定失败，生成10个新地址存入数据库
                    this.createTenAddress();
                }
            }
        }
        logger.info(account + "返回结果=" + JSON.toJSONString(addressBtc));
        return addressBtc != null ? addressBtc.getAddress() : null;
    }
    
    /**
     * 获取平台总余额
     */
    @Override
    public BigDecimal findBalanceSum(String currency) throws IOException {
        return this.balanceBtcMapper.findBalanceSum(currency);
    }
    public static void main(String[] args) throws GeneralSecurityException {
        NetworkParameters params = MainNetParams.get();
        String dePriKey ="f55266b702af3baea374008674161367b57dd1c2939bcee938bfacc8ba7f7533";
        ECKey key = ECKey.fromPrivate(new BigInteger(dePriKey, 16));
//        ECKey key = new ECKey();
        // 旧地址（bhd只支持p2sh_p2wpkh地址）
        String legacyAddress = LegacyAddress.fromKey(params, key).toBase58();
        // 脚本地址（支持隔离见证）
        Script script = ScriptBuilder.createP2SHOutputScript(ScriptBuilder.createP2WPKHOutputScript(key));
        String scriptAddress = LegacyAddress.fromScriptHash(params, ScriptPattern.extractHashFromP2SH(script)).toString();
        // 账户私钥
        String priKey = key.getPrivateKeyAsHex();

        System.out.println("address:" + scriptAddress +"===="+key.getPrivateKeyAsWiF(params));
    }
}
