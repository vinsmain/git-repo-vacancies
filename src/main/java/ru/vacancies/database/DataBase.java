package ru.vacancies.database;

import ru.vacancies.parser.Vacancy;

import java.sql.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataBase {

    private Connection conn;
    private PreparedStatement insert;

    public DataBase() {
        connect();
    }

    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:src/main/resources/database/VacanciesDB.db");
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Vacancy (ID INTEGER PRIMARY KEY NOT NULL, Header TEXT NOT NULL, Date_Time DATETIME NOT NULL, Min_Salary INTEGER, Max_Salary INTEGER," +
                    "Company_ID INTEGER NOT NULL REFERENCES Company (ID), WorkingType_ID INTEGER NOT NULL REFERENCES WorkingType (ID)," +
                    "Shedule_ID INTEGER NOT NULL REFERENCES Shedule (ID), Contact_ID INTEGER NOT NULL REFERENCES Contacts (ID))");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Vacancy_Details (Vacancy_ID INTEGER PRIMARY KEY NOT NULL REFERENCES Vacancy (ID), Description TEXT," +
                    "Education_ID INTEGER NOT NULL REFERENCES Education (ID), Experience_ID INTEGER NOT NULL REFERENCES Experience (ID))");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Contacts (ID INTEGER PRIMARY KEY NOT NULL, Title TEXT, City TEXT, Subway TEXT, Street TEXT, Building TEXT, Phone TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Company (ID INTEGER PRIMARY KEY NOT NULL, Title TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS WorkingType (ID INTEGER PRIMARY KEY NOT NULL, Title TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Shedule (ID INTEGER PRIMARY KEY NOT NULL, Title TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Education (ID INTEGER PRIMARY KEY NOT NULL, Title TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Experience (ID INTEGER PRIMARY KEY NOT NULL, Title TEXT)");

            insert = conn.prepareStatement("INSERT INTO Vacancy(ID, Header, Date_Time, Min_Salary, Max_Salary, Company_ID, WorkingType_ID, Shedule_ID, Contact_ID) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)");
        } catch (Exception e) {
            System.out.println("Ошибка инициализации JDBC драйвера");
            e.printStackTrace();
        }
    }

    public void insert(CopyOnWriteArrayList<Vacancy> vacancyList) {
        int i = 0;
        for (Vacancy vacancy : vacancyList) {
            try {
                conn.setAutoCommit(false);
                insert.setInt(1, vacancy.getId());
                insert.setString(2, vacancy.getHeader());
                insert.setString(3, vacancy.getDateTime());
                insert.setInt(4, vacancy.getSalaryMin());
                insert.setInt(5, vacancy.getSalaryMax());
                insert.setInt(6, vacancy.getCompany().getId());
                insert.setInt(7, vacancy.getWorkingType().getId());
                insert.setInt(8, vacancy.getSchedule().getId());
                insert.setInt(9, vacancy.getId());
                insert.addBatch();
                i++;
                if (i % 1000 == 0 || i == vacancyList.size()) {
                    System.out.println(i);
                    insert.executeBatch();
                    conn.commit();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
