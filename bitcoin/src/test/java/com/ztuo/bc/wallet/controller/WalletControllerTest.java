package com.ztuo.bc.wallet.controller;

import com.spark.blockchain.rpcclient.Bitcoin;
import com.spark.blockchain.rpcclient.BitcoinException;
import com.ztuo.bc.wallet.interceptor.SignFailException;
import com.ztuo.bc.wallet.service.BitcoinRPCClientExtend;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.*;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.*;

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = {WalletBtcApplication.class})
//@TestPropertySource("C:\\ideaWorkSpace\\ztuo_wallet\\ztuo_wallet_rpc\\config\\application-dev.properties")
class WalletControllerTest {
    private BitcoinRPCClientExtend rpcClient = new BitcoinRPCClientExtend("http://bitcoin:bitcoin@192.168.100.7:8332/");
//    @Autowired
//    private BitcoinRPCClientExtend rpcClient;
    
    WalletControllerTest() throws MalformedURLException {
    }
    
    @Test
    void transactionTest() throws Exception {
        ArrayList<String> addresses = new ArrayList<>();
//        addresses.add("mqDV9iuFzieiGvXkwbLhooeEcT9yds5vc9");
//        addresses.add("mk6c7hVc42ySZPzJhHSqq9ZgrZ1NMWEzvV");
        addresses.add("mjBfrMKP7vGf8k4uS9GUwK7i8LgcPbQ4XY");
        addresses.add("mxzmnd9iowQTLELCciVZSftpd9q3Lv38kf");
        //获取未消费列表
        List<UTXO> utxos = this.getUnspent(addresses);
        if (utxos == null || utxos.size() == 0) {
            throw new SignFailException("未消费列表为空");
        }
        Map<String, ECKey> keyMap = this.getKeyMap(addresses);
        String sig = this.sign("mjBfrMKP7vGf8k4uS9GUwK7i8LgcPbQ4XY", Arrays.asList("mxzmnd9iowQTLELCciVZSftpd9q3Lv38kf"), keyMap,
                new BigDecimal(0.04).multiply(new BigDecimal("100000000")).longValue(), utxos);
        System.out.println("sig:" + sig);
        String txid = rpcClient.sendRawTransaction(sig);
        System.out.println("txid" + txid);
    }
    
    /**
     * @return
     */
    private Map<String, ECKey> getKeyMap(List<String> addresses) {
        HashMap<String, ECKey> keyMap = new HashMap<>();
        for (String address : addresses) {
            if ("mjBfrMKP7vGf8k4uS9GUwK7i8LgcPbQ4XY".equals(address)) {
                /*hex进制的私钥，生成ECkey*/
//              ECKey ecKey = ECKey.fromPrivate(new BigInteger(privateKey,16));
                /*wif格式的私钥，生成ECkey*/
                ECKey ecKey = DumpedPrivateKey.fromBase58(TestNet3Params.get(), "cUZbu6nT1VfGHJt9c1JSQZMT5vEe9LuR2EHy8gnrBnWHuAVx5siS").getKey();
                keyMap.put(address, ecKey);
            } else {
//                ECKey ecKey = ECKey.fromPrivate("cVvZ12L3Xr9Lhtt8AR9U2EsKvzMRc8H73ZkAAUdvapU43Up4f3i4".getBytes());
                ECKey ecKey = DumpedPrivateKey.fromBase58(TestNet3Params.get(), "cPqHc9LJfntfT7puNiYupXnpkqQcjGgui6VS8gRzzFVGviyv6v8H").getKey();
                keyMap.put(address, ecKey);
            }
        }
        return keyMap;
//        // 从数据库中获取转账人信息
//        AccountBtcDTO accountDTO = this.accountBtcDao.getAccountInfo(transactionDetailDto.getFromAddress());
//        // 获取交易记录
//        SysRecordEntity sysRecordEntity = new SysRecordEntity();
//        try {
//            // 私钥
//            String dePriKey;
//            boolean flag = accountDTO != null && transactionDetailDto.getFromAddress().equals(accountDTO.getAddress());
//            if (flag) {
//                // 转账人私钥需要解密
//                String deAesKey = AESUtils.deCode(accountDTO.getAesKey(), RestConfig.aesKey);
//                dePriKey = AESUtils.decryptForCoupons(accountDTO.getPriKey(), deAesKey);
//            } else {
//                if (StringUtils.isEmpty(transactionDetailDto.getPriKey())) {
//                    log.info("转出地址非本系统地址");
//                    throw new BaseException(ErrorCode.ADDRESS_ERROR_7);
//                }
//                dePriKey = transactionDetailDto.getPriKey();
//            }
//        }
    
    
    }
    
