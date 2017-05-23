package ru.vacancies.parser.model;

import com.google.gson.annotations.SerializedName;

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

    private ContactPhone phone;

    public Contact() {
        this.name = null;
        this.city = new City();
        this.subway = new Subway();
        this.street = null;
        this.building = null;
        this.phone = null;
    }

    public Contact(String name, City city, Subway subway, String street, String building) {
        this.name = name;
        this.city = city;
        this.subway = subway;
        this.street = street;
        this.building = building;
    }

    public void setPhone(ContactPhone phone) {
        this.phone = phone;
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

    public ContactPhone getPhone() {
        return phone;
    }
}
