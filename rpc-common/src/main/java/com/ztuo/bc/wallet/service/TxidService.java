package com.ztuo.bc.wallet.service;

import com.alibaba.fastjson.JSONObject;
import com.ztuo.bc.wallet.entity.Account;
import com.ztuo.bc.wallet.entity.Coin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TxidService {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private Coin coin;
    /**
     * 获取集合名称
     * @return
     */
    public String getCollectionName(){
        return "charge" + "_feeTxid";
    }

    public String getCollectionName(String coinName){
        return coinName + "_feeTxid";
    }

    public void save(String txId){
        JSONObject object = new JSONObject();
        object.put("txid",txId);
        object.put("currency",coin.getName());
        mongoTemplate.insert(object,getCollectionName());
    }

    public void removeByName(String name){
        Query query = new Query();
        Criteria criteria = Criteria.where("account").is(name);
        query.addCriteria(criteria);
        mongoTemplate.remove(query,getCollectionName());
    }

    public boolean isTxIdExist(String txId){
        Query query = new Query();
        Criteria criteria = Criteria.where("txid").is(txId);
        query.addCriteria(criteria);
        return  mongoTemplate.exists(query,getCollectionName());
    }

    /**
     * 获取所有账户
     * @return
     */
    public List<Account> findAll() {
        return mongoTemplate.findAll(Account.class,getCollectionName());
    }

    /**
     * 分页获取账户
     * @param pageNo
     * @param pageSize
     * @return
     */
    public List<Account> find(int pageNo,int pageSize){
        Sort.Order order = new Sort.Order(Sort.Direction.ASC, "_id");
        Sort sort = Sort.by(order);
        PageRequest page = PageRequest.of(pageNo, pageSize, sort);
        Query query = new Query();
        query.with(page);
        return mongoTemplate.find(query,Account.class,getCollectionName());
    }
}
