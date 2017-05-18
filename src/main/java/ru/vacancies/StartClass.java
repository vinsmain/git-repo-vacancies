package ru.vacancies;

import ru.vacancies.database.DataBase;

public class StartClass {

    public static void main(String[] args) {

        DataBase dataBase = new DataBase();
        dataBase.disconnect();

    }
}