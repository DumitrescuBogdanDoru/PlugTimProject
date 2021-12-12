package com.dbd.plugtimproject.models;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String username, firstName, lastName;
    private List<Car> cars;


    public User(String username, String firstName, String lastName) {
        this.username = username;

        this.firstName = firstName;
        this.lastName = lastName;
        cars = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<Car> getCars() {
        return cars;
    }

    public void setCars(List<Car> cars) {
        this.cars = cars;
    }
}
