package ru.vacancies;

import ru.vacancies.database.DataBase;
import ru.vacancies.parser.*;

public class StartClass {

    public static void main(String[] args) {

        DataBase dataBase = new DataBase();
        dataBase.disconnect();
        Parser parser = new Parser();
        parser.parseVacancyIDList();
    }
}