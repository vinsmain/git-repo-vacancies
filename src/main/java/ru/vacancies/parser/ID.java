package ru.vacancies.parser;

import com.google.gson.annotations.SerializedName;
import ru.vacancies.parser.model.Publication;

public class ID {

    @SerializedName("id")
    private int id;

    @SerializedName("publication")
    private Publication publication;

    private int status;

    public ID(int ID, Publication publication) {
        this.id = ID;
        this.publication = publication;
    }

    public int getId() {
        return id;
    }

    public Publication getPublication() {
        return publication;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
