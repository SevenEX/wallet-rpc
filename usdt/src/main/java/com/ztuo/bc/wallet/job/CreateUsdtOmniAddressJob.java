package com.ztuo.bc.wallet.job;

import com.spark.blockchain.rpcclient.BitcoinRPCClient;
import com.ztuo.bc.wallet.mapper.AddressUsdtOmniMapper;
import com.ztuo.bc.wallet.model.AddressUsdtOmni;
import com.ztuo.bc.wallet.model.AddressUsdtOmniExample;
import com.ztuo.bc.wallet.service.UsdtOmniAccountService;
import org.bitcoinj.core.*;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author nz
 * @Description: 创建地址
 * @ClassName: CreateGethAddressJob
 * @date 2018年4月29日 上午10:28:14
 */
@Component
public class CreateUsdtOmniAddressJob {
    
    private static final Logger logger = LoggerFactory.getLogger(CreateUsdtOmniAddressJob.class);
    
    @Autowired
    private AddressUsdtOmniMapper addressUsdtOmniMapper;
    @Autowired
    private UsdtOmniAccountService usdtOmniAccountService;
    @Autowired
    private BitcoinRPCClient bitcoinRPCClient;
    
    
    /**
     * usdt地址池数量不足10个时，及时补充
     *
     * @throws Exception
     */
//    @Scheduled(cron = "0 0 * * * ?")
    @Scheduled(fixedDelayString = "3600000")//间隔1小时
    @Transactional(rollbackFor = Exception.class)
    public void createUsdtAddress() throws Exception {
        // 查询usdt地址是否足够
        AddressUsdtOmniExample addressUsdtOmniExample = new AddressUsdtOmniExample();
        AddressUsdtOmniExample.Criteria criteria = addressUsdtOmniExample.createCriteria();
        criteria.andUserIdIsNull();
        List<AddressUsdtOmni> list = this.addressUsdtOmniMapper.selectByExample(addressUsdtOmniExample);
        int count = list.size();
        //判断是否已经有大钱包地址
        criteria = addressUsdtOmniExample.createCriteria();
        criteria.andUserIdEqualTo("1");
        AddressUsdtOmni addressSystem =addressUsdtOmniMapper.selectOneByExample(addressUsdtOmniExample);
        // 如果空闲地址少于10个 就新建100个地址
        if (count < 10) {
            for (int i = 0; i < 100; i++) {
                boolean flag = false;
                if(i==0 && addressSystem ==null){
                    flag = true;
                }
                AddressUsdtOmni addressUsdtOmni = this.usdtOmniAccountService.createAddress(flag);
                if (addressUsdtOmni != null) {
                    //todo 如果没有正确导入的异常未处理
                    this.bitcoinRPCClient.query("importaddress", addressUsdtOmni.getAddress(), "watch-only", false);
                }
            }
        }
    }
    
    /**
     * hex字符串转byte数组
     * @param inHex 待转换的Hex字符串
     * @return  转换后的byte数组结果
     */
    private static byte[] hexToByteArray(String inHex){
        int hexlen = inHex.length();
        byte[] result;
        if (hexlen % 2 == 1){
            //奇数
            hexlen++;
            result = new byte[(hexlen/2)];
            inHex="0"+inHex;
        }else {
            //偶数
            result = new byte[(hexlen/2)];
        }
        int j=0;
        for (int i = 0; i < hexlen; i+=2){
            result[j]=(byte)Integer.parseInt(inHex.substring(i,i+2),16);
            j++;
        }
        return result;
    }


    public static void main(String[] args) {
//        NetworkParameters params = MainNetParams.get();
        NetworkParameters params = TestNet3Params.get();
        ECKey key = new ECKey();
        System.out.format("hex私钥 => %s\n", key.getPrivateKeyAsHex());
        System.out.format("wif私钥 => %s\n", key.getPrivateKeyAsWiF(params));
        System.out.format("公钥 => %s\n", key.getPublicKeyAsHex());
        // 原始地址
        String legacyAddress = LegacyAddress.fromKey(params, key).toBase58();
        System.out.format("legacyAddress => %s\n", legacyAddress);
        // 脚本地址（支持隔离见证）
        Script script = ScriptBuilder.createP2SHOutputScript(ScriptBuilder.createP2WPKHOutputScript(key));
        String scriptAddress = LegacyAddress.fromScriptHash(params, ScriptPattern.extractHashFromP2SH(script)).toString();
        System.out.format("scriptAddress => %s\n", scriptAddress);
        //隔离见证地址
        String segwitAddress = SegwitAddress.fromKey(params, key).toBech32();
        System.out.format("segwitAddress => %s\n", segwitAddress);
        
        //从16进制公钥生成原始地址
            //获取公钥脚本hash
        byte[] bytes2 = Utils.sha256hash160(hexToByteArray(key.getPublicKeyAsHex()));
//        byte[] bytes2 = Utils.sha256hash160(hexToByteArray("033ec93470c8dc3af5618199fd4875e10f1e9ea61def10eee5a2527e1c5b8e4b32"));
        String legacyAddress2 = LegacyAddress.fromPubKeyHash(params, bytes2).toBase58();
        System.out.format("legacyAddress2 => %s\n", legacyAddress2);
        //从16进制公钥生成脚本地址（支持隔离见证）
        Script script2 = ScriptBuilder.createP2SHOutputScript(ScriptBuilder.createP2WPKHOutputScript(bytes2));
        String scriptAddress2 = LegacyAddress.fromScriptHash(params, ScriptPattern.extractHashFromP2SH(script2)).toString();
        System.out.format("scriptAddress2 => %s\n", scriptAddress2);
        //从16进制公钥生成隔离见证地址
        String segwitAddress2 = SegwitAddress.fromHash(params, bytes2).toBech32();
        System.out.format("segwitAddress2 => %s\n", segwitAddress2);
//
////		String priKey="bfdd950e25eed76765f973893de633e428a62c0bddf4df2b1dcc105815e4cc72";
////		// 获取帐户AES密钥
////		String aesKey = AESUtils.generateAESSecretKey();
////		// 用账户AES密钥加密账户私钥
////		System.out.println("加密后priKey " + AESUtils.encryptForCoupons(priKey, aesKey));
////		// 用系统AES密钥加密账户AES密钥
////		String enCodeAesKey = AESUtils.enCode(aesKey, "d6efc78c997c8b595a7a3dd5dc3c1f6a");
////		System.out.println("加密后aeskey " + enCodeAesKey);
//
    }
}
