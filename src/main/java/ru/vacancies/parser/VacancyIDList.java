package ru.vacancies.parser;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class VacancyIDList {

    @SerializedName("vacancies")
    public ArrayList<VacancyID> list;
}
