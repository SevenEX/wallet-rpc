package com.ztuo.bc.wallet.controller;

import com.ztuo.bc.wallet.component.EthWatcher;
import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.interceptor.SystemLog;
import com.ztuo.bc.wallet.service.EthService;
import com.ztuo.bc.wallet.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

@RestController
@Api(tags = "账户管理")
@RequestMapping("/rpc")
public class WalletController {
    private Logger logger = LoggerFactory.getLogger(WalletController.class);
    @Autowired
    private EthService service;
    @Autowired
    private Web3j web3j;
    @Autowired
    private EthWatcher watcher;
    @Autowired
    private Coin coin;

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
    @SystemLog(module="ETH", methods="绑定地址")
    @GetMapping("address/{account}")
    public MessageResult getNewAddress(@PathVariable String account, @RequestParam(required = false, defaultValue = "") String password) {
        logger.info("create new account={},password={}", account, password);
        try {
            return service.bindAddress(account);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "rpc error:" + e.getMessage());
        }
    }

    /**
     * 资金归结，热钱包转冷却包
     *
     * @return ResultDto    返回类型
     * @Title: logout
     * @lastModify 2018年7月29日
     */
    @ApiOperation(value = "资金归结，热钱包转冷却包")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "address", value = "冷钱包地址", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "amount", value = "总归结金额", required = true, dataType = "BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "fee", value = "手续费", required = false, dataType = "BigDecimal", paramType = "query"),
    })
    @GetMapping("transfer")
    @SystemLog(module="ETH", methods="资金归结")
    public MessageResult transfer(String address, BigDecimal amount, BigDecimal fee) {
        logger.info("transfer:address={},amount={},fee={}", address, amount, fee);
        try {
            if (fee == null || fee.compareTo(BigDecimal.ZERO) <= 0) {
                fee = service.getMinerFee(coin.getGasLimit());
            }
            MessageResult result = service.transferFromWallet(address, amount, fee, coin.getMinCollectAmount(),coin.getName());
            logger.info("返回结果 : " + result.toString());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "error:" + e.getMessage());
        }
    }

    /**
     *
     *
     * @return ResultDto    返回类型
     * @Title: logout
     * @lastModify 2018年7月29日
     */
    @ApiOperation(value = "总账户转到指定账户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "address", value = "冷钱包地址", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "amount", value = "提币金额", required = true, dataType = "BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "sync", value = "false：异步交易", required = true, dataType = "Boolean", paramType = "query"),
            @ApiImplicitParam(name = "withdrawId", value = "提现id", required = true, dataType = "String", paramType = "query")
    })
    @SystemLog(module="ETH", methods="总账户转到指定账户")
    @GetMapping("withdraw")
    public MessageResult withdraw(String address, BigDecimal amount,
                                  @RequestParam(name = "sync",required = false,defaultValue = "true") Boolean sync,
                                  @RequestParam(name = "withdrawId",required = false,defaultValue = "") String withdrawId) {
        logger.info("withdraw:to={},amount={},sync={},withdrawId={}", address, amount, sync,withdrawId);
        try {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return MessageResult.error(500, "额度须大于0");
            }
            MessageResult result = service.transferFromWithdrawWallet(address, amount,sync,withdrawId, coin.getName());
            return result;
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
    @SystemLog(module="ETH", methods="获取热钱包总额")
    public MessageResult balance() {
        try {
            BigDecimal balance = this.service.findBalanceSum(coin.getName());
            MessageResult result = new MessageResult(0, "success");
            result.setData(balance);
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
    @ApiImplicitParams({
            @ApiImplicitParam(name = "address", value = "钱包地址", required = true, dataType = "String", paramType = "path")
    })
    @SystemLog(module="ETH", methods="获取单个地址余额")
    @GetMapping("balance/{address}")
    public MessageResult addressBalance(@PathVariable String address) {
        try {
            BigDecimal balance = service.getBalance(address);
            MessageResult result = new MessageResult(0, "success");
            result.setData(balance);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "查询失败，error:" + e.getMessage());
        }
    }

    @ApiOperation(value = "根据txid获取交易详细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "txid", value = "txid", required = true, dataType = "String", paramType = "path")
    })
    @GetMapping("transaction/{txid}")
    @SystemLog(module="ETH", methods="根据txid获取交易详细")
    public MessageResult transaction(@PathVariable String txid) throws IOException {
        EthTransaction transaction = web3j.ethGetTransactionByHash(txid).send();
        EthGasPrice gasPrice = web3j.ethGasPrice().send();

        System.out.println(gasPrice.getGasPrice());
        System.out.println(transaction.getRawResponse());
        MessageResult result = new MessageResult(0, "success");
        result.setData(transaction.getTransaction());
        return result;
    }

    @ApiOperation(value = "获取当前gasPrice")
    @GetMapping("gas-price")
    @SystemLog(module="ETH", methods="获取当前gasPrice")
    public MessageResult gasPrice() throws IOException {
        try {
            BigInteger gasPrice = service.getGasPrice();
            MessageResult result = new MessageResult(0, "success");
            result.setData(Convert.fromWei(gasPrice.toString(), Convert.Unit.GWEI));
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "查询失败，error:" + e.getMessage());
        }
    }

    @ApiOperation(value = "指定开始结束区块进行扫描交易")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startBlock", value = "开始区块", required = true, dataType = "Long", paramType = "query"),
            @ApiImplicitParam(name = "endBlock", value = "结束区块", required = true, dataType = "Long", paramType = "query")
    })
    @SystemLog(module="ETH", methods="指定开始结束区块进行扫描交易")
    @GetMapping("sync-block")
    public MessageResult manualSync(Long startBlock, Long endBlock) {
        try {
            watcher.replayBlockInit(startBlock, endBlock);
        } catch (IOException e) {
            e.printStackTrace();
            return MessageResult.error(500, "同步失败：" + e.getMessage());
        }
        return MessageResult.success();
    }
}
