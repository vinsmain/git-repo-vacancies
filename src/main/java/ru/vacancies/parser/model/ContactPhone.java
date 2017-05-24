package ru.vacancies.parser.model;

import com.google.gson.annotations.SerializedName;

public class ContactPhone {

    @SerializedName("phone")
    public String phone;

    public ContactPhone() {
        this.phone = null;
    }

    public ContactPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }
}
