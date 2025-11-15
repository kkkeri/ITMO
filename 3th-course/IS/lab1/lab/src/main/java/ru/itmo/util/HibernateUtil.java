package ru.itmo.util;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;


public class HibernateUtil {

    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Регистрируем конфигурацию Hibernate (hibernate.cfg.xml)
            StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                    .configure("hibernate.cfg.xml") // Файл берётся из resources
                    .build();

            // Добавляем mapping-файлы вручную (опционально, но надёжно)
            Metadata metadata = new MetadataSources(registry)
                    .addResource("mapping/BookCreature.hbm.xml")
                    .addResource("mapping/MagicCity.hbm.xml")
                    .getMetadataBuilder()
                    .build();

            System.out.println("Hibernate SessionFactory успешно создана!");
            return metadata.getSessionFactoryBuilder().build();

        } catch (Exception e) {
            System.err.println("Ошибка при создании Hibernate SessionFactory: " + e.getMessage());
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
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