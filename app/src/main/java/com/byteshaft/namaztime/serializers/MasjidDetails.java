package com.byteshaft.namaztime.serializers;

/**
 * Created by s9iper1 on 12/24/17.
 */

public class MasjidDetails {

    private String masjidName;
    private double lat;
    private double lng;
    private String city;
    private String country;

    public String getMasjidName() {
        return masjidName;
    }

    public void setMasjidName(String masjidName) {
        this.masjidName = masjidName;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
