package com.ztuo.bc.wallet.mapperextend;

import com.ztuo.bc.wallet.mapper.BalanceBtcMapper;
import com.ztuo.bc.wallet.mapper.BalanceUsdtOmniMapper;
import com.ztuo.bc.wallet.model.BalanceUsdtOmni;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Mapper
@Repository
public interface BalanceUsdtOmniMapperExtend extends BalanceUsdtOmniMapper {
    @Select("SELECT ifnull(sum(amount),0.0) FROM t_balance_usdt_omni where currency=#{currency}")
    BigDecimal findBalanceSum(String currency);

    @Select("SELECT address FROM t_balance_usdt_omni where currency = #{currency} and amount >= #{minCollectAmount}")
    List<String> getUsdtAddress(String currency, BigDecimal minCollectAmount);

    @Select("SELECT * FROM t_balance_usdt_omni where address=#{address}")
    List<BalanceUsdtOmni> findByAddress(String address);
}