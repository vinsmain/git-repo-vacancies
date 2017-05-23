package ru.vacancies;

import ru.vacancies.parser.Parser;
import ru.vacancies.parser.VacancyList;
import ru.vacancies.parser.Vacancy;

public class StartClass {


    public static void main(String[] args) {

        Parser parser = new Parser();
        VacancyList array = parser.getJSON("https://api.zp.ru/v1/vacancies?offset=0&geo_id=994&limit=10");
        int i = 1;
        for (Vacancy vacancy : array.list) {
            parser.checkVacancy(vacancy);
            System.out.println(parser.parseContactPhone("https://api.zp.ru/v1/vacancies/" + vacancy.getId() + "?geo_id=994").getPhone());
            vacancy.getContact().setPhone(parser.parseContactPhone("https://api.zp.ru/v1/vacancies/" + vacancy.getId() + "?geo_id=994"));
            //System.out.println(i + " " + vacancy.getId() + " " + vacancy.getHeader() + " " + vacancy.getEducation().getId() + vacancy.getEducation().getTitle() + " " + vacancy.getExperience().getId() + vacancy.getExperience().getTitle() +
             //       " " + vacancy.getWorkingType().getId() + vacancy.getWorkingType().getTitle() + " " + vacancy.getSchedule().getId() + vacancy.getSchedule().getTitle() + " " + vacancy.getContact().getName() + " " + vacancy.getContact().getPhone().getPhone() + " " + vacancy.getContact().getCity().getTitle() + " " + vacancy.getContact().getSubway().getTitle() + " " + vacancy.getContact().getStreet() + " " + vacancy.getContact().getBuilding() + " " + vacancy.getSalaryMin() + " " + vacancy.getSalaryMax());
            i++;
        }

    }
}
