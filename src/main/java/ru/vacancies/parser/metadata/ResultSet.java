package ru.vacancies.parser.metadata;

import com.google.gson.annotations.SerializedName;

public class ResultSet {

    @SerializedName("count")
    private int count;

    public ResultSet(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
