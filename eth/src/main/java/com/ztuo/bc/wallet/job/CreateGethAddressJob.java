package com.ztuo.bc.wallet.job;

import com.alibaba.fastjson.JSON;
import com.ztuo.bc.wallet.mapper.AddressEthMapper;
import com.ztuo.bc.wallet.model.AddressEth;
import com.ztuo.bc.wallet.model.AddressEthExample;
import com.ztuo.bc.wallet.aes.AESUtils;
import com.ztuo.bc.wallet.component.SysCode;
import com.ztuo.bc.wallet.web3j.PassPhraseUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthSyncing;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.util.Date;
import java.util.List;


/** 
 * @Description: 创建地址
 * @ClassName: CreateGethAddressJob
 * @author nz
 * @date 2018年4月29日 上午10:28:14
 */
@Component
@Lazy(false)
public class CreateGethAddressJob {
	private static final Logger logger = LoggerFactory.getLogger(CreateGethAddressJob.class);

	/** accountDao:账户DAO */
	@Autowired
	private AddressEthMapper addressEthMapper;

	@Value("${aes.key}")
	private String aesKeySecret = "";
	/**
	 *  定时扫描空余以太坊地址，地址不足及时创建新地址
	 */
	@SuppressWarnings("rawtypes")
//	@Scheduled(cron = "0 * * * * ?")
	@Scheduled(fixedDelayString = "3600000")//间隔1小时
	@Transactional(rollbackFor = Exception.class)
	public void issueCreated() throws Exception {
		// 查询ETH地址是否足够
		AddressEthExample addressEthExample = new AddressEthExample();
		AddressEthExample.Criteria criteria = addressEthExample.createCriteria();
		criteria.andUserIdIsNull();
		List<AddressEth> list = this.addressEthMapper.selectByExample(addressEthExample);
		int count = list.size();
		//判断是否已经有大钱包地址
		criteria = addressEthExample.createCriteria();
		criteria.andUserIdEqualTo("1");
		AddressEth addressSystem =addressEthMapper.selectOneByExample(addressEthExample);
		boolean flag = false;
		// 如果空闲地址少于10个 就新建10个地址
		if(count < 10){
			for(int i =0; i<100; i++){
				ECKeyPair ecKeyPair = Keys.createEcKeyPair();
				String password = PassPhraseUtility.getPassPhrase(8);
				// 账户私钥
				String priKey = Numeric.toHexStringNoPrefix(ecKeyPair.getPrivateKey());
				// 获取帐户AES密钥
				String aesKey = AESUtils.generateAESSecretKey();
				AddressEth accountEntity = new AddressEth();
				// 生产的账户地址地址
				String address = Numeric.prependHexPrefix(Keys.getAddress(ecKeyPair));
				accountEntity.setAddress(address);
				// 是否删除
				accountEntity.setIsDelete(SysCode.IS_DELETE.NO);
				// 是否启用
				accountEntity.setIsEnable(SysCode.IS_ENABLE.YES);
				// 用账户AES密钥加密账户私钥
				accountEntity.setPriKey(AESUtils.encryptForCoupons(priKey, aesKey));
				// 业务系统ID
				accountEntity.setSysId("app");
				if(i==0 && addressSystem == null){
					accountEntity.setUserId("1");
					accountEntity.setMasterFlg("1");
				}else {
					//业务系统，大钱包标识 0：否 1:是"
					accountEntity.setMasterFlg("0");
				}
				// 更新时间
				accountEntity.setUpdateTime(new Date());
				// 随机密码
				accountEntity.setPassword(AESUtils.encryptForCoupons(password, aesKey));
				// 钱包创建时间
				accountEntity.setCreateTime(new Date());
				// 用系统AES密钥加密账户AES密钥
//				String enCodeAesKey = AESUtils.enCode(aesKey, RestConfig.aesKey);
				String enCodeAesKey = AESUtils.enCode(aesKey, aesKeySecret);
				// 加密后账户AES密钥
				accountEntity.setAesKey(enCodeAesKey);
				accountEntity.setNonce(0);
				// 保存结果
				this.addressEthMapper.insert(accountEntity);

			}
		}

	}

}
