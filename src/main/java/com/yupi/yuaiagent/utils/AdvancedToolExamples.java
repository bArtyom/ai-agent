package com.yupi.yuaiagent.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 展示 Spring AI 中所有工具定义方式
 * 
 * 核心概念:
 * 1. 工具本质上是一个函数: Input -> Output
 * 2. AI 通过工具的描述(@Description/@Tool)理解其功能
 * 3. AI 通过参数描述(@JsonPropertyDescription/@ToolParam)理解如何传参
 * 4. Spring AI 框架负责实际执行并返回结果
 */
@Slf4j
@Configuration
public class AdvancedToolExamples {

    // ==================== 方式1: Function Bean (最推荐) ====================
    
    /**
     * 简单的单参数函数
     * 优点: 类型安全、自动序列化、代码简洁
     */
    @Bean
    @Description("获取指定城市的当前时间")
    public Function<String, String> getCurrentTime() {
        return city -> {
            log.info("🕐 [工具调用] 获取城市时间: {}", city);
            LocalDateTime now = LocalDateTime.now();
            String time = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return String.format("%s 当前时间: %s", city, time);
        };
    }

    /**
     * 使用 Record 作为复杂参数 (推荐)
     * AI 会自动理解 Record 的字段并生成正确的 JSON
     */
    public record WeatherQuery(
        @JsonPropertyDescription("城市名称,例如: 北京、上海") String city,
        @JsonPropertyDescription("温度单位: C(摄氏度) 或 F(华氏度)") String unit
    ) {}

    @Bean
    @Description("查询指定城市的天气信息,返回温度、天气状况等")
    public Function<WeatherQuery, String> queryWeather() {
        return query -> {
            log.info("🌤️ [工具调用] 查询天气 - 城市: {}, 单位: {}", query.city(), query.unit());
            
            // 模拟天气查询逻辑
            Map<String, String> weatherData = Map.of(
                "北京", "晴天,25°" + query.unit(),
                "上海", "多云,22°" + query.unit(),
                "深圳", "雨天,28°" + query.unit()
            );
            
            return weatherData.getOrDefault(query.city(), "暂无该城市天气数据");
        };
    }

    /**
     * 返回复杂结构化数据
     * Spring AI 会自动将返回值序列化为 JSON 供 AI 理解
     */
    public record StockInfo(
        String symbol,
        double price,
        double change,
        String trend
    ) {}

    public record StockQuery(
        @JsonPropertyDescription("股票代码,例如: AAPL, GOOGL") String symbol
    ) {}

    @Bean
    @Description("查询股票实时价格和涨跌情况")
    public Function<StockQuery, StockInfo> queryStock() {
        return query -> {
            log.info("📈 [工具调用] 查询股票: {}", query.symbol());
            
            // 模拟股票数据
            return new StockInfo(
                query.symbol(),
                150.25,
                +2.5,
                "上涨"
            );
        };
    }

    // ==================== 方式2: @Tool 注解方式 ====================
    
    /**
     * 使用 @Tool 注解的方法
     * 需要通过 ToolCallbacks.from() 手动注册
     * 优点: 可以在普通类中定义,适合有状态的工具
     */
    public static class DatabaseTool {
        
        @Tool(description = "在数据库中搜索用户信息")
        public String searchUser(
            @ToolParam(description = "用户ID或用户名") String query
        ) {
            log.info("🔍 [工具调用] 搜索用户: {}", query);
            // 模拟数据库查询
            return String.format("用户: %s, 邮箱: %s@example.com, 状态: 活跃", query, query);
        }

        @Tool(description = "更新用户的个人资料")
        public String updateUserProfile(
            @ToolParam(description = "用户ID") String userId,
            @ToolParam(description = "要更新的字段,JSON格式") String fields
        ) {
            log.info("✏️ [工具调用] 更新用户资料 - ID: {}, 字段: {}", userId, fields);
            return "用户资料更新成功";
        }
    }

    // ==================== 方式3: 复杂业务逻辑工具 ====================
    
