package ru.vacancies.parser.model;

import com.google.gson.annotations.SerializedName;

public class Publication {

    @SerializedName("published_at")
    private String dateTime;

    public String getDateTime() {
        return dateTime;
    }
}
