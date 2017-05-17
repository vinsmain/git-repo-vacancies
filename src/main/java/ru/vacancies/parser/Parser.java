package ru.vacancies.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Parser {

    public Document getPage(String url) throws IOException {
        System.setProperty("phantomjs.binary.path", "src/main/resources/phantomjs/bin/phantomjs.exe");
        WebDriver ghostDriver = new PhantomJSDriver();
        ghostDriver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        try {
            ghostDriver.get(url);
            return Jsoup.parse(ghostDriver.getPageSource());
        } finally {
            ghostDriver.quit();
        }
    }
}
