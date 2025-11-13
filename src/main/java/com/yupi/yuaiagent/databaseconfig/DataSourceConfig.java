package com.yupi.yuaiagent.databaseconfig;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
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
     * 使用手动配置避免 @ConfigurationProperties 绑定问题
     */
    @Bean(name = "pgVectorDataSource")
    public DataSource pgVectorDataSource(
            @Value("${spring.ai.datasource.url}") String url,
            @Value("${spring.ai.datasource.username}") String username,
            @Value("${spring.ai.datasource.password}") String password,
            @Value("${spring.ai.datasource.driver-class-name:org.postgresql.Driver}") String driverClassName
    ) {
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // 连接池配置
        config.setMinimumIdle(2);
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        return new HikariDataSource(config);
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
     * 这个 JdbcTemplate 专门用于 PGVector 向量存储
     */
    @Bean(name = "pgVectorJdbcTemplate")
    public JdbcTemplate pgVectorJdbcTemplate(
            @Qualifier("pgVectorDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}