package com.wpf.test.db.apachesharding;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

/**
 * @author wangpingfei
 * @Description
 * @create 2020-06-11 7:16 下午
 **/
public class ApacheShardingDatabase implements PreciseShardingAlgorithm<Long> {
    @Override
    public String doSharding(Collection<String> databaseNames, PreciseShardingValue<Long> shardingValue) {
        for (String database : databaseNames) {
            if (database.endsWith((shardingValue.getValue() % 2) + "")) {
                System.out.println("数据库:" + database);
                return database;
            }
        }
        throw new UnsupportedOperationException();
  }
}
