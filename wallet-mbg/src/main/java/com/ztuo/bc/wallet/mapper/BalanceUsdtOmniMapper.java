package com.ztuo.bc.wallet.mapper;

import com.ztuo.bc.wallet.base.BaseMapper;
import com.ztuo.bc.wallet.model.BalanceUsdtOmni;
import com.ztuo.bc.wallet.model.BalanceUsdtOmniExample;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface BalanceUsdtOmniMapper extends BaseMapper<BalanceUsdtOmni> {
    long countByExample(BalanceUsdtOmniExample example);

    int deleteByExample(BalanceUsdtOmniExample example);

    int deleteByPrimaryKey(@Param("address") String address, @Param("currency") String currency);

    int insert(BalanceUsdtOmni record);

    int insertSelective(@Param("record") BalanceUsdtOmni record, @Param("selective") BalanceUsdtOmni.Column ... selective);

    BalanceUsdtOmni selectOneByExample(BalanceUsdtOmniExample example);

    BalanceUsdtOmni selectOneByExampleSelective(@Param("example") BalanceUsdtOmniExample example, @Param("selective") BalanceUsdtOmni.Column ... selective);

    List<BalanceUsdtOmni> selectByExampleSelective(@Param("example") BalanceUsdtOmniExample example, @Param("selective") BalanceUsdtOmni.Column ... selective);

    List<BalanceUsdtOmni> selectByExample(BalanceUsdtOmniExample example);

    BalanceUsdtOmni selectByPrimaryKeySelective(@Param("address") String address, @Param("currency") String currency, @Param("selective") BalanceUsdtOmni.Column ... selective);

    BalanceUsdtOmni selectByPrimaryKey(@Param("address") String address, @Param("currency") String currency);

    int updateByExampleSelective(@Param("record") BalanceUsdtOmni record, @Param("example") BalanceUsdtOmniExample example, @Param("selective") BalanceUsdtOmni.Column ... selective);

    int updateByExample(@Param("record") BalanceUsdtOmni record, @Param("example") BalanceUsdtOmniExample example);

    int updateByPrimaryKeySelective(@Param("record") BalanceUsdtOmni record, @Param("selective") BalanceUsdtOmni.Column ... selective);

    int updateByPrimaryKey(BalanceUsdtOmni record);

    int batchInsert(@Param("list") List<BalanceUsdtOmni> list);

    int batchInsertSelective(@Param("list") List<BalanceUsdtOmni> list, @Param("selective") BalanceUsdtOmni.Column ... selective);

    int upsert(BalanceUsdtOmni record);

    int upsertSelective(@Param("record") BalanceUsdtOmni record, @Param("selective") BalanceUsdtOmni.Column ... selective);
}