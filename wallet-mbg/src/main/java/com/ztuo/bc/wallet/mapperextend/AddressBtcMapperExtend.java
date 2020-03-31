package com.ztuo.bc.wallet.mapperextend;

import com.ztuo.bc.wallet.mapper.AddressBtcMapper;
import com.ztuo.bc.wallet.mapper.AddressEthMapper;
import com.ztuo.bc.wallet.model.AddressBtc;
import com.ztuo.bc.wallet.model.AddressBtcExample;
import com.ztuo.bc.wallet.model.AddressEth;
import com.ztuo.bc.wallet.model.AddressEthExample;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface AddressBtcMapperExtend extends AddressBtcMapper {

    @Update("update t_address_btc  set user_id = #{userId} where user_id is null ORDER BY address LIMIT 1")
    int bindAccount(String userId);
    @Select("select address from t_address_btc")
    List<String> getAllAddress();
    @Select("select address from t_address_btc where user_id is not null")
    List<String> getInUseAddress();
    @Select("SELECT * FROM t_address_btc")
    @Results({
            @Result(column = "address", property = "address"),
            @Result(column = "sys_id", property = "sysId"),
            @Result(column = "user_id", property = "userId"),
            @Result(property = "balanceList", column = "address", many = @Many(select = "com.ztuo.bc.wallet.mapperextend.BalanceBtcMapperExtend.findByAddress"))
    })
    List<AddressBtc> selectAddressAndBalance(AddressBtcExample example);
}