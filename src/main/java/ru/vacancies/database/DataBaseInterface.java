package ru.vacancies.database;

import ru.vacancies.parser.model.ID;
import ru.vacancies.parser.model.Vacancy;

import java.util.Vector;

public interface DataBaseInterface {

    /*
    // Метод, запускающий обновление данных в базе
    // Vector<Vacancy> vacanciesList - коллекция, содержащая вакансии, требующие создания или обновления (но не удаления)
    */
    void updateDataBase(Vector<Vacancy> vacanciesList);

    /*
    // Проверка, требуется ли обновление вакансии из БД
    // return 0 - insert new vacancy, 1 - update vacancy, 2 - skip vacancy
    // ID vacancy - содержит краткие сведения о вакансии
    */
    int checkUpdate(ID vacancy);

    /*
    // Обновление статуса (поле "Update_Status" в БД)
    // Vacancy vacancy - вакансия, для которой обновляем статус
    // int status - статус, который будет установлен
    */
    void updateStatus(Vacancy vacancy, int status);

    /*
    // Печать отчета по обновлению БД
    // int countError - количество ошибок при парсинге (404 и 502)
    */
    void printReport(int countError);
}
