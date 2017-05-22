package ru.vacancies.parser;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;

public class Parser {
    /*
    //https://api.zp.ru/v1/vacancies?offset=0&geo_id=994&limit=50 API НЕ УДАЛЯТЬ!!!
    */

    public VacancyList getJSON(String url) {
        try {
            String json = readUrl(url);
            System.out.println(json);
            return new Gson().fromJson(json, VacancyList.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) buffer.append(chars, 0, read);
            return buffer.toString();
        } finally {
            if (reader != null) reader.close();
        }
    }

    public void checkVacancy(Vacancy vacancy) {
        Class c = vacancy.getClass();
        Field[] publicFields = c.getDeclaredFields();
        for (Field field : publicFields) {
            field.setAccessible(true);
            Class fieldType = field.getType();
            try {
                if (field.get(vacancy) == null) {
                    field.set(vacancy, fieldType.newInstance());
                }
            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
    }
}
