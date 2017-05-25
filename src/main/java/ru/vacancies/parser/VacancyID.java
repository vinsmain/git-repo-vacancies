package ru.vacancies.parser;

import com.google.gson.annotations.SerializedName;

public class VacancyID {

    @SerializedName("id")
    private int id;

    public VacancyID(int ID) {
        this.id = ID;
    }

    public int getId() {
        return id;
    }
}
