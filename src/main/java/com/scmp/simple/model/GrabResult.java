package com.scmp.simple.model;

import java.time.LocalDateTime;

/**
 * 抢单结果模型
 */
public class GrabResult {
    private String contractNumber;
    private int retryCount;
    private String result;
    private boolean success;
    private LocalDateTime timestamp;
    
    public GrabResult(String contractNumber, int retryCount, String result, boolean success) {
        this.contractNumber = contractNumber;
        this.retryCount = retryCount;
        this.result = result;
        this.success = success;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getContractNumber() {
        return contractNumber;
    }
    
    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }
    
    public int getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "GrabResult{" +
                "contractNumber='" + contractNumber + '\'' +
                ", retryCount=" + retryCount +
                ", result='" + result + '\'' +
                ", success=" + success +
                ", timestamp=" + timestamp +
                '}';
    }
}