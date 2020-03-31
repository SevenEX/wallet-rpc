/**
 * Copyright (C), 2016-2019, XXX有限公司
 * FileName: BitcoinUtilExtend
 * Author:   Administrator
 * Date:     2019/12/4/004 18:58
 * Description: BitcoinUtil
 * History:
 * <author>          <time>          <version>          <desc>
 * simon          修改时间           版本号              描述
 */
package com.ztuo.bc.wallet.service;

import com.spark.blockchain.rpcclient.Bitcoin;
import com.spark.blockchain.rpcclient.BitcoinException;
import com.spark.blockchain.rpcclient.BitcoinUtil;
import com.ztuo.bc.wallet.aes.AESUtils;
import com.ztuo.bc.wallet.config.RpcEnvironmentConfig;
import com.ztuo.bc.wallet.interceptor.SignFailException;
import com.ztuo.bc.wallet.mapperextend.AddressBtcMapperExtend;
import com.ztuo.bc.wallet.mapperextend.BalanceBtcMapperExtend;
import com.ztuo.bc.wallet.model.*;
import com.ztuo.bc.wallet.service.impl.BtcTransactionServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.*;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Bitcoin核心包工具类
 *
 * @author Administrator
 * @create 2019/12/4/004
 * @since 1.0.0
 */
@Component
public class BitcoinUtilExtend extends BitcoinUtil {

    Logger logger = LoggerFactory.getLogger(BitcoinUtilExtend.class);

    @Autowired
    private BitcoinRPCClientExtend rpcClient;
    @Autowired
    private AddressBtcMapperExtend addressBtcMapper;
    @Autowired
    private BalanceBtcMapperExtend balanceBtcMapper;
    @Autowired
    private BtcTransactionServiceImpl transactionService;
    @Autowired
    private NetworkParameters params;
    @Autowired
    private com.ztuo.bc.wallet.entity.Coin coin;

    /**
     * 创建交易签名后由节点广播
     */
    public String sendSignTransaction(String toAddress, BigDecimal amount, BigDecimal fee, Boolean sync, String withdrawId) throws Exception {
        HashMap<String, BigDecimal> targetAddressesMap = new HashMap<>();
        targetAddressesMap.put(toAddress, amount);
        if (sync == null || sync) {
            //非异步
            return this.sendSignTransaction(new ArrayList<String>(), targetAddressesMap, fee);
        } else {
            //异步，则发送到队列中
            return this.sendAsyncTransaction(toAddress, amount, withdrawId);
        }
    }

    /**
     * 发送异步交易
     */
    private String sendAsyncTransaction(String toAddress, BigDecimal amount, String withdrawId) {
        transactionService.sendAsyncTransaction(coin.getName(), "", toAddress, amount, withdrawId);
        return null;
    }


    /**
     * 创建交易签名后由节点广播
     */
    public String sendSignTransaction(Map<String, BigDecimal> targetAddressesMap) throws Exception {
        if (targetAddressesMap!=null&&targetAddressesMap.size()>0) {
            return this.sendSignTransaction(new ArrayList<String>(), targetAddressesMap, null);
        }else {
            throw new RuntimeException("空map");
        }
    }

