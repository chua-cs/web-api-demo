package com.demo.entities;

import com.demo.enums.SubscriptionType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class Subscription {

    private BigDecimal amount;
    private SubscriptionType type;

    @JsonIgnore
    private DayOfWeek dayOfWeek;
    @JsonIgnore
    private int dayOfMonth;
    @JsonIgnore
    private LocalDate startDate;
    @JsonIgnore
    private LocalDate endDate;

    private List<String> invoiceDateList;

    public BigDecimal getAmount() {
        return this.amount;
    }

    public SubscriptionType getType() {
        return this.type;
    }

    public DayOfWeek getDayOfWeek() {
        return this.dayOfWeek;
    }

    public int getDayOfMonth() {
        return this.dayOfMonth;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public List<String> getInvoiceDateList() {
        return this.invoiceDateList;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setType(SubscriptionType type) {
        this.type = type;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setInvoiceDateList(List<String> invoiceDateList) {
        this.invoiceDateList = invoiceDateList;
    }
}
