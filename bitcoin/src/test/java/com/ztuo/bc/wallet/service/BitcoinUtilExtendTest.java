package com.ztuo.bc.wallet.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
class BitcoinUtilExtendTest {
    @Autowired
    private BitcoinUtilExtend bitcoinUtilExtend;
    @Test
    void getBalance() {
        bitcoinUtilExtend.getBalance("mfovHNgwxDmbYsUhDao4JWLKbzHsCDuizx");
    }
}