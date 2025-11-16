package ru.itmo.db;

import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.itmo.model.BookCreature;
import ru.itmo.util.HibernateUtil;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class BookCreatureRepository {

    /**
     * Сохранение нового существа.
     * Возвращает сгенерированный ID.
     */
    public Long save(BookCreature creature) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(creature);   // ID сгенерируется БД
            tx.commit();
            return creature.getId();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Ошибка при сохранении BookCreature", e);
        }
    }

    /**
     * Поиск по ID.
     */
    public Optional<BookCreature> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            BookCreature creature = session.get(BookCreature.class, id);
            return Optional.ofNullable(creature);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при поиске BookCreature по id=" + id, e);
        }
    }

    /**
     * Получить всех существ (без пагинации, просто список).
     * Потом можно будет сделать вариант с page/pageSize.
     */
    public List<BookCreature> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM BookCreature", BookCreature.class)
                    .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении списка всех BookCreature", e);
        }
    }

    /**
     * Обновление существующего объекта.
     */
    public void update(BookCreature creature) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(creature);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Ошибка при обновлении BookCreature с id=" + creature.getId(), e);
        }
    }

    /**
     * Удаление существа по ID.
     * Логику перепривязки связанных объектов (если понадобится)
     * будем делать в сервисе, поверх этого репозитория.
     */
    public void delete(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            BookCreature creature = session.get(BookCreature.class, id);
            if (creature != null) {
                session.remove(creature);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Ошибка при удалении BookCreature с id=" + id, e);
        }
    }

    /**
     * Найти всех существ, живущих в конкретном городе (по id города).
     * Это потом пригодится для перепривязки при удалении MagicCity.
     */
    public List<BookCreature> findByCityId(Long cityId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM BookCreature bc WHERE bc.creatureLocation.id = :cityId",
                            BookCreature.class)
                    .setParameter("cityId", cityId)
                    .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при поиске существ по городу id=" + cityId, e);
        }
    }

    /**
     * Пример: найти всех существ, у которых name содержит подстроку (для фильтрации).
     * Можно будет использовать на главном экране для фильтрации по имени.
     */
    public List<BookCreature> findByNameContains(String part) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM BookCreature bc WHERE LOWER(bc.name) LIKE :pattern",
                            BookCreature.class)
                    .setParameter("pattern", "%" + part.toLowerCase() + "%")
                    .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при фильтрации BookCreature по имени: " + part, e);
        }
    }
}