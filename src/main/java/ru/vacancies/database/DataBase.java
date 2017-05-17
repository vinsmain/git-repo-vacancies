package ru.vacancies.database;

import java.sql.*;

public class DataBase {

    private Connection conn;
    private DataBaseHandler handler;

    public DataBase() {
        connect();
        handler = new DataBaseHandler();
    }

    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:src/main/resources/database/VacanciesDB.db");
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Vacancy (ID INTEGER PRIMARY KEY NOT NULL, Name TEXT NOT NULL, Date_Time DATETIME NOT NULL, Min_Salary INTEGER, Max_Salary INTEGER," +
                    "Adress TEXT, Company_ID INTEGER NOT NULL REFERENCES Company (ID), Employment_ID INTEGER NOT NULL REFERENCES Employment (ID)," +
                    "Shedule_ID INTEGER NOT NULL REFERENCES Shedule (ID), Contact_ID INTEGER NOT NULL REFERENCES Contacts (ID))");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Vacancy_Details (Vacancy_ID INTEGER PRIMARY KEY NOT NULL REFERENCES Vacancy (ID), Description TEXT, " +
                    "Education_ID INTEGER NOT NULL REFERENCES Education (ID), Experience_ID INTEGER NOT NULL REFERENCES Experience (ID), Duties TEXT, Demands TEXT, Conditions TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Contacts (ID INTEGER PRIMARY KEY NOT NULL, Name TEXT NOT NULL, Phone TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Company (ID INTEGER PRIMARY KEY NOT NULL, Name TEXT NOT NULL)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Employment (ID INTEGER PRIMARY KEY NOT NULL, Name TEXT NOT NULL)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Shedule (ID INTEGER PRIMARY KEY NOT NULL, Name TEXT NOT NULL)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Education (ID INTEGER PRIMARY KEY NOT NULL, Name TEXT NOT NULL)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Experience (ID INTEGER PRIMARY KEY NOT NULL, Name TEXT NOT NULL)");
        } catch (Exception e) {
            System.out.println("Ошибка инициализации JDBC драйвера");
            e.printStackTrace();
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
