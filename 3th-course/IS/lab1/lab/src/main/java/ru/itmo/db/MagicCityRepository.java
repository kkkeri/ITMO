package ru.itmo.db;

import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.itmo.model.MagicCity;
import ru.itmo.model.util.BookCreatureType;
import ru.itmo.util.HibernateUtil;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MagicCityRepository {

    /**
     * Сохранение нового города.
     * Возвращает сгенерированный ID.
     */
    public Long save(MagicCity city) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(city);
            tx.commit();
            return city.getId();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Ошибка при сохранении MagicCity", e);
        }
    }

    /**
     * Поиск города по id.
     */
    public Optional<MagicCity> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            MagicCity city = session.get(MagicCity.class, id);
            return Optional.ofNullable(city);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при поиске MagicCity по id=" + id, e);
        }
    }

    /**
     * Список всех городов (для выпадающих списков, таблиц и т.п.).
     */
    public List<MagicCity> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM MagicCity", MagicCity.class)
                    .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении списка всех MagicCity", e);
        }
    }

    /**
     * Обновление города.
     */
    public void update(MagicCity city) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(city);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Ошибка при обновлении MagicCity id=" + city.getId(), e);
        }
    }

    /**
     * Удаление города по id.
     * Логику перепривязки существ к другому городу
     * будем делать в сервисе поверх этого репозитория.
     */
    public void delete(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            MagicCity city = session.get(MagicCity.class, id);
            if (city != null) {
                session.remove(city);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Ошибка при удалении MagicCity id=" + id, e);
        }
    }

    /**
     * Найти города по типу правителя (governor).
     * Пригодится для операции "уничтожить города эльфов".
     */
    public List<MagicCity> findByGovernor(BookCreatureType governorType) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM MagicCity c WHERE c.governor = :gov",
                            MagicCity.class)
                    .setParameter("gov", governorType)
                    .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при поиске городов по правителю " + governorType, e);
        }
    }

    /**
     * Удалить пачку городов (удобно для "уничтожить города эльфов").
     */
    public void deleteAll(List<MagicCity> cities) {
        if (cities == null || cities.isEmpty()) return;

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            for (MagicCity city : cities) {
                MagicCity managed = session.get(MagicCity.class, city.getId());
                if (managed != null) {
                    session.remove(managed);
                }
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Ошибка при пакетном удалении MagicCity", e);
        }
    }
}