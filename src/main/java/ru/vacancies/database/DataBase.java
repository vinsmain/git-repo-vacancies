package ru.vacancies.database;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteOpenMode;
import ru.vacancies.parser.model.ID;
import ru.vacancies.parser.model.Vacancy;
import java.sql.*;
import java.sql.ResultSet;
import java.util.*;
import java.util.Date;

public class DataBase implements DataBaseInterface {

    /*
    // Update_Status
    // 0 - НЕ обновляем вакансию
    // 1 - обновляем вакансию
    */

    private Connection conn;
    private Statement stmt;
    private PreparedStatement insert;
    private PreparedStatement checkUpdate;
    private PreparedStatement update;
    private PreparedStatement updateStatus;
    private PreparedStatement updateStatusAfterParsing;
    private PreparedStatement delete;
    private PreparedStatement countAll;
    private PreparedStatement insertContact;
    private PreparedStatement updateContact;
    private int count = 0;
    private int countAdd = 0;
    private int countUpdate = 0;
    private int countDelete = 0;
    private int countSkip = 0;
    private int successCount = 0;
    private int failDBWrite = 0;
    private int notAvailable = 0;

    public DataBase() {
        connect();
    }

    /*
    // Подключение к БД
    // Создание необходимых таблиц, если таковые отсутствуют
    // Инициализация основных запросов
    */
    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
            config.setOpenMode(SQLiteOpenMode.FULLMUTEX);
            config.setJournalMode(SQLiteConfig.JournalMode.WAL);
            conn = DriverManager.getConnection("jdbc:sqlite:src/main/resources/database/VacanciesDB.db", config.toProperties());
            stmt = conn.createStatement();
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
            delete = conn.prepareStatement("DELETE FROM Vacancy WHERE Update_Status = ?");
            countAll = conn.prepareStatement("SELECT COUNT(*) FROM Vacancy");

