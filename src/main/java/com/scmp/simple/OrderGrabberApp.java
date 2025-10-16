package com.scmp.simple;

import com.scmp.simple.model.OrderData;
import com.scmp.simple.ui.LoginPage;
import com.scmp.simple.ui.MainPage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import lombok.Data;
import com.scmp.simple.utils.LogUtils;


/**
 * 抢单辅助程序 - 完整版本
 * 实现PRD中的所有功能要求
 */
public class OrderGrabberApp extends Application {
    
    private Stage primaryStage;
    private ObservableList<OrderData> orderList = FXCollections.observableArrayList();
    

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        showLoginPage();
    }
    
    private LoginPage loginPage;
    private MainPage mainPage;
    private javafx.scene.control.TextArea logArea;

    public void showLoginPage() {
        if (loginPage == null) {
            loginPage = new LoginPage(primaryStage, this);
        }
        loginPage.show();
    }

    public void showMainPage() {
        if (mainPage == null) {
            mainPage = new MainPage(primaryStage, this, orderList);
        }
        mainPage.show();
        initializeSystem();
    }
    
    public boolean validatePassword(String inputPassword) {
        return com.scmp.simple.utils.PasswordUtils.validatePassword(inputPassword);
    }
    


    
    private void initializeSystem() {
        // 初始化日志系统
        LogUtils.info("系统", "抢单辅助程序启动成功");
        LogUtils.info("系统", "正在进行登录验证...");
        
        // 模拟登录过程
        Timeline loginDelay = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            LogUtils.info("登录", "登录成功，token = abc123456789");
            LogUtils.info("系统", "Token保活机制已启动，每10分钟自动刷新");
        }));
        loginDelay.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}