package com.scmp.simple.ui;

import com.scmp.simple.controller.MainController;
import com.scmp.simple.model.OrderData;
import com.scmp.simple.model.OrderQuery;
import com.scmp.simple.model.GrabResult;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.cell.CheckBoxTableCell;
import com.scmp.simple.OrderGrabberApp;
import com.scmp.simple.utils.LogUtils;

import java.util.List;

/**
 * 主界面 - 采用MVC架构，负责UI展示和用户交互
 */
public class MainPage {
    private Stage primaryStage;
    private OrderGrabberApp app;
    private MainController controller;
    private TextArea logArea;
    
    // UI组件
    private CheckBox[] contractCheckboxes;
    private TextField minOverdueField;
    private TextField maxOverdueField;
    private TableView<OrderData> tableView;
    private TableView<GrabResult> resultTableView;
    private Button grabImmediatelyButton;
    private Button scheduleGrabButton;
    private Button stopGrabButton;
    private TextField hourField;
    private TextField minuteField;
    private TextField secondField;
    private Label countdownLabel;
    private javafx.animation.Timeline countdownTimeline;

    public MainPage(Stage primaryStage, OrderGrabberApp app, ObservableList<OrderData> orderList) {
        this.primaryStage = primaryStage;
        this.app = app;
        this.controller = new MainController();
    }

    public void show() {
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
        
        Scene mainScene = new Scene(mainRoot, 1200, 800);
        primaryStage.setScene(mainScene);
        primaryStage.show();
        
        // 初始化日志系统
        LogUtils.init(logArea);
        LogUtils.info("主界面", "主界面初始化完成");
    }

    private VBox createQuerySection() {
        VBox querySection = new VBox(15);
        querySection.setPadding(new Insets(15));
        querySection.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        Label sectionTitle = new Label("订单查询条件");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #495057;");
        
        // 逾期天数范围 - 放在上面
        HBox overdueRow = new HBox(15);
        overdueRow.setAlignment(Pos.CENTER_LEFT);
        
        Label overdueLabel = new Label("逾期天数:");
        overdueLabel.setMinWidth(60);
        minOverdueField = new TextField();
        minOverdueField.setPromptText("最小值");
        minOverdueField.setPrefWidth(100);
        
        Label toLabel = new Label("至");
        toLabel.setMinWidth(20);
        maxOverdueField = new TextField();
        maxOverdueField.setPromptText("最大值");
        maxOverdueField.setPrefWidth(100);
        
        // 查询按钮
        Button queryButton = new Button("查询");
        queryButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold;");
        queryButton.setOnAction(e -> executeQuery());
        
        Button resetButton = new Button("重置");
        resetButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        resetButton.setOnAction(e -> resetQuery());
        
        HBox buttonRow = new HBox(10);
        buttonRow.getChildren().addAll(queryButton, resetButton);
        
        overdueRow.getChildren().addAll(overdueLabel, minOverdueField, toLabel, maxOverdueField, buttonRow);
        
        // 合同号多选框区域 - 放在下面
        VBox contractSection = new VBox(10);
        Label contractLabel = new Label("合同号(A-Z):");
        contractLabel.setStyle("-fx-font-weight: bold;");
        
        // 创建A-Z字母多选框 - 改为一行显示
        HBox contractCheckboxRow = new HBox(5); // 减少间距，让一行能放下
        contractCheckboxes = new CheckBox[26];
        
        for (int i = 0; i < 26; i++) {
            char letter = (char) ('A' + i);
            CheckBox checkbox = new CheckBox(String.valueOf(letter));
            checkbox.setMinWidth(30); // 设置最小宽度，确保紧凑显示
            contractCheckboxes[i] = checkbox;
            contractCheckboxRow.getChildren().add(checkbox);
        }
        
        contractSection.getChildren().addAll(contractLabel, contractCheckboxRow);
        querySection.getChildren().addAll(sectionTitle, overdueRow, contractSection);
        return querySection;
    }
    
