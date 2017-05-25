package ru.vacancies.parser;

import com.google.gson.annotations.SerializedName;
import ru.vacancies.parser.metadata.MetaData;

import java.util.ArrayList;

public class VacancyIDList {

    @SerializedName("metadata")
    public MetaData metaData;

    @SerializedName("vacancies")
    public ArrayList<VacancyID> list;
}
