package com.dbd.plugtimproject.models;

public class Station {

    private String description;
    private Integer numberOfPorts;
    private LocationHelper locationHelper;
    private String addedBy;
    private boolean isType1, isType2, isCcs, isChademo;

    public Station() {

    }

    public Station(String description, Integer numberOfPorts, LocationHelper locationHelper, String addedBy, boolean isType1, boolean isType2, boolean isCcs, boolean isChademo) {
        this.description = description;
        this.numberOfPorts = numberOfPorts;
        this.locationHelper = locationHelper;
        this.addedBy = addedBy;
        this.isType1 = isType1;
        this.isType2 = isType2;
        this.isCcs = isCcs;
        this.isChademo = isChademo;
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

    public boolean isType1() {
        return isType1;
    }

    public void setType1(boolean type1) {
        isType1 = type1;
    }

    public boolean isType2() {
        return isType2;
    }

    public void setType2(boolean type2) {
        isType2 = type2;
    }

    public boolean isCcs() {
        return isCcs;
    }

    public void setCcs(boolean ccs) {
        isCcs = ccs;
    }

    public boolean isChademo() {
        return isChademo;
    }

    public void setChademo(boolean chademo) {
        isChademo = chademo;
    }
}
