package com.ztuo.bc.wallet.config;


import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.entity.WatcherSetting;
import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.entity.WatcherSetting;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 自动配置币种参数
 */
@Configuration
@ConditionalOnProperty(name = "coin.name")
public class CoinConfig {

    @Bean
    @ConfigurationProperties(prefix = "coin")
    public Coin getCoin(){
        return new Coin();
    }

}
