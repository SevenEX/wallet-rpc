package com.ztuo.bc.wallet.job;

import com.ztuo.bc.wallet.mapper.AddressBtcMapper;
import com.ztuo.bc.wallet.model.AddressBtc;
import com.ztuo.bc.wallet.model.AddressBtcExample;
import com.ztuo.bc.wallet.service.BitcoinUtilExtend;
import com.ztuo.bc.wallet.service.BtcAccountService;
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
public class CreateBtcAddressJob {
    
    private static final Logger logger = LoggerFactory.getLogger(CreateBtcAddressJob.class);
    
    @Autowired
    private AddressBtcMapper addressBtcMapper;
    @Autowired
    private BtcAccountService btcAccountService;
    @Autowired
    private BitcoinUtilExtend bitcoinUtil;
    
    /**
     * BTC地址池数量不足10个时，及时补充
     *
     * @throws Exception
     */
//    @Scheduled(cron = "0 0 * * * ?")
    @Scheduled(fixedDelayString = "3600000")//间隔1小时
    @Transactional(rollbackFor = Exception.class)
    public void createBtcAddress() throws Exception {
        // 查询BTC地址是否足够
        AddressBtcExample addressBtcExample = new AddressBtcExample();
        AddressBtcExample.Criteria criteria = addressBtcExample.createCriteria();
        criteria.andUserIdIsNull();
        List<AddressBtc> list = this.addressBtcMapper.selectByExample(addressBtcExample);
        int count = list.size();
        //判断是否已经有大钱包地址
        criteria = addressBtcExample.createCriteria();
        criteria.andUserIdEqualTo("1");
        AddressBtc addressSystem =addressBtcMapper.selectOneByExample(addressBtcExample);
        // 如果空闲地址少于10个 就新建100个地址
        if (count < 10) {
            for (int i = 0; i < 100; i++) {
                boolean flag = false;
                if(i==0 && addressSystem ==null){
                    flag = true;
                }
                AddressBtc addressBtc = this.btcAccountService.createAddress(flag);
                if (addressBtc != null) {
                    //todo 如果没有正确导入的异常未处理
                    this.bitcoinUtil.importAddress(addressBtc.getAddress());
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

}