    /**
     * 创建交易签名后由节点广播
     */
    public String sendSignTransaction(List<String> fromAddresses, Map<String, BigDecimal> targetAddressesMap, BigDecimal fee) throws Exception {
        // 如果没有指定fromaddress，则默认查找全部的 fromAddresses
        if (fromAddresses.size() < 1) {
            List<String> allAddress = this.addressBtcMapper.getInUseAddress();
            fromAddresses.addAll(allAddress);
        }
        //获取未消费列表
        List<UTXO> utxos = this.getUnspent(fromAddresses);
        if (utxos == null || utxos.size() == 0) {
            throw new SignFailException("未消费列表为空");
        }
        /*根据地址去数据库查询秘钥*/
        Map<String, ECKey> keyMap = this.getKeyMap(fromAddresses);
        /*创建交易并签名*/
        // 获取主账户
        String chargeAddress = "";
        AddressBtcExample addressBtcExample = new AddressBtcExample();
        AddressBtcExample.Criteria criteria = addressBtcExample.createCriteria();
        criteria.andUserIdEqualTo("1").andMasterFlgEqualTo("1");
        AddressBtc addressBtc = this.addressBtcMapper.selectOneByExample(addressBtcExample);
        if (addressBtc != null) {
            chargeAddress = addressBtc.getAddress();
        } else {
            throw new SignFailException("获取主账号失败");
        }
        Transaction transaction = this.signTx(chargeAddress, targetAddressesMap, keyMap, utxos, fee);
        String sig = Hex.toHexString(transaction.bitcoinSerialize());
        logger.info("sig:" + sig);
        /*向节点发送签名裸交易，并广播*/
        try {
            String txid = rpcClient.sendRawTransaction(sig);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("txid:" + transaction.getTxId().toString());
        return transaction.getTxId().toString();
    }

    /**
     * 根据地址去数据库查询秘钥
     */
    private Map<String, ECKey> getKeyMap(List<String> fromAddresses) throws Exception {
        HashMap<String, ECKey> keyMap = new HashMap<>();
        for (String address : fromAddresses) {
            // 从数据库中获取转账人信息
            AddressBtcExample addressBtcExample = new AddressBtcExample();
            addressBtcExample.createCriteria().andAddressEqualTo(address);
            List<AddressBtc> addressBtcs = this.addressBtcMapper.selectByExample(addressBtcExample);
            if (addressBtcs.size() != 1) {
                logger.info("address:" + address);
                throw new RuntimeException(addressBtcs.size() > 1 ? "数据库此地址信息不唯一！" : "数据库无此地址信息！");
            }
//        // 获取交易记录
//        SysRecordEntity sysRecordEntity = new SysRecordEntity();
            // 转账人私钥需要解密，先解密私钥的aes秘钥，再解密私钥
            String deAesKey = AESUtils.deCode(addressBtcs.get(0).getAesKey(), RpcEnvironmentConfig.aesKeySecret);
            String dePriKey = AESUtils.decryptForCoupons(addressBtcs.get(0).getPriKey(), deAesKey);
            /*hex进制的私钥，生成ECkey*/
            ECKey ecKey = ECKey.fromPrivate(new BigInteger(dePriKey, 16));
            /*wif格式的私钥，生成ECkey*/
//            ECKey ecKey = DumpedPrivateKey.fromBase58(params, dePriKey).getKey();
            keyMap.put(address, ecKey);
        }
        return keyMap;
    }

    /**
     * 签名交易
     */
    public Transaction signTx(String changeAddress, Map<String, BigDecimal> targetAddressesMap, Map<String, ECKey> keyMap,
                              List<UTXO> utxos, BigDecimal fee) throws Exception {
        Transaction transaction = new Transaction(params);
        //输出总金额
        long outAmounts = 0L;
        //找零总金额
        long changeAmount = 0L;
        //输入总金额
        long utxoAmount = 0L;
        //遍历生成Output
        for (String address : targetAddressesMap.keySet()) {
            long outAmount = targetAddressesMap.get(address).multiply(new BigDecimal("100000000")).longValue();
            transaction.addOutput(Coin.valueOf(outAmount), LegacyAddress.fromString(params, address));
            outAmounts += outAmount;
        }
        Long estimateFee = 0L;
        if (fee == null || fee.compareTo(BigDecimal.ZERO) == 0) {
            //估算手续费
            estimateFee = getFee(outAmounts, utxos, targetAddressesMap.size());
        } else {
            //估算手续费为fee
            estimateFee = new BigDecimal("100000000").multiply(fee).longValue();
        }

        //遍历未花费列表，组装实际需要花费的utxo
        List<UTXO> needspentUtxos = new ArrayList<UTXO>();
        for (UTXO utxo : utxos) {
            if (utxoAmount >= (outAmounts + estimateFee)) {
                break;
            } else {
                needspentUtxos.add(utxo);
                utxoAmount += utxo.getValue().value;
            }
        }
        //消费列表总金额 - 已经转账的金额 - 手续费 就等于需要返回给自己的金额了
        changeAmount = utxoAmount - (outAmounts + estimateFee);
        logger.info("estimateFee:" + estimateFee + ",outAmounts:" + outAmounts + ",changeAmount:" + changeAmount + "needspentUtxos" + needspentUtxos);
        if (changeAmount < 0) {
            logger.info("utxo余额不足");
            throw new SignFailException("utxo余额不足");
        }
        //输出-转给自己(找零)
        if (changeAmount > 0) {
            transaction.addOutput(Coin.valueOf(changeAmount), LegacyAddress.fromBase58(params, changeAddress));
        }
        /*SIGHASH_ALL、SIGHASH_NONE、SIGHASH_SINGLE、SIGHASH_ANYONECANPAY，四类共计六种，分别对应不同方式的交易*/
        /*以下2种签名方式均可以使用*/
        /*使用[ALL|ANYONECANPAY] 签名--开始*/
//        for (UTXO utxo : needspentUtxos) {
//            ECKey ecKey = keyMap.get(utxo.getAddress());
//            TransactionOutPoint outPoint = new TransactionOutPoint(params, utxo.getIndex(), utxo.getHash());
//            transaction.addSignedInput(outPoint, utxo.getScript(), ecKey, Transaction.SigHash.ALL, true);
//        }
        /*使用[ALL|ANYONECANPAY] 签名--结束*/
        /*使用[ALL]签名，--开始*/
        for (UTXO utxo : needspentUtxos) {
            transaction.addInput(utxo.getHash(), utxo.getIndex(), utxo.getScript());
        }
        for (TransactionInput input : transaction.getInputs()) {
            ECKey ecKey = keyMap.get(input.getScriptSig().getToAddress(params).toString());
            if (ScriptPattern.isP2PK(input.getScriptSig())) {
                TransactionSignature signature = transaction.calculateSignature(input.getIndex(), ecKey, input.getScriptSig(), Transaction.SigHash.ALL,
                        false);
                input.setScriptSig(ScriptBuilder.createInputScript(signature));
                input.setWitness(null);
            } else if (ScriptPattern.isP2PKH(input.getScriptSig())) {
                TransactionSignature signature = transaction.calculateSignature(input.getIndex(), ecKey, input.getScriptSig(), Transaction.SigHash.ALL,
                        false);
                input.setScriptSig(ScriptBuilder.createInputScript(signature, ecKey));
                input.setWitness(null);
            } else if (ScriptPattern.isP2WPKH(input.getScriptSig())) {
                Script scriptCode = new ScriptBuilder()
                        .data(ScriptBuilder.createOutputScript(LegacyAddress.fromKey(params, ecKey)).getProgram()).build();
                TransactionSignature signature = transaction.calculateWitnessSignature(input.getIndex(), ecKey, scriptCode, input.getValue(),
                        Transaction.SigHash.ALL, false);
                input.setScriptSig(ScriptBuilder.createEmpty());
                input.setWitness(TransactionWitness.redeemP2WPKH(signature, ecKey));
            } else {
                throw new ScriptException(ScriptError.SCRIPT_ERR_UNKNOWN_ERROR, "Don't know how to sign for this kind of scriptPubKey: " + input.getScriptSig());
            }
        }
        /*使用[ALL]签名，方法还需修改---结束*/
//        return Hex.toHexString(transaction.bitcoinSerialize());
        return transaction;
    }

    /***
     * 获取地址余额
     */
    public BigDecimal getBalance(String address) {
        List<UTXO> unspent = this.getUnspent(Arrays.asList(address));
        Long balance = 0L;
        for (UTXO utxo : unspent) {
            balance = balance + utxo.getValue().value;
        }
        logger.info(unspent.toString());
        return new BigDecimal(balance / 100000000.0).setScale(8, BigDecimal.ROUND_DOWN);
    }

    /***
     * 获取未消费列表
     */
    public List<UTXO> getUnspent(List<String> addresses) {
        Object[] arrayObj = addresses.toArray();
        String[] arrayStr = new String[arrayObj.length];
        for (int i = arrayObj.length - 1; i >= 0; i--) {
            arrayStr[i] = (String) arrayObj[i];
        }
        List<UTXO> utxos = new ArrayList();
        try {
            List<Bitcoin.Unspent> list = rpcClient.listUnspent(1, 9999999, arrayStr);
            if (list.size() == 0) {
                return utxos;
            } else {
                for (Bitcoin.Unspent unspent : list) {
                    //低于充值金额的交易，不使用此花费
//                    if (unspent.amount().compareTo(coin.getRechargeMinerFee()) <= 0) {
//                        continue;
//                    }
                    UTXO utxo = new UTXO(Sha256Hash.wrap(unspent.txid()), unspent.vout(), Coin.valueOf(unspent.amount().multiply(new BigDecimal("100000000")).longValue()),
                            0, false, new Script(Hex.decode(unspent.scriptPubKey())), unspent.address());
                    utxos.add(utxo);
                }
                return utxos;
            }
        } catch (BitcoinException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 估算矿工费用
     */
    private Long getFee(long amount, List<UTXO> utxos, Integer outPutSize) {
        //获取费率
        BigDecimal feeRate = this.getFeeRate();
        Long utxoAmount = 0L;
        Long fee = 0L;
        Long utxoSize = 0L;
        for (UTXO us : utxos) {
            utxoSize++;
            if (utxoAmount >= (amount + fee)) {
                break;
            } else {
                utxoAmount += us.getValue().value;
                fee = new BigDecimal(utxoSize * 148 + (outPutSize + 1) * 34 + 10).multiply(feeRate).longValue();
            }
        }
        return fee;
    }

    /**
     * 获取btc/b费率
     */
    public BigDecimal getFeeRate() {
        try {
            // 获取的费率是BTC/KB 需转换为BTC/B 除以 1000,再转换成链上的数值需乘以10000_0000,这里再加大1%，提高优先级
            BigDecimal multiply = new BigDecimal(rpcClient.estimatesmartfee().getString("feerate")).multiply(new BigDecimal(101000));
            return multiply.compareTo(BigDecimal.ONE) <= 0 ? new BigDecimal(1.01) : multiply;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    /**
     * 同步余额
     *
     * @param address
     * @throws IOException
     */
    public void syncAddressBalance(String address) throws IOException {
        logger.info("更新地址{}余额",address);
        BigDecimal balance = this.getBalance(address);
        logger.info("地址{}余额为：{}",address,balance.toString());
        BalanceBtc balanceBtc = new BalanceBtc();
        balanceBtc.setAddress(address);
        balanceBtc.setCurrency(coin.getName());
        balanceBtc.setAmount(balance);
        balanceBtc.setUpdateTime(new Date());
        int i = balanceBtcMapper.updateByPrimaryKey(balanceBtc);
        if (i < 1) {
            balanceBtcMapper.insert(balanceBtc);
        }
    }

    /**
     * 新地址导入节点
     */
    public Object importAddress(String address) throws BitcoinException {
        return rpcClient.importaddress(address);
    }
}
