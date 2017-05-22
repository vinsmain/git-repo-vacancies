package ru.vacancies.parser;

import com.google.gson.annotations.SerializedName;
import ru.vacancies.parser.model.Education;
import ru.vacancies.parser.model.Experience;
import ru.vacancies.parser.model.Schedule;
import ru.vacancies.parser.model.WorkingType;

public class Vacancy {

    @SerializedName("id")
    private int id;

    @SerializedName("header")
    private String header;

    @SerializedName("education")
    private Education education;

    @SerializedName("experience_length")
    private Experience experience;

    @SerializedName("working_type")
    private WorkingType workingType;

    @SerializedName("schedule")
    private Schedule shedule;

    @SerializedName("description")
    private String description;

    public Vacancy(int ID, String header, Education education, Experience experience, WorkingType workingType, Schedule shedule, String description) {
        this.id = ID;
        this.header = header;
        this.education = education;
        this.experience = experience;
        this.workingType = workingType;
        this.shedule = shedule;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public Education getEducation() {
        return education;
    }

    public void setEducation(Education education) {
        this.education = education;
    }

    public Experience getExperience() {
        return experience;
    }

    public WorkingType getWorkingType() {
        return workingType;
    }

    public Schedule getShedule() {
        return shedule;
    }

    public String getDescription() {
        return description;
    }
}
