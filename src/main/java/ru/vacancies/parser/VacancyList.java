package ru.vacancies.parser;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class VacancyList {

    @SerializedName("vacancies")
    public ArrayList<Vacancy> list;
}
