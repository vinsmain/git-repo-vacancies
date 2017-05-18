package ru.vacancies;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.vacancies.parser.Parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class StartClass {


    public static void main(String[] args) {

        Parser parser = new Parser();
        final CopyOnWriteArrayList<String> urlArray = new CopyOnWriteArrayList<String>();

        try {
            Document page = parser.getPage("https://ekb.zarplata.ru/vacancy?offset=350");
            Elements vacancyURL = page.select("h2[class=ui header tiny]");
            System.out.println(vacancyURL);

            for (final Element aURL : vacancyURL) {
                new Thread(new Runnable() {
                    public void run() {
                        Document vacPage = null;
                        try {
                            vacPage = new Parser().getPage1("https://ekb.zarplata.ru" + aURL.select("a").attr("href"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String name = null;
                        Element element;
                        try {

                            if (vacPage != null) {
                                Elements elements = vacPage.select("div[class=contacts_HtZXz ui list]");
                                //System.out.println("12345 " + elements);
                                element = elements.select("div[class=item]").first();
                                //System.out.println(element);
                                name = element.text();
                                System.out.println(name);
                            }
                        } catch (NullPointerException e) {
                            System.out.println("12345 " + "https://ekb.zarplata.ru" + aURL.select("a").attr("href"));
                            e.printStackTrace();
                        }


                        //urlArray.add(name);
                    }
                }).start();
                break;

                //System.out.println("https://ekb.zarplata.ru" + aURL.select("a").attr("href"));
            }
            /*
            for (String url : urlArray) {
                System.out.println(url);
            }*/
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
