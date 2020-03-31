package com.ztuo.bc.wallet.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ztuo.bc.wallet.entity.*;
import com.ztuo.bc.wallet.util.*;
import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.entity.Contract;
import com.ztuo.bc.wallet.entity.Payment;
import com.ztuo.bc.wallet.util.EthConvert;
import com.ztuo.bc.wallet.util.MessageResult;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;


/**
 * ETH与Token付款模块，支持同步任务与异步任务，在单地址可能出现连续付款的情况的下，使用异步阶列
 */
@Component
public class PaymentHandler {
	private Logger logger = LoggerFactory.getLogger(PaymentHandler.class);
	@Autowired
	private Web3j web3j;
	@Autowired
	private EthService ethService;
	@Autowired(required = false)
	private Contract contract;
	@Autowired
	private Coin coin;
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	@Autowired(required = false)
	private EtherscanApi etherscanApi;
	private Payment current;
	private LinkedList<Payment> tasks = new LinkedList<>();
	private int checkTimes = 0;
	private int maxCheckTimes = 100;
	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	private TxidService txidService;
	@Autowired
	private Dto2Payment dto2Payment;

	//    private AtomicReference<BigInteger> lastNonce = new AtomicReference<>();
//    private AtomicLong lastNonceRetrieve = new AtomicLong(0);
	private AtomicBoolean noncePending = new AtomicBoolean(false);
//    private AtomicBoolean nonceFixing = new AtomicBoolean(false);

	public void transferTokenAsync(Credentials credentials, String to, BigDecimal amount, String withdrawId) {
//        Payment payment = Payment.builder()
//                .credentials(credentials)
//                .amount(amount)
//                .to(to)
//                .txBizNumber(withdrawId)
//                .unit(coin.getUnit())
//                .build();
//		synchronized (tasks) {
//            tasks.addLast(payment);
//		}
		EthTransactionDto dto = EthTransactionDto.builder()
				.coinName(coin.getUnit()).withdrawId(withdrawId).fromAddress(credentials.getAddress()).toAddress(to).amount(amount)
				.build();
		redisUtil.lSet(coin.getName() + "_transaction", JSON.toJSONString(dto));
	}

	public void notify(Payment payment, int status) {
		JSONObject json = new JSONObject();
		json.put("withdrawId", payment.getTxBizNumber());
		json.put("txid", payment.getTxid());
		json.put("status", status);
		kafkaTemplate.send("withdraw-notify", coin.getName(), JSON.toJSONString(json));
	}

	// 异步发送交易
	public void transferEthAsync(Credentials credentials, String to, BigDecimal amount, String withdrawId,  String currency) {
//		Payment payment = Payment.builder()
//				.credentials(credentials)
//				.amount(amount)
//				.to(to)
//				.txBizNumber(withdrawId)
//				.unit(coin.getUnit())
//				.build();
//		synchronized (tasks) {
//			tasks.addLast(payment);
//		}
		EthTransactionDto dto = EthTransactionDto.builder()
				.coinName("ETH").withdrawId(withdrawId).fromAddress(credentials.getAddress()).toAddress(to).amount(amount)
				.build();
		redisUtil.lSet(currency + "_transaction", JSON.toJSONString(dto));
	}

	// 异步发送交易
	public void transferEtcAsync(Credentials credentials, String to, BigDecimal amount, String withdrawId,  String currency) {
//		Payment payment = Payment.builder()
//				.credentials(credentials)
//				.amount(amount)
//				.to(to)
//				.txBizNumber(withdrawId)
//				.unit(coin.getUnit())
//				.build();
//		synchronized (tasks) {
//			tasks.addLast(payment);
//		}
		EthTransactionDto dto = EthTransactionDto.builder()
				.coinName("ETC").withdrawId(withdrawId).fromAddress(credentials.getAddress()).toAddress(to).amount(amount)
				.build();
		redisUtil.lSet(currency + "_transaction", JSON.toJSONString(dto));
	}

	public MessageResult<String> transferEth(Credentials credentials, String to, BigDecimal amount) {
		Payment payment = Payment.builder()
				.credentials(credentials)
				.amount(amount)
				.to(to)
				.unit(coin.getUnit())
				.build();
		return transferEth(payment);
	}

