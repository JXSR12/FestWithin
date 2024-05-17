package com.jxsr.festwithin.models;

import java.util.ArrayList;

public class EventPricing {
    private String EventID;
    private String EPClassName;
    private String EPClassCurrency;
    private double EPClassPrice;
    private int EPClassStock;

    public EventPricing(String eventID, String EPClassName, String EPClassCurrency, double EPClassPrice, int EPClassStock) {
        EventID = eventID;
        this.EPClassName = EPClassName;
        this.EPClassCurrency = EPClassCurrency;
        this.EPClassPrice = EPClassPrice;
        this.EPClassStock = EPClassStock;
    }

    public String getEventID() {
        return EventID;
    }

    public void setEventID(String eventID) {
        EventID = eventID;
    }

    public String getEPClassName() {
        return EPClassName;
    }

    public void setEPClassName(String EPClassName) {
        this.EPClassName = EPClassName;
    }

    public String getEPClassCurrency() {
        return EPClassCurrency;
    }

    public void setEPClassCurrency(String EPClassCurrency) {
        this.EPClassCurrency = EPClassCurrency;
    }

    public double getEPClassPrice() {
        return EPClassPrice;
    }

    public void setEPClassPrice(double EPClassPrice) {
        this.EPClassPrice = EPClassPrice;
    }

    public int getEPClassStock() {
        return EPClassStock;
    }

    public void setEPClassStock(int EPClassStock) {
        this.EPClassStock = EPClassStock;
    }
}
