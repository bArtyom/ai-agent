package com.yupi.yuaiagent.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * 日期时间工具
 * 
 * 功能:
 * 1. 获取当前时间(多种格式)
 * 2. 日期计算(加减天数、月数等)
 * 3. 日期格式转换
 * 4. 计算两个日期之间的差值
 * 5. 判断是否为工作日/周末
 * 6. 获取时区时间
 * 
 * 使用场景:
 * - 用户: "今天是几号?" -> 获取当前日期
 * - 用户: "3天后是星期几?" -> 日期计算
 * - 用户: "距离2025年春节还有多少天?" -> 日期差值计算
 * 
 * @author yupi
 * @since 2025-11-23
 */
@Slf4j
@Component
public class DateTimeTool {

    /**
     * 获取当前时间
     */
    @Tool(name = "getCurrentTime", description = """
        获取当前的日期和时间信息。
        
        返回内容包括:
        - 当前日期时间(标准格式和友好格式)
        - 星期几
        - 是否为工作日
        - Unix时间戳
        - 当前时区
        
        使用场景:
        - "现在几点了?"
        - "今天是几号?"
        - "今天星期几?"
        """)
    public String getCurrentTime() {
        log.info("🕐 [DateTimeTool] 获取当前时间");
        
        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime zonedNow = ZonedDateTime.now();
        
        // 多种格式
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH时mm分ss秒");
        DateTimeFormatter formatter3 = DateTimeFormatter.ofPattern("MM/dd HH:mm");
        
        // 星期
        String dayOfWeek = switch (now.getDayOfWeek()) {
            case MONDAY -> "星期一";
            case TUESDAY -> "星期二";
            case WEDNESDAY -> "星期三";
            case THURSDAY -> "星期四";
            case FRIDAY -> "星期五";
            case SATURDAY -> "星期六";
            case SUNDAY -> "星期日";
        };
        
        // 是否工作日
        boolean isWorkday = now.getDayOfWeek().getValue() <= 5;
        
        String result = String.format("""
            📅 当前时间信息:
            
            标准格式: %s
            中文格式: %s
            简短格式: %s
            星期: %s
            类型: %s
            时区: %s
            Unix时间戳: %d
            """,
            now.format(formatter1),
            now.format(formatter2),
            now.format(formatter3),
            dayOfWeek,
            isWorkday ? "工作日" : "周末",
            zonedNow.getZone().getId(),
            now.atZone(ZoneId.systemDefault()).toEpochSecond()
        );
        
        log.info("✅ [DateTimeTool] 当前时间: {}", now.format(formatter1));
        return result;
    }

    /**
     * 日期计算请求
     */
    public record DateCalculateRequest(
        @JsonProperty(required = false)
        @JsonPropertyDescription("起始日期,格式: yyyy-MM-dd,不填则为今天")
        String startDate,
        
        @JsonProperty(required = true)
        @JsonPropertyDescription("要加减的数量,正数表示向后,负数表示向前")
        int amount,
        
        @JsonProperty(required = true)
        @JsonPropertyDescription("单位: DAYS(天), WEEKS(周), MONTHS(月), YEARS(年)")
        String unit
    ) {}

    /**
     * 日期计算
     */
    @Tool(name = "calculateDate", description = """
        计算日期。可以计算某个日期之前或之后的日期。
        
        参数:
        - startDate: 起始日期(可选,默认今天),格式: yyyy-MM-dd
        - amount: 加减的数量,正数向后,负数向前
        - unit: 单位,可选: DAYS(天), WEEKS(周), MONTHS(月), YEARS(年)
        
        使用场景:
        - "3天后是几号?" -> amount=3, unit=DAYS
        - "2周前是什么日期?" -> amount=-2, unit=WEEKS
        - "明年的今天是几号?" -> amount=1, unit=YEARS
        """)
    public String calculateDate(DateCalculateRequest request) {
        log.info("🧮 [DateTimeTool] 日期计算 -> 起始: {}, 数量: {}, 单位: {}", 
            request.startDate(), request.amount(), request.unit());
        
        try {
            // 解析起始日期
            LocalDate startDate = request.startDate() == null || request.startDate().trim().isEmpty()
                ? LocalDate.now()
                : LocalDate.parse(request.startDate());
            
            // 计算目标日期
            LocalDate targetDate = switch (request.unit().toUpperCase()) {
                case "DAYS" -> startDate.plusDays(request.amount());
                case "WEEKS" -> startDate.plusWeeks(request.amount());
                case "MONTHS" -> startDate.plusMonths(request.amount());
                case "YEARS" -> startDate.plusYears(request.amount());
                default -> throw new IllegalArgumentException("不支持的单位: " + request.unit());
            };
            
            // 格式化输出
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String dayOfWeek = getDayOfWeekInChinese(targetDate.getDayOfWeek());
            
            String result = String.format("""
                📆 日期计算结果:
                
                起始日期: %s
                计算方式: %+d %s
                目标日期: %s (%s)
                相差天数: %d天
                """,
                startDate.format(formatter),
                request.amount(), getUnitInChinese(request.unit()),
                targetDate.format(formatter), dayOfWeek,
                ChronoUnit.DAYS.between(startDate, targetDate)
            );
            
            log.info("✅ [DateTimeTool] 计算结果: {}", targetDate.format(formatter));
            return result;
            
        } catch (Exception e) {
            log.error("❌ [DateTimeTool] 日期计算失败", e);
            return "❌ 日期计算失败: " + e.getMessage();
        }
    }

