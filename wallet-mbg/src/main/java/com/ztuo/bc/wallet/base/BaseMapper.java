package com.ztuo.bc.wallet.base;

import com.google.common.collect.Lists;
import com.ztuo.bc.wallet.model.AddressBtc;
import com.ztuo.bc.wallet.model.AddressBtcExample;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public interface BaseMapper<T> {
    Logger logger = LoggerFactory.getLogger(BaseMapper.class);

    int deleteByPrimaryKey(String uniqueId);

    int insert(T record);

    T selectByPrimaryKey(String uniqueId);

    int updateByPrimaryKeyWithBLOBs(T record);

    int updateByPrimaryKey(T record);

    int batchInsert(@Param("list") List<T> list);

    int upsert(T record);

    int upsertWithBLOBs(T record);

    default int saveList(List<T> list){
        return saveList(list, 100);
    }

    default int saveList(List<T> list, int batchSize){
        return Lists.partition(list, batchSize).stream().map(partition -> {
            try {
                return this.batchInsert(list);
            } catch (Exception e) {
                logger.warn("saveList", e);
                int i = 0;
                for (T item : list) {
                    try {
                        this.insert(item);
                        i++;
                    } catch (Exception ignored) {
                    }
                }
                return i;
            }
        }).reduce(0, (a, b) -> a + b);
    }
}
