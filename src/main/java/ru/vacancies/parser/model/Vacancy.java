package ru.vacancies.parser.model;

import com.google.gson.annotations.SerializedName;

public class Vacancy {

    @SerializedName("id")
    private int id;

    @SerializedName("header")
    private String header;

    @SerializedName("salary_min")
    private long salaryMin;

    @SerializedName("salary_max")
    private long salaryMax;

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

    @SerializedName("company")
    private Company company;

    private int status;

    private String dateTime;

    public Vacancy(int id) {
        this.id = id;
    }

    public Vacancy(int id, String header, Education education, Experience experience, WorkingType workingType, Schedule schedule, String description, Contact contact, Company company, long salaryMin, long salaryMax) {
        this.id = id;
        this.header = header;
        this.education = education;
        this.experience = experience;
        this.workingType = workingType;
        this.schedule = schedule;
        this.description = description;
        this.contact = contact;
        this.company = company;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
    }

    public int getId() {
        return id;
    }

    public String getHeader() {
        return header;
    }

    public long getSalaryMin() {
        return salaryMin;
    }

    public long getSalaryMax() {
        return salaryMax;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
