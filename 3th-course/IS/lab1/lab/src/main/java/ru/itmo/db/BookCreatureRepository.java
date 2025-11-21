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

    public Long save(BookCreature creature) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            session.persist(creature);   // генерация id бд

            tx.commit();
            return creature.getId();
        } catch (Exception e) {
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception ignored) {
                    // игнорируем вторичную ошибку при rollback
                }
            }
            throw new RuntimeException("Ошибка при сохранении BookCreature", e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public Optional<BookCreature> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            BookCreature creature = session.get(BookCreature.class, id);
            return Optional.ofNullable(creature);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при поиске BookCreature по id=" + id, e);
        }
    }


    public List<BookCreature> findAll() { // получаем всех сущкств
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM BookCreature", BookCreature.class)
                    .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении списка всех BookCreature", e);
        }
    }

    public void update(BookCreature creature) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            session.merge(creature);

            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception ignored) {}
            }
            throw new RuntimeException(
                    "Ошибка при обновлении BookCreature с id=" + creature.getId(), e
            );
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public void delete(Long id) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            BookCreature creature = session.get(BookCreature.class, id);
            if (creature != null) {
                session.remove(creature);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception ignored) {}
            }
            throw new RuntimeException("Ошибка при удалении BookCreature с id=" + id, e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

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