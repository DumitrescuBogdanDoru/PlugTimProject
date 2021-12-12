package com.dbd.plugtimproject.models;

public class Car {
    private String company, model, color;
    private Integer year;

    public Car(String company, String model, String color, Integer year) {
        this.company = company;
        this.model = model;
        this.color = color;
        this.year = year;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
