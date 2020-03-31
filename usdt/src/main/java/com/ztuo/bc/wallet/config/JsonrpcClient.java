package com.ztuo.bc.wallet.config;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.spark.blockchain.rpcclient.BitcoinException;
import com.spark.blockchain.rpcclient.BitcoinRPCClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 基于比特币rpc接口开发新的功能
 * <p>
 * TODO
 * </p>
 * 
 * @author: shangxl
 * @Date : 2017年11月16日 下午6:10:02
 */
public class JsonrpcClient extends BitcoinRPCClient {
	
	@Value("${omni.currency.usdt.propertyid}")
	private BigInteger propertyid;

	private Logger logger = LoggerFactory.getLogger(JsonrpcClient.class);
	/**
	 * <p>
	 * TODO
	 * </p>
	 * 
	 * @author: shangxl
	 * @param: @param
	 *             rpcUrl
	 * @param: @throws
	 *             MalformedURLException
	 */
	public JsonrpcClient(String rpcUrl) throws MalformedURLException {
		super(rpcUrl);
	}
	
   //接口废弃
	@Override
	public String getNewAddress(String accountName) {
		try {
			if (StringUtils.isNotEmpty(accountName)) {
					return super.getNewAddress(accountName);
			} else {
				return super.getNewAddress();
			}
		} catch (BitcoinException e) {
			logger.info("创建新币账户出错：  accountName=" + accountName);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 获取 btc余额
	 * @param address
	 * @return
	 */
	public BigDecimal getAddressBalance(String address) {
		BigDecimal balance = BigDecimal.ZERO;
		try {
			List<Unspent> unspents = this.listUnspent(1, 99999999, address);
			for(Unspent unspent:unspents){
				balance = balance.add(unspent.amount());
			}
			return balance;
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return new BigDecimal("-1");
	}



	/**
	 * 根据地址获取usdt的账户余额
	 * <p>
	 * TODO
	 * </p>
	 *
	 * @author: shangxl
	 * @param: @param
	 *             address
	 * @param: @return
	 * @return: String
	 * @Date : 2017年12月18日 下午5:39:04
	 * @throws:
	 */
	public BigDecimal omniGetBalance(String address) {
		String balance="0";
		try {
			Map<String, Object> map = (Map<String, Object>) query("omni_getbalance", new Object[] { address,propertyid});
			if (map != null) {
				balance= map.get("balance").toString();
			}
		} catch (BitcoinException e) {
			e.printStackTrace();
		}
		return new BigDecimal(balance);
	}

	public Map<String,Object> omniGetTransactions(String txid){
		try {
			return (Map<String, Object>) query("omni_gettransaction", new Object[]{txid});
		} catch (BitcoinException e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<String> omniListBlockTransactions(Long blockHeight){
		try {
			return (List<String>) query("omni_listblocktransactions", new Object[]{blockHeight});
		} catch (BitcoinException e) {
			e.printStackTrace();
			return null;
		}
	}
//	调用返回内存池中等待确认的omni交易清单
	public List<JSONObject> omniListpendingtransactions(String address){
		try {
			return (List<JSONObject>) query("omni_listpendingtransactions", new Object[]{address});
		} catch (BitcoinException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * usdt 转币
	 * <p> TODO</p>
	 * @author:         shangxl
	 * @param:    @return
	 * @return: String
	 * @Date :          2017年12月19日 下午3:38:31
	 * @throws:
	 */
	public String omniSend(String fromaddress,String toaddress,BigDecimal amount){
		try {
			return query("omni_send", new Object[]{fromaddress,toaddress,propertyid,amount.toPlainString()}).toString();
		} catch (BitcoinException e) {
			logger.info("fromaddress="+fromaddress+" toaddress="+toaddress+" propertyid="+propertyid+" amount="+amount);
			e.printStackTrace();
			return null;
		}
	}

	public String omniSend(String fromaddress, String toaddress, BigDecimal amount, BigDecimal bitcoinFee){
		try {
			return query("omni_send", new Object[]{fromaddress,toaddress,propertyid,amount.toPlainString(),fromaddress,bitcoinFee.toPlainString()}).toString();
		} catch (BitcoinException e) {
			logger.info("fromaddress="+fromaddress+" toaddress="+toaddress+" propertyid="+propertyid+" amount="+amount);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 预估手续费费率（0.16.0后已废弃）
	 *
	 * @return
	 * @throws BitcoinException
	 */
	public BigDecimal estimatefee() throws BitcoinException {
		return (BigDecimal) this.query("estimatefee", 3);
//		return (BigDecimal) this.query("estimatesmartfee ", 3);
	}
	/**
	 * 预估下个区块手续费费率
	 *
	 * @return
	 * @throws BitcoinException
	 */
	public JSONObject estimatesmartfee() throws BitcoinException {
		return (JSONObject) this.query("estimatesmartfee", 2, "CONSERVATIVE");
	}

	public static void main(String[] args) throws Exception {
		JsonrpcClient client = new JsonrpcClient("http://bitcoin:bitcoin@36.153.147.94:8332/");
//		JsonrpcClient client = new JsonrpcClient("http://bitcoin:bitcoin@127.0.0.1:18332/");
		System.out.println(client.estimatesmartfee());
		System.out.println(client.omniListpendingtransactions("miJX1RcZz5DheMmt6kX4wdZyNSoh2kgjfu"));
//		JSONObject.parseObject(client.omniListpendingtransactions("miJX1RcZz5DheMmt6kX4wdZyNSoh2kgjfu"))
		List<JSONObject> list = client.omniListpendingtransactions("miJX1RcZz5DheMmt6kX4wdZyNSoh2kgjfu");
		BigDecimal total=BigDecimal.ZERO;
		for (JSONObject transaction : list) {
			total=total.add(transaction.getBigDecimal("amount"));
		}
		System.out.println(total);
	}
}
