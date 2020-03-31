package com.ztuo.bc.wallet.mapper;

import com.ztuo.bc.wallet.base.BaseMapper;
import com.ztuo.bc.wallet.model.AddressUsdtOmni;
import com.ztuo.bc.wallet.model.AddressUsdtOmniExample;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface AddressUsdtOmniMapper extends BaseMapper<AddressUsdtOmni> {
    long countByExample(AddressUsdtOmniExample example);

    int deleteByExample(AddressUsdtOmniExample example);

    int deleteByPrimaryKey(String address);

    int insert(AddressUsdtOmni record);

    int insertSelective(@Param("record") AddressUsdtOmni record, @Param("selective") AddressUsdtOmni.Column ... selective);

    AddressUsdtOmni selectOneByExample(AddressUsdtOmniExample example);

    AddressUsdtOmni selectOneByExampleSelective(@Param("example") AddressUsdtOmniExample example, @Param("selective") AddressUsdtOmni.Column ... selective);

    List<AddressUsdtOmni> selectByExampleSelective(@Param("example") AddressUsdtOmniExample example, @Param("selective") AddressUsdtOmni.Column ... selective);

    List<AddressUsdtOmni> selectByExample(AddressUsdtOmniExample example);

    AddressUsdtOmni selectByPrimaryKeySelective(@Param("address") String address, @Param("selective") AddressUsdtOmni.Column ... selective);

    AddressUsdtOmni selectByPrimaryKey(String address);

    int updateByExampleSelective(@Param("record") AddressUsdtOmni record, @Param("example") AddressUsdtOmniExample example, @Param("selective") AddressUsdtOmni.Column ... selective);

    int updateByExample(@Param("record") AddressUsdtOmni record, @Param("example") AddressUsdtOmniExample example);

    int updateByPrimaryKeySelective(@Param("record") AddressUsdtOmni record, @Param("selective") AddressUsdtOmni.Column ... selective);

    int updateByPrimaryKey(AddressUsdtOmni record);

    int batchInsert(@Param("list") List<AddressUsdtOmni> list);

    int batchInsertSelective(@Param("list") List<AddressUsdtOmni> list, @Param("selective") AddressUsdtOmni.Column ... selective);

    int upsert(AddressUsdtOmni record);

    int upsertSelective(@Param("record") AddressUsdtOmni record, @Param("selective") AddressUsdtOmni.Column ... selective);
}