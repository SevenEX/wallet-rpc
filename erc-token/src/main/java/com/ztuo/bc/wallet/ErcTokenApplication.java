package com.ztuo.bc.wallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableScheduling
@SpringBootApplication
@EnableSwagger2
public class ErcTokenApplication {

    public static void main(String ... args){
        SpringApplication.run(ErcTokenApplication.class,args);
    }
}
