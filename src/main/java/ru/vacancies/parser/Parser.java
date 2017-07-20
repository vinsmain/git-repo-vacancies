package ru.vacancies.parser;

import com.google.gson.Gson;
import ru.vacancies.database.DataBase;
import ru.vacancies.database.DataBaseInterface;
import ru.vacancies.parser.exception.TimeOutException;
import ru.vacancies.parser.lists.IDList;
import ru.vacancies.parser.lists.VacancyList;
import ru.vacancies.parser.model.ContactPhone;
import ru.vacancies.parser.model.ID;
import ru.vacancies.parser.model.Vacancy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.*;

public class Parser {
    /*
    // API количества вакансий в списке
    // https://api.zp.ru/v1/vacancies?offset=0&geo_id=994&limit=0
    //
    // API списка вакансий
    // https://api.zp.ru/v1/vacancies?offset=0&geo_id=994&limit=50
    //
    // API одной вакансии
    // https://api.zp.ru/v1/vacancies/79125333?geo_id=994
    */
    private final String API = "https://api.zp.ru/v1/vacancies";

    /*
    // ID города, для которого ищем вакансии (GEO_ID = 994 для Екатеринбурга)
    */
    private final int GEO_ID = 994;

    /*
    // Число потоков
    */
    private final int THREADS_COUNT = 30;

    /*
    // Ограничение на получаемое число вакансий одним запросом (max = 100)
    */
    private final int LIMITS_COUNT = 100;

    /*
    // Максимальное время ожидания выполнения операции (MILLISECONDS)
    */
    private final int TIMEOUT = 60000;

    /*
    // Число попыток подключения к API
    */
    private final int CONNECT_COUNT = 10;

    private Vector<ID> resultIDList = new Vector<>();
    private Vector<Vacancy> vacanciesList = new Vector<>();
    private DataBaseInterface dataBase = new DataBase();
    private int count = 0;
    private int offset = 0;
    private int countError = 0;
    private CountDownLatch cdl;
    private CountDownLatch cdlID;

    /*
    // Запуск парсинга
    */
    public void startParsing() {
        Date startTime = new Date();
        System.out.println(startTime + " Запуск парсинга");
        count = getCount();
        if (count != 0) {
            parseIDList();
            if (resultIDList.size() != 0) {
                parseVacancy(resultIDList);
                dataBase.updateDataBase(vacanciesList);
                dataBase.printReport(countError);
            }
        }
        Date finishTime = new Date();
        System.out.println(finishTime + " Парсинг завершен за " + (finishTime.getTime() - startTime.getTime()) + " мс");
    }

    /*
    // Получаем общее количество вакансий на данный момент
    */
    private int getCount() {
        int count;
        IDList list = getIDList(API + "?offset=" + offset + "&geo_id=" + GEO_ID + "&limit=0");
        if (list != null) {
            count = list.metaData.getResultSet().getCount();
        } else {
            count = 0;
        }
        System.out.println(new Date() + " Всего найдено вакансий: " + count);
        return count;
    }

