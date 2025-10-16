package com.scmp.simple.utils;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogUtils {

    
    private static TextArea logArea;
    
    public static void init(TextArea area) {
        logArea = area;
    }
    
    public static void info(String module, String message, Object... args) {
        String formatted = String.format("[%s] %s", module, message);
        log.info(formatted, args);
        appendToUI(formatted);
    }
    
    public static void error(String module, String message, Object... args) {
        String formatted = String.format("[ERROR] [%s] %s", module, message);
        log.error(formatted, args);
        appendToUI(formatted);
    }
    
    public static void warn(String module, String message, Object... args) {
        String formatted = String.format("[WARN] [%s] %s", module, message);
        log.warn(formatted, args);
        appendToUI(formatted);
    }
    
    private static void appendToUI(String message) {
        if (logArea != null) {
            Platform.runLater(() -> {
                logArea.appendText(message + "\n");
                logArea.setScrollTop(Double.MAX_VALUE);
            });
        }
    }
}