    public String sign(String changeAddress, List<String> toAddresses, Map<String, ECKey> keyMap, long amount, List<UTXO> utxos) throws Exception {
//        NetworkParameters networkParameters = RpcEnvironment.isTest ? TestNet3Params.get() : MainNetParams.get();
        NetworkParameters networkParameters = TestNet3Params.get();
        Transaction transaction = new Transaction(networkParameters);
        long changeAmount = 0L;
        long utxoAmount = 0L;
        List<UTXO> spentUtxos = new ArrayList<UTXO>();
        for (String toAddress : toAddresses) {
            transaction.addOutput(Coin.valueOf(amount), LegacyAddress.fromBase58(networkParameters, toAddress));
        }

        //估算手续费
        Long fee = getFee(amount, utxos, toAddresses.size());
        //遍历未花费列表，组装合适的spentUtxos
        for (UTXO utxo : utxos) {
            if (utxoAmount >= (amount + fee)) {
                break;
            } else {
                spentUtxos.add(utxo);
                utxoAmount += utxo.getValue().value;
            }
        }
        //消费列表总金额 - 已经转账的金额 - 手续费 就等于需要返回给自己的金额了
        changeAmount = utxoAmount - (amount + fee);
        System.out.printf("fee:" + fee + ",utxoAmount:" + utxoAmount + ",changeAmount:" + changeAmount);
        if (changeAmount < 0) {
            throw new Exception("utxo余额不足");
        }
        //输出-转给自己(找零)
        if (changeAmount > 0) {
            transaction.addOutput(Coin.valueOf(changeAmount), LegacyAddress.fromBase58(networkParameters, changeAddress));
        }
        /*SIGHASH_ALL、SIGHASH_NONE、SIGHASH_SINGLE、SIGHASH_ANYONECANPAY，四类共计六种，分别对应不同方式的交易*/
        /*以下2种签名方式均可以使用*/
        /*使用[ALL|ANYONECANPAY] 签名--开始*/
//        for (UTXO utxo : spentUtxos) {
//            ECKey ecKey = keyMap.get(utxo.getAddress());
//            TransactionOutPoint outPoint = new TransactionOutPoint(networkParameters, utxo.getIndex(), utxo.getHash());
//            transaction.addSignedInput(outPoint, utxo.getScript(), ecKey, Transaction.SigHash.ALL, true);
//        }
        /*使用[ALL|ANYONECANPAY] 签名--结束*/
        /*使用[ALL]签名，--开始*/
        for (UTXO utxo : spentUtxos) {
            transaction.addInput(utxo.getHash(), utxo.getIndex(), utxo.getScript());
        }
        for (TransactionInput input : transaction.getInputs()) {
            ECKey ecKey = keyMap.get(input.getScriptSig().getToAddress(TestNet3Params.get()).toString());
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
                        .data(ScriptBuilder.createOutputScript(LegacyAddress.fromKey(networkParameters, ecKey)).getProgram()).build();
                TransactionSignature signature = transaction.calculateWitnessSignature(input.getIndex(), ecKey, scriptCode, input.getValue(),
                        Transaction.SigHash.ALL, false);
                input.setScriptSig(ScriptBuilder.createEmpty());
                input.setWitness(TransactionWitness.redeemP2WPKH(signature, ecKey));
            } else {
                throw new ScriptException(ScriptError.SCRIPT_ERR_UNKNOWN_ERROR, " Don't know how to sign for this kind of scriptPubKey: " + input.getScriptSig());
            }
        }
        /*使用[ALL]签名，方法还需修改---结束*/
        return Hex.toHexString(transaction.bitcoinSerialize());
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
                Iterator var10 = list.iterator();
                while (var10.hasNext()) {
                    Bitcoin.Unspent unspent = (Bitcoin.Unspent) var10.next();
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
     * 获取矿工费用
     */
    public Long getFee(long amount, List<UTXO> utxos, Integer outPutSize) {
        BigDecimal feeRate = this.getFeeRate();//获取费率
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
     * 获取btc费率
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
}