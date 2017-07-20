package ru.vacancies.parser.lists;

import com.google.gson.annotations.SerializedName;
import ru.vacancies.parser.model.Vacancy;

import java.util.ArrayList;

public class VacancyList {

    @SerializedName("vacancies")
    public ArrayList<Vacancy> list;
}
