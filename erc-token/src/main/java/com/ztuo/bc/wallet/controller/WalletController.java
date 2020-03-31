package com.ztuo.bc.wallet.controller;


import com.alibaba.fastjson.JSON;
import com.ztuo.bc.wallet.component.TokenWatcher;
import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.entity.Contract;
import com.ztuo.bc.wallet.entity.Payment;
import com.ztuo.bc.wallet.interceptor.SystemLog;
import com.ztuo.bc.wallet.service.EthService;
import com.ztuo.bc.wallet.service.PaymentHandler;
import com.ztuo.bc.wallet.util.MessageResult;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlockNumber;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RequestMapping("/rpc")
@RestController
public class WalletController {
    @Autowired
    private EthService service;
    @Autowired
    private Coin coin;
    @Autowired
    private Contract contract;
    @Autowired
    private TokenWatcher watcher;
    private Logger logger = LoggerFactory.getLogger(WalletController.class);
    @Autowired
    private Web3j web3j;
    @Autowired
    private PaymentHandler paymentHandler;
    /**
     * 获取当前区块高度
     *
     * @return ResultDto    返回类型
     * @Title: logout
     * @lastModify 2018年7月29日
     */
    @ApiOperation(value = "获取当前区块高度")
    @GetMapping("height")
    @SystemLog(module="ETH", methods="获取当前区块高度")
    public MessageResult getHeight() {
        try {
            EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
            long rpcBlockNumber = blockNumber.getBlockNumber().longValue();
            MessageResult result = new MessageResult(0, "success");
            result.setData(rpcBlockNumber);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "查询失败,error:" + e.getMessage());
        }
    }
    /**
     * 获取热钱包总额
     *
     * @return
     */
    @ApiOperation(value = "获取热钱包总额")
    @GetMapping("balance")
    @SystemLog(module="HNB", methods="获取热钱包总额")
    public MessageResult<BigDecimal> walletBalance() {
        try {
            BigDecimal amt = this.service.findBalanceSum(coin.getName());
            MessageResult<BigDecimal> result = new MessageResult<>(0, "success");
            result.setData(amt);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "查询失败，error:" + e.getMessage());
        }
    }

    /**
     * 获取热钱包总额
     *
     * @return
     */
    @ApiOperation(value = "测试")
    @GetMapping("signTest")
    @SystemLog(module="HNB", methods="获取热钱包总额")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "业务层账户id", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "password", value = "密码（不需要的参数）", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "sign", value = "密码（不需要的参数）", required = false, dataType = "String", paramType = "query"),
    })
    public MessageResult signTest(String account, @RequestParam(required = false, defaultValue = "") String password,String sign) {
        try {
            TreeMap<String, Object> map = new TreeMap<>();
//            securityService.verifySign(map);
            BigInteger a =paymentHandler.getNonce(account);
            BigDecimal amt = this.service.findBalanceSum(coin.getName());

            MessageResult result = new MessageResult(0, "success");
            result.setData(a);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "查询失败，error:" + e.getMessage());
        }
    }
    /**
     * 获取单个地址余额
     *
     * @param address
     * @return
     */
    @ApiOperation(value = "获取单个地址余额")
    @SystemLog(module="HNB", methods="获取单个地址余额")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "address", value = "钱包地址", required = true, dataType = "String", paramType = "path")
    })
    @GetMapping("balance/{address}")
    public MessageResult<BigDecimal> addressBalance(@PathVariable String address) {
        try {
            BigDecimal amt = service.getTokenBalance(address);
            MessageResult<BigDecimal> result = new MessageResult<>(0, "success");
            result.setData(amt);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "查询失败，error:" + e.getMessage());
        }
    }

    /**
     * 绑定地址
     *
     * @return ResultDto    返回类型
     * @Title: logout
     * @lastModify 2018年7月29日
     */
    @ApiOperation(value = "绑定地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "业务层账户id", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "password", value = "密码（不需要的参数）", required = false, dataType = "String", paramType = "query"),
    })
    @GetMapping("address/{account}")
    @SystemLog(module="HNB", methods="绑定地址")
    public MessageResult<String> getNewAddress(@PathVariable String account, @RequestParam(required = false, defaultValue = "") String password) {
        logger.info("create new account={},password={}", account, password);
        try {
            return service.bindAddress(account);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "rpc error:" + e.getMessage());
        }
    }

    /**
     * 指定账户之间转账
     *
     * @return ResultDto    返回类型
     * @Title: logout
     * @lastModify 2018年7月29日
     */
    @ApiOperation(value = "指定账户之间转账")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fromAddress", value = "转出地址", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "address", value = "转入地址", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "amount", value = "转账金额", required = true, dataType = "BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "fee", value = "手续费", required = false, dataType = "BigDecimal", paramType = "query"),
    })
    @SystemLog(module="HNB", methods="指定账户之间转账")
    @GetMapping("transfer-from-address")
    public MessageResult<String> transferFromAddress(String fromAddress,String address, BigDecimal amount, BigDecimal fee) {
        logger.info("transferFromAddress:from={},to={},amount={},fee={}",fromAddress,address, amount, fee);
        try {
            if (fee == null || fee.compareTo(BigDecimal.ZERO) <= 0) {
                fee = service.getMinerFee(contract.getGasLimit());
            }
            if(!service.isAddressExist(address)){
                return MessageResult.error(500,"转出地址必须是本系统地址");
            }
            if(service.getBalance(fromAddress).compareTo(fee) < 0){
                logger.info("地址{}手续费不足，最低为{}ETH",fromAddress,fee);
                return MessageResult.error(500,"矿工费不足");
            }
            MessageResult<String> result = service.transferToken(fromAddress,address, amount, true);
            logger.info("返回结果 : " + result.toString());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "error:" + e.getMessage());
        }
    }

    /**
     * 资金归结到指定账户
     *
     * @return ResultDto    返回类型
     * @Title: logout
     * @lastModify 2018年7月29日
     */
    @ApiOperation(value = "资金归结到指定账户")
    @SystemLog(module="HNB", methods="资金归结到指定账户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "address", value = "转入地址", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "amount", value = "转账金额", required = true, dataType = "BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "fee", value = "手续费", required = false, dataType = "BigDecimal", paramType = "query"),
    })
    @GetMapping("transfer")
    public MessageResult transfer(String address, BigDecimal amount, BigDecimal fee) {
        logger.info("transfer:address={},amount={},fee={}", address, amount, fee);
        BigDecimal transferredAmount = BigDecimal.ZERO;
        Map<String, String> mapResult = new HashMap<>();
        StringBuilder stringBuilder = new StringBuilder();
        try {
            if (fee == null || fee.compareTo(BigDecimal.ZERO) <= 0) {
                fee = service.getMinerFee(contract.getGasLimit());
            }
            List<String> accountList = service.findByBalanceAndGas(coin.getMinCollectAmount(),fee);
            for(String account:accountList) {
                if(service.getBalance(account).compareTo(fee) < 0){
                    logger.info("地址{}手续费不足，最低为{}",account,fee);
                    continue;
                }
                BigDecimal availAmt = service.getTokenBalance(account);
                if(availAmt.compareTo(coin.getMinCollectAmount()) < 0){
                    logger.info("地址{}余额不足，最低为{}",account,coin.getMinCollectAmount());
                    continue;
                }
                logger.info("from={},amount={},fee={}",account,availAmt,fee);
                MessageResult<String> result = service.transferToken(account, address, availAmt, true);
                if(result.getCode() == 0) {
                    stringBuilder.append(result.getData()+";");
                    transferredAmount = transferredAmount.add(availAmt);
                }
                if(transferredAmount.compareTo(amount) >= 0) {
                    break;
                }
            }
            logger.info("累计转出:{}",transferredAmount);
            if(transferredAmount.compareTo(BigDecimal.ZERO) <=0){
                return MessageResult.error(500, "没有满足条件的转账账户!");
            }
            mapResult.put("amount",transferredAmount.toPlainString());
            mapResult.put("txids",stringBuilder.toString());
            logger.info("返回：" + JSON.toJSONString(mapResult));
//            mr.setData(transferredAmount);
            return MessageResult.success(mapResult);
        } catch (Exception e) {
            e.printStackTrace();
            if(transferredAmount.compareTo(BigDecimal.ZERO) <=0){
                return MessageResult.error(500, "没有满足条件的转账账户!");
            }
            mapResult.put("amount",transferredAmount.toPlainString());
            mapResult.put("txids",stringBuilder.toString());
//            return MessageResult.error(500, "error:" + e.getMessage());
            return MessageResult.success(mapResult);
        }
    }

    /**
     * 总账户转到指定账户
     *
     * @return ResultDto    返回类型
     * @lastModify 2018年7月29日
     */
    @ApiOperation(value = "总账户转到指定账户")
    @SystemLog(module="HNB", methods="总账户转到指定账户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "address", value = "冷钱包地址", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "amount", value = "提币金额", required = true, dataType = "BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "sync", value = "false：异步交易", required = true, dataType = "Boolean", paramType = "query"),
            @ApiImplicitParam(name = "withdrawId", value = "提现id", required = true, dataType = "String", paramType = "query")
    })
    @GetMapping("withdraw")
    public MessageResult<String> withdraw(String address, BigDecimal amount,
                                  @RequestParam(name = "sync",required = false,defaultValue = "true") Boolean sync,
                                  @RequestParam(name = "withdrawId",required = false,defaultValue = "") String withdrawId) {
        logger.info("withdraw:to={},amount={},sync={},withdrawId={}", address, amount, sync,withdrawId);
        try {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return MessageResult.error(500, "额度须大于0");
            }
            return service.transferTokenFromWithdrawWallet(address, amount, sync,withdrawId);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "error:" + e.getMessage());
        }
    }

    @ApiOperation(value = "指定开始结束区块进行扫描交易")
    @SystemLog(module="HNB", methods="指定开始结束区块进行扫描交易")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startBlock", value = "开始区块", required = true, dataType = "Long", paramType = "query"),
            @ApiImplicitParam(name = "endBlock", value = "结束区块", required = true, dataType = "Long", paramType = "query")
    })
    @GetMapping("sync-block")
    public MessageResult manualSync(Long startBlock, Long endBlock) {
        try {
            logger.info("withdraw:startBlock={},endBlock={}", startBlock, endBlock);
            watcher.replayBlockInit(startBlock,endBlock);
            return MessageResult.success();
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "同步失败：" + e.getMessage());
        }
    }

    /**
     * 获取同步区块高度
     *
     * @return ResultDto    返回类型
     * @Title: logout
     * @lastModify 2018年7月29日
     */
    @ApiOperation(value = "获取同步区块高度")
    @SystemLog(module="HNB", methods="获取同步区块高度")
    @GetMapping("sync-height")
    public MessageResult<Long> getCurrentSyncHeight(){
        MessageResult<Long> result = MessageResult.success();
        result.setData(watcher.getNetworkBlockHeight());
        return result;
    }
}
