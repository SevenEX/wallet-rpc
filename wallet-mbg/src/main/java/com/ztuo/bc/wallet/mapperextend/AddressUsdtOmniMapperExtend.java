package com.ztuo.bc.wallet.mapperextend;

import com.ztuo.bc.wallet.mapper.AddressUsdtOmniMapper;
import com.ztuo.bc.wallet.model.AddressBtc;
import com.ztuo.bc.wallet.model.AddressBtcExample;
import com.ztuo.bc.wallet.model.AddressUsdtOmni;
import com.ztuo.bc.wallet.model.AddressUsdtOmniExample;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface AddressUsdtOmniMapperExtend extends AddressUsdtOmniMapper {

    @Update("update t_address_usdt_omni  set user_id = #{userId} where user_id is null ORDER BY address LIMIT 1")
    int bindAccount(String userId);
    @Select("select address from t_address_usdt_omni")
    List<String> getAllAddress();
    @Select("select address from t_address_usdt_omni where user_id is not null")
    List<String> getInUseAddress();
    @Select("SELECT * FROM t_address_usdt_omni")
    @Results({
            @Result(column = "address", property = "address"),
            @Result(column = "sys_id", property = "sysId"),
            @Result(column = "user_id", property = "userId"),
            @Result(property = "balanceList", column = "address", many = @Many(select = "com.ztuo.bc.wallet.mapperextend.BalanceUsdtOmniMapperExtend.findByAddress"))
    })
    List<AddressUsdtOmni> selectAddressAndBalance(AddressUsdtOmniExample example);
}