    /**
     * 包含复杂业务逻辑的工具
     * 可以注入其他 Spring Bean
     */
    public record EmailRequest(
        @JsonPropertyDescription("收件人邮箱地址") String to,
        @JsonPropertyDescription("邮件主题") String subject,
        @JsonPropertyDescription("邮件正文内容") String body,
        @JsonPropertyDescription("是否需要确认,默认false") Boolean requireConfirmation
    ) {}

    @Bean
    @Description("发送电子邮件给指定收件人")
    public Function<EmailRequest, String> sendEmail() {
        return request -> {
            log.info("📧 [工具调用] 发送邮件 - 收件人: {}, 主题: {}", request.to(), request.subject());
            
            // 这里可以注入真实的邮件服务
            // @Resource private EmailService emailService;
            
            if (Boolean.TRUE.equals(request.requireConfirmation())) {
                return "邮件已发送至 " + request.to() + ",请用户确认";
            }
            
            return "邮件发送成功";
        };
    }

    // ==================== 方式4: 异步工具(长时间运行) ====================
    
    public record ReportRequest(
        @JsonPropertyDescription("报告类型: daily, weekly, monthly") String type,
        @JsonPropertyDescription("开始日期 yyyy-MM-dd") String startDate,
        @JsonPropertyDescription("结束日期 yyyy-MM-dd") String endDate
    ) {}

    @Bean
    @Description("生成销售数据分析报告,可能需要几秒钟")
    public Function<ReportRequest, String> generateReport() {
        return request -> {
            log.info("📊 [工具调用] 生成报告 - 类型: {}, 日期范围: {} 至 {}", 
                request.type(), request.startDate(), request.endDate());
            
            try {
                // 模拟耗时操作
                Thread.sleep(2000);
                return String.format("报告生成完成! 类型: %s, 包含 %d 条记录", 
                    request.type(), 1250);
            } catch (InterruptedException e) {
                return "报告生成失败: " + e.getMessage();
            }
        };
    }

    // ==================== 方式5: 多步骤工具链 ====================
    
    /**
     * 需要多次调用才能完成的复杂任务
     * AI 会自动识别并进行多轮调用
     */
    public record DataProcessRequest(
        @JsonPropertyDescription("操作类型: fetch, transform, save") String action,
        @JsonPropertyDescription("数据源或目标") String target,
        @JsonPropertyDescription("可选的额外参数") Map<String, String> options
    ) {}

    @Bean
    @Description("执行数据处理操作,支持获取、转换、保存等多个步骤")
    public Function<DataProcessRequest, String> processData() {
        return request -> {
            log.info("⚙️ [工具调用] 数据处理 - 操作: {}, 目标: {}", 
                request.action(), request.target());
            
            switch (request.action()) {
                case "fetch":
                    return "已获取数据: " + request.target() + " (100条记录)";
                case "transform":
                    return "数据转换完成,应用了过滤条件: " + request.options();
                case "save":
                    return "数据已保存至: " + request.target();
                default:
                    return "未知操作: " + request.action();
            }
        };
    }

    // ==================== 方式6: 返回结构化错误信息 ====================
    
    public record ToolResult(
        boolean success,
        String message,
        Object data,
        String error
    ) {}

    public record ValidationRequest(
        @JsonPropertyDescription("要验证的数据") String data,
        @JsonPropertyDescription("验证类型: email, phone, url") String type
    ) {}

    @Bean
    @Description("验证输入数据的格式是否正确")
    public Function<ValidationRequest, ToolResult> validateData() {
        return request -> {
            log.info("✅ [工具调用] 数据验证 - 类型: {}, 数据: {}", request.type(), request.data());
            
            boolean isValid = switch (request.type()) {
                case "email" -> request.data().contains("@");
                case "phone" -> request.data().matches("\\d{11}");
                case "url" -> request.data().startsWith("http");
                default -> false;
            };
            
            if (isValid) {
                return new ToolResult(true, "验证通过", request.data(), null);
            } else {
                return new ToolResult(false, "验证失败", null, 
                    "格式不正确: " + request.type());
            }
        };
    }
}
