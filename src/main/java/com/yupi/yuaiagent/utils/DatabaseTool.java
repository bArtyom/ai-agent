package com.yupi.yuaiagent.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 数据库操作工具
 * 
 * 功能:
 * 1. 执行SQL查询(SELECT)
 * 2. 执行SQL更新(INSERT/UPDATE/DELETE)
 * 3. 获取表结构信息
 * 4. 统计数据
 * 
 * ⚠️ 安全警告:
 * 这个工具允许AI执行SQL语句,具有较高的风险。
 * 建议:
 * - 仅在受控环境中使用
 * - 限制数据库用户权限(只读或特定表)
 * - 添加SQL白名单验证
 * - 记录所有执行的SQL
 * 
 * 使用场景:
 * - 用户: "查询用户表中所有管理员" -> 执行SELECT
 * - 用户: "统计今天新增了多少用户" -> 执行COUNT查询
 * - 用户: "更新用户ID为1的状态为激活" -> 执行UPDATE
 * 
 * @author yupi
 * @since 2025-11-23
 */
@Slf4j
@Component
public class DatabaseTool {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * SQL查询请求
     */
    public record QueryRequest(
        @JsonProperty(required = true)
        @JsonPropertyDescription("要执行的SQL查询语句,只允许SELECT语句")
        String sql,
        
        @JsonProperty(required = false)
        @JsonPropertyDescription("最大返回行数,默认100,防止数据量过大")
        Integer limit
    ) {}

    /**
     * 执行SQL查询
     */
    @Tool(name = "executeQuery", description = """
        执行SQL查询语句(SELECT)。可以查询数据库中的数据。
        
        ⚠️ 安全限制:
        - 只能执行SELECT语句
        - 默认最多返回100行数据
        - 禁止执行DROP、DELETE等危险操作
        
        参数:
        - sql: 查询SQL语句,例如: "SELECT * FROM users WHERE role='admin'"
        - limit: 最大返回行数,默认100
        
        使用场景:
        - "查询所有管理员用户"
        - "显示最近10条订单"
        - "统计用户总数"
        
        示例SQL:
        - SELECT * FROM users LIMIT 10
        - SELECT COUNT(*) FROM orders WHERE status='completed'
        - SELECT name, email FROM users WHERE role='admin'
        """)
    public String executeQuery(QueryRequest request) {
        String sql = request.sql().trim();
        log.info("🔍 [DatabaseTool] 执行查询 -> SQL: {}", sql);
        
        try {
            // 安全检查: 只允许SELECT
            if (!sql.toUpperCase().startsWith("SELECT")) {
                return "❌ 安全限制: 只允许执行SELECT查询语句";
            }
            
            // 禁止危险关键字
            String upperSql = sql.toUpperCase();
            if (upperSql.contains("DROP") || upperSql.contains("DELETE") || 
                upperSql.contains("TRUNCATE") || upperSql.contains("ALTER")) {
                return "❌ 安全限制: SQL中包含禁止的关键字";
            }
            
            // 添加LIMIT限制
            int limit = request.limit() != null ? request.limit() : 100;
            if (!upperSql.contains("LIMIT")) {
                sql += " LIMIT " + limit;
            }
            
            // 执行查询
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            
            if (results.isEmpty()) {
                return "✅ 查询成功,但没有找到匹配的数据";
            }
            
            // 格式化输出
            StringBuilder output = new StringBuilder();
            output.append(String.format("✅ 查询成功,返回 %d 行数据:\n\n", results.size()));
            
            // 如果只有一行且是聚合查询(如COUNT),直接显示结果
            if (results.size() == 1 && results.get(0).size() == 1) {
                Map.Entry<String, Object> entry = results.get(0).entrySet().iterator().next();
                output.append(String.format("%s: %s", entry.getKey(), entry.getValue()));
            } else {
                // 多行数据,格式化为表格
                for (int i = 0; i < Math.min(results.size(), 10); i++) {
                    Map<String, Object> row = results.get(i);
                    output.append(String.format("第 %d 行:\n", i + 1));
                    row.forEach((key, value) -> 
                        output.append(String.format("  %s: %s\n", key, value))
                    );
                    output.append("\n");
                }
                
                if (results.size() > 10) {
                    output.append(String.format("... 还有 %d 行数据未显示\n", results.size() - 10));
                }
            }
            
            log.info("✅ [DatabaseTool] 查询成功,返回 {} 行", results.size());
            return output.toString();
            
        } catch (Exception e) {
            log.error("❌ [DatabaseTool] 查询失败", e);
            return "❌ 查询失败: " + e.getMessage();
        }
    }

    /**
     * SQL更新请求
     */
    public record UpdateRequest(
        @JsonProperty(required = true)
        @JsonPropertyDescription("要执行的SQL更新语句,可以是INSERT、UPDATE或DELETE")
        String sql,
        
        @JsonProperty(required = false)
        @JsonPropertyDescription("是否需要用户确认,默认true。危险操作必须确认")
        Boolean requireConfirmation
    ) {}