    /*
    // Получаем список ID всех вакансий
    */
    private void parseIDList() {
        System.out.println(new Date() + " Начинаем формировать список вакансий, требующих обновления");
        cdlID = new CountDownLatch(count / LIMITS_COUNT + 1);
        ExecutorService serviceParsingIDList = Executors.newFixedThreadPool(THREADS_COUNT);

        while (offset <= count / LIMITS_COUNT * LIMITS_COUNT) {
            final int finalOffset = offset;
            serviceParsingIDList.submit((Runnable) () -> {
                //TODO Может быть есть более удачный способ создания ссылки, чем конкатенация из 7 фрагментов. Изучить вопрос.
                IDList tempIDList = getIDList(API + "?offset=" + finalOffset + "&geo_id=" + GEO_ID + "&limit=" + LIMITS_COUNT);
                int status;
                if (tempIDList != null) {
                    for (ID id : tempIDList.list) {
                        status = dataBase.checkUpdate(id);
                        if (status == 0 || status == 1) {
                            id.setStatus(status);
                            resultIDList.add(id);
                        } else {
                            dataBase.updateStatus(new Vacancy(id.getId()), 1);
                        }
                    }
                }
                cdlID.countDown();
            });
            offset += LIMITS_COUNT;
        }
        try {
            cdlID.await(TIMEOUT, TimeUnit.MILLISECONDS);
            serviceParsingIDList.shutdown();
            System.out.println(new Date() + " Список вакансий, требующих обновления сформирован. Всего вакансий в списке: " + resultIDList.size());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
    // Получаем список всех вакансий
    */
    private void parseVacancy(Vector<ID> resultIDList) {
        System.out.println(new Date() + " Начинаем получение данных по вакансиям");
        ExecutorService service = Executors.newFixedThreadPool(THREADS_COUNT);
        cdl = new CountDownLatch(resultIDList.size());
        for (ID id : resultIDList) {
            service.submit((Runnable) () -> {
                VacancyList vacancyList = getVacancyList(API + "/" + id.getId() + "?geo_id=" + GEO_ID);
                if (vacancyList != null) {
                    Vacancy vacancy = vacancyList.list.get(0);
                    vacancy.setDateTime(id.getPublication().getDateTime());
                    vacancy.setStatus(id.getStatus());
                    checkEmptyFields(vacancy);
                    checkSymbol(vacancy);
                    checkPhoneList(vacancy);
                    vacanciesList.add(vacancy);
                } else countError++;
                cdl.countDown();
            });
        }
        try {
            cdl.await(TIMEOUT, TimeUnit.MILLISECONDS);
            service.shutdown();
            System.out.println(new Date() + " Получение данных завершено. Всего получено вакансий: " + vacanciesList.size());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
    // Получаем список ID для указанного числа вакансий
    */
    private IDList getIDList(String url) {
        try {
            String json = readUrl(url, CONNECT_COUNT);
            return new Gson().fromJson(json, IDList.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    // Получаем список вакансий, содержащий одну вакансию по указанному ID
    */
    private VacancyList getVacancyList(String url) {
        try {
            String json = readUrl(url, CONNECT_COUNT);
            return new Gson().fromJson(json, VacancyList.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    // Получаем JSON по ссылке
    */
    private String readUrl(String urlString, int connectCount) throws Exception {
        BufferedReader reader = null;
        try {
            if (connectCount == 0) throw new TimeOutException("Превышено время ожидания ответа страницы: " + urlString + " : error 502");
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) buffer.append(chars, 0, read);
            return buffer.toString();
        } catch (FileNotFoundException e){
            System.out.println(new Date() + " Страница не найдена: " + urlString + " : error 404");
            return null;
        } catch (TimeOutException e){
            System.out.println(new Date() + " " + e.getMessage());
            return null;
        } catch (IOException e) {
            return readUrl(urlString, connectCount - 1);
        } finally{
            if (reader != null) reader.close();
        }
    }

    /*
    // Проверка на наличие отсутствующих полей
    // Если такие поля обнаруживаются, то они создаются с использованием конструктора по умолчанию
    */
    private void checkEmptyFields(Object obj) {
        Class c = obj.getClass();
        Field[] publicFields = c.getDeclaredFields();
        for (Field field : publicFields) {
            field.setAccessible(true);
            Class fieldType = field.getType();
            try {
                if (field.get(obj) == null) {
                    field.set(obj, fieldType.newInstance());
                } else if (fieldType.getSimpleName().equals("Contact")) {
                    checkEmptyFields(field.get(obj));
                } else if (fieldType.getSimpleName().equals("ArrayList<ContactPhone>")) {
                    checkEmptyFields(field.get(obj));
                }
            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    // Проверка списка телефонных номеров
    // Если список пустой - создаем телефонный номер по умолчанию
    */
    private void checkPhoneList(Vacancy vacancy) {
        if (vacancy.getContact().getPhone().isEmpty()) vacancy.getContact().getPhone().add(new ContactPhone());
    }

    /*
    // Проверка строковых полей на наличие одинарных кавычек(')
    // Если таковые присутствуют, то экранируем их еще одной одинарной кавычкой
    // Иначе наблюдаются проблемы с записью в БД
    */
    private void checkSymbol(Vacancy vacancy) {
        String title = vacancy.getHeader();
        if (title != null && title.contains("'")) vacancy.setHeader(title.replace("'", "''"));

        title = vacancy.getDescription();
        if (title != null && title.contains("'")) vacancy.setDescription(title.replace("'", "''"));

        title = vacancy.getContact().getCity().getTitle();
        if (title != null && title.contains("'")) vacancy.getContact().getCity().setTitle(title.replace("'", "''"));

        title = vacancy.getContact().getSubway().getTitle();
        if (title != null && title.contains("'")) vacancy.getContact().getSubway().setTitle(title.replace("'", "''"));

        title = vacancy.getCompany().getTitle();
        if (title != null && title.contains("'")) vacancy.getCompany().setTitle(title.replace("'", "''"));

        title = vacancy.getEducation().getTitle();
        if (title != null && title.contains("'")) vacancy.getEducation().setTitle(title.replace("'", "''"));

        title = vacancy.getExperience().getTitle();
        if (title != null && title.contains("'")) vacancy.getExperience().setTitle(title.replace("'", "''"));

        title = vacancy.getSchedule().getTitle();
        if (title != null && title.contains("'")) vacancy.getSchedule().setTitle(title.replace("'", "''"));

        title = vacancy.getWorkingType().getTitle();
        if (title != null && title.contains("'")) vacancy.getWorkingType().setTitle(title.replace("'", "''"));
    }
}