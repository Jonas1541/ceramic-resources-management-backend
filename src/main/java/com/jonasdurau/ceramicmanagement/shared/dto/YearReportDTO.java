package com.jonasdurau.ceramicmanagement.shared.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class YearReportDTO {

    private int year;
    private List<MonthReportDTO> months = new ArrayList<>();

    private double totalIncomingQty;
    private BigDecimal totalIncomingCost;
    private double totalOutgoingQty;
    private BigDecimal totalOutgoingProfit;

    public YearReportDTO() {
    }

    public YearReportDTO(int year) {
        this.year = year;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<MonthReportDTO> getMonths() {
        return months;
    }

    public double getTotalIncomingQty() {
        return totalIncomingQty;
    }

    public void setTotalIncomingQty(double totalIncomingQty) {
        this.totalIncomingQty = totalIncomingQty;
    }

    public BigDecimal getTotalIncomingCost() {
        return totalIncomingCost;
    }

    public void setTotalIncomingCost(BigDecimal totalIncomingCost) {
        this.totalIncomingCost = totalIncomingCost;
    }

    public double getTotalOutgoingQty() {
        return totalOutgoingQty;
    }

    public void setTotalOutgoingQty(double totalOutgoingQty) {
        this.totalOutgoingQty = totalOutgoingQty;
    }

    public BigDecimal getTotalOutgoingProfit() {
        return totalOutgoingProfit;
    }

    public void setTotalOutgoingProfit(BigDecimal totalOutgoingProfit) {
        this.totalOutgoingProfit = totalOutgoingProfit;
    }
}
