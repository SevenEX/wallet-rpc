package com.ztuo.bc.wallet.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.ztuo.bc.wallet.aes.AESUtils;
import com.ztuo.bc.wallet.component.SysCode;
import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.entity.Contract;
import com.ztuo.bc.wallet.mapperextend.AddressEthMapperExtend;
import com.ztuo.bc.wallet.mapperextend.BalanceEthMapperExtend;
import com.ztuo.bc.wallet.model.*;
import com.ztuo.bc.wallet.util.EthConvert;
import com.ztuo.bc.wallet.util.MessageResult;
import com.ztuo.bc.wallet.web3j.PassPhraseUtility;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Component
public class EthService {
    private Logger logger = LoggerFactory.getLogger(EthService.class);
    @Autowired
    private Coin coin;
    @Autowired
    private Web3j web3j;
    @Autowired
    private PaymentHandler paymentHandler;
    @Autowired
    private JsonRpcHttpClient jsonrpcClient;
    @Autowired(required = false)
    private Contract contract;
    @Autowired
    private AddressEthMapperExtend addressEthMapper;
    @Autowired
    private BalanceEthMapperExtend balanceEthMapper;

    @Value("${aes.key}")
    private String aesKeySecret = "";

    /**
     * 地址是否是本平台的
     *
     * @param address 地址
     */
    public Boolean isAddressExist(String address) {
        AddressEth addressEth = addressEthMapper.selectByPrimaryKey(address);
        return addressEth != null && StringUtils.equals(address, addressEth.getAddress());
    }

    /**
     * 同步余额
     *
     * @param address 地址
     * @param currency 币种
     */
    public void syncAddressBalance(String address,String currency) throws IOException {
        BigDecimal balance;
        if(StringUtils.equals(currency, "ETH") || StringUtils.equals(currency, "Ethereum")|| StringUtils.equals(currency, "ETC")){
            balance = getBalance(address);
        }else {
            balance = getTokenBalance(address);
        }

        BalanceEth balanceEth = new BalanceEth();
        balanceEth.setAddress(address);
        balanceEth.setCurrency(currency);
        balanceEth.setAmount(balance);
        balanceEth.setUpdateTime(new Date());
        int i= balanceEthMapper.updateByPrimaryKey(balanceEth);
        if(i < 1){
            balanceEthMapper.insert(balanceEth);
        }
    }

    /**
     * 获取平台总余额
     *
     * @param currency 币种
     */
    public BigDecimal findBalanceSum(String currency) {
       return this.balanceEthMapper.findBalanceSum(currency);
    }

    /**
     * 根据余额和手续费查询符合条件的地址
     * @param minAmount 最小代币金额
     * @param gasLimit 最小gas
     * @return List<String> 地址list
     */
    public List<String> findByBalanceAndGas(BigDecimal minAmount,BigDecimal gasLimit) {
        return balanceEthMapper.findByBalanceAndGas(coin.getName(),minAmount, gasLimit);
    }

    /**
     * 根据余额查询
     * @param minAmount 最小金额
     * @return list
     */
    public List<BalanceEth> findByBalance(BigDecimal minAmount,String currency) {
        BalanceEthExample balanceEthExample = new BalanceEthExample();
        BalanceEthExample.Criteria criteria = balanceEthExample.createCriteria();
        criteria.andAmountGreaterThanOrEqualTo(minAmount)
        .andCurrencyEqualTo(currency);
        return balanceEthMapper.selectByExample(balanceEthExample);
    }

    /**
     * 从主账户中转账
     * @param toAddress 转入地址
     * @param amount 转账金额
     * @param amount 转账金额
     * @return list
     */
    public MessageResult<String> transferFromWithdrawWallet(String toAddress, BigDecimal amount, boolean sync, String withdrawId, String currency) {
        // 获取主账户
        AddressEthExample addressEthExample = new AddressEthExample();
        AddressEthExample.Criteria criteria = addressEthExample.createCriteria();
        criteria.andUserIdEqualTo("1").andMasterFlgEqualTo("1");
         AddressEth addressEth = this.addressEthMapper.selectOneByExample(addressEthExample);
        return transfer(addressEth, toAddress, amount, sync,withdrawId,currency);
    }

