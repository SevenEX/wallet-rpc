//package com.ztuo.bc.wallet.job;
//
//import com.ztuo.bc.wallet.mapperextend.AddressEthMapperExtend;
//import com.ztuo.bc.wallet.mapper.BalanceEthMapper;
//import com.ztuo.bc.wallet.mapper.ContractAddressMapper;
//import com.ztuo.bc.wallet.model.AddressEth;
//import com.ztuo.bc.wallet.model.AddressEthExample;
//import com.ztuo.bc.wallet.model.BalanceEth;
//import com.ztuo.bc.wallet.model.ContractAddress;
//import com.ztuo.bc.wallet.service.EthService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.List;
//
//
///**
// * @author nz
// * @Description: 定时查询本地所有以太坊账户的余额
// * @ClassName: QueryTransactionFromGeth
// * @date 2018年4月29日 上午10:28:14
// */
//@Component
//@Lazy(false)
//public class QueryBalanceFromGeth {
//    private static final Logger logger = LoggerFactory.getLogger(QueryBalanceFromGeth.class);
//
//    @Autowired
//    private AddressEthMapperExtend addressEthMapper;
//
//    @Autowired
//    private BalanceEthMapper balanceEthMapper;
//
//    @Autowired
//    private EthService ethService;
//
//    /**
//     * 定时同步任务 入口
//     */
//    @Scheduled(cron = "0 0 1 * * ?")
//    public void issueCreated() {
//        logger.info("查询链上余额开始。。。。。");
//        try {
//            // 获取所有代币的契约地址
//            List<ContractAddress> contractAddressList = this.contractAddressMapper.selectByExample(null);
//            // 获取本系统用户地址
//            List<AddressEth> accountList = new ArrayList<>();
//            // 获取本系统用户地址
//            AddressEthExample addressEthExample = new AddressEthExample();
//            AddressEthExample.Criteria criteria = addressEthExample.createCriteria();
//            criteria.andUserIdIsNotNull();
//            accountList = this.addressEthMapper.selectByExample(addressEthExample);
//            // 从参数中去除地址列表
//            // 根据传入的地址列表，进行循环查询
//            for (AddressEth addressEth : accountList) {
//                try {
//                    String address = addressEth.getAddress();
//                    // 线上余额list
//                    List<BalanceEth> balanceDetailInfoList = this.ethService.getAllEthBalance(address, contractAddressList);
//
//                    for (BalanceEth balanceEth : balanceDetailInfoList) {
//                        int i = balanceEthMapper.updateByPrimaryKey(balanceEth);
//                        if (i < 1) {
//                            balanceEthMapper.insert(balanceEth);
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//
////	/**
////	 *  定时更新btc余额
////	 */
////	@Scheduled(cron = "0 0 2 * * ?")
////	@Transactional
////	public void updateBtcBalance() {
////		logger.info("查询btc链上余额开始。。。。。");
////		try {
////
////
////			BitcoinRPCClientExtend rpcClient = ApplicationConfig.rpcClient;
////			List<Bitcoin.Unspent> list = rpcClient.listUnspent(0, 9999999);
////			Map<String, BigDecimal> balanceMap = new HashMap<>();
////
////			Iterator var10 = list.iterator();
////			while (var10.hasNext()) {
////				Bitcoin.Unspent unspent = (Bitcoin.Unspent) var10.next();
////				if (balanceMap.containsKey(unspent.address())) {
////					balanceMap.put(unspent.address(), balanceMap.get(unspent.address()).add(unspent.amount()));
////				} else {
////					balanceMap.put(unspent.address(), unspent.amount());
////				}
////
////			}
////			logger.info("余额情况：" + JSON.toJSONString(balanceMap));
////			AccountBtcBalanceEntity accountBtcBalanceEntity;
////			for(String key :balanceMap.keySet()){
////				accountBtcBalanceEntity = this.accountBtcBalanceDao.getAccountBalance(key);
////				if(accountBtcBalanceEntity == null){
////					accountBtcBalanceEntity= new AccountBtcBalanceEntity();
////					accountBtcBalanceEntity.setAddress(key);
////					accountBtcBalanceEntity.setAmount(balanceMap.get(key));
////					this.accountBtcBalanceDao.save(accountBtcBalanceEntity);
////				}else {
////					if(accountBtcBalanceEntity.getAmount().compareTo(balanceMap.get(key)) != 0){
////						accountBtcBalanceEntity.setAmount(balanceMap.get(key));
////						this.accountBtcBalanceDao.saveOrUpdate(accountBtcBalanceEntity);
////					}
////
////				}
////			}
////		}catch (Exception e){
////			e.printStackTrace();
////		}
////
////	}
//}
