package com.syncNest.user_management.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app")
@Setter
@Getter
public class ApplicationProperties {
    private List<String> allowedOrigins;
    private String applicationName;
    private String baseUrl;
    private String loginPageUrl;
    private String loginSuccessUrl;
    private String adminUserEmail;
    private String adminUserPassword;
    private String vapidPublicKey;
    private String vapidPrivateKey;
    private String vapidSubject;
}