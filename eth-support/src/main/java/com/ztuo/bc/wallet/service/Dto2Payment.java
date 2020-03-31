package com.ztuo.bc.wallet.service;

import com.ztuo.bc.wallet.aes.AESUtils;
import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.entity.EthTransactionDto;
import com.ztuo.bc.wallet.entity.Payment;
import com.ztuo.bc.wallet.mapper.AddressEthMapper;
import com.ztuo.bc.wallet.model.AddressEth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;


@Component
public class Dto2Payment {
	private Logger logger = LoggerFactory.getLogger(Dto2Payment.class);
	@Autowired
	private AddressEthMapper addressEthMapper;
	@Value("${aes.key}")
	private String aesKeySecret = "";

	public Payment converter(EthTransactionDto dto) {
		try {
			Credentials credentials = null;
				// 获取主账户
				AddressEth address = this.addressEthMapper.selectByPrimaryKey(dto.getFromAddress());
				// 转账人私钥需要解密
				String deAesKey = AESUtils.deCode(address.getAesKey(), aesKeySecret);
				// 私钥
				String dePriKey = AESUtils.decryptForCoupons(address.getPriKey(), deAesKey);
				credentials = Credentials.create(dePriKey);
			Payment payment = Payment.builder()
					.credentials(credentials)
					.amount(dto.getAmount())
					.to(dto.getToAddress())
					.txBizNumber(dto.getWithdrawId())
					.unit(dto.getCoinName())
					.build();
			return payment;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}