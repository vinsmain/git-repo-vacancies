package ru.vacancies;

import ru.vacancies.database.DataBase;
import ru.vacancies.parser.*;

public class StartClass {

    public static void main(String[] args) {


        Parser parser = new Parser();
        parser.parseVacancyIDList();
    }
}