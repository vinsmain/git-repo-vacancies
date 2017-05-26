package ru.vacancies.database;

import ru.vacancies.parser.Vacancy;

import java.sql.*;

public class DataBase {

    /*
    // Update_Status
    // 0 - for delete
    // 1 - no update
    // 2 - update
    */

    private Connection conn;
    private PreparedStatement insert;
    private PreparedStatement checkUpdate;

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
                    "Shedule_ID INTEGER NOT NULL REFERENCES Shedule (ID), Update_Status INTEGER NOT NULL)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Vacancy_Details (Vacancy_ID INTEGER PRIMARY KEY NOT NULL REFERENCES Vacancy (ID), Description TEXT," +
                    "Education_ID INTEGER NOT NULL REFERENCES Education (ID), Experience_ID INTEGER NOT NULL REFERENCES Experience (ID))");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Contacts (ID INTEGER PRIMARY KEY NOT NULL, Title TEXT, City TEXT, Subway TEXT, Street TEXT, Building TEXT, Phone TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Company (ID INTEGER PRIMARY KEY NOT NULL, Title TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS WorkingType (ID INTEGER PRIMARY KEY NOT NULL, Title TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Shedule (ID INTEGER PRIMARY KEY NOT NULL, Title TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Education (ID INTEGER PRIMARY KEY NOT NULL, Title TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Experience (ID INTEGER PRIMARY KEY NOT NULL, Title TEXT)");

            insert = conn.prepareStatement("INSERT INTO Vacancy(ID, Header, Date_Time, Min_Salary, Max_Salary, Company_ID, WorkingType_ID, Shedule_ID, Update_Status) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)");
            checkUpdate = conn.prepareStatement("SELECT ID, Date_Time FROM Vacancy WHERE ID = ?");
        } catch (Exception e) {
            System.out.println("Ошибка инициализации JDBC драйвера");
            e.printStackTrace();
        }
    }

    public void insert(Vacancy vacancy) {
        try {
            insert.setInt(1, vacancy.getId());
            insert.setString(2, vacancy.getHeader());
            insert.setString(3, vacancy.getDateTime());
            insert.setInt(4, vacancy.getSalaryMin());
            insert.setInt(5, vacancy.getSalaryMax());
            insert.setInt(6, vacancy.getCompany().getId());
            insert.setInt(7, vacancy.getWorkingType().getId());
            insert.setInt(8, vacancy.getSchedule().getId());
            insert.setInt(9, 2);
            //insert.addBatch();
            //insert.executeBatch();
            insert.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean checkUpdate(Vacancy vacancy) {
        try {
            checkUpdate.setInt(1, vacancy.getId());
            ResultSet resultSet = checkUpdate.executeQuery();
            String  dateTime = null;
            while(resultSet.next()) {
                dateTime = resultSet.getString("Date_Time");
            }
            if (dateTime != null && !vacancy.getDateTime().equals(dateTime)) {
                return false;
            } else if (dateTime == null) return true;
            //return !vacancy.getDateTime().equals(dateTime);
        } catch (SQLException e) {
            return true;
        }
        return false;
    }

    public void disconnect() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConn() {
        return conn;
    }

    public PreparedStatement getInsert() {
        return insert;
    }
}
