# 实用工具使用指南

本文档介绍如何使用和配置三个实用工具:邮件发送、时间工具、数据库操作。

## 📚 目录

1. [时间工具 (DateTimeTool)](#1-时间工具)
2. [数据库工具 (DatabaseTool)](#2-数据库工具)
3. [邮件工具 (EmailTool)](#3-邮件工具)
4. [使用示例](#4-使用示例)
5. [安全建议](#5-安全建议)

---

## 1. 时间工具

### 功能列表

| 工具名称 | 功能描述 | 使用场景 |
|---------|---------|---------|
| `getCurrentTime` | 获取当前时间 | "现在几点?" "今天星期几?" |
| `calculateDate` | 日期计算 | "3天后是几号?" "2周前是什么日期?" |
| `calculateDateDifference` | 计算日期差值 | "距离春节还有多少天?" |
| `getTimezoneTime` | 获取时区时间 | "现在纽约几点了?" |

### 配置要求

✅ **无需配置**,开箱即用!

### 使用示例

```java
// 在 ChatClient 中使用
ChatClient chatClient = chatClientBuilder
    .defaultTools(ToolCallbacks.from(dateTimeTool))
    .build();

// 用户提问
String response = chatClient.prompt()
    .user("3天后是几号?是星期几?")
    .call()
    .content();
```

### AI调用示例

| 用户提问 | AI会调用 | 参数 |
|---------|---------|------|
| "现在几点了?" | `getCurrentTime()` | 无 |
| "7天后是几号?" | `calculateDate()` | `startDate=今天, amount=7, unit=DAYS` |
| "距离2025-12-31还有多少天?" | `calculateDateDifference()` | `startDate=今天, endDate=2025-12-31` |
| "现在东京几点?" | `getTimezoneTime()` | `timezone=Asia/Tokyo` |

---

## 2. 数据库工具

### 功能列表

| 工具名称 | 功能描述 | 安全级别 |
|---------|---------|---------|
| `listTables` | 列出所有表 | 🟢 安全 |
| `describeTable` | 查看表结构 | 🟢 安全 |
| `executeQuery` | 执行SELECT查询 | 🟡 需谨慎 |
| `executeUpdate` | 执行INSERT/UPDATE/DELETE | 🔴 危险 |

### 配置要求

✅ **使用项目现有的数据库配置**

已自动注入 `JdbcTemplate`,使用 `application.yml` 中的数据库配置:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_agent
    username: root
    password: 1234
```

### 安全限制

```java
// ✅ 允许的操作
executeQuery("SELECT * FROM users")          // 查询数据
describeTable("users")                       // 查看表结构
listTables()                                 // 列出所有表

// ❌ 禁止的操作
executeQuery("DROP TABLE users")             // ❌ 不允许DROP
executeQuery("DELETE FROM users")            // ❌ 查询不能DELETE
executeUpdate("TRUNCATE TABLE users")        // ❌ 不允许TRUNCATE
```

### 使用示例

```java
// 在 ChatClient 中使用
ChatClient chatClient = chatClientBuilder
    .defaultTools(ToolCallbacks.from(databaseTool))
    .build();

// 用户提问
String response = chatClient.prompt()
    .user("查询users表中所有管理员用户")
    .call()
    .content();
```

### AI调用示例

| 用户提问 | AI会调用 | 生成的SQL |
|---------|---------|----------|
| "数据库有哪些表?" | `listTables()` | `SHOW TABLES` |
| "users表有哪些字段?" | `describeTable("users")` | `DESCRIBE users` |
| "查询所有管理员" | `executeQuery()` | `SELECT * FROM users WHERE role='admin'` |
| "统计用户总数" | `executeQuery()` | `SELECT COUNT(*) FROM users` |

---

## 3. 邮件工具

### 功能列表

- ✅ 发送纯文本邮件
- ✅ 发送HTML格式邮件
- ✅ 支持多个收件人(逗号分隔)
- ✅ 支持抄送(CC)和密送(BCC)

### 配置要求

⚠️ **需要在 `application.yml` 中配置邮箱信息**

#### 配置步骤

**步骤1**: 在 `application.yml` 中添加配置

```yaml
email:
  enabled: true                # 是否启用邮件功能
  smtp-host: smtp.qq.com       # SMTP服务器地址
  smtp-port: 587               # SMTP端口
  username: your@qq.com        # 邮箱账号
  password: xyzabc123456       # 邮箱密码/授权码
  from: your@qq.com            # 发件人地址
  from-name: AI助手             # 发件人名称
```

**步骤2**: 获取邮箱授权码

##### 🔹 QQ邮箱授权码获取

1. 登录 [QQ邮箱网页版](https://mail.qq.com/)
2. 点击 **设置** → **账户**
3. 找到 **POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务**
4. 开启 **IMAP/SMTP服务**
5. 点击 **生成授权码**
6. 使用手机QQ扫码验证
7. 复制生成的授权码(16位字母)

##### 🔹 163邮箱授权码获取

1. 登录 [163邮箱](https://mail.163.com/)
2. 点击 **设置** → **POP3/SMTP/IMAP**
3. 开启 **SMTP服务**
4. 设置客户端授权码

##### 🔹 Gmail配置

```yaml
email:
  smtp-host: smtp.gmail.com
  smtp-port: 587
  username: your@gmail.com
  password: your_app_password  # 需要在Google账户中生成应用专用密码
```

#### 常用邮箱SMTP配置表

| 邮箱 | SMTP服务器 | 端口 | 需要授权码 |
|-----|-----------|-----|-----------|
| QQ邮箱 | smtp.qq.com | 587 | ✅ 是 |
| 163邮箱 | smtp.163.com | 25 | ✅ 是 |
| Gmail | smtp.gmail.com | 587 | ✅ 是(应用专用密码) |
| Outlook | smtp-mail.outlook.com | 587 | ❌ 否 |

### 使用示例

```java
// 在 ChatClient 中使用
ChatClient chatClient = chatClientBuilder
    .defaultTools(ToolCallbacks.from(emailTool))
    .build();

// 用户提问
String response = chatClient.prompt()
    .user("发邮件给 zhang@test.com,主题是'会议通知',内容是'明天下午3点开会'")
    .call()
    .content();
```

### AI调用示例

| 用户提问 | AI提取的参数 |
|---------|-------------|
| "发邮件给 zhang@test.com,主题是会议通知,内容是明天开会" | `to="zhang@test.com"`<br>`subject="会议通知"`<br>`content="明天开会"` |
| "给 admin@test.com 发一封HTML格式的报告" | `to="admin@test.com"`<br>`isHtml=true`<br>`subject="报告"` |
| "发邮件给 a@test.com 和 b@test.com" | `to="a@test.com,b@test.com"` |

---

## 4. 使用示例

### 示例1: 时间工具独立使用

```java
@Autowired
private DateTimeTool dateTimeTool;

@Autowired
private ChatClient.Builder chatClientBuilder;

public void demo() {
    ChatClient chatClient = chatClientBuilder
        .defaultTools(ToolCallbacks.from(dateTimeTool))
        .build();
    
    // 用户提问
    String response = chatClient.prompt()
        .user("3天后是几号?")
        .call()
        .content();
    
    System.out.println(response);
    // 输出: 3天后是2025年11月26日,星期二
}
```

### 示例2: 数据库工具独立使用

```java
@Autowired
private DatabaseTool databaseTool;

public void demo() {
    ChatClient chatClient = chatClientBuilder
        .defaultTools(ToolCallbacks.from(databaseTool))
        .build();
    
    // 用户提问
    String response = chatClient.prompt()
        .user("统计users表中有多少个用户?")
        .call()
        .content();
    
    System.out.println(response);
    // AI会自动执行: SELECT COUNT(*) FROM users
}
```

### 示例3: 组合使用多个工具

```java
public void demo() {
    ChatClient chatClient = chatClientBuilder
        .defaultTools(ToolCallbacks.from(
            dateTimeTool,
            databaseTool,
            emailTool
        ))
        .build();
    
    // 复杂任务: 查询数据 + 生成报告 + 发送邮件
    String response = chatClient.prompt()
        .user("""
            请帮我完成:
            1. 查询users表的用户总数
            2. 生成一份报告,包含当前时间和统计结果
            3. 将报告发送到 admin@test.com
            """)
        .call()
        .content();
    
    // AI会自动:
    // 1. 调用 databaseTool.executeQuery() 查询用户数
    // 2. 调用 dateTimeTool.getCurrentTime() 获取当前时间
    // 3. 调用 emailTool.sendEmail() 发送报告
}
```

### 示例4: 在 LoveApp 中使用

```java
@Component
public class LoveApp {
    
    @Autowired
    private ToolCallback[] allTools;  // 包含所有工具
    
    public void chat(String message) {
        ChatClient chatClient = chatClientBuilder
            .defaultTools(allTools)  // 使用所有工具
            .build();
        
        String response = chatClient.prompt()
            .user(message)
            .call()
            .content();
        
        System.out.println(response);
    }
}
```

### 运行测试

```bash
# 运行测试类查看效果
mvn test -Dtest=UtilityToolsTest#testDateTimeTool
mvn test -Dtest=UtilityToolsTest#testDatabaseTool
mvn test -Dtest=UtilityToolsTest#testEmailTool
```

---

## 5. 安全建议

### 🔒 数据库工具安全建议

1. **限制数据库权限**
   ```sql
   -- 创建只读用户
   CREATE USER 'ai_readonly'@'localhost' IDENTIFIED BY 'password';
   GRANT SELECT ON ai_agent.* TO 'ai_readonly'@'localhost';
   ```

2. **添加SQL白名单**
   ```java
   private boolean isSqlAllowed(String sql) {
       // 只允许查询特定表
       Set<String> allowedTables = Set.of("users", "orders", "products");
       // ... 验证逻辑
   }
   ```

3. **记录所有SQL执行**
   ```java
   @Aspect
   public class SqlAuditAspect {
       @Before("execution(* DatabaseTool.execute*(..))")
       public void logSql(JoinPoint jp) {
           // 记录SQL到审计日志
       }
   }
   ```

### 🔒 邮件工具安全建议

1. **限制收件人域名**
   ```java
   private boolean isEmailAllowed(String email) {
       // 只允许发送到公司内部邮箱
       return email.endsWith("@yourcompany.com");
   }
   ```

2. **添加发送频率限制**
   ```java
   @Component
   public class EmailRateLimiter {
       private final Map<String, Integer> sendCount = new HashMap<>();
       
       public boolean canSend(String from) {
           // 每小时最多发送10封
           return sendCount.getOrDefault(from, 0) < 10;
       }
   }
   ```

3. **使用授权码而非密码**
   - ✅ 使用邮箱授权码
   - ❌ 不要使用真实密码

---

## 📝 总结

| 工具 | 配置难度 | 安全风险 | 推荐场景 |
|-----|---------|---------|---------|
| DateTimeTool | 🟢 无需配置 | 🟢 无风险 | 所有场景 |
| DatabaseTool | 🟢 自动配置 | 🟡 中等风险 | 内部系统,限制权限 |
| EmailTool | 🟡 需要配置 | 🟡 中等风险 | 通知提醒,添加频率限制 |

**最佳实践**:
1. ✅ 时间工具: 开箱即用,无需担心
2. ⚠️ 数据库工具: 使用只读账户,限制查询表
3. ⚠️ 邮件工具: 使用授权码,限制收件人域名

---

## 🎯 快速开始

1. **时间工具** - 立即可用
   ```bash
   mvn test -Dtest=UtilityToolsTest#testDateTimeTool
   ```

2. **数据库工具** - 立即可用
   ```bash
   mvn test -Dtest=UtilityToolsTest#testDatabaseTool
   ```

3. **邮件工具** - 需要先配置
   ```yaml
   # 1. 在 application.yml 中添加配置
   email:
     enabled: true
     smtp-host: smtp.qq.com
     smtp-port: 587
     username: your@qq.com
     password: your_auth_code
     from: your@qq.com
     from-name: AI助手
   
   # 2. 运行测试
   mvn test -Dtest=UtilityToolsTest#testEmailTool
   ```

完成配置后,AI就可以自动发送邮件、查询数据库、计算时间啦! 🎉
