package com.yupi.yuaiagent.databaseconfig;

import com.alibaba.druid.pool.DruidDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    /**
     * 主数据源:MySQL(业务数据,spring.datasource)
     * @Primary 标记为默认数据源
     * 使用 Druid 连接池,通过 @ConfigurationProperties 自动绑定所有配置
     */
    @Primary
    @Bean(name = "mysqlDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource mysqlDataSource() {
        return new DruidDataSource();
    }

    /**
     * 向量数据源:PostgreSQL(向量存储,spring.ai.datasource)
     * 使用手动配置避免 @ConfigurationProperties 绑定问题
     * 只有在配置了 spring.ai.datasource.url 时才创建
     */
    @Bean(name = "pgVectorDataSource")
    @ConditionalOnProperty(name = "spring.ai.datasource.url")
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
     * PostgreSQL JdbcTemplate(用于向量存储操作)
     * 这个 JdbcTemplate 专门用于 PGVector 向量存储
     * 只有在 pgVectorDataSource 存在时才创建
     */
    @Bean(name = "pgVectorJdbcTemplate")
    @ConditionalOnProperty(name = "spring.ai.datasource.url")
    public JdbcTemplate pgVectorJdbcTemplate(
            @Qualifier("pgVectorDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}