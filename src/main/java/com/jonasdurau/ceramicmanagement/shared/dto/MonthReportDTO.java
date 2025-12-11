package com.jonasdurau.ceramicmanagement.shared.dto;

import java.math.BigDecimal;

public class MonthReportDTO {
    
    private String monthName;
    private double incomingQty;
    private BigDecimal incomingCost;
    private double outgoingQty;
    private BigDecimal outgoingProfit;

    public MonthReportDTO() {
    }

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public double getIncomingQty() {
        return incomingQty;
    }

    public void setIncomingQty(double incomingQty) {
        this.incomingQty = incomingQty;
    }

    public BigDecimal getIncomingCost() {
        return incomingCost;
    }

    public void setIncomingCost(BigDecimal incomingCost) {
        this.incomingCost = incomingCost;
    }

    public double getOutgoingQty() {
        return outgoingQty;
    }

    public void setOutgoingQty(double outgoingQty) {
        this.outgoingQty = outgoingQty;
    }

    public BigDecimal getOutgoingProfit() {
        return outgoingProfit;
    }

    public void setOutgoingProfit(BigDecimal outgoingProfit) {
        this.outgoingProfit = outgoingProfit;
    }
}
