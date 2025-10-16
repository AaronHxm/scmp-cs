package com.scmp.simple.controller;

import com.scmp.simple.model.OrderData;
import com.scmp.simple.model.OrderQuery;
import com.scmp.simple.service.OrderService;
import com.scmp.simple.utils.LogUtils;
import com.scmp.simple.model.GrabResult;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 主界面控制器 - 处理UI事件和业务逻辑协调
 */
public class MainController {
    private OrderService orderService;
    private ObservableList<OrderData> displayedOrders;
    private ObservableList<GrabResult> grabResults;
    private ScheduledExecutorService scheduler;
    private List<ScheduledFuture<?>> scheduledTasks;
    private volatile boolean isGrabRunning;
    
    public MainController() {
        this.orderService = new OrderService();
        this.displayedOrders = orderService.getAllOrders();
        this.grabResults = FXCollections.observableArrayList();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.scheduledTasks = new CopyOnWriteArrayList<>();
        this.isGrabRunning = false;
    }
    
    /**
     * 查询订单
     */
    public void queryOrders(OrderQuery query) {
        LogUtils.info("主控制器", "接收查询请求: " + query);
        
        Task<ObservableList<OrderData>> queryTask = new Task<>() {
            @Override
            protected ObservableList<OrderData> call() throws Exception {
                return orderService.queryOrders(query);
            }
        };
        
        queryTask.setOnSucceeded(e -> {
            ObservableList<OrderData> result = queryTask.getValue();
            displayedOrders.setAll(result);
            LogUtils.info("主控制器", "查询完成，显示 " + result.size() + " 条记录");
        });
        
        queryTask.setOnFailed(e -> {
            LogUtils.error("主控制器", "查询失败: " + queryTask.getException().getMessage());
        });
        
        new Thread(queryTask).start();
    }
    
