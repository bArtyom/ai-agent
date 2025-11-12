package com.yupi.yuaiagent.databaseconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    /**
     * 主数据源：MySQL（业务数据，spring.datasource）
     * @Primary 标记为默认数据源
     */
    @Primary
    @Bean(name = "mysqlDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource mysqlDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * 向量数据源：PostgreSQL（向量存储，spring.ai.datasource）
     */
    @Bean(name = "pgVectorDataSource")
    @ConfigurationProperties(prefix = "spring.ai.datasource")
    public DataSource pgVectorDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * MySQL JdbcTemplate（用于业务操作）
     */
    @Primary
    @Bean(name = "mysqlJdbcTemplate")
    public JdbcTemplate mysqlJdbcTemplate(
            @Qualifier("mysqlDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * PostgreSQL JdbcTemplate（用于向量存储操作）
     * ← 这个是你需要注入的！
     */
    @Bean(name = "pgVectorJdbcTemplate")
    public JdbcTemplate pgVectorJdbcTemplate(
            @Qualifier("pgVectorDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}