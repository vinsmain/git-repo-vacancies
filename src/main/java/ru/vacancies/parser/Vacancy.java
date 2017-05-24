package ru.vacancies.parser;

import com.google.gson.annotations.SerializedName;
import ru.vacancies.parser.model.*;

public class Vacancy {

    @SerializedName("id")
    private int id;

    @SerializedName("header")
    private String header;

    @SerializedName("salary_min")
    private int salaryMin;

    @SerializedName("salary_max")
    private int SalaryMax;

    @SerializedName("education")
    private Education education;

    @SerializedName("experience_length")
    private Experience experience;

    @SerializedName("working_type")
    private WorkingType workingType;

    @SerializedName("schedule")
    private Schedule schedule;

    @SerializedName("description")
    private String description;

    @SerializedName("contact")
    private Contact contact;

    @SerializedName("mod_date")
    private String dateTime;

    @SerializedName("company")
    private Company company;

    public Vacancy(int ID, String header, Education education, Experience experience, WorkingType workingType, Schedule schedule, String description, Contact contact, String dateTime, Company company) {
        this.id = ID;
        this.header = header;
        this.education = education;
        this.experience = experience;
        this.workingType = workingType;
        this.schedule = schedule;
        this.description = description;
        this.contact = contact;
        this.dateTime = dateTime;
        this.company = company;
    }

    public int getId() {
        return id;
    }

    public String getHeader() {
        return header;
    }

    public int getSalaryMin() {
        return salaryMin;
    }

    public int getSalaryMax() {
        return SalaryMax;
    }

    public Education getEducation() {
        return education;
    }

    public Experience getExperience() {
        return experience;
    }

    public WorkingType getWorkingType() {
        return workingType;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public String getDescription() {
        return description;
    }

    public Contact getContact() {
        return contact;
    }

    public String getDateTime() {
        return dateTime;
    }

    public Company getCompany() {
        return company;
    }
}
