package com.ztuo.bc.wallet.mapper;

import com.ztuo.bc.wallet.base.BaseMapper;
import com.ztuo.bc.wallet.model.AddressBtc;
import com.ztuo.bc.wallet.model.AddressBtcExample;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface AddressBtcMapper extends BaseMapper<AddressBtc> {
    long countByExample(AddressBtcExample example);

    int deleteByExample(AddressBtcExample example);

    int deleteByPrimaryKey(String address);

    int insert(AddressBtc record);

    int insertSelective(@Param("record") AddressBtc record, @Param("selective") AddressBtc.Column ... selective);

    AddressBtc selectOneByExample(AddressBtcExample example);

    AddressBtc selectOneByExampleSelective(@Param("example") AddressBtcExample example, @Param("selective") AddressBtc.Column ... selective);

    List<AddressBtc> selectByExampleSelective(@Param("example") AddressBtcExample example, @Param("selective") AddressBtc.Column ... selective);

    List<AddressBtc> selectByExample(AddressBtcExample example);

    AddressBtc selectByPrimaryKeySelective(@Param("address") String address, @Param("selective") AddressBtc.Column ... selective);

    AddressBtc selectByPrimaryKey(String address);

    int updateByExampleSelective(@Param("record") AddressBtc record, @Param("example") AddressBtcExample example, @Param("selective") AddressBtc.Column ... selective);

    int updateByExample(@Param("record") AddressBtc record, @Param("example") AddressBtcExample example);

    int updateByPrimaryKeySelective(@Param("record") AddressBtc record, @Param("selective") AddressBtc.Column ... selective);

    int updateByPrimaryKey(AddressBtc record);

    int batchInsert(@Param("list") List<AddressBtc> list);

    int batchInsertSelective(@Param("list") List<AddressBtc> list, @Param("selective") AddressBtc.Column ... selective);

    int upsert(AddressBtc record);

    int upsertSelective(@Param("record") AddressBtc record, @Param("selective") AddressBtc.Column ... selective);
}