package com.ztuo.bc.wallet.controller;

import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.entity.TransactionDto;
import com.ztuo.bc.wallet.service.AccountService;
import com.ztuo.bc.wallet.service.BtcAccountService;
import com.ztuo.bc.wallet.service.BitcoinRPCClientExtend;
import com.ztuo.bc.wallet.service.BitcoinUtilExtend;
import com.ztuo.bc.wallet.util.AddressAssert;
import com.ztuo.bc.wallet.util.MessageResult;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;

@RestController
@RequestMapping("/rpc")
@Api(tags = "btc-rpc接口")
public class WalletController {
    private Logger logger = LoggerFactory.getLogger(WalletController.class);
    @Autowired
    private BitcoinRPCClientExtend rpcClient;
    @Autowired
    private BitcoinUtilExtend bitcoinUtil;
    @Autowired
    private NetworkParameters params;
    @Autowired
    private BtcAccountService btcAccountService;
    @Autowired
    private Coin coin;

    @GetMapping("height")
    @ApiOperation(value = "获取区块高度")
    public MessageResult getHeight() {
        try {
            int height = rpcClient.getBlockCount();
            return MessageResult.success("success",height-1);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "查询失败,error:" + e.getMessage());
        }
    }

    @GetMapping("address/{account}")
    @ApiOperation(value = "新建账户/获取地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "账户号", required = true, dataType = "String", paramType = "path")
    })
    public MessageResult getNewAddress(@PathVariable String account) {

        logger.info("bind new account={},", account);
        try {
            String address = btcAccountService.bindAddress(account);
            if (address != null) {
                return MessageResult.success("SUCCESS", address);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, " btc rpc error:" + e.getMessage());
        }
        return MessageResult.error(500, "rpc error:无地址绑定失败");
    }


    @GetMapping("withdraw")
    @ApiOperation(value = "交易")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "address", value = "地址", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "amount", value = "金额", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "fee", value = "手续费(无用)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "sync", value = "false：异步交易", required = true, dataType = "Boolean", paramType = "query"),
            @ApiImplicitParam(name = "withdrawId", value = "提现id", required = true, dataType = "Boolean", paramType = "query")
    })
    public MessageResult withdraw(String address, BigDecimal amount, BigDecimal fee,Boolean sync,String withdrawId) {
        logger.info("withdraw:address={},amount={},fee={}", address, amount, fee);
        try {
            if (amount.compareTo(coin.getMinSpentAmount()) <= 0) {
                return MessageResult.error(500, "提币金额须大于"+coin.getMinSpentAmount().toString());
            }
            AddressAssert.checkAddress(params,address);
            String txid = bitcoinUtil.sendSignTransaction(address, amount, fee,sync,withdrawId);
            if(sync==null||sync) {
                if (StringUtils.isBlank(txid)) {
                    return MessageResult.error(500,"提币失败");
                }
                return MessageResult.success("success", txid);
            } else{
                return MessageResult.error(200,"异步提交成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "error:" + e.getMessage());
        }
    }
    @GetMapping("transfer")
    @ApiOperation(value = "提币到冷钱包")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "address", value = "地址", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "amount", value = "金额", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "fee", value = "手续费(无用)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "sync", value = "false：异步交易", required = true, dataType = "Boolean", paramType = "query"),
            @ApiImplicitParam(name = "withdrawId", value = "提现id", required = true, dataType = "Boolean", paramType = "query")
    })
    public MessageResult transfer(String address, BigDecimal amount, BigDecimal fee,Boolean sync,String withdrawId) {
        logger.info("withdraw:address={},amount={},fee={}", address, amount, fee);
        try {
            if (amount.compareTo(coin.getMinSpentAmount()) <= 0) {
                return MessageResult.error(500, "提币金额须大于"+coin.getMinSpentAmount().toString());
            }
            AddressAssert.checkAddress(params,address);
            String txid = bitcoinUtil.sendSignTransaction(address, amount, fee,sync,withdrawId);
            if(sync==null||sync) {
                if (StringUtils.isBlank(txid)) {
                    return MessageResult.error(500,"提币失败");
                }
                HashMap<String, String> mapResult = new HashMap<>();
                mapResult.put("amount",amount.toString());
                mapResult.put("txids",txid);
                return MessageResult.success("success", mapResult);
            } else{
                return MessageResult.error(200,"异步提交成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "error:" + e.getMessage());
        }
    }
    @PostMapping("batchWithdraw")
    @ApiOperation(value = "批量交易")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dto", value = "交易对象", paramType = "body", dataType = "TransactionDto")
    })
    public MessageResult batchWithdraw(TransactionDto dto) {
        logger.info("batchWithdraw:TransactionDto={}", dto);
        try {
            for (String key : dto.getTargetAddressesMap().keySet()) {
                if (dto.getTargetAddressesMap().get(key).compareTo(BigDecimal.ZERO) <= 0) {
                    return MessageResult.error(500, "额度须大于0");
                }
            }
            String txid = bitcoinUtil.sendSignTransaction(dto.getFromAddress(), dto.getTargetAddressesMap(), null);
            return MessageResult.success("success", txid);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "error:" + e.getMessage());
        }
    }

    /**
     * 获取热钱包总额
     *
     * @return
     */
    @ApiOperation(value = "获取热钱包总额")
    @GetMapping("balance")
    public MessageResult balance() {
        try {
            BigDecimal balance = this.btcAccountService.findBalanceSum(coin.getName());
            return MessageResult.success("success", balance);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "查询失败，error:" + e.getMessage());
        }
    }

    /**
     * 获取单个地址余额（0.16.0后已废弃,getaccount的rpc接口已经废弃）
     *
     * @param address
     */
    @ApiOperation(value = "获取地址的余额（0.16.0后已废弃,getaccount的rpc接口已经废弃）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "address", value = "钱包地址", required = true, dataType = "String", paramType = "path")
    })
    @GetMapping("balance/{address}")
    public MessageResult balance(@PathVariable String address) {
        try {
            String account = rpcClient.getAccount(address);
            logger.info("account=" + account + ",address=" + address);
            BigDecimal balance = new BigDecimal(rpcClient.getBalance(account)).setScale(8, BigDecimal.ROUND_DOWN);
            return MessageResult.success("success", balance);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "error:" + e.getMessage());
        }
    }
}
