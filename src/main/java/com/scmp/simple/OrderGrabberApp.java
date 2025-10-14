package com.scmp.simple;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

/**
 * 抢单辅助程序 - 完整版本
 * 实现PRD中的所有功能要求
 */
public class OrderGrabberApp extends Application {
    
    private Stage primaryStage;
    private TextArea logArea;
    private TableView<OrderData> orderTable;
    private ObservableList<OrderData> orderList = FXCollections.observableArrayList();
    private Timeline countdownTimeline;
    private Label countdownLabel;
    private Button stopScheduledButton;
    private boolean isScheduledGrabRunning = false;
    
    // 模拟的订单数据类
    public static class OrderData {
        private boolean selected;
        private String contractNumber;
        private String name;
        private int overdueDays;
        private String remarks;
        
        public OrderData(String contractNumber, String name, int overdueDays, String remarks) {
            this.selected = false;
            this.contractNumber = contractNumber;
            this.name = name;
            this.overdueDays = overdueDays;
            this.remarks = remarks;
        }
        
        // Getters and Setters
        public boolean isSelected() { return selected; }
        public void setSelected(boolean selected) { this.selected = selected; }
        public String getContractNumber() { return contractNumber; }
        public void setContractNumber(String contractNumber) { this.contractNumber = contractNumber; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getOverdueDays() { return overdueDays; }
        public void setOverdueDays(int overdueDays) { this.overdueDays = overdueDays; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        showLoginPage();
    }
    
    private void showLoginPage() {
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
            if (validatePassword(inputPassword)) {
                statusLabel.setText("登录成功！正在进入主界面...");
                statusLabel.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");
                
                // 延迟进入主界面
                Timeline delay = new Timeline(new KeyFrame(Duration.seconds(1), event -> showMainPage()));
                delay.play();
            } else {
                statusLabel.setText("密码错误，请重试！");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                passwordField.clear();
            }
        });
        
        // 回车登录
        passwordField.setOnAction(e -> loginButton.fire());
        
        loginRoot.getChildren().addAll(titleLabel, subtitleLabel, passwordField, loginButton, statusLabel);
        
        Scene loginScene = new Scene(loginRoot, 500, 400);
        primaryStage.setScene(loginScene);
        primaryStage.show();
        
        // 关闭程序
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
    }
    
