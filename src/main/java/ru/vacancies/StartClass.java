package ru.vacancies;

import ru.vacancies.parser.Parser;
import ru.vacancies.parser.VacancyList;
import ru.vacancies.parser.Vacancy;

public class StartClass {


    public static void main(String[] args) {

        Parser parser = new Parser();
        VacancyList array = parser.getJSON("https://api.zp.ru/v1/vacancies?offset=0&geo_id=994&limit=100");
        int i = 1;
        for (Vacancy vacancy : array.list) {
            parser.checkVacancy(vacancy);
            System.out.println(i + " " + vacancy.getId() + " " + vacancy.getHeader() + " " + vacancy.getEducation().getId() + vacancy.getEducation().getTitle() + " " + vacancy.getExperience().getId() + vacancy.getExperience().getTitle() +
                    " " + vacancy.getWorkingType().getId() + vacancy.getWorkingType().getTitle() + " " + vacancy.getShedule().getId() + vacancy.getShedule().getTitle() + " " + vacancy.getDescription());
            i++;
        }

    }
}
