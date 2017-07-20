package ru.vacancies.parser.model;

public class SecondaryField {

    private int id;
    private String title;

    public SecondaryField() {
        this.id = 0;
        this.title = null;
    }

    public SecondaryField(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
