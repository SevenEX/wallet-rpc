/**
 * Copyright (C), 2016-2019, XXX有限公司
 * FileName: UsdtServiceimpl
 * Author:   Administrator
 * Date:     2019/12/7/007 18:13
 * Description: 交易类
 * History:
 * <author>          <time>          <version>          <desc>
 * simon          修改时间           版本号              描述
 */
package com.ztuo.bc.wallet.service;

import com.alibaba.fastjson.JSONObject;
import com.spark.blockchain.rpcclient.Bitcoin;
import com.spark.blockchain.rpcclient.BitcoinException;
import com.ztuo.bc.wallet.aes.AESUtils;
import com.ztuo.bc.wallet.config.JsonrpcClient;
import com.ztuo.bc.wallet.config.RpcEnvironmentConfig;
import com.ztuo.bc.wallet.interceptor.SignFailException;
import com.ztuo.bc.wallet.mapperextend.AddressUsdtOmniMapperExtend;
import com.ztuo.bc.wallet.mapperextend.BalanceUsdtOmniMapperExtend;
import com.ztuo.bc.wallet.model.*;
import com.ztuo.bc.wallet.service.impl.BtcTransactionServiceImpl;
import org.bitcoinj.core.*;
import org.bitcoinj.script.Script;
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
 * 〈一句话功能简述〉<br>
 * 〈交易类〉
 *
 * @author Administrator
 * @create 2019/12/7/007
 * @since 1.0.0
 */
@Component
public class UsdtServiceimpl {
    Logger logger = LoggerFactory.getLogger(UsdtServiceimpl.class);
    @Autowired
    private JsonrpcClient rpcClient;
    @Autowired
    private NetworkParameters params;
    @Autowired
    private com.ztuo.bc.wallet.entity.Coin coin;
    @Autowired
    private AddressUsdtOmniMapperExtend addressUsdtOmniMapper;
    @Autowired
    private BalanceUsdtOmniMapperExtend balanceUsdtOmniMapper;
    @Autowired
    private BtcTransactionServiceImpl btcTransactionServiceImpl;

    @Value("${omni.currency.usdt.propertyid}")
    private BigInteger propertyid;

    /**
     * 创建交易签名后由节点广播
     */
    public String sendSignTransaction(String fromAddress, String toAddress, BigDecimal amount, Boolean sync, String withdrawId) throws Exception {
        if (sync == null || sync) {
            //非异步
            return this.sendSignTransaction(fromAddress, toAddress, amount);
        } else {
            //异步，则发送到队列中
            btcTransactionServiceImpl.sendAsyncTransaction(coin.getName(), fromAddress, toAddress, amount, withdrawId);
            return null;
        }
    }

