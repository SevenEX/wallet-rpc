package com.ztuo.bc.wallet.service;

import com.github.pagehelper.PageHelper;
import com.mongodb.client.result.UpdateResult;
import com.ztuo.bc.wallet.entity.Account;
import com.ztuo.bc.wallet.entity.BalanceSum;
import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.event.Erc20TokenWrapper;
import com.ztuo.bc.wallet.mapperextend.AddressEthMapperExtend;
import com.ztuo.bc.wallet.model.AddressEth;
import com.ztuo.bc.wallet.model.AddressEthExample;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class EnventService {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private Coin coin;
    @Autowired
    private AddressEthMapperExtend addressEthMapper;
    /**
     * 获取集合名称
     * @return
     */
    public String getCollectionName(){
        return coin.getName() + "_event";
    }

    public String getCollectionName(String coinName){
        return coinName + "_event";
    }

    public void save(Erc20TokenWrapper.TransferEventResponse transferEventResponse){
        mongoTemplate.insert(transferEventResponse,getCollectionName());
    }

    /**
     * 根据推送状态
     * @param status
     * @return
     */
    public List<Erc20TokenWrapper.TransferEventResponse> findByStatus(int status){
        Query query = new Query();
        Criteria criteria = Criteria.where("status").is(status);
        query.addCriteria(criteria);
        return mongoTemplate.find(query,Erc20TokenWrapper.TransferEventResponse.class,getCollectionName());
    }

    public void removeByName(String name){
        Query query = new Query();
        Criteria criteria = Criteria.where("account").is(name);
        query.addCriteria(criteria);
        mongoTemplate.remove(query,getCollectionName());
    }

    public boolean isTxIdExist(String txId){
        Query query = new Query();
        Criteria criteria = Criteria.where("txId").is(txId);
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


    /**
     * 根据余额查询
     * @param minAmount
     * @return
     */
    public List<Account> findByBalance(BigDecimal minAmount) {
        Query query = new Query();
        Criteria criteria = Criteria.where("balance").gte(minAmount);
        query.addCriteria(criteria);
        Sort sort =  Sort.by(new Sort.Order(Sort.Direction.DESC, "balance"));
        query.with(sort);
        return mongoTemplate.find(query, Account.class, getCollectionName());
    }

    /**
     * 根据余额和手续费查询
     * @param minAmount
     * @param gasLimit
     * @return
     */
    public List<Account> findByBalanceAndGas(BigDecimal minAmount,BigDecimal gasLimit) {
        Query query = new Query();
        Criteria criteria = Criteria.where("balance").gte(minAmount);
        criteria.andOperator(Criteria.where("gas").gte(gasLimit));
        query.addCriteria(criteria);
        Sort sort =  Sort.by(new Sort.Order(Sort.Direction.DESC, "balance"));
        query.with(sort);
        return mongoTemplate.find(query, Account.class, getCollectionName());
    }

    /**
     * 查询钱包总额
     *
     * @return
     */
    public BigDecimal findBalanceSum() {
        Aggregation aggregation = Aggregation.
                newAggregation(Aggregation.group("max").sum("balance").as("totalBalance"))
                .withOptions(Aggregation.newAggregationOptions().cursor(new Document()).build());
        AggregationResults<BalanceSum> results = mongoTemplate.aggregate(aggregation, getCollectionName(), BalanceSum.class);
        List<BalanceSum> list = results.getMappedResults();
        return list.get(0).getTotalBalance().setScale(8, BigDecimal.ROUND_DOWN);
    }


    /**
     * 更新状态
     *
     * @param txId
     * @param balance
     */
    public void updateStatus(String txId) {
        Query query = new Query();
        Criteria criteria = Criteria.where("txId").is(txId);
        query.addCriteria(criteria);
        UpdateResult result = mongoTemplate.updateFirst(query, Update.update("status", 1), getCollectionName());
    }

    public void updateBalanceAndGas(String address, BigDecimal balance,BigDecimal gas) {
        Query query = new Query();
        Criteria criteria = Criteria.where("address").is(address.toLowerCase());
        query.addCriteria(criteria);
        Update update =  new Update();
        update.set("balance", balance.setScale(8, BigDecimal.ROUND_DOWN));
        update.set("gas",gas);
        UpdateResult result = mongoTemplate.updateFirst(query,update, getCollectionName());
    }
}
