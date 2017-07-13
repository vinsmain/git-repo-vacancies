package ru.vacancies.parser;

import com.google.gson.annotations.SerializedName;

public class ID {

    @SerializedName("id")
    private int id;

    public ID(int ID) {
        this.id = ID;
    }

    public int getId() {
        return id;
    }
}
