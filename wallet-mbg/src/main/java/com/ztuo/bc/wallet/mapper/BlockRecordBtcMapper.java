package com.ztuo.bc.wallet.mapper;

import com.ztuo.bc.wallet.base.BaseMapper;
import com.ztuo.bc.wallet.model.BlockRecordBtc;
import com.ztuo.bc.wallet.model.BlockRecordBtcExample;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface BlockRecordBtcMapper extends BaseMapper<BlockRecordBtc> {
    long countByExample(BlockRecordBtcExample example);

    int deleteByExample(BlockRecordBtcExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(BlockRecordBtc record);

    int insertSelective(@Param("record") BlockRecordBtc record, @Param("selective") BlockRecordBtc.Column ... selective);

    BlockRecordBtc selectOneByExample(BlockRecordBtcExample example);

    BlockRecordBtc selectOneByExampleSelective(@Param("example") BlockRecordBtcExample example, @Param("selective") BlockRecordBtc.Column ... selective);

    List<BlockRecordBtc> selectByExampleSelective(@Param("example") BlockRecordBtcExample example, @Param("selective") BlockRecordBtc.Column ... selective);

    List<BlockRecordBtc> selectByExample(BlockRecordBtcExample example);

    BlockRecordBtc selectByPrimaryKeySelective(@Param("id") Integer id, @Param("selective") BlockRecordBtc.Column ... selective);

    BlockRecordBtc selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") BlockRecordBtc record, @Param("example") BlockRecordBtcExample example, @Param("selective") BlockRecordBtc.Column ... selective);

    int updateByExample(@Param("record") BlockRecordBtc record, @Param("example") BlockRecordBtcExample example);

    int updateByPrimaryKeySelective(@Param("record") BlockRecordBtc record, @Param("selective") BlockRecordBtc.Column ... selective);

    int updateByPrimaryKey(BlockRecordBtc record);

    int batchInsert(@Param("list") List<BlockRecordBtc> list);

    int batchInsertSelective(@Param("list") List<BlockRecordBtc> list, @Param("selective") BlockRecordBtc.Column ... selective);

    int upsert(BlockRecordBtc record);

    int upsertSelective(@Param("record") BlockRecordBtc record, @Param("selective") BlockRecordBtc.Column ... selective);
}