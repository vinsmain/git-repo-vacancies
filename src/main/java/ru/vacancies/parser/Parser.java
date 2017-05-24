package ru.vacancies.parser;

import com.google.gson.Gson;
import ru.vacancies.parser.model.ContactPhone;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;

public class Parser {
    /*
    //https://api.zp.ru/v1/vacancies?offset=0&geo_id=994&limit=50 - API списка вакансий
    //https://api.zp.ru/v1/vacancies/79125333?geo_id=994 - API одной вакансии
    */

    public VacancyIDList getJSON(String url) {
        try {
            String json = readUrl(url);
            System.out.println(json);
            return new Gson().fromJson(json, VacancyIDList.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public VacancyList getVacancy(String url) {
        try {
            String json = readUrl(url);
            //System.out.println(json);
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
        } catch (FileNotFoundException e){
            return null;
        } finally {
            if (reader != null) reader.close();
        }
    }

    public void checkVacancy(Object obj) {
        Class c = obj.getClass();
        Field[] publicFields = c.getDeclaredFields();
        for (Field field : publicFields) {
            field.setAccessible(true);
            Class fieldType = field.getType();
            try {
                if (field.get(obj) == null) {
                    field.set(obj, fieldType.newInstance());
                } else if (fieldType.getSimpleName().equals("Contact")) {
                    checkVacancy(field.get(obj));
                } else if (fieldType.getSimpleName().equals("ArrayList<ContactPhone>")) {
                    checkVacancy(field.get(obj));
                }
            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    public void checkPhoneList(Vacancy vacancy) {
        if (vacancy.getContact().getPhone().isEmpty()) vacancy.getContact().getPhone().add(new ContactPhone());
    }
}
