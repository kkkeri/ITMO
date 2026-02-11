package ru.itmo.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class HibernateUtil {

    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        StandardServiceRegistry registry = null;
        try {
            // регистрируем конфигурацию
            registry = new StandardServiceRegistryBuilder()
                    .configure("hibernate.cfg.xml") // Файл берётся из resources
                    .build();

            // добавляем mapping-файлы вручную
            Metadata metadata = new MetadataSources(registry)
                    .addResource("mapper/BookCreature.hbm.xml")
                    .addResource("mapper/MagicCity.hbm.xml")
                    .addResource("mapper/ImportOperation.hbm.xml")
                    .getMetadataBuilder()
                    .build();

            SessionFactory sf = metadata.getSessionFactoryBuilder().build();
            System.out.println("Hibernate SessionFactory успешно создана!");

            //добавляем колонку version
            ensureVersionColumn(sf);

            return sf;

        } catch (Exception e) {
            System.err.println("Ошибка при создании Hibernate SessionFactory: " + e.getMessage());
            e.printStackTrace();

            // важно закрыть registry, если успели создать
            if (registry != null) {
                try { StandardServiceRegistryBuilder.destroy(registry); } catch (Exception ignored) {}
            }

            throw new ExceptionInInitializerError(e);
        }
    }


     //Добавляет колонку optimistic-lock version в magic_city, если её нет.

    private static void ensureVersionColumn(SessionFactory sf) {
        Transaction tx = null;
        try (Session session = sf.openSession()) {
            tx = session.beginTransaction();

            session.createNativeQuery(
                    "ALTER TABLE magic_city " +
                            "ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0"
            ).executeUpdate();

            tx.commit();
            System.out.println("Проверка/создание колонки magic_city.version выполнена.");

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            // если прав на ALTER нет - просто увидишь предупреждение и поймёшь причину
            System.err.println("WARN: не удалось создать колонку magic_city.version: " + e.getMessage());
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            System.out.println("Hibernate SessionFactory закрыта.");
        }
    }
}