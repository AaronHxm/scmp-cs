package com.scmp.simple.ui;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import com.scmp.simple.OrderGrabberApp;
import com.scmp.simple.utils.LogUtils;

public class LoginPage {
    private Stage primaryStage;
    private OrderGrabberApp app;

    public LoginPage(Stage primaryStage, OrderGrabberApp app) {
        this.primaryStage = primaryStage;
        this.app = app;
    }

    public void show() {
        primaryStage.setTitle("抢单辅助程序 - 登录");
        
        VBox loginRoot = new VBox(20);
        loginRoot.setPadding(new Insets(50));
        loginRoot.setAlignment(Pos.CENTER);
        loginRoot.setStyle("-fx-background-color: #f5f5f5;");
        
        Label titleLabel = new Label("抢单辅助程序");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label subtitleLabel = new Label("请输入密码登录系统");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("请输入密码");
        passwordField.setPrefWidth(300);
        passwordField.setPrefHeight(40);
        passwordField.setStyle("-fx-font-size: 14px;");
        
        Button loginButton = new Button("登录");
        loginButton.setPrefWidth(120);
        loginButton.setPrefHeight(40);
        loginButton.setStyle("-fx-font-size: 16px; -fx-background-color: #3498db; -fx-text-fill: white;");
        
        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-font-size: 12px;");
        
        loginButton.setOnAction(e -> {
            String inputPassword = passwordField.getText();
            if (app.validatePassword(inputPassword)) {
                statusLabel.setText("登录成功！正在进入主界面...");
                statusLabel.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");
                app.showMainPage();
            } else {
                statusLabel.setText("密码错误，请重试！");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                passwordField.clear();
            }
        });
        
        passwordField.setOnAction(e -> loginButton.fire());
        
        loginRoot.getChildren().addAll(titleLabel, subtitleLabel, passwordField, loginButton, statusLabel);
        
        Scene loginScene = new Scene(loginRoot, 500, 400);
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }
}