    private void selectAllContractLetters(boolean selected) {
        for (CheckBox checkbox : contractCheckboxes) {
            checkbox.setSelected(selected);
        }
        LogUtils.info("查询条件", selected ? "全选合同号" : "清空合同号选择");
    }
    
    private void executeQuery() {
        OrderQuery query = new OrderQuery();
        
        // 获取选中的合同号字母
        StringBuilder selectedLetters = new StringBuilder();
        for (int i = 0; i < contractCheckboxes.length; i++) {
            if (contractCheckboxes[i].isSelected()) {
                char letter = (char) ('A' + i);
                selectedLetters.append(letter);
            }
        }
        
        if (selectedLetters.length() == 0) {
            showAlert("查询条件", "请至少选择一个合同号字母");
            return;
        }
        
        query.setContractNumber(selectedLetters.toString());
        
        try {
            if (!minOverdueField.getText().isEmpty()) {
                query.setMinOverdueDays(Integer.parseInt(minOverdueField.getText()));
            }
            if (!maxOverdueField.getText().isEmpty()) {
                query.setMaxOverdueDays(Integer.parseInt(maxOverdueField.getText()));
            }
        } catch (NumberFormatException e) {
            LogUtils.error("查询", "逾期天数格式错误: " + e.getMessage());
            showAlert("输入错误", "逾期天数必须为数字");
            return;
        }
        
        controller.queryOrders(query);
    }
    
    private void resetQuery() {
        // 移除合同号全选功能，只清空逾期天数
        minOverdueField.clear();
        maxOverdueField.clear();
        tableView.setItems(controller.getDisplayedOrders());
        LogUtils.info("查询", "查询条件已重置");
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private VBox createTableSection() {
        VBox tableSection = new VBox(10);
        tableSection.setPadding(new Insets(10));
        
        // 订单列表表格
        Label orderTableTitle = new Label("订单列表");
        orderTableTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        tableView = new TableView<>();
        tableView.setItems(controller.getDisplayedOrders());
        tableView.setPrefHeight(400); // 进一步增加高度
        
        // 创建表格列 - 在列头添加全选复选框
        TableColumn<OrderData, Boolean> selectCol = new TableColumn<>("选择");
        selectCol.setMinWidth(80);
        selectCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("selected"));
        selectCol.setCellFactory(col -> new CheckBoxTableCell<>());
        
        // 添加列头全选功能
        CheckBox selectAllCheckbox = new CheckBox();
        selectAllCheckbox.setOnAction(e -> {
            boolean selected = selectAllCheckbox.isSelected();
            selectAllOrders(selected);
        });
        
        selectCol.setGraphic(selectAllCheckbox);
        
        TableColumn<OrderData, String> contractCol = new TableColumn<>("合同号");
        contractCol.setMinWidth(150);
        contractCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("contractNumber"));
        
        TableColumn<OrderData, String> nameCol = new TableColumn<>("姓名");
        nameCol.setMinWidth(100);
        nameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
        
