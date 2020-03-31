package com.ztuo.bc.wallet.controller;

import com.ztuo.bc.wallet.config.JsonrpcClient;
import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.interceptor.SignFailException;
import com.ztuo.bc.wallet.mapperextend.AddressUsdtOmniMapperExtend;
import com.ztuo.bc.wallet.model.AddressUsdtOmni;
import com.ztuo.bc.wallet.model.AddressUsdtOmniExample;
import com.ztuo.bc.wallet.service.*;
import com.ztuo.bc.wallet.util.AddressAssert;
import com.ztuo.bc.wallet.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/rpc")
@Api(tags = "usdt-rpc接口")
public class WalletController {
    private Logger logger = LoggerFactory.getLogger(WalletController.class);
    @Autowired
    private JsonrpcClient rpcClient;
    @Autowired
    private UsdtServiceimpl usdtServiceimpl;
    @Autowired
    private UsdtOmniAccountService usdtOmniAccountService;
    @Autowired
    private AddressUsdtOmniMapperExtend addressUsdtOmniMapper;
    @Autowired
    private Coin coin;
    @Autowired
    private NetworkParameters params;
    
    @GetMapping("address/{account}")
    @ApiOperation(value = "新建账户/获取地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "账户号", required = true, dataType = "String", paramType = "path")
    })
    public MessageResult getNewAddress(@PathVariable String account){
        logger.info("bind new account={},", account);
        try {
            String address = usdtOmniAccountService.bindAddress(account);
            if (address != null) {
                return MessageResult.success("SUCCESS", address);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "usdt rpc error:" + e.getMessage());
        }
        return MessageResult.error(500, "rpc error:无地址绑定失败");
    }
    
    @GetMapping("withdraw")
    @ApiOperation(value = "从总账户提币")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "address", value = "to地址", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "amount", value = "金额", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "fee", value = "手续费(无用)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "sync", value = "false：异步交易", required = true, dataType = "Boolean", paramType = "query"),
            @ApiImplicitParam(name = "withdrawId", value = "提现id", required = true, dataType = "Boolean", paramType = "query")
    })
    public MessageResult withdraw(String address, BigDecimal amount,BigDecimal fee,Boolean sync,String withdrawId){
        logger.info("withdraw:address={},amount={},fee={}",address,amount,fee);
        try {
            if (amount.compareTo(BigDecimal.ZERO)<=0) {
                return MessageResult.error(500,"提币金额需大于0");
            }
            AddressAssert.checkAddress(params,address);
            // 获取主账户
            String chargeAddress = "";
            AddressUsdtOmniExample addressUsdtOmniExample = new AddressUsdtOmniExample();
            AddressUsdtOmniExample.Criteria criteria = addressUsdtOmniExample.createCriteria();
            criteria.andUserIdEqualTo("1").andMasterFlgEqualTo("1");
            AddressUsdtOmni addressUsdtOmni = this.addressUsdtOmniMapper.selectOneByExample(addressUsdtOmniExample);
            if (addressUsdtOmni != null) {
                chargeAddress = addressUsdtOmni.getAddress();
            } else {
                throw new SignFailException("获取主账号失败");
            }
//            BigDecimal balance = usdtServiceimpl.getUsableBalance(chargeAddress);
//            logger.info("{}---balance:{}", chargeAddress,balance.toString());
//            if (balance.compareTo(amount)<0) {
//                return MessageResult.error(500,"总账户余额="+balance.toString()+"，小于提币金额.");
//            }
            String txid = this.usdtServiceimpl.sendSignTransaction(chargeAddress,address,amount,sync,withdrawId);
            if(sync==null||sync) {
                if (StringUtils.isBlank(txid)) {
                    return MessageResult.error(500,"提币失败");
                }
                return MessageResult.success("success", txid);
            } else{
                return MessageResult.error(200,"异步提交成功");
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return MessageResult.error(500,"error:"+e.getMessage());
        }
    }

    @GetMapping("transfer")
    @ApiOperation(value = "从所有可用账户提币(如果提币金额大于可提额则提全部)资金归结")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "address", value = "to地址", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "amount", value = "金额", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "fee", value = "手续费(无用)", required = true, dataType = "String", paramType = "query")
    })
    public MessageResult transfer(String address, BigDecimal amount){
        logger.info("transfer:address={},amount={},fee={}",address,amount);
        try {
            AddressAssert.checkAddress(params,address);
            if (amount.compareTo(BigDecimal.ZERO)<=0) {
                return MessageResult.error(500,"提币金额需大于0");
            }
            Map<String, String> stringStringMap = this.usdtServiceimpl.sendTXFromAll(address, amount);
            return MessageResult.success("success",stringStringMap);
        }
        catch (Exception e){
            e.printStackTrace();
            return MessageResult.error(500,"error:"+e.getMessage());
        }
    }
    @GetMapping("transfer-from-address")
    @ApiOperation(value = "从指定账户提币(如果提币金额大于可提额则提全部)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fromAddress", value = "from地址", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "address", value = "to地址", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "amount", value = "金额", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "fee", value = "手续费(无用)", required = true, dataType = "String", paramType = "query")
    })
    public MessageResult transferFromAddress(String fromAddress,String address, BigDecimal amount,BigDecimal fee){
        logger.info("transfer:fromAddress={},address={},amount={},fee={}",fromAddress,address,amount,fee);
        try {
            AddressAssert.checkAddress(params,address);
            BigDecimal transferedAmt = BigDecimal.ZERO;
            if(fromAddress.equalsIgnoreCase(address)) {
                return MessageResult.error(500,"转入转出地址不能一样");
            }
            if (amount.compareTo(BigDecimal.ZERO)<=0) {
                return MessageResult.error(500,"提币金额需大于0");
            }
            BigDecimal availAmt = usdtServiceimpl.getUsableBalance(fromAddress);
            if (availAmt.compareTo(BigDecimal.ZERO)<=0) {
                return MessageResult.error(500,"提币地址的余额为0");
            }
            if(availAmt.compareTo(amount) > 0){
                availAmt = amount;
            }
            String txid = this.usdtServiceimpl.sendSignTransaction(fromAddress,address,availAmt);
            if(txid != null) {
                logger.info("fromAddress:"+fromAddress+",txid:"+txid);
                transferedAmt = transferedAmt.add(availAmt);
            }
            return MessageResult.success("success",transferedAmt);
        }
        catch (Exception e){
            e.printStackTrace();
            return MessageResult.error(500,"error:"+e.getMessage());
        }
    }
    /**
     * 获取热钱包总额
     */
    @ApiOperation(value = "获取热钱包总额")
    @GetMapping("balance")
    public MessageResult balance(){
        try {
            BigDecimal balance = this.usdtOmniAccountService.findBalanceSum(coin.getName());
            return MessageResult.success("success", balance);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "usdt查询失败，error:" + e.getMessage());
        }
    }
    
    /**
     * 获取单个地址余额
     */
    @ApiOperation(value = "获取地址的余额")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "address", value = "钱包地址", required = true, dataType = "String", paramType = "path")
    })
    @GetMapping("balance/{address}")
    public MessageResult balance(@PathVariable String address){
        try {
            BigDecimal balance = usdtServiceimpl.getUsableBalance(address);
            return MessageResult.success("success",balance);
        }
        catch (Exception e){
            e.printStackTrace();
            return MessageResult.error(500,"error:"+e.getMessage());
        }
    }

}
