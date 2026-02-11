package ru.itmo.db;

import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.itmo.model.ImportOperation;
import ru.itmo.model.util.ImportStatus;
import ru.itmo.util.HibernateUtil;

import javax.enterprise.context.ApplicationScoped;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ImportOperationRepository {

    public Long create(String username, String role, String entityType) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            ImportOperation op = new ImportOperation();
            op.setUsername(username);
            op.setRole(role);
            op.setEntityType(entityType);
            op.setStatus(ImportStatus.IN_PROGRESS);
            op.setCreatedAt(ZonedDateTime.now());
            op.setFinishedAt(null);
            op.setInsertedCount(null);
            op.setErrorMessage(null);

            session.persist(op);
            tx.commit();
            return op.getId();

        } catch (Exception e) {
            if (tx != null) {
                try { tx.rollback(); } catch (Exception ignored) {}
            }
            throw new RuntimeException("Ошибка при создании ImportOperation", e);
        }
    }

    public void markSuccess(long opId, int insertedCount) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // Hibernate 6: find вместо get
            ImportOperation op = session.find(ImportOperation.class, opId);
            if (op == null) {
                throw new IllegalArgumentException("ImportOperation id=" + opId + " не найден");
            }

            op.setStatus(ImportStatus.SUCCESS);
            op.setFinishedAt(ZonedDateTime.now());
            op.setInsertedCount(insertedCount);
            op.setErrorMessage(null);

            session.merge(op);
            tx.commit();

        } catch (Exception e) {
            if (tx != null) {
                try { tx.rollback(); } catch (Exception ignored) {}
            }
            throw new RuntimeException("Ошибка при обновлении ImportOperation (SUCCESS) id=" + opId, e);
        }
    }

    public void markFailed(long opId, String errorMessage) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // Hibernate 6: find вместо get
            ImportOperation op = session.find(ImportOperation.class, opId);
            if (op == null) {
                throw new IllegalArgumentException("ImportOperation id=" + opId + " не найден");
            }

            op.setStatus(ImportStatus.FAILED);
            op.setFinishedAt(ZonedDateTime.now());
            op.setInsertedCount(null);
            op.setErrorMessage(errorMessage);

            session.merge(op);
            tx.commit();

        } catch (Exception e) {
            if (tx != null) {
                try { tx.rollback(); } catch (Exception ignored) {}
            }
            throw new RuntimeException("Ошибка при обновлении ImportOperation (FAILED) id=" + opId, e);
        }
    }

    public Optional<ImportOperation> findById(long opId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            return Optional.ofNullable(session.find(ImportOperation.class, opId));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при поиске ImportOperation id=" + opId, e);
        }
    }

    // Для обычного пользователя — только свои операции
    public List<ImportOperation> findAllByUser(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM ImportOperation o WHERE o.username = :u ORDER BY o.createdAt DESC",
                            ImportOperation.class
                    )
                    .setParameter("u", username)
                    .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении истории импорта пользователя " + username, e);
        }
    }

    // Для админа — все операции
    public List<ImportOperation> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM ImportOperation o ORDER BY o.createdAt DESC",
                            ImportOperation.class
                    )
                    .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении полной истории импорта", e);
        }
    }
}