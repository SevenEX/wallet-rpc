/**
 * Copyright (C), 2016-2019, XXX有限公司
 * FileName: BtcNetworkParameters
 * Author:   Administrator
 * Date:     2019/11/25/025 17:12
 * Description: NetworkParameters
 * History:
 * <author>          <time>          <version>          <desc>
 * simon          修改时间           版本号              描述
 */
package com.ztuo.bc.wallet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 〈一句话功能简述〉<br>
 * 〈NetworkParameters〉
 *
 * @author Administrator
 * @create 2019/11/25/025
 * @since 1.0.0
 */
@Component
public class RpcEnvironmentConfig {
    
    //当前系统的运行环境
    public static Boolean isTest;
    
    //当前系统的aes秘钥
    public static String aesKeySecret;
    
    @Value("${rpc.environment.istest}")
    public void setIsTest(String isTest) {
        RpcEnvironmentConfig.isTest="true".equals(isTest);
    }
    
    @Value("${aes.key}")
    public void setAesKeySecret(String aesKeySecret) {
        RpcEnvironmentConfig.aesKeySecret = aesKeySecret;
    }
}
