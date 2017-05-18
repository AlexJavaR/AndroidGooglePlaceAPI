package com.googleplaceapi;

import java.io.Serializable;

public class GoogleAddress implements Serializable {
    private String placeId;
    private String description;
    private String latitude;
    private String longitude;
    private String house;
    private String street;
    private String city;
    private Integer amountUser;

    public GoogleAddress() {
    }

    public GoogleAddress(String description, String latitude, String longitude, String placeId, String house, String street, String city, Integer amountUser) {
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeId = placeId;
        this.house = house;
        this.street = street;
        this.city = city;
        this.amountUser = amountUser;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getAmountUser() {
        return amountUser;
    }

    public void setAmountUser(Integer amountUser) {
        this.amountUser = amountUser;
    }

    @Override
    public String toString() {
        return "GoogleAddress{" +
                "description='" + description + '\'' +
                ", placeId='" + placeId + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