            insertContact = conn.prepareStatement("INSERT INTO Contacts(ID, Title, City_ID, Subway_ID, Street, Building, Phone) VALUES(?, ?, ?, ?, ?, ?, ?)");
            updateContact = conn.prepareStatement("UPDATE Contacts SET Title = ?, City_ID = ?, Subway_ID = ?, Street = ?, Building = ?, Phone = ? WHERE ID = ?");
        } catch (Exception e) {
            System.out.println("Ошибка инициализации JDBC драйвера");
            e.printStackTrace();
        }
    }

    /*
    // Метод, запускающий обновление данных в базе
    */
    public void updateDataBase(Vector<Vacancy> vacanciesList) {
        System.out.println(new java.util.Date() + " Начинаем обновление базы данных");
        Vacancy vac = null;
        try {
            conn.setAutoCommit(false);
            countSkip = updateStatus.executeBatch().length;
            conn.commit();
            int i = 0;
            for (Vacancy vacancy : vacanciesList) {
                vac = vacancy;
                int status = vacancy.getStatus();
                if (status == 0) {
                    insertVacancy(vacancy);
                    insertContact(vacancy);
                    insert.addBatch();
                    insertContact.addBatch();
                    insertSecondaryFieldsInDB(vacancy);
                    countAdd++;
                } else if (status == 1) {
                    updateVacancy(vacancy);
                    updateContact(vacancy);
                    update.addBatch();
                    updateContact.addBatch();
                    countUpdate++;
                }
                i++;
                if (i % 1000 == 0 || i == vacanciesList.size()) {
                    System.out.println(new java.util.Date() + " Обработано записей: " + i);
                    try {
                        stmt.executeBatch();
                        insert.executeBatch();
                        insertContact.executeBatch();
                        update.executeBatch();
                        updateContact.executeBatch();
                        updateStatus.executeBatch();
                    } catch (BatchUpdateException e) {
                        int[] updateCounts = e.getUpdateCounts();
                        for (int j = 0; j < updateCounts.length; j++) {
                            if (updateCounts[j] >= 0) {
                                successCount++;
                            } else if (updateCounts[j] == Statement.SUCCESS_NO_INFO) {
                                notAvailable++;
                            } else if (updateCounts[j] == Statement.EXECUTE_FAILED) {
                                failDBWrite++;
                                //Код ошибки: i - партия записей из 1000 штук (2000: запись 1001 - 2000 вакансии), j - запись конкретной вакансии из партии
                                System.out.println(new Date() + " Ошибка записи в БД. Код " + i + "-" + j);
                            }
                        }
                    }
                    conn.commit();
                }
            }
            countDelete = delete(0);
            updateStatusAfterParsing(0);
            count = getCountAll();
        } catch (SQLException e) {
            System.out.println("Ошибка записи в БД: ID " + (vac != null ? vac.getId() : 0));
            e.printStackTrace();
        } finally {
            try {
                conn.commit();
                disconnect();
                System.out.println(new java.util.Date() + " База обновлена");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    // Добавление новых данных в таблицу "Vacancy"
    */
    private void insertVacancy(Vacancy vacancy) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
    // Добавление новых данных в таблицу "Contacts"
    */
    private void insertContact(Vacancy vacancy) {
        try {
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

    /*
    // Добавление новых данных в другие таблицы
    */
    private void insertSecondaryFieldsInDB(Vacancy vacancy) {
        insertField("City", vacancy.getContact().getCity().getId(), vacancy.getContact().getCity().getTitle());
        insertField("Subway", vacancy.getContact().getSubway().getId(), vacancy.getContact().getSubway().getTitle());
        insertField("Company", vacancy.getCompany().getId(), vacancy.getCompany().getTitle());
        insertField("Education", vacancy.getEducation().getId(), vacancy.getEducation().getTitle());
        insertField("Experience", vacancy.getExperience().getId(), vacancy.getExperience().getTitle());
        insertField("Shedule", vacancy.getSchedule().getId(), vacancy.getSchedule().getTitle());
        insertField("WorkingType", vacancy.getWorkingType().getId(), vacancy.getWorkingType().getTitle());
    }

    /*
    // Добавление новых данных в другие таблицы
    */
    private void insertField(String database, int id, String title) {
            try {
                stmt.addBatch("INSERT OR IGNORE INTO " + database + " (ID, Title) VALUES(" + id + ", '" + title + "')");
                stmt.addBatch("UPDATE " + database + " SET Title = '" + title + "' WHERE ID = " + id);
            } catch (SQLException e) {
                System.out.println("INSERT INTO " + database + " (ID, Title) VALUES(" + id + ", '" + title + "')");
                e.printStackTrace();
            }
    }

    /*
    // Проверка, требуется ли обновление вакансии из БД
    // return 0 - insert new vacancy, 1 - update vacancy, 2 - skip vacancy
    */
    public synchronized int checkUpdate(ID vacancy) {
        try {
            checkUpdate.setInt(1, vacancy.getId());
            ResultSet resultSet = checkUpdate.executeQuery();
            String  dateTime = null;
            while(resultSet.next()) {
                dateTime = resultSet.getString("Date_Time");
            }
            if (dateTime == null) return 0;
            else if (!vacancy.getPublication().getDateTime().equals(dateTime)) return 1;
            else if (vacancy.getPublication().getDateTime().equals(dateTime)) return 2;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 2;
    }

    /*
    // Обновление данных в таблице "Vacancy"
    */
    private void updateVacancy(Vacancy vacancy) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
    // Обновление данных в таблице "Contacts"
    */
    private void updateContact(Vacancy vacancy) {
        try {
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

    /*
    // Обновление статуса (поле "Update_Status" в БД)
    */
    public synchronized void updateStatus(Vacancy vacancy, int status) {
        try {
            updateStatus.setInt(1, status);
            updateStatus.setInt(2, vacancy.getId());
            updateStatus.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
    // Удаление неактуальных вакансий из БД (если Update_Status = 0)
    */
    private int delete(int status) {
        int count = 0;
        try {
            delete.setInt(1, status);
            count = delete.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /*
    // Обновление статуса у актуальных вакансий в БД (установить Update_Status = 0)
    */
    private void updateStatusAfterParsing(int status) {
        try {
            updateStatusAfterParsing.setInt(1, status);
            updateStatusAfterParsing.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
    // Получить общее число вакансий в БД
    */
    private int getCountAll() {
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

    /*
    // Отключиться от БД
    */
    private void disconnect() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
    // Печать отчета по обновлению БД
    */
    public void printReport(int countError) {
        System.out.println("-----------------------------------------------");
        System.out.println("Всего вакансий в базе: " + count);
        System.out.println("Добавлено: " + countAdd);
        System.out.println("Обновлено: " + countUpdate);
        System.out.println("Удалено: " + countDelete);
        System.out.println("Без изменений: " + countSkip);
        System.out.println("Ошибки парсинга: " + countError);
        System.out.println("Ошибки записи данных в базу: " + failDBWrite);
        System.out.println("-----------------------------------------------");
    }
}