    /**
     * 从所有可用账户提币
     */
    public Map<String,String>  sendTXFromAll(String toAddress, BigDecimal amount) throws Exception {
        List<String> allAddress = this.addressUsdtOmniMapper.getInUseAddress();
        BigDecimal transferedAmt = BigDecimal.ZERO;
        HashMap<String, String> mapResult = new HashMap<>();
        StringBuilder stringBuilder = new StringBuilder();
        //从多个账户提币，直到提币总额达到指定金额，或者提完全部账户。
        for (String account : allAddress) {
            //如果余额账户是目标账户，则跳过
            if (account.equalsIgnoreCase(toAddress)) {
                continue;
            }
            BigDecimal btcFee = rpcClient.getAddressBalance(account);
            if (btcFee.compareTo(coin.getDefaultMinerFee()) < 0) {
                logger.info("地址{}矿工费不足，最小为{},当前为{}", account, coin.getDefaultMinerFee(), btcFee);
                continue;
            }
            BigDecimal availAmt = this.getUsableBalance(account);
            logger.info("地址{}usdt余额{}", account,availAmt);

            if (availAmt.compareTo(amount.subtract(transferedAmt)) > 0) {
                availAmt = amount.subtract(transferedAmt);
                logger.info("地址{}usdt实际转出金额{}", account,availAmt);
            }
            if (availAmt.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            //String txid = rpcClient.omniSend(account.getAddress(),address,amount.toPlainString());
            //String txid = rpcClient.omniSend(account.getAddress(),address,availAmt);
            String txid = this.sendSignTransaction(account, toAddress, availAmt);
            if (txid != null) {
                logger.info("fromAddress" + account + ",txid:" + txid);
                transferedAmt = transferedAmt.add(availAmt);
                stringBuilder.append(txid+";");

            }
            if (transferedAmt.compareTo(amount) >= 0) {
                break;
            }
        }
        mapResult.put("amount",transferedAmt.toString());
        mapResult.put("txids",stringBuilder.toString());
        return mapResult;
    }

    /**
     * usdt 离线签名并发送交易
     */
    public String sendSignTransaction(String fromAddress, String toAddress, BigDecimal amount) throws Exception {
        String signedHex = "";
        Transaction transaction = new Transaction(params);
        //btc网络允许的最小转账金额
        long minerFee = coin.getDefaultMinerFee().multiply(new BigDecimal(100000000)).longValue();
        //输出总金额
        long outAmounts = minerFee;
        //找零总金额
        long changeAmount = 0L;
        //输入总金额
        long utxoAmount = 0L;
        BigDecimal balance = rpcClient.omniGetBalance(fromAddress);
        if (balance.compareTo(amount) < 0) {
            throw new SignFailException("账号余额"+balance.toString()+",小于待转账"+amount.toString());
        }
        //获取未消费列表
        List<UTXO> utxos = this.getUnspent(fromAddress);

        //估算手续费
        Long estimateFee = getFee(outAmounts, utxos, 2);

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
        logger.info("estimateFee:" + estimateFee + ",outAmounts:" + outAmounts + ",changeAmount:" + changeAmount + ",needspentUtxos" + needspentUtxos);
        if (changeAmount < 0) {
            logger.info("utxo余额不足");
            throw new SignFailException("utxo余额不足");
        }
        //构建usdt的输出脚本 注意这里的金额是要乘10的8次方.propertyid代币id
        // 获取OmniHex:  调用RPC： get_balance 、 omni_createpayload_simplesend 、 omni_createrawtx_opreturn
        // Omni Transaction 6f6d6e6900000000800005370000000000002710   txid：bd1a53a3c26e0daad364f3ba7fc8af8bc168a4729e1d4f606a33563142987c97
        String usdtHex = "6a146f6d6e69" + String.format("%016x", propertyid) + String.format("%016x", amount.multiply(new BigDecimal(100000000)).longValue());
        transaction.addOutput(Coin.valueOf(0L), new Script(Utils.HEX.decode(usdtHex)));
        //输出-转给自己(找零)
        // 获取主账户
        String chargeAddress = "";
        AddressUsdtOmniExample addressUsdtOmniExample = new AddressUsdtOmniExample();
        AddressUsdtOmniExample.Criteria criteria = addressUsdtOmniExample.createCriteria();
        criteria.andUserIdEqualTo("1").andMasterFlgEqualTo("1");
        AddressUsdtOmni addressUsdtOmni = this.addressUsdtOmniMapper.selectOneByExample(addressUsdtOmniExample);
        if (addressUsdtOmni != null) {
            chargeAddress = addressUsdtOmni.getAddress();
        } else {
            throw new SignFailException("获取主账号失败");
        }

        if (changeAmount > 0) {
            transaction.addOutput(Coin.valueOf(changeAmount), LegacyAddress.fromBase58(params, chargeAddress));
        }
        //先添加未签名的输入，也就是utxo
//        for (UTXO utxo : needspentUtxos) {
//            transaction.addInput(utxo.getHash(), utxo.getIndex(), utxo.getScript());
//        }
        //这是比特币的限制最小转账金额，所以很多usdt转账会收到一笔0.00000546的btc
        transaction.addOutput(Coin.valueOf(minerFee), LegacyAddress.fromBase58(params, toAddress));
        //下面就是签名
        for (UTXO utxo : needspentUtxos) {
            ECKey ecKey = this.getKey(utxo.getAddress());
            TransactionOutPoint outPoint = new TransactionOutPoint(params, utxo.getIndex(), utxo.getHash());
            transaction.addSignedInput(outPoint, utxo.getScript(), ecKey, Transaction.SigHash.ALL, true);
        }
        //这是签名之后的原始交易，直接去广播就行了
        signedHex = Hex.toHexString(transaction.bitcoinSerialize());
        logger.info("sig:" + signedHex);
        /*向节点发送签名裸交易，并广播*/
        try {
            String txid = rpcClient.sendRawTransaction(signedHex);
            logger.info("txid:" + txid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //这是交易的hash
        String txHash = Hex.toHexString(Utils.reverseBytes(Sha256Hash.hash(Sha256Hash.hash(transaction.bitcoinSerialize()))));
        logger.info("交易的hash:" + txHash);
        /*向节点发送签名裸交易，并广播*/
        logger.info("txid:" + transaction.getTxId().toString());
        return transaction.getTxId().toString();
    }

    /**
     * 根据地址去数据库查询秘钥
     */
    private ECKey getKey(String fromAddress) throws Exception {
        // 从数据库中获取转账人信息
        AddressUsdtOmniExample addressUsdtOmniExample = new AddressUsdtOmniExample();
        addressUsdtOmniExample.createCriteria().andAddressEqualTo(fromAddress);
        List<AddressUsdtOmni> addressUsdtOmnis = this.addressUsdtOmniMapper.selectByExample(addressUsdtOmniExample);
        if (addressUsdtOmnis.size() != 1) {
            logger.info("address:" + fromAddress);
            throw new RuntimeException(addressUsdtOmnis.size() > 1 ? "数据库此地址信息不唯一！" : "数据库无此地址信息！");
        }
        // 转账人私钥需要解密，先解密私钥的aes秘钥，再解密私钥
        String deAesKey = AESUtils.deCode(addressUsdtOmnis.get(0).getAesKey(), RpcEnvironmentConfig.aesKeySecret);
        String dePriKey = AESUtils.decryptForCoupons(addressUsdtOmnis.get(0).getPriKey(), deAesKey);
        /*hex进制的私钥，生成ECkey*/
        return ECKey.fromPrivate(new BigInteger(dePriKey, 16));

    }

    /***
     * 获取未消费列表
     */
    public List<UTXO> getUnspent(String address) {
        List<UTXO> utxos = new ArrayList();
        try {
            List<Bitcoin.Unspent> list = this.rpcClient.listUnspent(1, 9999999, address);
//            List<Bitcoin.Unspent> list = this.rpcClient.listUnspent(1, 9999999, "mgTow8G9EVpa4u5VLLWYCRm9QLN7KKhyBu","mfovHNgwxDmbYsUhDao4JWLKbzHsCDuizx");
            for (Bitcoin.Unspent unspent : list) {
//                //低于微尘交易，不使用此花费
//                if (unspent.amount().compareTo(coin.getMinSpentAmount())<=0) {
//                    continue;
//                }
                UTXO utxo = new UTXO(Sha256Hash.wrap(unspent.txid()), unspent.vout(), Coin.valueOf(unspent.amount().multiply(new BigDecimal("100000000")).longValue()),
                        0, false, new Script(Hex.decode(unspent.scriptPubKey())), unspent.address());
                utxos.add(utxo);
            }
            return utxos;
        } catch (BitcoinException e) {
            e.printStackTrace();
            return null;
        }
    }

    /***
     * 获取usdt未确认总额
     */
    public BigDecimal getUnconfirmedBalance(String address) {
        List<JSONObject> list = rpcClient.omniListpendingtransactions(address);
        BigDecimal total = BigDecimal.ZERO;
        for (JSONObject transaction : list) {
            total = total.add(transaction.getBigDecimal("amount"));
        }
        return total;
    }

    /***
     * 获取usdt可使用总额
     */
    public BigDecimal getUsableBalance(String address) {
        try {
            BigDecimal estimateBalance = rpcClient.omniGetBalance(address);
            return estimateBalance.subtract(getUnconfirmedBalance(address));
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
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
        BigDecimal balance = this.rpcClient.omniGetBalance(address);
        BalanceUsdtOmni balanceUsdtOmni = new BalanceUsdtOmni();
        balanceUsdtOmni.setAddress(address);
        balanceUsdtOmni.setCurrency(coin.getName());
        balanceUsdtOmni.setAmount(balance);
        balanceUsdtOmni.setUpdateTime(new Date());
        int i = balanceUsdtOmniMapper.updateByPrimaryKey(balanceUsdtOmni);
        if (i < 1) {
            balanceUsdtOmniMapper.insert(balanceUsdtOmni);
        }
    }

}
