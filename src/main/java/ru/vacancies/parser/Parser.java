package ru.vacancies.parser;

import com.google.gson.Gson;
import ru.vacancies.database.DataBase;
import ru.vacancies.parser.model.ContactPhone;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class Parser {
    public static final String RETURN_NULL = "return null";
    /*
    // https://api.zp.ru/v1/vacancies?offset=0&geo_id=994&limit=0 - API количества вакансий в списке
    // https://api.zp.ru/v1/vacancies?offset=0&geo_id=994&limit=50 - API списка вакансий
    // https://api.zp.ru/v1/vacancies/79125333?geo_id=994 - API одной вакансии
    */

    private CopyOnWriteArrayList<Vacancy> vacanciesList = new CopyOnWriteArrayList<>();
    private HashMap<String, String[]> map = new HashMap<>();
    private CountDownLatch cdl;
    private CountDownLatch cdlID;

    public VacancyIDList getJSON(String url) {
        try {
            String json = readUrl(url);
            //System.out.println(json);
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
            //System.out.println(new Gson().fromJson(json, VacancyList.class));
            return new Gson().fromJson(json, VacancyList.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("ret null");
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
            System.out.println("Страница не найдена: " + urlString);
            //if (!urlString.contains("offset")) cdl.countDown();
            //else cdlID.countDown();
            return null;
        } catch (IOException e) {
            System.out.println("Ошибка открытия страницы. Повторная попытка: " + urlString);
            return readUrl(urlString);
        } finally{
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
        if (vacancy.getContact().getCity().getTitle() != null && vacancy.getContact().getCity().getTitle().contains("'")) vacancy.getContact().getCity().setTitle(vacancy.getContact().getCity().getTitle().replace("'", "''"));
        if (vacancy.getContact().getSubway().getTitle() != null && vacancy.getContact().getSubway().getTitle().contains("'")) vacancy.getContact().getSubway().setTitle(vacancy.getContact().getSubway().getTitle().replace("'", "''"));
        if (vacancy.getCompany().getTitle() != null && vacancy.getCompany().getTitle().contains("'")) vacancy.getCompany().setTitle(vacancy.getCompany().getTitle().replace("'", "''"));
        if (vacancy.getEducation().getTitle() != null && vacancy.getEducation().getTitle().contains("'")) vacancy.getEducation().setTitle(vacancy.getEducation().getTitle().replace("'", "''"));
        if (vacancy.getExperience().getTitle() != null && vacancy.getExperience().getTitle().contains("'")) vacancy.getExperience().setTitle(vacancy.getExperience().getTitle().replace("'", "''"));
        if (vacancy.getSchedule().getTitle() != null && vacancy.getSchedule().getTitle().contains("'")) vacancy.getSchedule().setTitle(vacancy.getSchedule().getTitle().replace("'", "''"));
        if (vacancy.getWorkingType().getTitle() != null && vacancy.getWorkingType().getTitle().contains("'")) vacancy.getWorkingType().setTitle(vacancy.getWorkingType().getTitle().replace("'", "''"));
    }

    public void checkSymbol(Vacancy vacancy) {
        if (vacancy.getContact().getCity().getTitle() != null && vacancy.getContact().getCity().getTitle().contains("'")) vacancy.getContact().getCity().setTitle(vacancy.getContact().getCity().getTitle().replace("'", "''"));
        if (vacancy.getContact().getSubway().getTitle() != null && vacancy.getContact().getSubway().getTitle().contains("'")) vacancy.getContact().getSubway().setTitle(vacancy.getContact().getSubway().getTitle().replace("'", "''"));
        if (vacancy.getCompany().getTitle() != null && vacancy.getCompany().getTitle().contains("'")) vacancy.getCompany().setTitle(vacancy.getCompany().getTitle().replace("'", "''"));
        if (vacancy.getEducation().getTitle() != null && vacancy.getEducation().getTitle().contains("'")) vacancy.getEducation().setTitle(vacancy.getEducation().getTitle().replace("'", "''"));
        if (vacancy.getExperience().getTitle() != null && vacancy.getExperience().getTitle().contains("'")) vacancy.getExperience().setTitle(vacancy.getExperience().getTitle().replace("'", "''"));
        if (vacancy.getSchedule().getTitle() != null && vacancy.getSchedule().getTitle().contains("'")) vacancy.getSchedule().setTitle(vacancy.getSchedule().getTitle().replace("'", "''"));
        if (vacancy.getWorkingType().getTitle() != null && vacancy.getWorkingType().getTitle().contains("'")) vacancy.getWorkingType().setTitle(vacancy.getWorkingType().getTitle().replace("'", "''"));
    }

    public void parseVacancy(VacancyIDList vacancyIDList) {
        ExecutorService service = Executors.newFixedThreadPool(10);
        cdl = new CountDownLatch(vacancyIDList.list.size());
        for (VacancyID vacancyID : vacancyIDList.list) {
            service.submit((Runnable) () -> {
                VacancyList vacancyList = getVacancy("https://api.zp.ru/v1/vacancies/" + vacancyID.getId() + "?geo_id=994");
                if (vacancyList != null) {
                    Vacancy vacancy = vacancyList.list.get(0);
                    //System.out.println(vacancy.getId());
                    checkVacancy(vacancy);
                    checkPhoneList(vacancy);
                    vacanciesList.add(vacancy);
                }
                cdl.countDown();
            });
        }
        try {
            cdl.await();
            service.shutdown();
            System.out.println("123");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void parseVacancyIDList() {
        int count = getJSON("https://api.zp.ru/v1/vacancies?offset=0&geo_id=994&limit=0").metaData.getResultSet().getCount() / 100 * 100;
        System.out.println(count);
        cdlID = new CountDownLatch(count / 100 + 1);
        ExecutorService serviceParsingID = Executors.newFixedThreadPool(10);
        ExecutorService serviceParsingVacancies = Executors.newFixedThreadPool(10);
        int offset = 0;
        do {
            final int finalOffset = offset;
            serviceParsingID.submit((Runnable) () -> {
                VacancyIDList array = getJSON("https://api.zp.ru/v1/vacancies?offset=" + finalOffset + "&geo_id=994&limit=100");
                serviceParsingVacancies.submit((Runnable) () -> {
                    parseVacancy(array);
                    cdlID.countDown();
                });
            });
            offset += 100;
        } while (offset <= count);
        try {
            cdlID.await(60000, TimeUnit.MILLISECONDS);
            serviceParsingID.shutdown();
            serviceParsingVacancies.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Парсинг завершен. Всего вакансий: " + vacanciesList.size());
        updateDataBase(vacanciesList);
    }

    public void updateDataBase(CopyOnWriteArrayList<Vacancy> vacanciesList) {
        DataBase dataBase = new DataBase();
        int countAll = 0;
        int countAdd = 0;
        int countUpdate = 0;
        int countDelete = 0;
        Vacancy vac = null;
        try {
            int i = 0;
            dataBase.getConn().setAutoCommit(false);
            for (Vacancy vacancy : vacanciesList) {
                vac = vacancy;
                int status = dataBase.checkUpdate(vacancy);
                if (status == 0) {
                    dataBase.insert(vacancy);
                    dataBase.getInsert().addBatch();
                    dataBase.getInsertContact().addBatch();
                    countAdd++;
                    map.put("City", new String[]{String.valueOf(vacancy.getContact().getCity().getId()), vacancy.getContact().getCity().getTitle()});
                    map.put("Subway", new String[]{String.valueOf(vacancy.getContact().getSubway().getId()), vacancy.getContact().getSubway().getTitle()});
                    map.put("Company", new String[]{String.valueOf(vacancy.getCompany().getId()), vacancy.getCompany().getTitle()});
                    map.put("Education", new String[]{String.valueOf(vacancy.getEducation().getId()), vacancy.getEducation().getTitle()});
                    map.put("Experience", new String[]{String.valueOf(vacancy.getExperience().getId()), vacancy.getExperience().getTitle()});
                    map.put("Shedule", new String[]{String.valueOf(vacancy.getSchedule().getId()), vacancy.getSchedule().getTitle()});
                    map.put("WorkingType", new String[]{String.valueOf(vacancy.getWorkingType().getId()), vacancy.getWorkingType().getTitle()});
                    for(Map.Entry<String, String[]> entry : map.entrySet()) {
                        String key = entry.getKey();
                        String[] value = entry.getValue();
                        dataBase.insertDirectory(key, value);
                    }
                } else if (status == 1) {
                    dataBase.update(vacancy);
                    dataBase.getUpdate().addBatch();
                    dataBase.getUpdateContact().addBatch();
                    countUpdate++;
                } else if (status == 2) {
                    dataBase.updateStatus(vacancy, 1);
                    dataBase.getUpdateStatus().addBatch();
                }
                i++;
                if (i % 1000 == 0 || i == vacanciesList.size()) {
                    System.out.println(i);
                    dataBase.getStmt().executeBatch();
                    dataBase.getInsert().executeBatch();
                    dataBase.getInsertContact().executeBatch();
                    dataBase.getUpdate().executeBatch();
                    dataBase.getUpdateContact().executeBatch();
                    dataBase.getUpdateStatus().executeBatch();
                    dataBase.getConn().commit();
                }
            }
            dataBase.getConn().setAutoCommit(true);
            countDelete = dataBase.delete(0);
            countAll = dataBase.getCountAll();
        } catch (SQLException e) {
            System.out.println(vac != null ? vac.getId() : 0);
            e.printStackTrace();
        }
        dataBase.disconnect();
        System.out.println("Всего вакансий в базе: " + countAll);
        System.out.println("Добавлено: " + countAdd);
        System.out.println("Обновлено: " + countUpdate);
        System.out.println("Удалено: " + countDelete);
    }

    public void printVacancyListInfo(CopyOnWriteArrayList<Vacancy> vacanciesList) {
        int i = 1;
        for (Vacancy vacancy : vacanciesList) {
            System.out.println(i + " " + vacancy.getId() + " " + vacancy.getHeader() + " " + vacancy.getEducation().getId() + vacancy.getEducation().getTitle() + " " + vacancy.getExperience().getId() + vacancy.getExperience().getTitle() +
                    " " + vacancy.getWorkingType().getId() + vacancy.getWorkingType().getTitle() + " " + vacancy.getSchedule().getId() + vacancy.getSchedule().getTitle() + " " + vacancy.getContact().getName() +
                    " " + vacancy.getContact().getPhone().get(0).getPhone() + " " + vacancy.getContact().getCity().getTitle() + " " + vacancy.getContact().getSubway().getTitle() + " " + vacancy.getContact().getStreet() +
                    " " + vacancy.getContact().getBuilding() + " " + vacancy.getSalaryMin() + " " + vacancy.getSalaryMax() + " " + vacancy.getDateTime() + " " + vacancy.getCompany().getTitle());
            i++;
        }
    }
}