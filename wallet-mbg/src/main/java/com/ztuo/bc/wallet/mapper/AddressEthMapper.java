package com.ztuo.bc.wallet.mapper;

import com.ztuo.bc.wallet.base.BaseMapper;
import com.ztuo.bc.wallet.model.AddressEth;
import com.ztuo.bc.wallet.model.AddressEthExample;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface AddressEthMapper extends BaseMapper<AddressEth> {
    long countByExample(AddressEthExample example);

    int deleteByExample(AddressEthExample example);

    int deleteByPrimaryKey(String address);

    int insert(AddressEth record);

    int insertSelective(@Param("record") AddressEth record, @Param("selective") AddressEth.Column ... selective);

    AddressEth selectOneByExample(AddressEthExample example);

    AddressEth selectOneByExampleSelective(@Param("example") AddressEthExample example, @Param("selective") AddressEth.Column ... selective);

    List<AddressEth> selectByExampleSelective(@Param("example") AddressEthExample example, @Param("selective") AddressEth.Column ... selective);

    List<AddressEth> selectByExample(AddressEthExample example);

    AddressEth selectByPrimaryKeySelective(@Param("address") String address, @Param("selective") AddressEth.Column ... selective);

    AddressEth selectByPrimaryKey(String address);

    int updateByExampleSelective(@Param("record") AddressEth record, @Param("example") AddressEthExample example, @Param("selective") AddressEth.Column ... selective);

    int updateByExample(@Param("record") AddressEth record, @Param("example") AddressEthExample example);

    int updateByPrimaryKeySelective(@Param("record") AddressEth record, @Param("selective") AddressEth.Column ... selective);

    int updateByPrimaryKey(AddressEth record);

    int batchInsert(@Param("list") List<AddressEth> list);

    int batchInsertSelective(@Param("list") List<AddressEth> list, @Param("selective") AddressEth.Column ... selective);

    int upsert(AddressEth record);

    int upsertSelective(@Param("record") AddressEth record, @Param("selective") AddressEth.Column ... selective);
}