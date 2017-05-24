package ru.vacancies.parser.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Contact {

    @SerializedName("name")
    private String name;

    @SerializedName("city")
    private City city;

    @SerializedName("subway")
    private Subway subway;

    @SerializedName("street")
    private String street;

    @SerializedName("building")
    private String building;

    @SerializedName("phones")
    private ArrayList<ContactPhone> phoneList;

    public Contact() {
        this.name = null;
        this.city = new City();
        this.subway = new Subway();
        this.street = null;
        this.building = null;
        this.phoneList = null;
    }

    public Contact(String name, City city, Subway subway, String street, String building, ArrayList<ContactPhone> phoneList) {
        this.name = name;
        this.city = city;
        this.subway = subway;
        this.street = street;
        this.building = building;
        this.phoneList = phoneList;
    }

    public void setPhone(ContactPhone phone) {
        this.phoneList = phoneList;
    }

    public String getName() {
        return name;
    }

    public City getCity() {
        return city;
    }

    public Subway getSubway() {
        return subway;
    }

    public String getStreet() {
        return street;
    }

    public String getBuilding() {
        return building;
    }

    public ArrayList<ContactPhone> getPhone() {
        return phoneList;
    }
}
