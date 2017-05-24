package ru.vacancies;

import ru.vacancies.parser.*;

import java.io.FileNotFoundException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StartClass {

    private static Parser parser = new Parser();
    private static CopyOnWriteArrayList<Vacancy> vacancies = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        ExecutorService service = Executors.newFixedThreadPool(50);
        int offset = 0;
        while (offset <= 6000) {
            System.out.println(offset);
            VacancyIDList array = parser.getJSON("https://api.zp.ru/v1/vacancies?offset=" + offset + "&geo_id=994&limit=100");

            service.submit(new Runnable() {
                public void run() {
                    parse(array);
                    //cdl.countDown();
                }
            });
            offset += 100;

        }
        service.shutdown();
        System.out.println("Array size: " + vacancies.size());
    }

    public static void parse(VacancyIDList vacancyIDList) {
        int i = 1;
        ExecutorService service = Executors.newFixedThreadPool(50);
        CountDownLatch cdl = new CountDownLatch(100);
        for (VacancyID vacancyID : vacancyIDList.list) {
            service.submit(new Runnable() {
                public void run() {
                    Vacancy vacancy = (parser.getVacancy("https://api.zp.ru/v1/vacancies/" + vacancyID.getId() + "?geo_id=994").list.get(0));
                    parser.checkVacancy(vacancy);
                    parser.checkPhoneList(vacancy);
                    vacancies.add(vacancy);
                    cdl.countDown();
                }
            });
        }
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        service.shutdown();

/*
        for (Vacancy vacancy : vacancies) {
            //parser.checkVacancy(vacancy);
            //parser.checkPhoneList(vacancy);
            System.out.println(i + " " + vacancy.getId() + " " + vacancy.getHeader() + " " + vacancy.getEducation().getId() + vacancy.getEducation().getTitle() + " " + vacancy.getExperience().getId() + vacancy.getExperience().getTitle() +
                   " " + vacancy.getWorkingType().getId() + vacancy.getWorkingType().getTitle() + " " + vacancy.getSchedule().getId() + vacancy.getSchedule().getTitle() + " " + vacancy.getContact().getName() +
                    " " + vacancy.getContact().getPhone().get(0).getPhone() + " " + vacancy.getContact().getCity().getTitle() + " " + vacancy.getContact().getSubway().getTitle() + " " + vacancy.getContact().getStreet() +
                    " " + vacancy.getContact().getBuilding() + " " + vacancy.getSalaryMin() + " " + vacancy.getSalaryMax());
            i++;
        }
*/
    }
}
