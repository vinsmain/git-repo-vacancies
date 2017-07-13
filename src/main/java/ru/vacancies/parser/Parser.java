package ru.vacancies.parser;

import com.google.gson.Gson;
import javafx.collections.ObservableList;
import ru.vacancies.database.DataBase;
import ru.vacancies.parser.model.ContactPhone;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class Parser {
    public static final String RETURN_NULL = "return null";
    /*
    // API количества вакансий в списке
    */
    private final String COUNT_API = "https://api.zp.ru/v1/vacancies?offset=0&geo_id=994&limit=0";

    /*
    // API списка вакансий
    */
    private final String VAC_LIST_API = "https://api.zp.ru/v1/vacancies?offset=0&geo_id=994&limit=50";

    /*
    // API одной вакансии
    */
    private final String VAC_API = "https://api.zp.ru/v1/vacancies/79125333?geo_id=994";


    private CopyOnWriteArrayList<ID> resultIDList = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Vacancy> vacanciesList = new CopyOnWriteArrayList<>();
    private HashMap<String, String[]> map = new HashMap<>();
    private int count;
    private int offset = 0;
    private CountDownLatch cdl;
    private CountDownLatch cdlID;
    private DataBase dataBase;

    /*
    // Запуск парсинга
    */
    public void startParsing() {
        getCount();
        parseIDList();
        parseVacancy(resultIDList);
        updateDataBase(vacanciesList);
    }

    /*
    // Получаем общее количество вакансий на данный момент
    */
    private void getCount() {
        count = getIDList(COUNT_API).metaData.getResultSet().getCount();
    }

    /*
    // Получаем список ID всех вакансий
    */
    public void parseIDList() {
        System.out.println(count);
        cdlID = new CountDownLatch(count / 100 + 1);
        ExecutorService serviceParsingIDList = Executors.newFixedThreadPool(20);

        do {
            final int finalOffset = offset;
            serviceParsingIDList.submit((Runnable) () -> {
                IDList tempIDList = getIDList("https://api.zp.ru/v1/vacancies?offset=" + finalOffset + "&geo_id=994&limit=100");
                resultIDList.addAll(tempIDList.list);
                cdlID.countDown();
            });
            offset += 100;
        } while (offset <= count / 100 * 100);

        try {
            cdlID.await(30000, TimeUnit.MILLISECONDS);
            serviceParsingIDList.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Парсинг завершен. Всего вакансий: " + resultIDList.size());
        /*for (ID id: resultIDList) {
            System.out.println(id.getId());
        }*/
    }

    /*
    // Получаем список всех вакансий
    */
    public void parseVacancy(CopyOnWriteArrayList<ID> resultIDList) {
        ExecutorService service = Executors.newFixedThreadPool(50);
        cdl = new CountDownLatch(resultIDList.size());
        for (ID id : resultIDList) {
            service.submit((Runnable) () -> {
                VacancyList vacancyList = getVacancyList("https://api.zp.ru/v1/vacancies/" + id.getId() + "?geo_id=994");
                if (vacancyList != null) {
                    Vacancy vacancy = vacancyList.list.get(0);

                    checkVacancy(vacancy);
                    checkPhoneList(vacancy);
                    vacanciesList.add(vacancy);
                } else System.out.println(123);
                cdl.countDown();
            });
        }
        try {
            cdl.await(60000, TimeUnit.MILLISECONDS);
            service.shutdown();
            System.out.println("END " + vacanciesList.size());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
    // Получаем список ID для указанного числа вакансий
    */
    public IDList getIDList(String url) {
        try {
            String json = readUrl(url);
            return new Gson().fromJson(json, IDList.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    // Получаем список вакансий, содержащий одну вакансию по указанному ID
    */
    public VacancyList getVacancyList(String url) {
        try {
            String json = readUrl(url);
            return new Gson().fromJson(json, VacancyList.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    // Получаем JSON по ссылке
    */
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
            System.out.println("Страница не найдена: " + urlString + " : 404");
            return null;
        } catch (IOException e) {
            System.out.println("Ошибка открытия страницы. Повторная попытка: " + urlString);
            return readUrl(urlString);
        } finally{
            if (reader != null) reader.close();
        }
    }

    /*
    // Проверка на наличие отсутствующих полей
    // Если такие поля обнаруживаются, то они создаются с использованием конструктора по умолчанию
    */
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





    public void updateDataBase(CopyOnWriteArrayList<Vacancy> vacanciesList) {
        dataBase = new DataBase();
        int countAll = 0;
        int countAdd = 0;
        int countUpdate = 0;
        int countDelete = 0;
        int countSkip = 0;
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
                    countAdd++;
                } else if (status == 1) {
                    dataBase.update(vacancy);
                    dataBase.getUpdate().addBatch();
                    dataBase.getUpdateContact().addBatch();
                    countUpdate++;
                } else if (status == 2) {
                    dataBase.updateStatus(vacancy, 1);
                    dataBase.getUpdateStatus().addBatch();
                    countSkip++;
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
        System.out.println("Без изменений: " + countSkip);
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