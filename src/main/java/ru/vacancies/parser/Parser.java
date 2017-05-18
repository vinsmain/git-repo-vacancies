package ru.vacancies.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.net.URL;

public class Parser {
    /*
    //https://api.zp.ru/v1/vacancies?offset=0&geo_id=994&limit=50 API НЕ УДАЛЯТЬ!!!
    */

    public String getJSON(String url) {
        try {
            return Jsoup.parse(new URL(url), 3000).text();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
