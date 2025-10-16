package com.scmp.simple.config;

import java.util.prefs.Preferences;

/**
 * 配置类 - 管理用户名、密码和token
 */
public class AppConfig {
    
    private static final Preferences prefs = Preferences.userNodeForPackage(AppConfig.class);
    
    // 写死的用户名和密码
    private static final String FIXED_USERNAME = "admin";
    private static final String FIXED_PASSWORD = "123456";
    
    // 配置键名
    private static final String KEY_TOKEN = "auth_token";
    
    /**
     * 获取用户名（写死）
     */
    public static String getUsername() {
        return FIXED_USERNAME;
    }
    
    /**
     * 获取密码（写死）
     */
    public static String getPassword() {
        return FIXED_PASSWORD;
    }
    
    /**
     * 获取token
     */
    public static String getToken() {
        return prefs.get(KEY_TOKEN, "");
    }
    
    /**
     * 设置token
     */
    public static void setToken(String token) {
        prefs.put(KEY_TOKEN, token);
    }

}