package ru.vacancies.parser.metadata;

import com.google.gson.annotations.SerializedName;

public class MetaData {

    @SerializedName("resultset")
    private ResultSet resultSet;

    public MetaData(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }
}
