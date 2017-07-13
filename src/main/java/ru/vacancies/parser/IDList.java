package ru.vacancies.parser;

import com.google.gson.annotations.SerializedName;
import ru.vacancies.parser.metadata.MetaData;

import java.util.ArrayList;

public class IDList {

    @SerializedName("metadata")
    public MetaData metaData;

    @SerializedName("vacancies")
    public ArrayList<ID> list;
}
