package ru.vacancies.database;

import org.sqlite.SQLiteConfig;
import ru.vacancies.parser.Vacancy;
import java.sql.*;
import java.sql.ResultSet;

public class DataBase {

    /*
    // Update_Status
    // 0 - no update
    // 1 - update
    */

    private Connection conn;
    private Statement stmt;
    private PreparedStatement insert;
    private PreparedStatement checkUpdate;
    private PreparedStatement update;
    private PreparedStatement updateStatus;
    private PreparedStatement updateStatusAfterParsing;
    //private PreparedStatement selectForDelete;
    private PreparedStatement delete;
    private PreparedStatement countAll;

    private PreparedStatement insertContact;
    private PreparedStatement updateContact;
    //private PreparedStatement deleteContact;

    private PreparedStatement insertCity;
    private PreparedStatement updateCity;

    private PreparedStatement insertSubway;
    private PreparedStatement updateSubway;

    private PreparedStatement insertCompany;
    private PreparedStatement updateCompany;

    private PreparedStatement insertEducation;
    private PreparedStatement updateEsucation;

    private PreparedStatement insertExperience;
    private PreparedStatement updateExperience;

    private PreparedStatement insertShedule;
    private PreparedStatement updateShedule;

    private PreparedStatement insertWorkingType;
    private PreparedStatement updateWorkingType;

    private String[] dataBase = {"City", "Subway", "Company", "Education", "Experience", "Shedule", "WorkingType"};

    public DataBase() {
        connect();
    }

    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
            conn = DriverManager.getConnection("jdbc:sqlite:src/main/resources/database/VacanciesDB.db", config.toProperties());
            stmt = conn.createStatement();
            stmt.executeUpdate("PRAGMA foreign_keys = 1");
            stmt.executeUpdate("PRAGMA foreign_keys = ON");
            System.out.println(stmt.executeUpdate("PRAGMA foreign_keys"));
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Vacancy (ID INTEGER PRIMARY KEY NOT NULL, Header TEXT NOT NULL, Date_Time DATETIME NOT NULL, Min_Salary INTEGER, Max_Salary INTEGER, " +
                    "Company_ID INTEGER NOT NULL REFERENCES Company (ID), WorkingType_ID INTEGER NOT NULL REFERENCES WorkingType (ID), " +
                    "Shedule_ID INTEGER NOT NULL REFERENCES Shedule (ID), Description TEXT, Education_ID INTEGER NOT NULL REFERENCES Education (ID), " +
                    "Experience_ID INTEGER NOT NULL REFERENCES Experience (ID), Update_Status INTEGER NOT NULL)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Contacts (ID INTEGER PRIMARY KEY NOT NULL REFERENCES Vacancy (ID) ON DELETE CASCADE, Title TEXT, City_ID INTEGER NOT NULL REFERENCES City (ID), Subway_ID INTEGER NOT NULL REFERENCES Subway (ID), Street TEXT, Building TEXT, Phone TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Company (ID INTEGER PRIMARY KEY NOT NULL, Title TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS WorkingType (ID INTEGER PRIMARY KEY NOT NULL, Title TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Shedule (ID INTEGER PRIMARY KEY NOT NULL, Title TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Education (ID INTEGER PRIMARY KEY NOT NULL, Title TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Experience (ID INTEGER PRIMARY KEY NOT NULL, Title TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS City (ID INTEGER PRIMARY KEY NOT NULL, Title TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Subway (ID INTEGER PRIMARY KEY NOT NULL, Title TEXT)");

