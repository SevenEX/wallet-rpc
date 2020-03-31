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
import com.ztuo.bc.wallet.mapperextend.AddressUsdtOmniMapperExtend;
import com.ztuo.bc.wallet.mapperextend.BalanceUsdtOmniMapperExtend;
import com.ztuo.bc.wallet.model.AddressUsdtOmni;
import com.ztuo.bc.wallet.model.AddressUsdtOmniExample;
import com.ztuo.bc.wallet.service.UsdtOmniAccountService;
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
import java.util.Date;

/**
 * usdtAccountService<br>
 * @create 2019/11/25/025
 */
@Service
public class UsdtOmniAccountServiceImpl implements UsdtOmniAccountService {
    
    Logger logger = LoggerFactory.getLogger(UsdtOmniAccountServiceImpl.class);
    
    @Autowired
    private AddressUsdtOmniMapperExtend addressUsdtOmniMapper;
    @Autowired
    private BalanceUsdtOmniMapperExtend balanceUsdtOmniMapper;
    @Autowired
    private BitcoinRPCClient bitcoinRPCClient;
    @Autowired
    private Coin coin;
    
    
    /*创建10个地址*/
    @Override
    public void createTenAddress() throws BitcoinException {
        for (int i = 0; i < 10; i++) {
            AddressUsdtOmni address = this.createAddress(false);
            if (address != null) {
                //todo 如果没有正确导入的异常未处理
                 this.bitcoinRPCClient.query("importaddress", address, "watch-only", false);
            }
        }
    }
    
    
    /*创建地址*/
    @Override
    public AddressUsdtOmni createAddress(boolean flag) {
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
        AddressUsdtOmni addressUsdtOmni = new AddressUsdtOmni();
        addressUsdtOmni.setAddress(legacyAddress);
        addressUsdtOmni.setScriptAddress(scriptAddress);
        // 是否删除
        addressUsdtOmni.setIsDelete(SysCode.IS_DELETE.NO);
        // 是否启用
        addressUsdtOmni.setIsEnable(SysCode.IS_ENABLE.YES);
        // 用账户AES密钥加密账户私钥
        addressUsdtOmni.setPriKey(AESUtils.encryptForCoupons(priKey, aesKey));
        // 业务系统ID
        addressUsdtOmni.setSysId(SystemId.APP.getName());
        // 更新时间
        addressUsdtOmni.setUpdateTime(new Date());
        // 随机密码
//        addressBtc.setPassword(AESUtils.encryptForCoupons(password, aesKey));
        // 钱包创建时间
        addressUsdtOmni.setCreateTime(new Date());
        // 用系统AES密钥加密账户AES密钥
        String enCodeAesKey = AESUtils.enCode(aesKey, RpcEnvironmentConfig.aesKeySecret);
        // 加密后账户AES密钥
        addressUsdtOmni.setAesKey(enCodeAesKey);
        if(flag){
            addressUsdtOmni.setUserId("1");
            addressUsdtOmni.setMasterFlg("1");
        }else {
            //业务系统，大钱包标识 0：否 1:是"
            addressUsdtOmni.setMasterFlg("0");
        }
        // 保存结果
        this.addressUsdtOmniMapper.insert(addressUsdtOmni);
        return addressUsdtOmni;
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
        AddressUsdtOmniExample addressUsdtOmniExample = new AddressUsdtOmniExample();
        AddressUsdtOmniExample.Criteria criteria = addressUsdtOmniExample.createCriteria();
        criteria.andUserIdEqualTo(account);
        AddressUsdtOmni addressUsdtOmni = this.addressUsdtOmniMapper.selectOneByExample(addressUsdtOmniExample);
        logger.info(account + "查询数据库=" + JSON.toJSONString(addressUsdtOmni));
        if (addressUsdtOmni == null) {
            for (int j = 0; j < 2; j++) {
                // 绑定账户
                int i = this.addressUsdtOmniMapper.bindAccount(account);
                // 如果更新成功检索出绑定的地址
                if (i == 1) {
                    addressUsdtOmni = this.addressUsdtOmniMapper.selectOneByExample(addressUsdtOmniExample);
                    break;
                } else {
                    // 无地址绑定失败，生成10个新地址存入数据库
                    this.createTenAddress();
                }
            }
        }
        logger.info(account + "返回结果=" + JSON.toJSONString(addressUsdtOmni));
        return addressUsdtOmni != null ? addressUsdtOmni.getAddress() : null;
    }
    
    /**
     * 获取平台总余额
     */
    @Override
    public BigDecimal findBalanceSum(String currency) throws IOException {
        return this.balanceUsdtOmniMapper.findBalanceSum(currency);
    }

}