	public MessageResult<String> transferEth(Payment payment) {
		String transactionHash ="";
		try {
//            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(payment.getCredentials().getAddress(), DefaultBlockParameterName.LATEST)
//                    .sendAsync()
//                    .get();
			// 获取最小手续费
			BigDecimal fee = ethService.getMinerFee(BigInteger.valueOf(21000));
			BigDecimal ethBalance = ethService.getBalance(payment.getCredentials().getAddress());

			if(ethBalance.compareTo(fee) < 0){
				return new MessageResult<>(500, "总账余额不足");
			}
			BigInteger nonce = getNonce(payment.getCredentials().getAddress());
			BigInteger gasPrice = ethService.getGasPrice();
			BigInteger value = Convert.toWei(payment.getAmount(), Convert.Unit.ETHER).toBigInteger();

			BigInteger maxGas = BigInteger.valueOf(21000);
			logger.info("value={},gasPrice={},gasLimit={},nonce={},address={}", value, gasPrice, maxGas, nonce, payment.getTo());
			RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
					nonce, gasPrice, maxGas, payment.getTo(), value);
			byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, payment.getCredentials());
			String hexValue = Numeric.toHexString(signedMessage);
			EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
			logger.info("ethSendTransaction={}", JSON.toJSONString(ethSendTransaction));
			transactionHash = ethSendTransaction.getTransactionHash();
			logger.info("txid = {}", transactionHash);
			if (StringUtils.isEmpty(transactionHash)) {
				return MessageResult.error(500, "发送交易失败");
			} else {
				if (etherscanApi != null) {
					logger.info("=====发送Etherscan广播交易======");
					etherscanApi.sendRawTransaction(hexValue);
				}
				//如果改交易是手续费转账
				if (StringUtils.equals(payment.getTxBizNumber(), "charegefee")) {
					txidService.save(transactionHash);
				}
				MessageResult<String> mr = new MessageResult<>(0, "success");
				mr.setData(transactionHash);
				// 更新nonce
				redisUtil.set("nonce:" + payment.getCredentials().getAddress(), nonce);
				return mr;
			}
		} catch (Exception e) {
			if (StringUtils.isEmpty(transactionHash)) {
				return MessageResult.error(500, "发送交易失败");
			}else {
				return MessageResult.success("success", transactionHash);
			}
		}
	}

	public MessageResult<String> transferToken(Payment payment) {
		String transactionHash="";
		try {
//            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(payment.getCredentials().getAddress(), DefaultBlockParameterName.LATEST)
//                    .sendAsync()
//                    .get();
			BigDecimal tokenBalance = ethService.getTokenBalance(payment.getCredentials().getAddress());
			if(tokenBalance.compareTo(payment.getAmount()) < 0){
				return new MessageResult<>(500, "总账token余额不足");
			}
			// 获取最小手续费
			BigDecimal fee = ethService.getMinerFee(contract.getGasLimit());
			BigDecimal ethBalance = ethService.getBalance(payment.getCredentials().getAddress());

			if(ethBalance.compareTo(fee) < 0){
				return new MessageResult<>(500, "总账余额不足");
			}
			BigInteger nonce = getNonce(payment.getCredentials().getAddress());
			BigInteger gasPrice = ethService.getGasPrice();
			BigInteger value = EthConvert.toWei(payment.getAmount(), contract.getUnit()).toBigInteger();
			Function fn = new Function("transfer", Arrays.asList(new Address(payment.getTo()), new Uint256(value)),
					Arrays.asList(new TypeReference<Address>() {
					}, new TypeReference<Uint256>() {
					}));
			String data = FunctionEncoder.encode(fn);
			BigInteger maxGas = contract.getGasLimit();
			logger.info("from={},value={},gasPrice={},gasLimit={},nonce={},address={}", payment.getCredentials().getAddress(), value, gasPrice, maxGas, nonce, payment.getTo());
			RawTransaction rawTransaction = RawTransaction.createTransaction(
					nonce, gasPrice, maxGas, contract.getAddress(), data);
			byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, payment.getCredentials());
			String hexValue = Numeric.toHexString(signedMessage);
			logger.info("hexRawValue={}", hexValue);
			EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
			logger.info("ethSendTransaction={}", JSON.toJSONString(ethSendTransaction));
			transactionHash = ethSendTransaction.getTransactionHash();
			logger.info("txid:" + transactionHash);
			if (StringUtils.isEmpty(transactionHash)) {
				return MessageResult.error(500, "发送交易失败");
			} else {
				if (etherscanApi != null) {
					logger.info("=====发送Etherscan广播交易======");
					etherscanApi.sendRawTransaction(hexValue);
				}
				payment.setTxid(transactionHash);
				MessageResult<String> mr = MessageResult.success("success");
				mr.setData(transactionHash);
				return mr;
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (StringUtils.isEmpty(transactionHash)) {
				return MessageResult.error(500, "发送交易失败");
			}else {
				return MessageResult.success("success", transactionHash);
			}
		}
	}

	public MessageResult<String> transferToken(Credentials credentials, String to, BigDecimal amount) {
		Payment payment = Payment.builder()
				.credentials(credentials)
				.amount(amount)
				.to(to)
				.unit(coin.getUnit())
				.build();
		return transferToken(payment);
	}

	/**
	 * 检查当前任务是否支付完成
	 */
	@Scheduled(cron = "0/30 * * * * *")
	public synchronized void checkJob() {
		logger.info("检查付款任务状态");
//        && StringUtils.isNotEmpty(current.getTxid())
		if (current != null) {
			synchronized (current) {
				try {
					checkTimes++;
					if (ethService.isTransactionSuccess(current.getTxid())) {
						logger.info("转账{}已成功,检查次数:{}", JSON.toJSON(current), checkTimes);
						// 不是系统打手续费就进行回调
						if(!StringUtils.equals("charegefee", current.getTxBizNumber())){
							notify(current, 1);
						}
						current = null;
					} else {
						logger.info("转账{}未成功,检查次数:{}", JSON.toJSON(current), checkTimes);
						if (checkTimes > maxCheckTimes) {
							//超时未成功
							notify(current, 0);
							current = null;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			logger.info("无待确认的任务");
		}
	}

	public MessageResult transfer(Payment payment) {
		if (payment.getUnit().equalsIgnoreCase("ETH")) {
			return transferEth(payment);
		} else if (payment.getUnit().equalsIgnoreCase("ETC")) {
			return transferEth(payment);
		} else {
			return transferToken(payment);
	}
	}

	@Scheduled(cron = "0/30 * * * * *")
	public synchronized void doJob() {
		synchronized (tasks) {
			logger.info("开始执行付款任务，当前队列长度{}", tasks.size());
			if (current == null && tasks.size() > 0) {
				Payment payment = tasks.getFirst();
//                logger.debug("开始执行付款任务:payment---" + JSONObject.toJSONString(payment));
				MessageResult result = transfer(payment);
				logger.info("开始执行付款结果:result---" + JSONObject.toJSONString(result));
				if (result.getCode() == 0) {
					logger.info("------txID:" + result.getData().toString());
					payment.setTxid(result.getData().toString());
					tasks.removeFirst();
					current = payment;
					checkTimes = 0;
					// 异步交易失败回调业务层
				}else if(result.getCode() == 500){
					tasks.removeFirst();
					notify(payment, 0);
				}
			}else if(tasks.size()==0){
				Object object = redisUtil.lLeftPop(coin.getName() + "_transaction");
				if (object!=null&&!object.equals(0)) {
					EthTransactionDto dto = JSONObject.parseObject((String) object, EthTransactionDto.class);
					Payment payment = dto2Payment.converter(dto);
					if (payment!=null){
						tasks.addLast(payment);
					}
				}
			}
		}
	}



    public BigInteger getNonce(String address) {
//        if(nonceFixing.get()){
//            return BigInteger.ZERO;
//        }
		long thisTime = System.currentTimeMillis();
		Object lastTime = redisUtil.getAndSet("lastTime:" + address, thisTime);
		if (lastTime == null || thisTime - (long) lastTime > 10000) {
			if (noncePending.compareAndSet(false, true)) {
				try {

					EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING)
							.sendAsync()
							.get();
					BigInteger newNonce = ethGetTransactionCount.getTransactionCount();
					if (newNonce == null) {
						redisUtil.set("nonce:" + address, 0);
						return BigInteger.ZERO;
					}
					redisUtil.set("nonce:" + address, newNonce.intValue());
//                    lastNonce.set(newNonce);
					return newNonce;
				} catch (Exception e) {
					return BigInteger.ZERO;
//                    return lastNonce.accumulateAndGet(BigInteger.ONE, BigInteger::add);
				} finally {
					noncePending.set(false);
				}
			}
		}
		while (noncePending.get()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		String nonce = (String) redisUtil.get("nonce:" + address);
		return new BigInteger(nonce);
	}

	public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.connectTimeout(30 * 1000, TimeUnit.MILLISECONDS);
		builder.writeTimeout(30 * 1000, TimeUnit.MILLISECONDS);
		builder.readTimeout(30 * 1000, TimeUnit.MILLISECONDS);
		OkHttpClient httpClient = builder.build();
		Web3j web3j = Web3j.build(new HttpService("http://36.153.147.94:27545/", httpClient, false));

		Credentials credentials = Credentials.create("35CE8F873F72876D29689D75BB9643A62529BB8B845556298244E9300AE161BD");
		Payment payment = Payment.builder()
				.credentials(credentials)
				.amount(new BigDecimal("0.000000000000000000000000000000001"))
				.to("0x026cea94dbe77e699513af3f130c960f13bea149")
				.unit("ETC")
				.build();
		EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(payment.getCredentials().getAddress(), DefaultBlockParameterName.PENDING)
				.sendAsync()
				.get();
		BigInteger nonce = ethGetTransactionCount.getTransactionCount();

		EthGasPrice gasPrice = web3j.ethGasPrice().send();
		BigInteger baseGasPrice = gasPrice.getGasPrice();

		BigInteger value = Convert.toWei(payment.getAmount(), Convert.Unit.ETHER).toBigInteger();

		BigInteger maxGas = new BigInteger("21000");
//        logger.info("value={},gasPrice={},gasLimit={},nonce={},address={}", value, gasPrice, maxGas, nonce, payment.getTo());
		RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
				nonce, baseGasPrice, maxGas, payment.getTo(), value);

		byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, payment.getCredentials());
		String hexValue = Numeric.toHexString(signedMessage);
		EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
//        logger.info("ethSendTransaction={}", JSON.toJSONString(ethSendTransaction));
		String transactionHash = ethSendTransaction.getTransactionHash();
		System.out.println("txid = {}" + transactionHash);
	}
}