    /**
     * 日期差值请求
     */
    public record DateDifferenceRequest(
        @JsonProperty(required = true)
        @JsonPropertyDescription("开始日期,格式: yyyy-MM-dd")
        String startDate,
        
        @JsonProperty(required = true)
        @JsonPropertyDescription("结束日期,格式: yyyy-MM-dd")
        String endDate
    ) {}

    /**
     * 计算日期差值
     */
    @Tool(name = "calculateDateDifference", description = """
        计算两个日期之间的差值。返回相差的天数、周数、月数、年数。
        
        参数:
        - startDate: 开始日期,格式: yyyy-MM-dd
        - endDate: 结束日期,格式: yyyy-MM-dd
        
        使用场景:
        - "距离2025年春节还有多少天?"
        - "从今天到年底还有几天?"
        - "两个日期相差多久?"
        """)
    public String calculateDateDifference(DateDifferenceRequest request) {
        log.info("📏 [DateTimeTool] 计算日期差值 -> {} 到 {}", 
            request.startDate(), request.endDate());
        
        try {
            LocalDate start = LocalDate.parse(request.startDate());
            LocalDate end = LocalDate.parse(request.endDate());
            
            long days = ChronoUnit.DAYS.between(start, end);
            long weeks = ChronoUnit.WEEKS.between(start, end);
            long months = ChronoUnit.MONTHS.between(start, end);
            long years = ChronoUnit.YEARS.between(start, end);
            
            String result = String.format("""
                📏 日期差值计算:
                
                开始日期: %s
                结束日期: %s
                
                相差: %d 天
                约: %d 周
                约: %d 个月
                约: %d 年
                """,
                request.startDate(), request.endDate(),
                days, weeks, months, years
            );
            
            log.info("✅ [DateTimeTool] 差值: {} 天", days);
            return result;
            
        } catch (Exception e) {
            log.error("❌ [DateTimeTool] 日期差值计算失败", e);
            return "❌ 计算失败: " + e.getMessage();
        }
    }

    /**
     * 时区转换请求
     */
    public record TimezoneRequest(
        @JsonProperty(required = true)
        @JsonPropertyDescription("目标时区,例如: Asia/Tokyo, America/New_York, Europe/London")
        String timezone
    ) {}

    /**
     * 获取指定时区的时间
     */
    @Tool(name = "getTimezoneTime", description = """
        获取指定时区的当前时间。
        
        常用时区:
        - Asia/Shanghai (中国)
        - Asia/Tokyo (日本)
        - America/New_York (美国纽约)
        - Europe/London (英国伦敦)
        - America/Los_Angeles (美国洛杉矶)
        
        使用场景:
        - "现在纽约几点了?"
        - "东京的当前时间是?"
        """)
    public String getTimezoneTime(TimezoneRequest request) {
        log.info("🌍 [DateTimeTool] 获取时区时间 -> {}", request.timezone());
        
        try {
            ZoneId zoneId = ZoneId.of(request.timezone());
            ZonedDateTime zonedTime = ZonedDateTime.now(zoneId);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            String result = String.format("""
                🌍 时区时间:
                
                时区: %s
                当前时间: %s
                与本地时差: %s
                """,
                request.timezone(),
                zonedTime.format(formatter),
                zonedTime.getOffset().toString()
            );
            
            log.info("✅ [DateTimeTool] 时区时间: {}", zonedTime.format(formatter));
            return result;
            
        } catch (Exception e) {
            log.error("❌ [DateTimeTool] 时区时间获取失败", e);
            return "❌ 获取失败: " + e.getMessage() + "\n请使用正确的时区格式,如: Asia/Shanghai";
        }
    }

    // ==================== 辅助方法 ====================

    private String getDayOfWeekInChinese(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "星期一";
            case TUESDAY -> "星期二";
            case WEDNESDAY -> "星期三";
            case THURSDAY -> "星期四";
            case FRIDAY -> "星期五";
            case SATURDAY -> "星期六";
            case SUNDAY -> "星期日";
        };
    }

    private String getUnitInChinese(String unit) {
        return switch (unit.toUpperCase()) {
            case "DAYS" -> "天";
            case "WEEKS" -> "周";
            case "MONTHS" -> "个月";
            case "YEARS" -> "年";
            default -> unit;
        };
    }
}
