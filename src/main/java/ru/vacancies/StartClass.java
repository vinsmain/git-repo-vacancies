package ru.vacancies;

import ru.vacancies.parser.Parser;

public class StartClass {


    public static void main(String[] args) {

        Parser parser = new Parser();
        String json = parser.getJSON("https://api.zp.ru/v1/vacancies?offset=0&geo_id=994&limit=50");
        System.out.println(json);
    }
}
