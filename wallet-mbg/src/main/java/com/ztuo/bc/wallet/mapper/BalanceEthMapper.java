package com.ztuo.bc.wallet.mapper;

import com.ztuo.bc.wallet.base.BaseMapper;
import com.ztuo.bc.wallet.model.BalanceEth;
import com.ztuo.bc.wallet.model.BalanceEthExample;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface BalanceEthMapper extends BaseMapper<BalanceEth> {
    long countByExample(BalanceEthExample example);

    int deleteByExample(BalanceEthExample example);

    int deleteByPrimaryKey(@Param("address") String address, @Param("currency") String currency);

    int insert(BalanceEth record);

    int insertSelective(@Param("record") BalanceEth record, @Param("selective") BalanceEth.Column ... selective);

    BalanceEth selectOneByExample(BalanceEthExample example);

    BalanceEth selectOneByExampleSelective(@Param("example") BalanceEthExample example, @Param("selective") BalanceEth.Column ... selective);

    List<BalanceEth> selectByExampleSelective(@Param("example") BalanceEthExample example, @Param("selective") BalanceEth.Column ... selective);

    List<BalanceEth> selectByExample(BalanceEthExample example);

    BalanceEth selectByPrimaryKeySelective(@Param("address") String address, @Param("currency") String currency, @Param("selective") BalanceEth.Column ... selective);

    BalanceEth selectByPrimaryKey(@Param("address") String address, @Param("currency") String currency);

    int updateByExampleSelective(@Param("record") BalanceEth record, @Param("example") BalanceEthExample example, @Param("selective") BalanceEth.Column ... selective);

    int updateByExample(@Param("record") BalanceEth record, @Param("example") BalanceEthExample example);

    int updateByPrimaryKeySelective(@Param("record") BalanceEth record, @Param("selective") BalanceEth.Column ... selective);

    int updateByPrimaryKey(BalanceEth record);

    int batchInsert(@Param("list") List<BalanceEth> list);

    int batchInsertSelective(@Param("list") List<BalanceEth> list, @Param("selective") BalanceEth.Column ... selective);

    int upsert(BalanceEth record);

    int upsertSelective(@Param("record") BalanceEth record, @Param("selective") BalanceEth.Column ... selective);
}