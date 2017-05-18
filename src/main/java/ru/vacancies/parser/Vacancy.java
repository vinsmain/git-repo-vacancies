package ru.vacancies.parser;

import com.google.gson.annotations.SerializedName;

public class Vacancy {

    @SerializedName("id")
    private int ID;

    @SerializedName("header")
    private String header;

    public Vacancy(int ID, String header) {
        this.ID = ID;
        this.header = header;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }
}
