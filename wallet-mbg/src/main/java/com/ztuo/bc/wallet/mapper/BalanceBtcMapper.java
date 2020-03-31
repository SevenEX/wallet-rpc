package com.ztuo.bc.wallet.mapper;

import com.ztuo.bc.wallet.base.BaseMapper;
import com.ztuo.bc.wallet.model.BalanceBtc;
import com.ztuo.bc.wallet.model.BalanceBtcExample;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface BalanceBtcMapper extends BaseMapper<BalanceBtc> {
    long countByExample(BalanceBtcExample example);

    int deleteByExample(BalanceBtcExample example);

    int deleteByPrimaryKey(@Param("address") String address, @Param("currency") String currency);

    int insert(BalanceBtc record);

    int insertSelective(@Param("record") BalanceBtc record, @Param("selective") BalanceBtc.Column ... selective);

    BalanceBtc selectOneByExample(BalanceBtcExample example);

    BalanceBtc selectOneByExampleSelective(@Param("example") BalanceBtcExample example, @Param("selective") BalanceBtc.Column ... selective);

    List<BalanceBtc> selectByExampleSelective(@Param("example") BalanceBtcExample example, @Param("selective") BalanceBtc.Column ... selective);

    List<BalanceBtc> selectByExample(BalanceBtcExample example);

    BalanceBtc selectByPrimaryKeySelective(@Param("address") String address, @Param("currency") String currency, @Param("selective") BalanceBtc.Column ... selective);

    BalanceBtc selectByPrimaryKey(@Param("address") String address, @Param("currency") String currency);

    int updateByExampleSelective(@Param("record") BalanceBtc record, @Param("example") BalanceBtcExample example, @Param("selective") BalanceBtc.Column ... selective);

    int updateByExample(@Param("record") BalanceBtc record, @Param("example") BalanceBtcExample example);

    int updateByPrimaryKeySelective(@Param("record") BalanceBtc record, @Param("selective") BalanceBtc.Column ... selective);

    int updateByPrimaryKey(BalanceBtc record);

    int batchInsert(@Param("list") List<BalanceBtc> list);

    int batchInsertSelective(@Param("list") List<BalanceBtc> list, @Param("selective") BalanceBtc.Column ... selective);

    int upsert(BalanceBtc record);

    int upsertSelective(@Param("record") BalanceBtc record, @Param("selective") BalanceBtc.Column ... selective);
}