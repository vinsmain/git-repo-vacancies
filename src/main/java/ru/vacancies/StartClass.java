package ru.vacancies;

import ru.vacancies.parser.Parser;
import ru.vacancies.parser.VacList;
import ru.vacancies.parser.Vacancy;

public class StartClass {


    public static void main(String[] args) {

        Parser parser = new Parser();
        VacList array = parser.getJSON("https://api.zp.ru/v1/vacancies?offset=0&geo_id=994&limit=100");
        int i = 1;
        for (Vacancy vacancy : array.list) {
            System.out.println(i + " " + vacancy.getID() + " " + vacancy.getHeader());
            i++;
        }

    }
}
