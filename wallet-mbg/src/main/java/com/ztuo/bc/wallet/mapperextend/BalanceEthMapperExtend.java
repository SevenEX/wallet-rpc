package com.ztuo.bc.wallet.mapperextend;

import com.ztuo.bc.wallet.mapper.BalanceEthMapper;
import com.ztuo.bc.wallet.model.BalanceEth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Mapper
@Repository
public interface BalanceEthMapperExtend extends BalanceEthMapper {
    @Select("SELECT sum(amount) FROM t_balance_eth where currency=#{currency}")
    BigDecimal findBalanceSum(String currency);

    @Select("SELECT address FROM t_balance_eth where currency = #{currency} and amount>= #{minAmount} and address in( " +
            "SELECT address FROM t_balance_eth where currency = 'ETH' and amount >= #{gasLimit})")
    List<String> findByBalanceAndGas(String currency,BigDecimal minAmount,BigDecimal gasLimit);

    @Select("SELECT * FROM t_balance_eth where address=#{address}")
    List<BalanceEth> findByAddress(String address);
}