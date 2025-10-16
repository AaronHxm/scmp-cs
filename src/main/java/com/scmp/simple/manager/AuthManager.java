package com.scmp.simple.manager;

import com.scmp.simple.config.AppConfig;
import com.scmp.simple.utils.LogUtils;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 认证管理器 - 负责token保活机制
 */
public class AuthManager {
    
    private static final long KEEP_ALIVE_INTERVAL = 5; // 10分钟
    private static ScheduledExecutorService scheduler;
    private static boolean isRunning = false;
    
    /**
     * 启动token保活机制
     */
    public static void startKeepAlive() {
        if (isRunning) {
            LogUtils.info("认证管理", "Token保活机制已在运行中");
            return;
        }
        
        scheduler = Executors.newSingleThreadScheduledExecutor();
        isRunning = true;
        
        // 每10分钟执行一次token保活
        scheduler.scheduleAtFixedRate(() -> {
            try {
                keepTokenAlive();
            } catch (Exception e) {
                LogUtils.error("认证管理", "Token保活失败: " + e.getMessage());
            }
        }, KEEP_ALIVE_INTERVAL, KEEP_ALIVE_INTERVAL, TimeUnit.MINUTES);
        
        LogUtils.info("认证管理", "Token保活机制已启动，每" + KEEP_ALIVE_INTERVAL + "分钟自动刷新");
        
        // 立即执行一次保活
        keepTokenAlive();
    }
    
    /**
     * 停止token保活机制
     */
    public static void stopKeepAlive() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        isRunning = false;
        LogUtils.info("认证管理", "Token保活机制已停止");
    }
    
    /**
     * token保活逻辑
     */
    private static void keepTokenAlive() {
        String currentToken = AppConfig.getToken();
        
        if (currentToken.isEmpty()) {
            LogUtils.warn("认证管理", "当前token为空，无法进行保活");
            return;
        }
        
        // 模拟token刷新逻辑
        String newToken = refreshToken(currentToken);
        
        if (newToken != null && !newToken.equals(currentToken)) {
            AppConfig.setToken(newToken);
            LogUtils.info("认证管理", "Token刷新成功: " + maskToken(newToken));
        } else {
            LogUtils.info("认证管理", "Token保活检查完成，token仍然有效");
        }
    }
    
    /**
     * 模拟token刷新逻辑
     */
    private static String refreshToken(String oldToken) {
        // 这里应该是实际的token刷新API调用
        // 伪代码：模拟调用认证服务刷新token
        
        try {
            // 模拟网络请求延迟
            Thread.sleep(100);
            
            // 模拟token刷新：在原有token基础上添加时间戳
            String timestamp = String.valueOf(System.currentTimeMillis());
            return oldToken + "_refreshed_" + timestamp.substring(timestamp.length() - 6);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return oldToken; // 刷新失败，返回原token
        } catch (Exception e) {
            LogUtils.error("认证管理", "Token刷新异常: " + e.getMessage());
            return oldToken; // 刷新失败，返回原token
        }
    }
    
    /**
     * 隐藏token的部分内容（安全考虑）
     */
    public static String maskToken(String token) {
        if (token.length() <= 8) {
            return "***" + token.substring(token.length() - 3);
        }
        return token.substring(0, 4) + "***" + token.substring(token.length() - 4);
    }
    
    /**
     * 检查保活机制是否在运行
     */
    public static boolean isKeepAliveRunning() {
        return isRunning;
    }
    
    /**
     * 获取下次保活时间（分钟）
     */
    public static long getNextKeepAliveTime() {
        return KEEP_ALIVE_INTERVAL;
    }
}