    /**
     * 执行SQL更新操作
     */
    @Tool(name = "executeUpdate", description = """
        执行SQL更新操作(INSERT/UPDATE/DELETE)。可以修改数据库中的数据。
        
        ⚠️ 高风险操作:
        - 会真实修改数据库
        - 建议设置requireConfirmation=true
        - 禁止执行DROP、TRUNCATE等危险操作
        
        参数:
        - sql: 更新SQL语句
        - requireConfirmation: 是否需要确认,默认true
        
        使用场景:
        - "新增一个用户,名字叫张三"
        - "更新订单ID为100的状态为已完成"
        - "删除过期的临时数据"
        
        示例SQL:
        - INSERT INTO users (name, email) VALUES ('张三', 'zhang@test.com')
        - UPDATE orders SET status='completed' WHERE id=100
        - DELETE FROM temp_data WHERE create_time < '2025-01-01'
        """)
    public String executeUpdate(UpdateRequest request) {
        String sql = request.sql().trim();
        log.warn("⚠️ [DatabaseTool] 执行更新 -> SQL: {}", sql);
        
        try {
            // 安全检查: 禁止超级危险操作
            String upperSql = sql.toUpperCase();
            if (upperSql.contains("DROP") || upperSql.contains("TRUNCATE")) {
                return "❌ 安全限制: 禁止执行DROP或TRUNCATE操作";
            }
            
            // 检查是否为更新语句
            if (!upperSql.startsWith("INSERT") && !upperSql.startsWith("UPDATE") && 
                !upperSql.startsWith("DELETE")) {
                return "❌ 只允许执行INSERT、UPDATE或DELETE语句";
            }
            
            // 是否需要确认
            boolean needConfirm = request.requireConfirmation() == null || request.requireConfirmation();
            if (needConfirm) {
                log.warn("⚠️ [DatabaseTool] 此操作需要用户确认");
                return String.format("""
                    ⚠️ 数据库更新操作需要确认:
                    
                    将要执行的SQL:
                    %s
                    
                    这是一个会修改数据库的操作,请确认是否继续执行。
                    (在实际应用中,这里应该等待用户确认)
                    """, sql);
            }
            
            // 执行更新
            int affectedRows = jdbcTemplate.update(sql);
            
            String result = String.format("""
                ✅ 更新操作执行成功!
                
                执行的SQL: %s
                影响行数: %d 行
                """, sql, affectedRows);
            
            log.info("✅ [DatabaseTool] 更新成功,影响 {} 行", affectedRows);
            return result;
            
        } catch (Exception e) {
            log.error("❌ [DatabaseTool] 更新失败", e);
            return "❌ 更新失败: " + e.getMessage();
        }
    }

    /**
     * 获取表结构
     */
    @Tool(name = "describeTable", description = """
        获取数据库表的结构信息,包括字段名称、类型、注释等。
        
        这个工具可以帮助AI了解数据库表的结构,从而生成更准确的SQL语句。
        
        使用场景:
        - AI需要查询某个表,但不知道有哪些字段
        - 用户询问"用户表有哪些字段?"
        
        参数:
        - tableName: 表名,例如: users, orders
        """)
    public String describeTable(
        @JsonProperty(required = true)
        @JsonPropertyDescription("要查询的表名")
        String tableName
    ) {
        log.info("📋 [DatabaseTool] 获取表结构 -> 表名: {}", tableName);
        
        try {
            String sql = String.format("""
                SELECT 
                    COLUMN_NAME as '字段名',
                    COLUMN_TYPE as '类型',
                    IS_NULLABLE as '允许空值',
                    COLUMN_KEY as '键',
                    COLUMN_DEFAULT as '默认值',
                    COLUMN_COMMENT as '注释'
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                AND TABLE_NAME = '%s'
                ORDER BY ORDINAL_POSITION
                """, tableName);
            
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql);
            
            if (columns.isEmpty()) {
                return "❌ 表不存在或没有访问权限: " + tableName;
            }
            
            StringBuilder output = new StringBuilder();
            output.append(String.format("📋 表结构: %s\n\n", tableName));
            
            for (Map<String, Object> column : columns) {
                output.append(String.format("""
                    字段: %s
                      类型: %s
                      允许空值: %s
                      键: %s
                      默认值: %s
                      注释: %s
                    
                    """,
                    column.get("字段名"),
                    column.get("类型"),
                    column.get("允许空值"),
                    column.getOrDefault("键", ""),
                    column.getOrDefault("默认值", "无"),
                    column.getOrDefault("注释", "无")
                ));
            }
            
            log.info("✅ [DatabaseTool] 表结构获取成功,共 {} 个字段", columns.size());
            return output.toString();
            
        } catch (Exception e) {
            log.error("❌ [DatabaseTool] 表结构获取失败", e);
            return "❌ 获取失败: " + e.getMessage();
        }
    }

    /**
     * 列出所有表
     */
    @Tool(name = "listTables", description = """
        列出当前数据库中的所有表。
        
        使用场景:
        - 用户询问"数据库里有哪些表?"
        - AI需要了解数据库结构
        """)
    public String listTables() {
        log.info("📚 [DatabaseTool] 列出所有表");
        
        try {
            String sql = """
                SELECT 
                    TABLE_NAME as '表名',
                    TABLE_COMMENT as '注释',
                    TABLE_ROWS as '行数'
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = DATABASE()
                AND TABLE_TYPE = 'BASE TABLE'
                ORDER BY TABLE_NAME
                """;
            
            List<Map<String, Object>> tables = jdbcTemplate.queryForList(sql);
            
            if (tables.isEmpty()) {
                return "数据库中没有表";
            }
            
            StringBuilder output = new StringBuilder();
            output.append(String.format("📚 数据库表列表 (共 %d 个表):\n\n", tables.size()));
            
            for (Map<String, Object> table : tables) {
                output.append(String.format("- %s", table.get("表名")));
                if (table.get("注释") != null && !table.get("注释").toString().isEmpty()) {
                    output.append(String.format(" (%s)", table.get("注释")));
                }
                if (table.get("行数") != null) {
                    output.append(String.format(" [%s 行]", table.get("行数")));
                }
                output.append("\n");
            }
            
            log.info("✅ [DatabaseTool] 共找到 {} 个表", tables.size());
            return output.toString();
            
        } catch (Exception e) {
            log.error("❌ [DatabaseTool] 列表获取失败", e);
            return "❌ 获取失败: " + e.getMessage();
        }
    }
}
