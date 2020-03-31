package com.ztuo.bc.wallet.config;


import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.entity.WatcherSetting;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配置币种参数
 */
@Configuration
public class WatchConfig {

    @Bean
    @ConfigurationProperties(prefix = "watcher")
    public WatcherSetting getWatcherSetting(){
        return new WatcherSetting();
    }

}
