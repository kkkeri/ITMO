package ru.itmo;

import ru.itmo.model.BookCreature;
import ru.itmo.model.Coordinates;
import ru.itmo.model.MagicCity;
import ru.itmo.model.Ring;
import ru.itmo.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class TestDB {
    public static void main(String[] args) {
        try {
            HibernateUtil hibernateUtil = new HibernateUtil();
            Session session = hibernateUtil.getSessionFactory().openSession();

            System.out.println("✅ База данных успешно инициализирована!");
            System.out.println("✅ Таблицы должны быть созданы автоматически");

            session.close();
            hibernateUtil.shutdown();

        } catch (Exception e) {
            System.err.println("❌ Ошибка инициализации БД: " + e.getMessage());
            e.printStackTrace();
        }
    }
}