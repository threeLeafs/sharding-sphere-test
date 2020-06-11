package com.wpf.test.db.apachesharding;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

/**
 * @author wangpingfei
 * @Description
 * @create 2020-06-11 7:18 下午
 **/
public class ApacheShardingTable implements PreciseShardingAlgorithm<Long> {
    @Override
    public String doSharding(final Collection<String> tableNames, final PreciseShardingValue<Long> shardingValue) {
        for (String table : tableNames) {
            if (table.endsWith(shardingValue.getValue() % 3 + "")) {
                System.out.println("表:" + table);
                return table;
            }
        }
        throw new UnsupportedOperationException();

    }
}