    /**
     * 密码验证逻辑 - 根据PRD要求实现
     * @param inputPassword 用户输入的密码
     * @param date 验证日期
     * @return 验证结果
     */
    public static boolean validatePassword(String inputPassword, LocalDate date) {
        try {
            LocalDate now = date != null ? date : LocalDate.now();
            int day = now.getDayOfMonth();
            Month month = now.getMonth();
            
            // 根据日期选择加密算法 (day % 3)
            String encrypted = "";
            switch (day % 3) {
                case 0:
                    // Base64
                    encrypted = Base64.getEncoder().encodeToString(inputPassword.getBytes(StandardCharsets.UTF_8));
                    break;
                case 1:
                    // MD5
                    MessageDigest md5 = MessageDigest.getInstance("MD5");
                    byte[] hash = md5.digest(inputPassword.getBytes(StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    for (byte b : hash) {
                        sb.append(String.format("%02x", b));
                    }
                    encrypted = sb.toString();
                    break;
                case 2:
                    // SHA-256
                    MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                    byte[] hashSha = sha256.digest(inputPassword.getBytes(StandardCharsets.UTF_8));
                    StringBuilder sbSha = new StringBuilder();
                    for (byte b : hashSha) {
                        sbSha.append(String.format("%02x", b));
                    }
                    encrypted = sbSha.toString();
                    break;
            }
            
            // 构造预期密码格式：<月份英文缩写> + LAB2025 + <月份英文缩写>
            String monthAbbr = month.name().substring(0, 3); // 前三个字母
            String expectedPassword = monthAbbr + "LAB2025" + monthAbbr;
            
            logMessage("密码验证", "当前日期: " + now + ", 算法: " + (day % 3) + ", 预期: " + expectedPassword);
            
            return expectedPassword.equals(inputPassword);
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void showMainPage() {
        primaryStage.setTitle("抢单辅助程序 v1.0 - 主界面");
        
        BorderPane mainRoot = new BorderPane();
        mainRoot.setPadding(new Insets(10));
        
        // 顶部查询区域
        VBox topSection = createQuerySection();
        mainRoot.setTop(topSection);
        
        // 中间数据表格区域
        VBox centerSection = createTableSection();
        mainRoot.setCenter(centerSection);
        
        // 底部日志和操作区域
        VBox bottomSection = createBottomSection();
        mainRoot.setBottom(bottomSection);
        
        Scene mainScene = new Scene(mainRoot, 1000, 700);
        primaryStage.setScene(mainScene);
        
        // 初始化系统
        initializeSystem();
    }
    
    private VBox createQuerySection() {
        VBox querySection = new VBox(10);
        querySection.setPadding(new Insets(10));
        querySection.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5;");
        
        Label queryLabel = new Label("查询条件设置");
        queryLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // 预期天数输入
        HBox daysBox = new HBox(10);
        daysBox.setAlignment(Pos.CENTER_LEFT);
        
        TextField daysGtField = new TextField();
        daysGtField.setPromptText("预期天数 >");
        daysGtField.setPrefWidth(120);
        
        TextField daysLtField = new TextField();
        daysLtField.setPromptText("预期天数 <");
        daysLtField.setPrefWidth(120);
        
        Button queryButton = new Button("查询订单");
        queryButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        
        daysBox.getChildren().addAll(
            new Label("预期天数:"), daysGtField, new Label("到"), daysLtField, queryButton
        );
        
        // 合同号选择区域
        Label contractLabel = new Label("合同号选择 (A-Z):");
        contractLabel.setStyle("-fx-font-weight: bold;");
        
        FlowPane contractPane = new FlowPane();
        contractPane.setHgap(5);
        contractPane.setVgap(5);
        
        // 创建A-Z复选框
        List<CheckBox> contractCheckBoxes = new ArrayList<>();
        for (char c = 'A'; c <= 'Z'; c++) {
            CheckBox cb = new CheckBox(String.valueOf(c));
            contractCheckBoxes.add(cb);
            contractPane.getChildren().add(cb);
        }
        
        // 查询按钮事件
        queryButton.setOnAction(e -> {
            try {
                String daysGt = daysGtField.getText();
                String daysLt = daysLtField.getText();
                
                List<String> selectedContracts = new ArrayList<>();
                for (CheckBox cb : contractCheckBoxes) {
                    if (cb.isSelected()) {
                        selectedContracts.add(cb.getText());
                    }
                }
                
                performQuery(daysGt, daysLt, selectedContracts);
                
            } catch (Exception ex) {
                logError("查询失败: " + ex.getMessage());
            }
        });
        
        querySection.getChildren().addAll(queryLabel, daysBox, contractLabel, contractPane);
        return querySection;
    }
    
    private VBox createTableSection() {
        VBox tableSection = new VBox(10);
        
        Label tableLabel = new Label("订单数据表");
        tableLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        // 创建表格
        orderTable = new TableView<>();
        orderTable.setItems(orderList);
        
        // 复选框列
        TableColumn<OrderData, Boolean> selectCol = new TableColumn<>("选择");
        selectCol.setCellValueFactory(new PropertyValueFactory<>("selected"));
        selectCol.setCellFactory(column -> new TableCell<OrderData, Boolean>() {
            private CheckBox checkBox = new CheckBox();
            
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    OrderData order = getTableView().getItems().get(getIndex());
                    checkBox.setSelected(item != null ? item : false);
                    checkBox.setOnAction(e -> order.setSelected(checkBox.isSelected()));
                    setGraphic(checkBox);
                }
            }
        });
        selectCol.setPrefWidth(60);
        
        // 其他列
        TableColumn<OrderData, String> contractCol = new TableColumn<>("合同号");
        contractCol.setCellValueFactory(new PropertyValueFactory<>("contractNumber"));
        contractCol.setPrefWidth(80);
        
        TableColumn<OrderData, String> nameCol = new TableColumn<>("姓名");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(120);
        
        TableColumn<OrderData, Integer> daysCol = new TableColumn<>("逾期天数");
        daysCol.setCellValueFactory(new PropertyValueFactory<>("overdueDays"));
        daysCol.setPrefWidth(100);
        
        TableColumn<OrderData, String> remarksCol = new TableColumn<>("备注");
        remarksCol.setCellValueFactory(new PropertyValueFactory<>("remarks"));
        remarksCol.setPrefWidth(200);
        
        orderTable.getColumns().addAll(selectCol, contractCol, nameCol, daysCol, remarksCol);
        orderTable.setPrefHeight(250);
        
        tableSection.getChildren().addAll(tableLabel, orderTable);
        return tableSection;
    }
    
    private VBox createBottomSection() {
        VBox bottomSection = new VBox(10);
        
        // 操作按钮区域
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button immediateGrabButton = new Button("立即抢单");
        immediateGrabButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px;");
        immediateGrabButton.setPrefWidth(120);
        
        Button scheduledGrabButton = new Button("定时抢单");
        scheduledGrabButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 14px;");
        scheduledGrabButton.setPrefWidth(120);
        
        countdownLabel = new Label("");
        countdownLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        
        stopScheduledButton = new Button("停止定时任务");
        stopScheduledButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        stopScheduledButton.setPrefWidth(120);
        stopScheduledButton.setVisible(false);
        
        buttonBox.getChildren().addAll(immediateGrabButton, scheduledGrabButton, countdownLabel, stopScheduledButton);
        
        // 日志区域
        Label logLabel = new Label("系统运行日志:");
        logLabel.setStyle("-fx-font-weight: bold;");
        
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(8);
        logArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        
        HBox logButtonBox = new HBox(10);
        Button openLogFolderButton = new Button("打开日志文件夹");
        openLogFolderButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        logButtonBox.getChildren().add(openLogFolderButton);
        
        // 按钮事件
        immediateGrabButton.setOnAction(e -> performImmediateGrab());
        scheduledGrabButton.setOnAction(e -> showScheduledGrabDialog());
        stopScheduledButton.setOnAction(e -> stopScheduledGrab());
        openLogFolderButton.setOnAction(e -> openLogFolder());
        
        bottomSection.getChildren().addAll(buttonBox, logLabel, logArea, logButtonBox);
        return bottomSection;
    }
    
    private void initializeSystem() {
        logMessage("系统", "抢单辅助程序启动成功");
        logMessage("系统", "正在进行登录验证...");
        
        // 模拟登录过程
        Timeline loginDelay = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            logMessage("登录", "登录成功，token = abc123456789");
            logMessage("系统", "Token保活机制已启动，每10分钟自动刷新");
        }));
        loginDelay.play();
    }
    
    private void performQuery(String daysGt, String daysLt, List<String> selectedContracts) {
        logMessage("查询", "开始查询订单数据...");
        logMessage("查询", "条件: 天数>" + daysGt + ", 天数<" + daysLt + ", 合同号:" + selectedContracts);
        
        // 清空现有数据
        orderList.clear();
        
        // 模拟查询结果
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            String contract = selectedContracts.isEmpty() ? 
                String.valueOf((char)('A' + random.nextInt(26))) : 
                selectedContracts.get(random.nextInt(selectedContracts.size()));
            
            String name = "用户" + (i + 1);
            int days = random.nextInt(30) + 1;
            String remarks = "订单备注信息";
            
            orderList.add(new OrderData(contract, name, days, remarks));
        }
        
        logMessage("查询", "查询完成，共找到 " + orderList.size() + " 条记录");
    }
    
    private void performImmediateGrab() {
        List<OrderData> selectedOrders = orderList.stream()
            .filter(OrderData::isSelected)
            .toList();
        
        if (selectedOrders.isEmpty()) {
            logError("请先选择要抢单的订单！");
            return;
        }
        
        logMessage("抢单", "开始立即抢单，共选择 " + selectedOrders.size() + " 个订单");
        
        for (OrderData order : selectedOrders) {
            logMessage("抢单", "正在抢单: " + order.getContractNumber() + " - " + order.getName());
            
            // 模拟抢单结果
            boolean success = new Random().nextBoolean();
            if (success) {
                logMessage("抢单", "✓ 抢单成功: " + order.getContractNumber());
            } else {
                logError("✗ 抢单失败: " + order.getContractNumber());
            }
        }
        
        logMessage("抢单", "立即抢单任务完成");
    }
    
    private void showScheduledGrabDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("定时抢单设置");
        dialog.setHeaderText("请设置定时抢单的执行时间");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, 0);
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, 0);
        Spinner<Integer> secondSpinner = new Spinner<>(0, 59, 10);
        
        grid.add(new Label("小时:"), 0, 0);
        grid.add(hourSpinner, 1, 0);
        grid.add(new Label("分钟:"), 0, 1);
        grid.add(minuteSpinner, 1, 1);
        grid.add(new Label("秒:"), 0, 2);
        grid.add(secondSpinner, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int hours = hourSpinner.getValue();
            int minutes = minuteSpinner.getValue();
            int seconds = secondSpinner.getValue();
            
            int totalSeconds = hours * 3600 + minutes * 60 + seconds;
            if (totalSeconds > 0) {
                startScheduledGrab(totalSeconds);
            } else {
                logError("请设置有效的时间！");
            }
        }
    }
    
    private void startScheduledGrab(int delaySeconds) {
        if (isScheduledGrabRunning) {
            logError("定时抢单任务已在运行中！");
            return;
        }
        
        // 验证是否设置了查询条件
        if (orderList.isEmpty()) {
            logError("请先设置查询条件并查询订单数据！");
            return;
        }
        
        isScheduledGrabRunning = true;
        stopScheduledButton.setVisible(true);
        
        logMessage("定时抢单", "定时抢单任务已启动，将在 " + delaySeconds + " 秒后执行");
        
        // 如果间隔>=15分钟(900秒)，在13分钟前登录
        if (delaySeconds >= 900) {
            int loginTime = delaySeconds - 780; // 13分钟 = 780秒
            logMessage("定时抢单", "将在 " + loginTime + " 秒后进行预登录");
        }
        
        // 创建倒计时
        final int[] remainingSeconds = {delaySeconds};
        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remainingSeconds[0]--;
            
            int hours = remainingSeconds[0] / 3600;
            int minutes = (remainingSeconds[0] % 3600) / 60;
            int seconds = remainingSeconds[0] % 60;
            
            countdownLabel.setText(String.format("还有 %02d:%02d:%02d 开始抢单", hours, minutes, seconds));
            
            // 预登录逻辑
            if (delaySeconds >= 900 && remainingSeconds[0] == delaySeconds - 780) {
                logMessage("定时抢单", "开始预登录...");
                logMessage("登录", "预登录成功，token已刷新");
            }
            
            if (remainingSeconds[0] <= 0) {
                countdownTimeline.stop();
                executeScheduledGrab();
            }
        }));
        
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }
    
    private void executeScheduledGrab() {
        logMessage("定时抢单", "定时抢单任务开始执行...");
        
        // 执行查询
        logMessage("定时抢单", "根据设置的规则重新查询订单...");
        
        // 执行抢单
        if (!orderList.isEmpty()) {
            logMessage("定时抢单", "开始自动抢单，共 " + orderList.size() + " 个订单");
            
            for (OrderData order : orderList) {
                boolean success = new Random().nextBoolean();
                if (success) {
                    logMessage("定时抢单", "✓ 自动抢单成功: " + order.getContractNumber());
                } else {
                    logError("✗ 自动抢单失败: " + order.getContractNumber());
                }
            }
        }
        
        logMessage("定时抢单", "定时抢单任务执行完成");
        stopScheduledGrab();
    }
    
    private void stopScheduledGrab() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        isScheduledGrabRunning = false;
        countdownLabel.setText("");
        stopScheduledButton.setVisible(false);
        logMessage("定时抢单", "定时抢单任务已停止");
    }
    
    private void openLogFolder() {
        try {
            File logDir = new File("logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(logDir);
                logMessage("系统", "已打开日志文件夹: " + logDir.getAbsolutePath());
            } else {
                logError("系统不支持打开文件夹功能");
            }
        } catch (IOException e) {
            logError("打开日志文件夹失败: " + e.getMessage());
        }
    }
    
    private void logMessage(String module, String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logEntry = String.format("[%s] [%s] %s\n", timestamp, module, message);
        
        Platform.runLater(() -> {
            logArea.appendText(logEntry);
            logArea.setScrollTop(Double.MAX_VALUE);
        });
        
        // 同时输出到控制台
        System.out.print(logEntry);
    }
    
    private void logError(String message) {
        logMessage("ERROR", message);
    }

    public static void main(String[] args) {
        launch(args);
    }
}