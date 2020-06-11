package com.wpf.test;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.collect.Lists;
import com.wpf.test.db.apachesharding.ApacheShardingDatabase;
import com.wpf.test.db.apachesharding.ApacheShardingTable;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.KeyGeneratorConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

/**
 * @author wangpingfei
 * @Description
 * @create 2020-06-11 7:05 下午
 **/
@Configuration
public class ApacheShardingConfig {

    @Bean
    public DataSource shardingDataSource() throws SQLException, ReflectiveOperationException {
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        shardingRuleConfiguration.getTableRuleConfigs().add(getUserRuleConfig());
        shardingRuleConfiguration.getTableRuleConfigs().add(getUserInfoRuleConfig());
        shardingRuleConfiguration.getBindingTableGroups().add("user, user_info");
        shardingRuleConfiguration.getBroadcastTables().add("dict");
        shardingRuleConfiguration.setDefaultDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("phone", new ApacheShardingDatabase()));
        shardingRuleConfiguration.setDefaultTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("phone", new ApacheShardingTable()));
        shardingRuleConfiguration.setMasterSlaveRuleConfigs(getMasterSlaveConfig());

        Properties properties = new Properties();
        properties.setProperty(ShardingPropertiesConstant.SQL_SHOW.getKey(), "true");
        return ShardingDataSourceFactory.createDataSource(createDataSourceMap(), shardingRuleConfiguration, properties);
    }


    @Bean
    public SqlSessionFactoryBean sqlSessionFactoryBean() throws SQLException, ReflectiveOperationException {
        DataSource dataSource = shardingDataSource();
        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        return sessionFactoryBean;
    }

    @Bean
    public DataSourceTransactionManager transactionManager() throws SQLException, ReflectiveOperationException {
        DataSourceTransactionManager manager = new DataSourceTransactionManager();
        manager.setDataSource(shardingDataSource());
        return manager;
    }

    /**
     * user_info表的分片规则
     * @author
     */
    private TableRuleConfiguration getUserInfoRuleConfig() {
        TableRuleConfiguration userInfoRuleConfig = new TableRuleConfiguration("user_info", "ds${0..1}.user_info_${0..2}");
        KeyGeneratorConfiguration keyGeneratorConfiguration = new KeyGeneratorConfiguration("SNOWFLAKE", "id");
        userInfoRuleConfig.setKeyGeneratorConfig(keyGeneratorConfiguration);
        return userInfoRuleConfig;
    }

    /**
     * user表的分片规则
     * @author
     */
    private TableRuleConfiguration getUserRuleConfig() {
        TableRuleConfiguration userRuleConfig = new TableRuleConfiguration("user", "ds${0..1}.user_${0..2}");
        KeyGeneratorConfiguration keyGeneratorConfiguration = new KeyGeneratorConfiguration("SNOWFLAKE", "phone");
        userRuleConfig.setKeyGeneratorConfig(keyGeneratorConfiguration);
        return userRuleConfig;
    }

    /**
     * 获取主从数据源配置
     * @author
     */
    private List<MasterSlaveRuleConfiguration> getMasterSlaveConfig() {
        MasterSlaveRuleConfiguration msConfig1 =
                new MasterSlaveRuleConfiguration("ds0", "ds_master1", Arrays.asList("ds_master1_slave1", "ds_master1_slave2"));
        MasterSlaveRuleConfiguration msConfig2 =
                new MasterSlaveRuleConfiguration("ds1", "ds_master2", Arrays.asList("ds_master2_slave1", "ds_master2_slave2"));
        return Lists.newArrayList(msConfig1, msConfig2);
    }
    /**
     * 数据源配置
     * @author
     */
    private Map<String, DataSource> createDataSourceMap() throws ReflectiveOperationException {
        System.out.println("------");
        final Map<String, DataSource> dbMap = new HashMap<>();
        dbMap.put("ds_master1", getDataSource("one_master"));
        dbMap.put("ds_master1_slave1", getDataSource("one_slave1"));
        dbMap.put("ds_master1_slave2", getDataSource("one_slave2"));
        dbMap.put("ds_master2", getDataSource("two_master"));
        dbMap.put("ds_master2_slave1", getDataSource("two_slave1"));
        dbMap.put("ds_master2_slave2", getDataSource("two_slave2"));
        return dbMap;
    }

    /**
     * 获取数据源
     * @author
     */
    private DataSource getDataSource(final String dbName) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDbType("com.alibaba.druid.pool.DruidDataSource");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(String.format("jdbc:mysql://127.0.0.1:3306/%s?useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT", dbName));
        dataSource.setUsername("root");
        dataSource.setPassword("123456");
        return dataSource;
    }

}
