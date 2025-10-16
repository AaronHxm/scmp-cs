package com.scmp.simple.manager;

import com.scmp.simple.model.OrderData;
import com.scmp.simple.service.OrderService;
import java.util.List;

public class OrderManager {
    private final OrderService orderService;
    
    public OrderManager(OrderService orderService) {
        this.orderService = orderService;
    }
    
    public void performImmediateGrab(List<OrderData> orders) {
        // 实现立即抢单逻辑
    }
    
    public void scheduleGrab(int delaySeconds) {
        // 实现定时抢单逻辑
    }
}