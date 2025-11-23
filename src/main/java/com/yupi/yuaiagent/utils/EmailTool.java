package com.yupi.yuaiagent.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.Tool;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.List;
import java.util.Properties;

/**
 * 邮件发送工具
 * 
 * 功能:
 * 1. 发送纯文本邮件
 * 2. 发送HTML格式邮件
 * 3. 支持多个收件人、抄送、密送
 * 4. 支持附件(可选)
 * 
 * 使用场景:
 * - 用户: "帮我发一封邮件给 zhang@example.com,告诉他会议改到下午3点"
 * - AI会调用这个工具自动发送邮件
 * 
 * 配置要求:
 * 在 application.yml 中添加:
 * email:
 *   smtp-host: smtp.qq.com
 *   smtp-port: 587
 *   username: your@qq.com
 *   password: your_auth_code  # QQ邮箱需要使用授权码
 *   from: your@qq.com
 *   from-name: AI助手
 * 
 * @author yupi
 * @since 2025-11-23
 */
@Slf4j
@Component
public class EmailTool {

    /**
     * 发送邮件的请求参数
     */
    public record EmailRequest(
        @JsonProperty(required = true)
        @JsonPropertyDescription("收件人邮箱地址,多个用逗号分隔,例如: 'user1@example.com,user2@example.com'")
        String to,
        
        @JsonProperty(required = true)
        @JsonPropertyDescription("邮件主题/标题")
        String subject,
        
        @JsonProperty(required = true)
        @JsonPropertyDescription("邮件正文内容")
        String content,
        
        @JsonProperty(required = false)
        @JsonPropertyDescription("是否为HTML格式,默认false表示纯文本")
        Boolean isHtml,
        
        @JsonProperty(required = false)
        @JsonPropertyDescription("抄送(CC)邮箱地址,多个用逗号分隔")
        String cc,
        
        @JsonProperty(required = false)
        @JsonPropertyDescription("密送(BCC)邮箱地址,多个用逗号分隔")
        String bcc
    ) {}

    // 邮件配置 - 从配置文件读取或使用默认值
    private String smtpHost = "smtp.qq.com";
    private int smtpPort = 587;
    private String username = "";  // 需要配置
    private String password = "";  // 需要配置
    private String from = "";      // 需要配置
    private String fromName = "AI助手";

    /**
     * 设置邮件服务器配置
     * 在 @Configuration 类中调用此方法注入配置
     */
    public void setEmailConfig(String smtpHost, int smtpPort, String username, 
                               String password, String from, String fromName) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.username = username;
        this.password = password;
        this.from = from;
        this.fromName = fromName;
    }

    /**
     * 发送邮件
     * 
     * @param request 邮件请求参数
     * @return 发送结果
     */
    @Tool(name = "sendEmail", description = """
        发送电子邮件。支持发送纯文本或HTML格式的邮件给一个或多个收件人。
        
        使用场景:
        - 用户要求发送通知邮件
        - 发送会议邀请或日程提醒
        - 发送报告或数据摘要
        
        参数说明:
        - to: 必填,收件人邮箱,多个用逗号分隔
        - subject: 必填,邮件主题
        - content: 必填,邮件正文
        - isHtml: 可选,是否HTML格式,默认false
        - cc: 可选,抄送地址
        - bcc: 可选,密送地址
        
        示例:
        用户: "发邮件给 zhang@test.com,主题是'会议通知',内容是'明天下午3点开会'"
        AI会提取参数: to="zhang@test.com", subject="会议通知", content="明天下午3点开会"
        """)
    public String sendEmail(EmailRequest request) {
        log.info("📧 [EmailTool] 准备发送邮件 -> 收件人: {}, 主题: {}", request.to(), request.subject());
        
        try {
            // 1. 验证配置
            if (username.isEmpty() || password.isEmpty() || from.isEmpty()) {
                return "❌ 邮件配置未完成,请先在配置文件中设置SMTP服务器信息";
            }

            // 2. 配置SMTP服务器
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpPort);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");  // 启用TLS加密
            props.put("mail.smtp.ssl.protocols", "TLSv1.2"); // 指定TLS版本

            // 3. 创建会话
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            // 4. 创建邮件消息
            Message message = new MimeMessage(session);
            
            // 设置发件人
            message.setFrom(new InternetAddress(from, fromName, "UTF-8"));
            
            // 设置收件人
            message.setRecipients(Message.RecipientType.TO, 
                InternetAddress.parse(request.to()));
            
            // 设置抄送
            if (request.cc() != null && !request.cc().trim().isEmpty()) {
                message.setRecipients(Message.RecipientType.CC, 
                    InternetAddress.parse(request.cc()));
            }
            
            // 设置密送
            if (request.bcc() != null && !request.bcc().trim().isEmpty()) {
                message.setRecipients(Message.RecipientType.BCC, 
                    InternetAddress.parse(request.bcc()));
            }
            
            // 设置主题
            message.setSubject(request.subject());
            
            // 设置内容
            boolean isHtml = request.isHtml() != null && request.isHtml();
            if (isHtml) {
                message.setContent(request.content(), "text/html; charset=UTF-8");
            } else {
                message.setText(request.content());
            }
            
            // 5. 发送邮件
            Transport.send(message);
            
            log.info("✅ [EmailTool] 邮件发送成功 -> 收件人: {}", request.to());
            return String.format("✅ 邮件发送成功!\n收件人: %s\n主题: %s", 
                request.to(), request.subject());
            
        } catch (MessagingException e) {
            log.error("❌ [EmailTool] 邮件发送失败", e);
            return "❌ 邮件发送失败: " + e.getMessage();
        } catch (Exception e) {
            log.error("❌ [EmailTool] 邮件发送异常", e);
            return "❌ 邮件发送异常: " + e.getMessage();
        }
    }
}
