package ru.vacancies.parser.model;

public class Model {

    private int id;
    private String title;

    public Model() {
        this.id = 0;
        this.title = null;
    }

    public Model(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
