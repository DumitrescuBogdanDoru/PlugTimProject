package com.dbd.plugtimproject.models;

public class Station {

    private String description;
    private Integer numberOfPorts;
    private LocationHelper locationHelper;
    private String addedBy;

    public Station() {

    }

    public Station(String description, Integer numberOfPorts, LocationHelper locationHelper, String addedBy) {
        this.description = description;
        this.numberOfPorts = numberOfPorts;
        this.locationHelper = locationHelper;
        this.addedBy = addedBy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getNumberOfPorts() {
        return numberOfPorts;
    }

    public void setNumberOfPorts(Integer numberOfPorts) {
        this.numberOfPorts = numberOfPorts;
    }

    public LocationHelper getLocationHelper() {
        return locationHelper;
    }

    public void setLocationHelper(LocationHelper locationHelper) {
        this.locationHelper = locationHelper;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }
}
