package ru.vacancies.parser;

import com.google.common.collect.FluentIterable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class Parser {

    public Document getPage(String url) throws IOException {
        System.setProperty("phantomjs.binary.path", "src/main/resources/phantomjs/bin/phantomjs.exe");
        WebDriver ghostDriver = new PhantomJSDriver();
        ghostDriver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        try {
            ghostDriver.get(url);
            //WebElement dynamicElement = (new WebDriverWait(ghostDriver, 10))
             //       .until(ExpectedConditions.presenceOfElementLocated(By.className("contacts_HtZXz ui list")));

            return Jsoup.parse(ghostDriver.getPageSource());
        } finally {
            ghostDriver.quit();
        }
    }

    public Document getPage1(String url) throws IOException {
        System.setProperty("phantomjs.binary.path", "src/main/resources/phantomjs/bin/phantomjs.exe");
        WebDriver ghostDriver = new PhantomJSDriver();
        ghostDriver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        try {
            ghostDriver.get(url);
            WebElement dynamicElement = (new WebDriverWait(ghostDriver, 3))
                    .until(ExpectedConditions.presenceOfElementLocated(By.className("ui container")));

            return Jsoup.parse(ghostDriver.getPageSource());
        } finally {
            ghostDriver.quit();
        }
    }


}
