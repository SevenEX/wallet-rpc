/**
 * Copyright (C), 2016-2019, XXX有限公司
 * FileName: AccountService
 * Author:   Administrator
 * Date:     2019/11/25/025 10:40
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * simon          修改时间           版本号              描述
 */
package com.ztuo.bc.wallet.service;

import com.spark.blockchain.rpcclient.BitcoinException;
import com.ztuo.bc.wallet.model.AddressBtc;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * 〈账户〉<br>
 *
 * @author Administrator
 * @create 2019/11/25/025
 * @since 1.0.0
 */
public interface BtcAccountService {
    
    /**一次创建10个地址*/
     void createTenAddress() throws BitcoinException;
     
    /**创建地址*/
     AddressBtc createAddress(boolean flag);
   
    /**绑定账户*/
     String bindAddress(String account) throws Exception;
    
    /**获取平台总余额*/
     BigDecimal findBalanceSum(String currency) throws IOException;

}
