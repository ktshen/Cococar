package com.tom.cococar;

/**
 * Created by Katherine on 2017/3/11.
 */

public class Transaction {
    String id;
    String rand;
    String latitude;
    String longitude;
    String url;

    public Transaction() {
    }

    public Transaction(String id, String rand, String latitude, String longitude, String url) {
        this.id = id;
        this.rand = rand;
        this.latitude = latitude;
        this.longitude = longitude;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRand() {
        return rand;
    }

    public void setRand(String rand) {
        this.rand = rand;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