    /**
     * 立即抢单
     */
    public void grabOrdersImmediately(List<OrderData> selectedOrders) {
        if (selectedOrders.isEmpty()) {
            LogUtils.warn("主控制器", "未选择任何订单");
            return;
        }
        
        if (isGrabRunning) {
            LogUtils.warn("主控制器", "抢单任务正在进行中，请先停止当前任务");
            return;
        }
        
        isGrabRunning = true;
        LogUtils.info("主控制器", "开始立即抢单，数量: " + selectedOrders.size());
        
        Task<Void> grabTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (OrderData order : selectedOrders) {
                    if (!isGrabRunning) {
                        LogUtils.info("主控制器", "抢单任务被停止");
                        break;
                    }
                    GrabResult result = orderService.grabOrder(order);
                    addGrabResult(result);
                }
                return null;
            }
        };
        
        grabTask.setOnSucceeded(e -> {
            isGrabRunning = false;
            LogUtils.info("主控制器", "立即抢单完成");
        });
        
        grabTask.setOnFailed(e -> {
            isGrabRunning = false;
            LogUtils.error("主控制器", "抢单失败: " + grabTask.getException().getMessage());
        });
        
        new Thread(grabTask).start();
    }
    
    /**
     * 停止所有抢单任务
     */
    public void stopAllGrabTasks() {
        isGrabRunning = false;
        
        // 取消所有定时任务
        for (ScheduledFuture<?> task : scheduledTasks) {
            if (!task.isDone() && !task.isCancelled()) {
                task.cancel(true);
            }
        }
        scheduledTasks.clear();
        
        // 关闭并重新创建调度器
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        // 重新创建调度器
        scheduler = Executors.newScheduledThreadPool(1);
        
        LogUtils.info("主控制器", "所有抢单任务已停止");
    }
    
    /**
     * 定时抢单（旧版本，保留兼容性）
     */
    public void scheduleGrabOrders(List<OrderData> selectedOrders, int delayMinutes) {
        if (selectedOrders.isEmpty()) {
            LogUtils.warn("主控制器", "未选择任何订单");
            return;
        }
        
        LogUtils.info("主控制器", "设置定时抢单，延迟: " + delayMinutes + "分钟，数量: " + selectedOrders.size());
        
        Task<Void> scheduleTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // 模拟定时逻辑
                Thread.sleep(delayMinutes * 60 * 1000L);
                orderService.grabOrders(selectedOrders);
                return null;
            }
        };
        
        scheduleTask.setOnSucceeded(e -> {
            LogUtils.info("主控制器", "定时抢单执行完成");
        });
        
        scheduleTask.setOnFailed(e -> {
            LogUtils.error("主控制器", "定时抢单失败: " + scheduleTask.getException().getMessage());
        });
        
        new Thread(scheduleTask).start();
    }
    
    /**
     * 每天定时抢单
     */
    public void scheduleDailyGrabOrders(List<OrderData> selectedOrders, OrderQuery query, int hour, int minute, int second) {
        LogUtils.info("主控制器", String.format("设置每天 %02d:%02d:%02d 定时抢单", hour, minute, second));
        
        // 计算第一次执行时间
        LocalTime targetTime = LocalTime.of(hour, minute, second);
        LocalTime now = LocalTime.now();
        
        long initialDelay;
        if (now.isBefore(targetTime)) {
            initialDelay = java.time.Duration.between(now, targetTime).getSeconds();
        } else {
            // 如果今天的时间已过，安排到明天
            initialDelay = java.time.Duration.between(now, targetTime).getSeconds() + 24 * 60 * 60;
        }
        
        // 设置预登录和查询任务
        LocalTime preLoginTime = targetTime.minusMinutes(15); // 提前15分钟登录
        LocalTime queryTime = targetTime.minusMinutes(10);    // 提前10分钟查询
        
        // 安排预登录任务
        scheduleDailyTask(preLoginTime, () -> {
            LogUtils.info("定时任务", "执行预登录");
            // 这里调用登录逻辑
        });
        
        // 安排查询任务
        scheduleDailyTask(queryTime, () -> {
            LogUtils.info("定时任务", "执行查询");
            if (selectedOrders.isEmpty()) {
                // 如果没有勾选订单，使用查询条件查询
                queryOrders(query);
            }
        });
        
        // 安排抢单任务
        scheduleDailyTask(targetTime, () -> {
            LogUtils.info("定时任务", "执行抢单");
            List<OrderData> ordersToGrab = selectedOrders.isEmpty() ? 
                getSelectedOrders(displayedOrders) : selectedOrders;
            
            if (!ordersToGrab.isEmpty()) {
                grabOrdersImmediately(ordersToGrab);
            } else {
                LogUtils.warn("定时任务", "没有可抢单的订单");
            }
        });
    }
    
    /**
     * 安排每天定时任务
     */
    private void scheduleDailyTask(LocalTime time, Runnable task) {
        LocalTime now = LocalTime.now();
        long initialDelay;
        
        if (now.isBefore(time)) {
            initialDelay = java.time.Duration.between(now, time).getSeconds();
        } else {
            initialDelay = java.time.Duration.between(now, time).getSeconds() + 24 * 60 * 60;
        }
        
        ScheduledFuture<?> scheduledTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                task.run();
            } catch (Exception e) {
                LogUtils.error("定时任务", "执行失败: " + e.getMessage());
            }
        }, initialDelay, 24 * 60 * 60, TimeUnit.SECONDS); // 每天执行一次
        
        scheduledTasks.add(scheduledTask);
    }
    
    /**
     * 添加抢单结果
     */
    public void addGrabResult(GrabResult result) {
        javafx.application.Platform.runLater(() -> {
            grabResults.add(result);
        });
    }
    
    /**
     * 获取选中的订单
     */
    public List<OrderData> getSelectedOrders(ObservableList<OrderData> orders) {
        return orders.stream()
            .filter(OrderData::isSelected)
            .collect(Collectors.toList());
    }
    
    public ObservableList<OrderData> getDisplayedOrders() {
        return displayedOrders;
    }
    
    public ObservableList<GrabResult> getGrabResults() {
        return grabResults;
    }
    
    public OrderService getOrderService() {
        return orderService;
    }
    
    /**
     * 关闭控制器，释放资源
     */
    public void shutdown() {
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
    }
}