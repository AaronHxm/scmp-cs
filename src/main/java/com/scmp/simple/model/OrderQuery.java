package com.scmp.simple.model;

/**
 * 订单查询条件模型
 */
public class OrderQuery {
    private String contractNumber;
    private String name;
    private Integer minOverdueDays;
    private Integer maxOverdueDays;
    
    public OrderQuery() {
        // 默认构造函数
    }
    
    public OrderQuery(String contractNumber, String name) {
        this.contractNumber = contractNumber;
        this.name = name;
    }
    
    // Getters and Setters
    public String getContractNumber() {
        return contractNumber;
    }
    
    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getMinOverdueDays() {
        return minOverdueDays;
    }
    
    public void setMinOverdueDays(Integer minOverdueDays) {
        this.minOverdueDays = minOverdueDays;
    }
    
    public Integer getMaxOverdueDays() {
        return maxOverdueDays;
    }
    
    public void setMaxOverdueDays(Integer maxOverdueDays) {
        this.maxOverdueDays = maxOverdueDays;
    }
    
    @Override
    public String toString() {
        return "OrderQuery{" +
                "contractNumber='" + contractNumber + '\'' +
                ", name='" + name + '\'' +
                ", minOverdueDays=" + minOverdueDays +
                ", maxOverdueDays=" + maxOverdueDays +
                '}';
    }
}