    public MessageResult<String> transfer(AddressEth addressEth, String toAddress, BigDecimal amount,boolean sync,String withdrawId, String currency) {
        Credentials credentials = null;
//        // 私钥
//        String dePriKey;
        try {
            // 转账人私钥需要解密
            String deAesKey = AESUtils.deCode(addressEth.getAesKey(), aesKeySecret);
            // 私钥
            String dePriKey = AESUtils.decryptForCoupons(addressEth.getPriKey(), deAesKey);
            credentials = Credentials.create(dePriKey);
//            credentials = WalletUtils.loadCredentials(password, walletFile);
        } catch (IOException e) {
            e.printStackTrace();
            return MessageResult.error(500, "钱包文件不存在");
        } catch (CipherException e) {
            e.printStackTrace();
            return MessageResult.error(500, "解密失败，密码不正确");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(sync) {
            return paymentHandler.transferEth(credentials, toAddress, amount);
        } else{
            paymentHandler.transferEthAsync(credentials, toAddress, amount,withdrawId, currency);
            return MessageResult.error(200,"提交成功");
        }
    }

    /**
     * 获取链上余额
     * @param address 地址
     * @return balance
     */
    public BigDecimal getBalance(String address) throws IOException {
        EthGetBalance getBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
        return Convert.fromWei(getBalance.getBalance().toString(), Convert.Unit.ETHER);
    }

//    /**
//     * 查询本地所有以太坊账户的余额
//     * @param address 地址
//     * @return balance
//     */
//    public List<BalanceEth> getAllEthBalance(String address, List<ContractAddress> contractAddressList)throws IOException {
//
//        // 链上余额list
//        List<BalanceEth> balanceDetailInfoList = new ArrayList<>();
//
//        // 查询eth的余额
//        BigInteger ethBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().getBalance();
//        // 返还参数用
//        BalanceEth balanceDetailInfo = new BalanceEth();
//        balanceDetailInfo.setCurrency("ETH");
//        balanceDetailInfo.setAmount(Convert.fromWei(ethBalance.toString(), Convert.Unit.ETHER));
//        balanceDetailInfoList.add(balanceDetailInfo);
//        // 代币查询方法
//        Function function2 = new Function("balanceOf", // 查询余额的方法名称
//                Arrays.asList(new Address(address)), Arrays.asList(new TypeReference<Address>() {
//        }));
//        String FunctionStr = FunctionEncoder.encode(function2);
//        // 循环查询所有代币地址
//        for (ContractAddress contractAddressDTO : contractAddressList) {
//            String value = web3j.ethCall(org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(address,
//                    contractAddressDTO.getContractAddress(), FunctionStr), DefaultBlockParameterName.LATEST).send()
//                    .getValue();
//            value = StringUtils.equals(value, "0x") ? "0" : value;
//            balanceDetailInfo = new BalanceEth();
//            balanceDetailInfo.setCurrency(contractAddressDTO.getName());
//            balanceDetailInfo.setAmount(new BigDecimal(Numeric.toBigInt(value).toString()).divide(BigDecimal.TEN.pow(contractAddressDTO.getNumPrecision())));
//            balanceDetailInfoList.add(balanceDetailInfo);
//
//        }
//        return balanceDetailInfoList;
//    }
    public BigInteger getGasPrice() throws IOException {
        EthGasPrice gasPrice = web3j.ethGasPrice().send();
        BigInteger baseGasPrice =  gasPrice.getGasPrice();
        return new BigDecimal(baseGasPrice).multiply(coin.getGasSpeedUp()).toBigInteger();
    }

    public MessageResult<Map<String, String>> transferFromWallet(String address, BigDecimal amount, BigDecimal fee, BigDecimal minAmount, String currency) {
        logger.info("transferFromWallet 方法");
        MessageResult<Map<String, String>> messageResult;
        Map<String, String> mapResult = new HashMap<>();
        StringBuilder stringBuilder = new StringBuilder();
        List<BalanceEth> accounts = this.findByBalance(minAmount,coin.getName());
        if (accounts == null || accounts.size() == 0) {
            messageResult = MessageResult.error(500, "没有满足条件的转账账户(大于0.1)!");
            logger.info(messageResult.toString());
            return messageResult;
        }
        // 已交易金额
        BigDecimal transferredAmount = BigDecimal.ZERO;
        for (BalanceEth account : accounts) {
            // 该账户扣除手续费后可交易金额
            BigDecimal realAmount = account.getAmount().subtract(fee);
            // 可交易金额 > 剩余需要转账金额 就转可剩下需要转的金额，不然刚账户金额全部转出
            if (realAmount.compareTo(amount.subtract(transferredAmount)) > 0) {
                realAmount = amount.subtract(transferredAmount);
            }
            // 获取账户
            AddressEth addressEth = this.addressEthMapper.selectByPrimaryKey(account.getAddress());
            MessageResult<String> result = transfer(addressEth, address, realAmount, true,"", currency);
//            MessageResult result = new MessageResult();
            if (result.getCode() == 0 && result.getData() != null) {
                stringBuilder.append(result.getData()+";");
                logger.info("transfer address={},amount={},txid={}", account.getAddress(), realAmount, result.getData());
                transferredAmount = transferredAmount.add(realAmount);
                try {
                    syncAddressBalance(account.getAddress(),coin.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (transferredAmount.compareTo(amount) >= 0) {
                break;
            }
        }
        mapResult.put("amount",transferredAmount.toPlainString());
        mapResult.put("txids",stringBuilder.toString());
        messageResult = MessageResult.success(mapResult);
        return messageResult;
    }

    public MessageResult<String> transferToken(String fromAddress, String toAddress, BigDecimal amount, boolean sync) {
        AddressEth addressEth = addressEthMapper.selectByPrimaryKey(fromAddress);
        Credentials credentials;
        try {
            // 转账人私钥需要解密
            String deAesKey = AESUtils.deCode(addressEth.getAesKey(), aesKeySecret);
            // 私钥
            String dePriKey = AESUtils.decryptForCoupons(addressEth.getPriKey(), deAesKey);
            credentials = Credentials.create(dePriKey);
            BigInteger nonce = paymentHandler.getNonce(fromAddress);
            BigInteger gasPrice = this.getGasPrice();
            BigInteger value = EthConvert.toWei(amount, contract.getUnit()).toBigInteger();
            Function fn = new Function("transfer", Arrays.asList(new Address(toAddress), new Uint256(value)),
                    Arrays.asList(new TypeReference<Address>() {
                    }, new TypeReference<Uint256>() {
                    }));
            String data = FunctionEncoder.encode(fn);
            BigInteger maxGas = contract.getGasLimit();
            logger.info("from={},value={},gasPrice={},gasLimit={},nonce={},address={}", fromAddress, value, gasPrice, maxGas, nonce, toAddress);
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                    nonce, gasPrice, maxGas, contract.getAddress(), data);
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);
            logger.info("hexRawValue={}", hexValue);
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
            logger.info("ethSendTransaction={}", JSON.toJSONString(ethSendTransaction));
            String transactionHash = ethSendTransaction.getTransactionHash();
            logger.info("txid:" + transactionHash);
            if (StringUtils.isEmpty(transactionHash)) {
                return MessageResult.error(500, "发送交易失败");
            } else {
//                if (etherscanApi != null) {
//                    logger.info("=====发送Etherscan广播交易======");
//                    etherscanApi.sendRawTransaction(hexValue);
//                }
//                payment.setTxid(transactionHash);
                MessageResult<String> mr = MessageResult.success("success");
                mr.setData(transactionHash);
                return mr;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new MessageResult(500, "交易失败,error:" + e.getMessage());
        }
    }

    public MessageResult<String> transferTokenFromWithdrawWallet(String toAddress, BigDecimal amount, boolean sync,String withdrawId) {
        // 获取主账户
        AddressEthExample addressEthExample = new AddressEthExample();
        AddressEthExample.Criteria criteria = addressEthExample.createCriteria();
        criteria.andUserIdEqualTo("1").andMasterFlgEqualTo("1");
        AddressEth addressEth = this.addressEthMapper.selectOneByExample(addressEthExample);
        Credentials credentials;
        try {
            // 转账人私钥需要解密
            String deAesKey = AESUtils.deCode(addressEth.getAesKey(), aesKeySecret);
            // 私钥
            String dePriKey = AESUtils.decryptForCoupons(addressEth.getPriKey(), deAesKey);
            credentials = Credentials.create(dePriKey);
//            //解锁提币钱包
//            credentials = WalletUtils.loadCredentials(coin.getWithdrawWalletPassword(), coin.getKeystorePath() + "/" + coin.getWithdrawWallet());
        } catch (IOException e) {
            e.printStackTrace();
            return new MessageResult<>(500, "私钥文件不存在");
        } catch (CipherException e) {
            e.printStackTrace();
            return new MessageResult<>(500, "解密失败，密码不正确");
        } catch (Exception e) {
            e.printStackTrace();
            return new MessageResult<>(500, "系统运行异常");
        }
        if(sync) {
            return paymentHandler.transferToken(credentials, toAddress, amount);
        }
        else{
            paymentHandler.transferTokenAsync(credentials, toAddress, amount, withdrawId);
            return new MessageResult<>(200,"提交成功");
        }
    }


    public BigDecimal getTokenBalance(String address) {
        BigInteger balance = BigInteger.ZERO;
        Function fn = new Function("balanceOf", Arrays.asList(new org.web3j.abi.datatypes.Address(address)), Collections.<TypeReference<?>>emptyList());
        String data = FunctionEncoder.encode(fn);
        Map<String, String> map = new HashMap<String, String>();
        map.put("to", contract.getAddress());
        map.put("data", data);
        try {
            String methodName = "eth_call";
            Object[] params = new Object[]{map, "latest"};
            String result = jsonrpcClient.invoke(methodName, params, Object.class).toString();
            if (StringUtils.isNotEmpty(result)) {
                if ("0x".equalsIgnoreCase(result) || result.length() == 2) {
                    result = "0x0";
                }
                balance = Numeric.decodeQuantity(result);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            logger.info("查询接口ERROR");
        }
        return  EthConvert.fromWei(new BigDecimal(balance), contract.getUnit());
    }

    public BigDecimal getMinerFee(BigInteger gasLimit) throws IOException {
        BigDecimal fee = new BigDecimal(getGasPrice().multiply(gasLimit));
        return Convert.fromWei(fee, Convert.Unit.ETHER);
    }

    public Boolean isTransactionSuccess(String txid) throws IOException {
        EthTransaction transaction =  web3j.ethGetTransactionByHash(txid).send();
        try {
            if (transaction != null && transaction.getTransaction().get() != null) {
                Transaction tx = transaction.getTransaction().get();
                if (!tx.getBlockHash().equalsIgnoreCase("0x0000000000000000000000000000000000000000000000000000000000000000")) {
                    Optional<TransactionReceipt> receipt = web3j.ethGetTransactionReceipt(txid).send().getTransactionReceipt();
                    if (receipt.isPresent() && receipt.get().getStatus().equalsIgnoreCase("0x1")) {
                        BigInteger lasterBlockNumber= web3j.ethBlockNumber().send().getBlockNumber();
                        boolean flag = lasterBlockNumber.subtract(receipt.get().getBlockNumber()).compareTo(new BigInteger("12")) >=0;
                        if(flag){
                            return true;
                        }

                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * 获取所有绑定账户数量
     * @return
     */
    public long count(){
        AddressEthExample addressEthExample = new AddressEthExample();
        AddressEthExample.Criteria criteria = addressEthExample.createCriteria();
        criteria.andUserIdIsNotNull();
        return this.addressEthMapper.countByExample(addressEthExample);
    }

    /**
     * 分页获取已绑定账户
     * @return
     */
    public List<AddressEth> getBindAccountPage(int pageNum, int pageSize){
        PageHelper.startPage(pageNum, pageSize);
        AddressEthExample addressEthExample = new AddressEthExample();
        AddressEthExample.Criteria criteria = addressEthExample.createCriteria();
        criteria.andUserIdIsNotNull();
        return this.addressEthMapper.selectByExample(addressEthExample);
    }

    /**
     * 绑定账户
     * @param account 业务层账户id
     * @return MessageResult
     */
    public MessageResult<String> bindAddress(String account) throws Exception{
        MessageResult<String> result = new MessageResult<>(0, "success");
        // 查询该账户是否绑定地址
        AddressEthExample addressEthExample = new AddressEthExample();
        AddressEthExample.Criteria criteria = addressEthExample.createCriteria();
        criteria.andUserIdEqualTo(account);
        AddressEth addressEth = this.addressEthMapper.selectOneByExample(addressEthExample);
        logger.info(account + "查询数据库=" + JSON.toJSONString(addressEth));
        if(addressEth == null){
            // 绑定账户
            int i = addressEthMapper.bindAccount(account);
            // 如果更新成功检索出绑定的地址
            if(i ==1){
                addressEth = this.addressEthMapper.selectOneByExample(addressEthExample);
            }else {
                // 绑定失败，生成一个新地址存入数据库
                addressEth= newAddress(account);
            }
        }
        logger.info(account + "返回结果=" + JSON.toJSONString(addressEth));
        result.setData(addressEth.getAddress());
        return result;
    }

    private AddressEth newAddress(String account) throws Exception{
        ECKeyPair ecKeyPair = null;
        ecKeyPair = Keys.createEcKeyPair();
        String password = PassPhraseUtility.getPassPhrase(8);
        // 账户私钥
        String priKey = Numeric.toHexStringNoPrefix(ecKeyPair.getPrivateKey());
        // 获取帐户AES密钥
        String aesKey = AESUtils.generateAESSecretKey();
        AddressEth addressEth = new AddressEth();
        // 生产的账户地址地址
        String address = Numeric.prependHexPrefix(Keys.getAddress(ecKeyPair));
        addressEth.setAddress(address);
        // 是否删除
        addressEth.setIsDelete(SysCode.IS_DELETE.NO);
        // 是否启用
        addressEth.setIsEnable(SysCode.IS_ENABLE.YES);
        // 用账户AES密钥加密账户私钥
        addressEth.setPriKey(AESUtils.encryptForCoupons(priKey, aesKey));
        // 业务系统ID
        addressEth.setSysId("app");
        addressEth.setMasterFlg("0");
        // 更新时间
        addressEth.setUpdateTime(new Date());
        // 随机密码
        addressEth.setPassword(AESUtils.encryptForCoupons(password, aesKey));
        // 钱包创建时间
        addressEth.setCreateTime(new Date());
        // 用系统AES密钥加密账户AES密钥
        String enCodeAesKey = AESUtils.enCode(aesKey, aesKeySecret);
        // 加密后账户AES密钥
        addressEth.setAesKey(enCodeAesKey);
        addressEth.setNonce(0);
        addressEth.setUserId(account);
        // 保存结果
        this.addressEthMapper.insert(addressEth);
        return addressEth;
    }

    public static void main(String[] args) throws IOException {
        EthService ethService = new EthService();
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30*1000, TimeUnit.MILLISECONDS);
        builder.writeTimeout(30*1000, TimeUnit.MILLISECONDS);
        builder.readTimeout(30*1000, TimeUnit.MILLISECONDS);
        OkHttpClient httpClient = builder.build();
        Web3j web3j = Web3j.build(new HttpService("http://36.153.147.94:8545/",httpClient,false));
        EthGetBalance getBalance = web3j.ethGetBalance("0x6fc84e5A02Cf7f05e0122Ae4D29733064bd03A49", DefaultBlockParameterName.LATEST).send();
        Convert.fromWei(getBalance.getBalance().toString(), Convert.Unit.ETHER);

        EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
        blockNumber.getBlockNumber().longValue();
    }
}
