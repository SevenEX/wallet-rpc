package com.ztuo.bc.wallet.service;

import com.mongodb.client.result.UpdateResult;
import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.entity.Deposit;
import com.ztuo.bc.wallet.entity.GrinDeposit;
import com.ztuo.bc.wallet.event.Erc20TokenWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GrinChargeService {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private Coin coin;

    public void save(GrinDeposit tx){
        mongoTemplate.insert(tx,getCollectionName());
    }

    public String getCollectionName(){
        return coin.getUnit() + "_charge";
    }

    public boolean exists(GrinDeposit deposit){
        Criteria criteria =  Criteria.where("uid").is(deposit.getUid())
                .andOperator(Criteria.where("txid").is(deposit.getTxid()));
        Query query = new Query(criteria);
        return mongoTemplate.exists(query,getCollectionName());
    }

    /**
     * 根据推送状态
     * @param status
     * @return
     */
    public List<GrinDeposit> findByStatus(int status){
        Query query = new Query();
        Criteria criteria = Criteria.where("status").is(status);
        query.addCriteria(criteria);
        return mongoTemplate.find(query,GrinDeposit.class,getCollectionName());
    }

    /**
     * 更新状态
     *
     * @param txId
     * @param balance
     */
    public void updateStatus(String txId,int Status){
        Query query = new Query();
        Criteria criteria = Criteria.where("txid").is(txId);
        query.addCriteria(criteria);
        UpdateResult result = mongoTemplate.updateFirst(query, Update.update("status", Status), getCollectionName());
    }

    public GrinDeposit findLatest(){
        Sort.Order order = new Sort.Order(Sort.Direction.DESC,"blockHeight");
        Sort sort = Sort.by(order);
        PageRequest page = PageRequest.of(0, 1, sort);
        Query query = new Query();
        query.with(page);
        GrinDeposit result = mongoTemplate.findOne(query,GrinDeposit.class,getCollectionName());
        return result;
    }
}
