package ru.vacancies.parser.model;

import com.google.gson.annotations.SerializedName;

public class ContactPhone {

    @SerializedName("user_type")
    public String phone;

    public String getPhone() {
        return phone;
    }
}