        TableColumn<OrderData, Integer> overdueCol = new TableColumn<>("逾期天数");
        overdueCol.setMinWidth(100);
        overdueCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("overdueDays"));
        
        TableColumn<OrderData, String> remarksCol = new TableColumn<>("备注");
        remarksCol.setMinWidth(200);
        remarksCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("remarks"));
        
        tableView.getColumns().addAll(selectCol, contractCol, nameCol, overdueCol, remarksCol);
        
        // 监听选择变化，更新全选复选框状态
        tableView.getItems().addListener((javafx.collections.ListChangeListener<OrderData>) c -> {
            updateSelectAllCheckbox(selectAllCheckbox);
        });
        
        // 抢单结果表格
        Label resultTableTitle = new Label("抢单结果");
        resultTableTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        resultTableView = new TableView<>();
        resultTableView.setItems(controller.getGrabResults());
        resultTableView.setPrefHeight(400); // 进一步增加高度
        
        // 创建抢单结果表格列
        TableColumn<GrabResult, String> resultContractCol = new TableColumn<>("合同号");
        resultContractCol.setMinWidth(120);
        resultContractCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("contractNumber"));
        
        TableColumn<GrabResult, Integer> retryCol = new TableColumn<>("重试次数");
        retryCol.setMinWidth(80);
        retryCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("retryCount"));
        
        TableColumn<GrabResult, String> resultCol = new TableColumn<>("抢单结果");
        resultCol.setMinWidth(150);
        resultCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("result"));
        
        TableColumn<GrabResult, Boolean> successCol = new TableColumn<>("是否成功");
        successCol.setMinWidth(80);
        successCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("success"));
        successCol.setCellFactory(col -> new TableCell<GrabResult, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "成功" : "失败");
                    setStyle(item ? "-fx-text-fill: green; -fx-font-weight: bold;" : "-fx-text-fill: red; -fx-font-weight: bold;");
                }
            }
        });
        
        resultTableView.getColumns().addAll(resultContractCol, retryCol, resultCol, successCol);
        
        tableSection.getChildren().addAll(orderTableTitle, tableView, resultTableTitle, resultTableView);
        return tableSection;
    }
    
    private void updateSelectAllCheckbox(CheckBox selectAllCheckbox) {
        if (controller.getDisplayedOrders().isEmpty()) {
            selectAllCheckbox.setSelected(false);
            selectAllCheckbox.setIndeterminate(false);
        } else {
            long selectedCount = controller.getDisplayedOrders().stream()
                .filter(OrderData::isSelected)
                .count();
            
            if (selectedCount == 0) {
                selectAllCheckbox.setSelected(false);
                selectAllCheckbox.setIndeterminate(false);
            } else if (selectedCount == controller.getDisplayedOrders().size()) {
                selectAllCheckbox.setSelected(true);
                selectAllCheckbox.setIndeterminate(false);
            } else {
                selectAllCheckbox.setSelected(false);
                selectAllCheckbox.setIndeterminate(true);
            }
        }
    }
    
    private void selectAllOrders(boolean selected) {
        controller.getDisplayedOrders().forEach(order -> order.setSelected(selected));
        updateSelectionCount(null);
        LogUtils.info("表格操作", selected ? "全选订单" : "清空选择");
    }
    
    private void updateSelectionCount(Label label) {
        if (label != null) {
            long selectedCount = controller.getDisplayedOrders().stream()
                .filter(OrderData::isSelected)
                .count();
            label.setText("已选择: " + selectedCount);
        }
    }

    private VBox createBottomSection() {
        VBox bottomSection = new VBox(15);
        bottomSection.setPadding(new Insets(15));
        
        // 抢单操作区域
        VBox grabSection = new VBox(10);
        grabSection.setPadding(new Insets(10));
        grabSection.setStyle("-fx-background-color: #e8f5e8; -fx-border-color: #c8e6c9; -fx-border-width: 1;");
        
        Label grabTitle = new Label("抢单操作");
        grabTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        
        HBox grabActions = new HBox(15);
        grabActions.setAlignment(Pos.CENTER_LEFT);
        
        grabImmediatelyButton = new Button("立即抢单");
        grabImmediatelyButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        grabImmediatelyButton.setOnAction(e -> grabOrdersImmediately());
        
        scheduleGrabButton = new Button("定时抢单");
        scheduleGrabButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold;");
        scheduleGrabButton.setOnAction(e -> scheduleGrabOrders());
        
        // 定时抢单时间设置
        Label timeLabel = new Label("定时时间:");
        hourField = new TextField();
        hourField.setPromptText("时");
        hourField.setPrefWidth(40);
        hourField.setText("8");
        
        Label colon1 = new Label(":");
        minuteField = new TextField();
        minuteField.setPromptText("分");
        minuteField.setPrefWidth(40);
        minuteField.setText("30");
        
        Label colon2 = new Label(":");
        secondField = new TextField();
        secondField.setPromptText("秒");
        secondField.setPrefWidth(40);
        secondField.setText("0");
        
        // 倒计时显示和停止按钮
        HBox countdownRow = new HBox(10);
        countdownRow.setAlignment(Pos.CENTER_LEFT);
        
        countdownLabel = new Label("倒计时: 未开始");
        countdownLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #dc3545;");
        
        stopGrabButton = new Button("停止抢单");
        stopGrabButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold;");
        stopGrabButton.setDisable(true);
        stopGrabButton.setOnAction(e -> stopGrabOrders());
        
        countdownRow.getChildren().addAll(countdownLabel, stopGrabButton);
        
        grabActions.getChildren().addAll(grabImmediatelyButton, scheduleGrabButton, timeLabel, 
                                        hourField, colon1, minuteField, colon2, secondField);
        grabSection.getChildren().addAll(grabTitle, grabActions, countdownRow);
        
        // 日志区域
        VBox logSection = new VBox(10);
        logSection.setPadding(new Insets(10));
        
        HBox logHeader = new HBox(10);
        logHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label logLabel = new Label("系统日志");
        logLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        Button clearLogButton = new Button("清空日志");
        clearLogButton.setOnAction(e -> logArea.clear());
        
        Button exportLogButton = new Button("导出日志");
        exportLogButton.setOnAction(e -> exportLogs());
        
        Button openLogButton = new Button("打开日志位置");
        openLogButton.setOnAction(e -> openLogDirectory());
        
        logHeader.getChildren().addAll(logLabel, clearLogButton, exportLogButton, openLogButton);
        
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(200);
        logArea.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 12px;");
        
        logSection.getChildren().addAll(logHeader, logArea);
        
        bottomSection.getChildren().addAll(grabSection, logSection);
        return bottomSection;
    }
    
    private void grabOrdersImmediately() {
        List<OrderData> selectedOrders = controller.getSelectedOrders(controller.getDisplayedOrders());
        if (selectedOrders.isEmpty()) {
            showAlert("操作提示", "请先选择要抢单的订单");
            return;
        }
        
        grabImmediatelyButton.setDisable(true);
        scheduleGrabButton.setDisable(true);
        stopGrabButton.setDisable(false);
        countdownLabel.setText("倒计时: 立即抢单中...");
        countdownLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #28a745;");
        
        controller.grabOrdersImmediately(selectedOrders);
        
        // 3秒后重新启用按钮
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
        pause.setOnFinished(e -> {
            grabImmediatelyButton.setDisable(false);
            scheduleGrabButton.setDisable(false);
            stopGrabButton.setDisable(true);
            countdownLabel.setText("倒计时: 未开始");
            countdownLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #dc3545;");
        });
        pause.play();
    }
    
    private void scheduleGrabOrders() {
        List<OrderData> selectedOrders = controller.getSelectedOrders(controller.getDisplayedOrders());
        OrderQuery currentQuery = getCurrentQuery();
        
        try {
            int hour = Integer.parseInt(hourField.getText());
            int minute = Integer.parseInt(minuteField.getText());
            int second = Integer.parseInt(secondField.getText());
            
            if (hour < 0 || hour > 23) {
                showAlert("输入错误", "小时必须在0-23之间");
                return;
            }
            if (minute < 0 || minute > 59) {
                showAlert("输入错误", "分钟必须在0-59之间");
                return;
            }
            if (second < 0 || second > 59) {
                showAlert("输入错误", "秒必须在0-59之间");
                return;
            }
            
            grabImmediatelyButton.setDisable(true);
            scheduleGrabButton.setDisable(true);
            stopGrabButton.setDisable(false);
            
            // 计算倒计时
            startCountdownTimer(hour, minute, second);
            
            controller.scheduleDailyGrabOrders(selectedOrders, currentQuery, hour, minute, second);
            
            LogUtils.info("定时抢单", String.format("已设置每天 %02d:%02d:%02d 执行抢单", hour, minute, second));
            
        } catch (NumberFormatException e) {
            showAlert("输入错误", "请输入有效的时间数字");
        }
    }
    
    private void startCountdownTimer(int targetHour, int targetMinute, int targetSecond) {
        // 停止之前的倒计时
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        
        // 创建倒计时任务
        countdownTimeline = new javafx.animation.Timeline();
        javafx.animation.KeyFrame keyFrame = new javafx.animation.KeyFrame(
            javafx.util.Duration.seconds(1),
            e -> updateCountdown(targetHour, targetMinute, targetSecond)
        );
        countdownTimeline.getKeyFrames().add(keyFrame);
        countdownTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        countdownTimeline.play();
    }
    
    private void updateCountdown(int targetHour, int targetMinute, int targetSecond) {
        java.time.LocalTime now = java.time.LocalTime.now();
        java.time.LocalTime targetTime = java.time.LocalTime.of(targetHour, targetMinute, targetSecond);
        
        long totalSeconds;
        if (now.isBefore(targetTime)) {
            totalSeconds = java.time.Duration.between(now, targetTime).getSeconds();
        } else {
            // 如果当前时间已过目标时间，计算到明天同一时间的倒计时
            totalSeconds = java.time.Duration.between(now, targetTime).getSeconds() + 24 * 60 * 60;
        }
        
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        
        countdownLabel.setText(String.format("倒计时: %02d:%02d:%02d", hours, minutes, seconds));
        countdownLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #17a2b8;");
    }
    
    private void stopGrabOrders() {
        controller.stopAllGrabTasks();
        
        // 停止倒计时
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        
        grabImmediatelyButton.setDisable(false);
        scheduleGrabButton.setDisable(false);
        stopGrabButton.setDisable(true);
        countdownLabel.setText("倒计时: 已停止");
        countdownLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #6c757d;");
        
        LogUtils.info("抢单操作", "所有抢单任务已停止");
        showAlert("操作成功", "抢单任务已停止");
    }
    
    private OrderQuery getCurrentQuery() {
        OrderQuery query = new OrderQuery();
        
        // 获取选中的合同号字母
        StringBuilder selectedLetters = new StringBuilder();
        for (int i = 0; i < contractCheckboxes.length; i++) {
            if (contractCheckboxes[i].isSelected()) {
                char letter = (char) ('A' + i);
                selectedLetters.append(letter);
            }
        }
        
        query.setContractNumber(selectedLetters.toString());
        
        try {
            if (!minOverdueField.getText().isEmpty()) {
                query.setMinOverdueDays(Integer.parseInt(minOverdueField.getText()));
            }
            if (!maxOverdueField.getText().isEmpty()) {
                query.setMaxOverdueDays(Integer.parseInt(maxOverdueField.getText()));
            }
        } catch (NumberFormatException e) {
            // 忽略逾期天数错误，使用默认值
        }
        
        return query;
    }
    
    private void exportLogs() {
        // 简单的日志导出功能
        LogUtils.info("日志管理", "导出日志功能待实现");
        showAlert("功能提示", "日志导出功能正在开发中");
    }
    
    private void openLogDirectory() {
        try {
            String logDir = System.getProperty("user.dir") + "/logs";
            java.nio.file.Path logPath = java.nio.file.Paths.get(logDir);
            
            // 如果日志目录不存在，创建它
            if (!java.nio.file.Files.exists(logPath)) {
                java.nio.file.Files.createDirectories(logPath);
            }
            
            // 打开文件管理器
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(logPath.toFile());
                LogUtils.info("日志管理", "已打开日志目录: " + logPath);
            } else {
                showAlert("系统不支持", "当前系统不支持直接打开文件管理器");
            }
        } catch (Exception e) {
            LogUtils.error("日志管理", "打开日志目录失败: " + e.getMessage());
            showAlert("操作失败", "无法打开日志目录: " + e.getMessage());
        }
    }
}