package ru.vacancies.parser.lists;

import com.google.gson.annotations.SerializedName;
import ru.vacancies.parser.model.ID;
import ru.vacancies.parser.metadata.MetaData;

import java.util.ArrayList;

public class IDList {

    @SerializedName("metadata")
    public MetaData metaData;

    @SerializedName("vacancies")
    public ArrayList<ID> list;
}
