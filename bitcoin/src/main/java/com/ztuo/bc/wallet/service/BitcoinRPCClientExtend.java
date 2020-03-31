package com.ztuo.bc.wallet.service;

import com.alibaba.fastjson.JSONObject;
import com.spark.blockchain.rpcclient.BitcoinException;
import com.spark.blockchain.rpcclient.BitcoinRPCClient;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

/**
 * PRC工具类
 * @author Administrator
 */
public class BitcoinRPCClientExtend extends BitcoinRPCClient {
    private Logger looger = LoggerFactory.getLogger(BitcoinRPCClientExtend.class);
    
    public BitcoinRPCClientExtend(String rpcUrl) throws MalformedURLException {
        super(rpcUrl);
    }
    
    
    /**
     * 根据导入账户的时候label查找账户
     *
     * @param label
     * @return
     * @throws BitcoinException
     */
    public JSONObject getaddressesbylabel(String label) throws BitcoinException {
        return (JSONObject) this.query("getaddressesbylabel", label);
    }
    
    /**
     * 导入账户到节点
     *
     * @param address
     * @return
     * @throws BitcoinException
     */
    public Object importaddress(String address) throws BitcoinException {
        return this.query("importaddress", address, "watch-only", false);
    }
    
    /**
     * 预估下个区块手续费费率
     *
     * @return
     * @throws BitcoinException
        2. estimate_mode    (string, optional, default=CONSERVATIVE) The fee estimate mode.
        Whether to return a more conservative estimate which also satisfies
        a longer history. A conservative estimate potentially returns a
        higher feerate and is more likely to be sufficient for the desired
        target, but is not as responsive to short term drops in the
        prevailing fee market.  Must be one of:
                "UNSET"
                "ECONOMICAL"
                "CONSERVATIVE"
     */

    public JSONObject estimatesmartfee() throws BitcoinException {
        return (JSONObject) this.query("estimatesmartfee", 2, "CONSERVATIVE");
    }
    /**
     * 预估手续费费率（0.16.0后已废弃）
     *
     * @return
     * @throws BitcoinException
     */
    public BigDecimal estimatefee() throws BitcoinException {
        return (BigDecimal) this.query("estimatefee", 3);
    }
    /**
     * 获取区块详情）
     */
    public JSONObject getBlockAndDetails(String blockHash) throws BitcoinException {
//        return new BitcoinRPCClient.BlockMapWrapper((Map)this.query("getblock", blockHash));
        return (JSONObject)this.query("getblock", blockHash,2);
    }
    
    public static void main(String[] args) throws Exception {
//        BitcoinRPCClientExtend bitcoinRPCClientExtend = new BitcoinRPCClientExtend("http://bitcoin:bitcoin@127.0.0.1:18332/");
        BitcoinRPCClientExtend bitcoinRPCClientExtend = new BitcoinRPCClientExtend("http://bitcoin:bitcoin@36.153.147.94:8332/");
        JSONObject estimatesmartfee = bitcoinRPCClientExtend.estimatesmartfee();
        System.out.println(estimatesmartfee);
//        JSONObject block = bitcoinRPCClientExtend.getBlockAndDetails("000000000000021c0b38de03d2936ffb8a9b2fe036c6643d8badcd64d5fdc7ba");
//        System.out.println(block);
        RawTransaction tx = bitcoinRPCClientExtend.getRawTransaction("e4b7f6734f6f25d1db5ba22631295a7e244478c6e1cf27b6b2aa0e008597521a");
        List<Unspent> unspents = bitcoinRPCClientExtend.listUnspent(1, 99999, "1Krebc2nFtjRsrKBQwGFtPsBj2cPDRD8VW");
        bitcoinRPCClientExtend.importaddress("1Krebc2nFtjRsrKBQwGFtPsBj2cPDRD8VW");
        List<Unspent> unspents2 = bitcoinRPCClientExtend.listUnspent(1, 99999, "12A8W4T43s8LrSMw3V9pyW8pYTkXd1zuvW");
        NetworkParameters params = MainNetParams.get();
        ECKey key = new ECKey();
        // 旧地址（bhd只支持p2sh_p2wpkh地址）
        String legacyAddress = LegacyAddress.fromKey(params, key).toBase58();
        // 脚本地址（支持隔离见证）
        Script script = ScriptBuilder.createP2SHOutputScript(ScriptBuilder.createP2WPKHOutputScript(key));
        String scriptAddress = LegacyAddress.fromScriptHash(params, ScriptPattern.extractHashFromP2SH(script)).toString();
        // 账户私钥
        String priKey = key.getPrivateKeyAsHex();

        System.out.println("address:" + scriptAddress +"===="+priKey);
    }
}