            insert = conn.prepareStatement("INSERT INTO Vacancy(ID, Header, Date_Time, Min_Salary, Max_Salary, Company_ID, WorkingType_ID, Shedule_ID, Description, Education_ID, Experience_ID, Update_Status) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            checkUpdate = conn.prepareStatement("SELECT ID, Date_Time FROM Vacancy WHERE ID = ?");
            update = conn.prepareStatement("UPDATE Vacancy SET Header = ?, Date_Time = ?, Min_Salary = ?, Max_Salary = ?, Company_ID = ?, WorkingType_ID = ?, Shedule_ID = ?, Description = ?, Education_ID = ?, Experience_ID = ?, Update_Status = ? WHERE ID = ?");
            updateStatus = conn.prepareStatement("UPDATE Vacancy SET Update_Status = ? WHERE ID = ?");
            updateStatusAfterParsing = conn.prepareStatement("UPDATE Vacancy SET Update_Status = ?");
            //selectForDelete = conn.prepareStatement("SELECT * FROM Vacancy WHERE Update_Status = ?");
            delete = conn.prepareStatement("DELETE FROM Vacancy WHERE Update_Status = ?");
            countAll = conn.prepareStatement("SELECT COUNT(*) FROM Vacancy");

            insertContact = conn.prepareStatement("INSERT INTO Contacts(ID, Title, City_ID, Subway_ID, Street, Building, Phone) VALUES(?, ?, ?, ?, ?, ?, ?)");
            updateContact = conn.prepareStatement("UPDATE Contacts SET Title = ?, City_ID = ?, Subway_ID = ?, Street = ?, Building = ?, Phone = ? WHERE ID = ?");
            //deleteContact = conn.prepareStatement("DELETE FROM Contacts WHERE ID = ?");

            insertCity = conn.prepareStatement("INSERT INTO City(ID, Title) VALUES(?, ?)");
            insertSubway = conn.prepareStatement("INSERT INTO Subway(ID, Title) VALUES(?, ?)");
            insertCompany = conn.prepareStatement("INSERT INTO Company(ID, Title) VALUES(?, ?)");
            insertEducation = conn.prepareStatement("INSERT INTO Education(ID, Title) VALUES(?, ?)");
            insertExperience = conn.prepareStatement("INSERT INTO Experience(ID, Title) VALUES(?, ?)");
            insertShedule = conn.prepareStatement("INSERT INTO Shedule(ID, Title) VALUES(?, ?)");
            insertWorkingType = conn.prepareStatement("INSERT INTO WorkingType(ID, Title) VALUES(?, ?)");

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
            insert.setString(9, vacancy.getDescription());
            insert.setInt(10, vacancy.getEducation().getId());
            insert.setInt(11, vacancy.getExperience().getId());
            insert.setInt(12, 1);

            insertContact.setInt(1, vacancy.getId());
            insertContact.setString(2, vacancy.getContact().getName());
            insertContact.setInt(3, vacancy.getContact().getCity().getId());
            insertContact.setInt(4, vacancy.getContact().getSubway().getId());
            insertContact.setString(5, vacancy.getContact().getStreet());
            insertContact.setString(6, vacancy.getContact().getBuilding());
            insertContact.setString(7, vacancy.getContact().getPhone().get(0).getPhone());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertCity(Vacancy vacancy) {
        try {
            insertCity.setInt(1, vacancy.getContact().getCity().getId());
            insertCity.setString(2, vacancy.getContact().getCity().getTitle());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertSubway(Vacancy vacancy) {
        try {
            insertSubway.setInt(1, vacancy.getContact().getSubway().getId());
            insertSubway.setString(2, vacancy.getContact().getSubway().getTitle());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertCompany(Vacancy vacancy) {
        try {
            insertCompany.setInt(1, vacancy.getCompany().getId());
            insertCompany.setString(2, vacancy.getCompany().getTitle());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertEducation(Vacancy vacancy) {
        try {
            insertEducation.setInt(1, vacancy.getEducation().getId());
            insertEducation.setString(2, vacancy.getEducation().getTitle());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertExperience(Vacancy vacancy) {
        try {
            insertExperience.setInt(1, vacancy.getExperience().getId());
            insertExperience.setString(2, vacancy.getExperience().getTitle());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertShedule(Vacancy vacancy) {
        try {
            insertShedule.setInt(1, vacancy.getSchedule().getId());
            insertShedule.setString(2, vacancy.getSchedule().getTitle());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertWorkingType(Vacancy vacancy) {
        try {
            insertWorkingType.setInt(1, vacancy.getWorkingType().getId());
            insertWorkingType.setString(2, vacancy.getWorkingType().getTitle());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean checkUpdateDirectory(String database, int id) {
        int count = 0;
        try {
            System.out.println("SELECT * FROM " + database + " WHERE ID = " + id);
            System.out.println(stmt.executeUpdate("SELECT * FROM " + database + " WHERE ID = " + id));
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM " + database + " WHERE ID = " + id);
            while (resultSet.next()) {
                System.out.println(resultSet.getInt(1));
                count++;
            }
            if (count == 0) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // return 0 - insert new vacancy, 1 - update vacancy, 2 - skip vacancy
    public int checkUpdate(Vacancy vacancy) {
        try {
            checkUpdate.setInt(1, vacancy.getId());
            ResultSet resultSet = checkUpdate.executeQuery();
            String  dateTime = null;
            while(resultSet.next()) {
                dateTime = resultSet.getString("Date_Time");
            }
            if (dateTime != null && !vacancy.getDateTime().equals(dateTime)) {
                return 1;
            } else if (dateTime != null && vacancy.getDateTime().equals(dateTime)) {
                return 2;
            } else if (dateTime == null) return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 2;
        }
        return 2;
    }

    public void update(Vacancy vacancy) {
        try {
            update.setString(1, vacancy.getHeader());
            update.setString(2, vacancy.getDateTime());
            update.setInt(3, vacancy.getSalaryMin());
            update.setInt(4, vacancy.getSalaryMax());
            update.setInt(5, vacancy.getCompany().getId());
            update.setInt(6, vacancy.getWorkingType().getId());
            update.setInt(7, vacancy.getSchedule().getId());
            update.setString(8, vacancy.getDescription());
            update.setInt(9, vacancy.getEducation().getId());
            update.setInt(10, vacancy.getExperience().getId());
            update.setInt(11, 1);
            update.setInt(12, vacancy.getId());

            updateContact.setString(1, vacancy.getContact().getName());
            updateContact.setInt(2, vacancy.getContact().getCity().getId());
            updateContact.setInt(3, vacancy.getContact().getSubway().getId());
            updateContact.setString(4, vacancy.getContact().getStreet());
            updateContact.setString(5, vacancy.getContact().getBuilding());
            updateContact.setString(6, vacancy.getContact().getPhone().get(0).getPhone());
            updateContact.setInt(7, vacancy.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateStatus(Vacancy vacancy, int status) {
        try {
            updateStatus.setInt(1, status);
            updateStatus.setInt(2, vacancy.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int delete(int status) {
        int count = 0;
        try {
            conn.setAutoCommit(false);
            delete.setInt(1, status);
            count = delete.executeUpdate();
            /*while (resultSet.next()) {
                deleteContact.setInt(1, resultSet.getInt(1));
                delete.setInt(1, resultSet.getInt(1));
                deleteContact.addBatch();
                delete.addBatch();
                count++;
            }*/
            //delete.executeBatch();
            //deleteContact.executeBatch();
            conn.commit();
            updateStatusAfterParsing.setInt(1, status);
            updateStatusAfterParsing.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public int getCountAll() {
        int count = 0;
        try {
            ResultSet resultSet = countAll.executeQuery();
            resultSet.next();
            count = resultSet.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
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

    public PreparedStatement getUpdate() {
        return update;
    }

    public PreparedStatement getUpdateStatus() {
        return updateStatus;
    }

    public PreparedStatement getInsertContact() {
        return insertContact;
    }

    public PreparedStatement getUpdateContact() {
        return updateContact;
    }

    public Statement getStmt() {
        return stmt;
    }

    public String[] getDataBase() {
        return dataBase;
    }

    public PreparedStatement getInsertCity() {
        return insertCity;
    }

    public PreparedStatement getInsertSubway() {
        return insertSubway;
    }

    public PreparedStatement getInsertCompany() {
        return insertCompany;
    }

    public PreparedStatement getInsertEducation() {
        return insertEducation;
    }

    public PreparedStatement getInsertExperience() {
        return insertExperience;
    }

    public PreparedStatement getInsertShedule() {
        return insertShedule;
    }

    public PreparedStatement getInsertWorkingType() {
        return insertWorkingType;
    }
}