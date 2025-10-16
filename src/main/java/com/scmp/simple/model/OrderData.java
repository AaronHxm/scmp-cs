package com.scmp.simple.model;

import lombok.Data;

@Data
public class OrderData {
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

}