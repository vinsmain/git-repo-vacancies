package ru.vacancies.parser;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class VacList {

    @SerializedName("vacancies")
    public ArrayList<Vacancy> list;
}
