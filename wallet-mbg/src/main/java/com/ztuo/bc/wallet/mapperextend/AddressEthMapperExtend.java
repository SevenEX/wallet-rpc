package com.ztuo.bc.wallet.mapperextend;

import com.ztuo.bc.wallet.mapper.AddressEthMapper;
import com.ztuo.bc.wallet.model.AddressEth;
import com.ztuo.bc.wallet.model.AddressEthExample;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface AddressEthMapperExtend extends AddressEthMapper {

    @Update("update t_address_eth  set user_id = #{userId} where user_id is null ORDER BY address LIMIT 1")
    int bindAccount(String userId);

    @Select("SELECT * FROM t_address_eth")
    @Results({
            @Result(column = "address", property = "address"),
            @Result(column = "sys_id", property = "sysId"),
            @Result(column = "user_id", property = "userId"),
            @Result(property = "balanceList", column = "address", many = @Many(select = "com.ztuo.bc.wallet.mapperextend.BalanceEthMapperExtend.findByAddress"))
    })
    List<AddressEth> selectAddressAndBalance(AddressEthExample example);

}