package com.scmp.simple.service;

import com.scmp.simple.model.GrabResult;
import com.scmp.simple.model.OrderData;
import com.scmp.simple.model.OrderQuery;
import com.scmp.simple.utils.LogUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务层 - 处理订单相关的业务逻辑
 */
public class OrderService {
    private ObservableList<OrderData> orderList;
    
    public OrderService() {
        this.orderList = FXCollections.observableArrayList();
        initializeSampleData();
    }
    
    /**
     * 查询订单
     */
    public ObservableList<OrderData> queryOrders(OrderQuery query) {
        LogUtils.info("订单服务", "执行订单查询: " + query);
        
        List<OrderData> filteredOrders = orderList.stream()
            .filter(order -> matchesQuery(order, query))
            .collect(Collectors.toList());
        
        LogUtils.info("订单服务", "查询结果: " + filteredOrders.size() + " 条记录");
        return FXCollections.observableArrayList(filteredOrders);
    }
    
    /**
     * 抢单操作
     */
    public GrabResult grabOrder(OrderData order) {
        LogUtils.info("抢单服务", "开始抢单: " + order.getContractNumber());
        
        try {
            Thread.sleep(1000); // 模拟网络请求延迟
            
            // 模拟抢单结果（随机成功/失败）
            boolean success = Math.random() > 0.3;
            int retryCount = (int) (Math.random() * 3) + 1; // 1-3次重试
            String result = success ? "抢单成功" : "抢单失败";
            
            GrabResult grabResult = new GrabResult(order.getContractNumber(), retryCount, result, success);
            
            if (success) {
                LogUtils.info("抢单服务", "抢单成功: " + order.getContractNumber());
            } else {
                LogUtils.warn("抢单服务", "抢单失败: " + order.getContractNumber());
            }
            
            return grabResult;
            
        } catch (InterruptedException e) {
            LogUtils.error("抢单服务", "抢单失败: " + e.getMessage());
            return new GrabResult(order.getContractNumber(), 0, "抢单中断", false);
        }
    }
    
    /**
     * 批量抢单
     */
    public int grabOrders(List<OrderData> orders) {
        LogUtils.info("抢单服务", "开始批量抢单，数量: " + orders.size());
        
        int successCount = 0;
        for (OrderData order : orders) {
            GrabResult result = grabOrder(order);
            if (result.isSuccess()) {
                successCount++;
            }
        }
        
        LogUtils.info("抢单服务", "批量抢单完成，成功: " + successCount + "/" + orders.size());
        return successCount;
    }
    
    private boolean matchesQuery(OrderData order, OrderQuery query) {
        if (query.getContractNumber() != null && !query.getContractNumber().isEmpty()) {
            if (!order.getContractNumber().contains(query.getContractNumber())) {
                return false;
            }
        }
        
        if (query.getName() != null && !query.getName().isEmpty()) {
            if (!order.getName().contains(query.getName())) {
                return false;
            }
        }
        
        if (query.getMinOverdueDays() != null) {
            if (order.getOverdueDays() < query.getMinOverdueDays()) {
                return false;
            }
        }
        
        if (query.getMaxOverdueDays() != null) {
            if (order.getOverdueDays() > query.getMaxOverdueDays()) {
                return false;
            }
        }
        
        return true;
    }
    
    private void initializeSampleData() {
        // 初始化示例数据
        orderList.add(new OrderData("CT2024001", "张三", 15, "优质客户"));
        orderList.add(new OrderData("CT2024002", "李四", 30, "逾期较长"));
        orderList.add(new OrderData("CT2024003", "王五", 5, "新客户"));
        orderList.add(new OrderData("CT2024004", "赵六", 45, "需要重点关注"));
        orderList.add(new OrderData("CT2024005", "钱七", 20, "一般客户"));
    }
    
    public ObservableList<OrderData> getAllOrders() {
        return orderList;
    }
}