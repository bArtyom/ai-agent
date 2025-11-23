package com.yupi.yuaiagent.config;

import com.yupi.yuaiagent.utils.EmailTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 邮件工具配置
 * 
 * 在 application.yml 中配置:
 * email:
 *   enabled: true              # 是否启用邮件功能
 *   smtp-host: smtp.qq.com     # SMTP服务器地址
 *   smtp-port: 587             # SMTP端口
 *   username: your@qq.com      # 邮箱账号
 *   password: your_auth_code   # 邮箱密码/授权码
 *   from: your@qq.com          # 发件人地址
 *   from-name: AI助手          # 发件人名称
 * 
 * 常用邮箱SMTP配置:
 * 
 * 1. QQ邮箱:
 *    smtp-host: smtp.qq.com
 *    smtp-port: 587
 *    密码: 需要使用授权码(在QQ邮箱设置-账户中生成)
 * 
 * 2. 163邮箱:
 *    smtp-host: smtp.163.com
 *    smtp-port: 25
 * 
 * 3. Gmail:
 *    smtp-host: smtp.gmail.com
 *    smtp-port: 587
 * 
 * @author yupi
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "email", name = "enabled", havingValue = "true")
public class EmailConfig {

    @Value("${email.smtp-host:smtp.qq.com}")
    private String smtpHost;

    @Value("${email.smtp-port:587}")
    private int smtpPort;

    @Value("${email.username:}")
    private String username;

    @Value("${email.password:}")
    private String password;

    @Value("${email.from:}")
    private String from;

    @Value("${email.from-name:AI助手}")
    private String fromName;

    @Bean
    public EmailTool configuredEmailTool(EmailTool emailTool) {
        emailTool.setEmailConfig(smtpHost, smtpPort, username, password, from, fromName);
        log.info("✅ [EmailConfig] 邮件工具已配置 -> SMTP: {}:{}, 发件人: {}", 
            smtpHost, smtpPort, from);
        return emailTool;
    }
}
