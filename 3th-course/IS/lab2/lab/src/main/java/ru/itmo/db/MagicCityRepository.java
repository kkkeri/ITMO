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

    // ===================== CRUD =====================

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

    public Optional<MagicCity> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            MagicCity city = session.find(MagicCity.class, id);
            return Optional.ofNullable(city);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при поиске MagicCity по id=" + id, e);
        }
    }

    public List<MagicCity> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM MagicCity", MagicCity.class).getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении списка всех MagicCity", e);
        }
    }

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

    public void delete(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            MagicCity city = session.find(MagicCity.class, id);
            if (city != null) {
                session.remove(city);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Ошибка при удалении MagicCity id=" + id, e);
        }
    }

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

    public void deleteAll(List<MagicCity> cities) {
        if (cities == null || cities.isEmpty()) return;

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            for (MagicCity city : cities) {
                MagicCity managed = session.find(MagicCity.class, city.getId());
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

    public Long saveIfUnique(MagicCity city) {
        if (city == null) {
            throw new IllegalArgumentException("Город не задан");
        }
        String name = normalizeName(city.getName());

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();


            // задаём уровень изоляции текущей транзакции.
            session.createNativeQuery("SET TRANSACTION ISOLATION LEVEL SERIALIZABLE")
                    .executeUpdate();

            // Проверка уникальности в этой же транзакции
            Long cnt = session.createQuery(
                            "select count(c) from MagicCity c where lower(c.name) = :n",
                            Long.class)
                    .setParameter("n", name.toLowerCase())
                    .uniqueResult();

            if (cnt != null && cnt > 0) {
                throw new IllegalArgumentException("Город с названием \"" + name + "\" уже существует");
            }

            // нормализованное имя фиксируем
            city.setName(name);

            session.persist(city);
            tx.commit();
            return city.getId();

        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            // если это наш IllegalArgumentException — пробрасываем его как есть,
            // чтобы сервлет вернул красивую ошибку 400 с сообщением
            throw e;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Ошибка при сохранении MagicCity (saveIfUnique)", e);
        }
    }

    /** true, если существует город с таким name (без учета регистра) */
    public boolean existsByNameIgnoreCase(String name) {
        if (name == null) return false;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long cnt = session.createQuery(
                            "select count(c) from MagicCity c where lower(c.name) = :n",
                            Long.class
                    )
                    .setParameter("n", name.trim().toLowerCase())
                    .uniqueResult();

            return cnt != null && cnt > 0;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке уникальности имени города: " + name, e);
        }
    }

    /** true, если существует город с таким name (без учета регистра), кроме города excludeId */
    public boolean existsByNameIgnoreCaseExceptId(String name, Long excludeId) {
        if (name == null) return false;
        if (excludeId == null) return existsByNameIgnoreCase(name);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long cnt = session.createQuery(
                            "select count(c) from MagicCity c " +
                                    "where lower(c.name) = :n and c.id <> :id",
                            Long.class
                    )
                    .setParameter("n", name.trim().toLowerCase())
                    .setParameter("id", excludeId)
                    .uniqueResult();

            return cnt != null && cnt > 0;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке уникальности имени города: " + name, e);
        }
    }

    // ===================== helpers =====================

    private String normalizeName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Название города обязательно");
        }
        String n = name.trim();
        if (n.isEmpty()) {
            throw new IllegalArgumentException("Название города не может быть пустым");
        }
        return n;
    }
}