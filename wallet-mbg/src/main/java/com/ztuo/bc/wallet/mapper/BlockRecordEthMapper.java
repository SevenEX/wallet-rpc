package com.ztuo.bc.wallet.mapper;

import com.ztuo.bc.wallet.base.BaseMapper;
import com.ztuo.bc.wallet.model.BlockRecordEth;
import com.ztuo.bc.wallet.model.BlockRecordEthExample;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface BlockRecordEthMapper extends BaseMapper<BlockRecordEth> {
    long countByExample(BlockRecordEthExample example);

    int deleteByExample(BlockRecordEthExample example);

    int deleteByPrimaryKey(String txid);

    int insert(BlockRecordEth record);

    int insertSelective(@Param("record") BlockRecordEth record, @Param("selective") BlockRecordEth.Column ... selective);

    BlockRecordEth selectOneByExample(BlockRecordEthExample example);

    BlockRecordEth selectOneByExampleSelective(@Param("example") BlockRecordEthExample example, @Param("selective") BlockRecordEth.Column ... selective);

    BlockRecordEth selectOneByExampleWithBLOBs(BlockRecordEthExample example);

    List<BlockRecordEth> selectByExampleSelective(@Param("example") BlockRecordEthExample example, @Param("selective") BlockRecordEth.Column ... selective);

    List<BlockRecordEth> selectByExampleWithBLOBs(BlockRecordEthExample example);

    List<BlockRecordEth> selectByExample(BlockRecordEthExample example);

    BlockRecordEth selectByPrimaryKeySelective(@Param("txid") String txid, @Param("selective") BlockRecordEth.Column ... selective);

    BlockRecordEth selectByPrimaryKey(String txid);

    int updateByExampleSelective(@Param("record") BlockRecordEth record, @Param("example") BlockRecordEthExample example, @Param("selective") BlockRecordEth.Column ... selective);

    int updateByExampleWithBLOBs(@Param("record") BlockRecordEth record, @Param("example") BlockRecordEthExample example);

    int updateByExample(@Param("record") BlockRecordEth record, @Param("example") BlockRecordEthExample example);

    int updateByPrimaryKeySelective(@Param("record") BlockRecordEth record, @Param("selective") BlockRecordEth.Column ... selective);

    int updateByPrimaryKeyWithBLOBs(BlockRecordEth record);

    int updateByPrimaryKey(BlockRecordEth record);

    int batchInsert(@Param("list") List<BlockRecordEth> list);

    int batchInsertSelective(@Param("list") List<BlockRecordEth> list, @Param("selective") BlockRecordEth.Column ... selective);

    int upsert(BlockRecordEth record);

    int upsertSelective(@Param("record") BlockRecordEth record, @Param("selective") BlockRecordEth.Column ... selective);

    int upsertWithBLOBs(BlockRecordEth record);
}