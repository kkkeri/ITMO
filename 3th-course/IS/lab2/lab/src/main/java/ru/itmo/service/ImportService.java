package ru.itmo.service;

import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.itmo.db.ImportOperationRepository;
import ru.itmo.model.BookCreature;
import ru.itmo.model.Coordinates;
import ru.itmo.model.MagicCity;
import ru.itmo.model.Ring;
import ru.itmo.util.HibernateUtil;
import ru.itmo.web.dto.*;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImportService {

    private final ImportOperationRepository opRepo = new ImportOperationRepository();


    public long importCreatures(String username, String role, ImportCreaturesRequest request) {
        long opId = opRepo.create(username, role, "creatures");

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            List<CreatureImportItem> items = (request == null) ? null : request.getItems();
            if (items == null || items.isEmpty()) {
                throw new IllegalArgumentException("Файл пустой: нет списка существ");
            }

            //PASS 1: VALIDATE ALL
            Set<String> seenInFile = new HashSet<>();

            for (int i = 0; i < items.size(); i++) {
                CreatureImportItem item = items.get(i);
                if (item == null) {
                    throw new IllegalArgumentException(idx(i) + "Элемент пустой");
                }

                String name = requireText(item.getName(), "Имя", i);

                CoordinatesDto c = item.getCoordinates();
                if (c == null) {
                    throw new IllegalArgumentException(idx(i) + field("Координаты") + "обязательно");
                }
                if (c.getY() == null) {
                    throw new IllegalArgumentException(idx(i) + field("Координата Y") + "обязательно");
                }

                if (item.getCreatureType() == null) {
                    throw new IllegalArgumentException(idx(i) + field("Тип существа") + "обязательно");
                }

                if (item.getAge() <= 0) {
                    throw new IllegalArgumentException(idx(i) + field("Возраст") + "должно быть больше 0");
                }

                Float attack = item.getAttackLevel();
                if (attack == null || attack <= 0 || attack > 100) {
                    throw new IllegalArgumentException(idx(i) + field("Уровень атаки") + "должен быть > 0 и ≤ 100");
                }

                Long cityId = item.getCreatureLocationId();
                if (cityId != null) {
                    String key = cityId + "|" + normLower(name);
                    if (!seenInFile.add(key)) {
                        throw new IllegalArgumentException(
                                idx(i) + "В одном городе нельзя иметь двух существ с одинаковым именем. " +
                                        "Дубликат: \"" + name + "\" (город id=" + cityId + ")"
                        );
                    }

                    MagicCity city = session.find(MagicCity.class, cityId);
                    if (city == null) {
                        throw new IllegalArgumentException(idx(i) + "Город с id=" + cityId + " не найден");
                    }

                    if (existsCreatureWithNameInCity(session, cityId, name)) {
                        throw new IllegalArgumentException(
                                idx(i) + "В городе id=" + cityId + " уже есть существо с именем \"" + name + "\""
                        );
                    }
                }

                if (item.getRing() != null) {
                    RingDto r = item.getRing();
                    if (r == null) {
                        throw new IllegalArgumentException(idx(i) + field("Кольцо") + "задано некорректно");
                    }
                    requireText(r.getName(), "Имя кольца", i);

                    Integer p = r.getPower();
                    if (p != null && (p <= 0 || p > 100)) {
                        throw new IllegalArgumentException(idx(i) + field("Сила кольца") + "должна быть от 1 до 100");
                    }
                }
            }

            //PASS 2: PERSIST ALL
            int inserted = 0;

            for (int i = 0; i < items.size(); i++) {
                CreatureImportItem item = items.get(i);

                String name = requireText(item.getName(), "Имя", i);

                BookCreature creature = new BookCreature();
                creature.setName(name);

                CoordinatesDto c = item.getCoordinates();
                Coordinates coords = new Coordinates();
                coords.setX(c.getX());
                coords.setY(c.getY());
                creature.setCoordinates(coords);

                creature.setAge(item.getAge());
                creature.setCreatureType(item.getCreatureType());
                creature.setAttackLevel(item.getAttackLevel());
                creature.setCreationDate(ZonedDateTime.now());

                Long cityId = item.getCreatureLocationId();
                if (cityId != null) {
                    MagicCity city = session.find(MagicCity.class, cityId); // уже проверили
                    creature.setCreatureLocation(city);
                } else {
                    creature.setCreatureLocation(null);
                }

                if (item.getRing() != null) {
                    RingDto r = item.getRing();
                    Ring ring = new Ring();
                    ring.setName(r.getName().trim());
                    ring.setPower(r.getPower());
                    creature.setRing(ring);
                } else {
                    creature.setRing(null);
                }

                session.persist(creature);
                inserted++;
            }

            tx.commit();
            opRepo.markSuccess(opId, inserted);
            return opId;

        } catch (Exception e) {
            if (tx != null) {
                try { tx.rollback(); } catch (Exception ignored) {}
            }
            opRepo.markFailed(opId, humanError(e));
            return opId;
        }
    }

    public long importCities(String username, String role, ImportCitiesRequest request) {
        long opId = opRepo.create(username, role, "cities");

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            List<CityImportItem> items = (request == null) ? null : request.getItems();
            if (items == null || items.isEmpty()) {
                throw new IllegalArgumentException("Файл пустой: нет списка городов");
            }

            //PASS 1: VALIDATE ALL
            Set<String> seenNamesInFile = new HashSet<>();

            for (int i = 0; i < items.size(); i++) {
                CityImportItem item = items.get(i);
                if (item == null) {
                    throw new IllegalArgumentException(idx(i) + "Элемент пустой");
                }

                String name = requireText(item.getName(), "Название города", i);
                String key = normLower(name);

                if (!seenNamesInFile.add(key)) {
                    throw new IllegalArgumentException(
                            idx(i) + "В файле два города с одинаковым названием: \"" + name + "\""
                    );
                }

                ensurePositive(item.getArea(), "Площадь", i);
                ensurePositive(item.getPopulation(), "Население", i);
                ensurePositive(item.getPopulationDensity(), "Плотность населения", i);

                if (existsCityByName(session, name)) {
                    throw new IllegalArgumentException(idx(i) + "Город с названием \"" + name + "\" уже существует");
                }
            }

            //PASS 2: PERSIST ALL
            int inserted = 0;
            for (int i = 0; i < items.size(); i++) {
                CityImportItem item = items.get(i);

                MagicCity city = new MagicCity();
                city.setName(requireText(item.getName(), "Название города", i));
                city.setArea(item.getArea());
                city.setPopulation(item.getPopulation());
                city.setEstablishmentDate(item.getEstablishmentDate());
                city.setGovernor(item.getGovernor());
                city.setCapital(item.isCapital());
                city.setPopulationDensity(item.getPopulationDensity());

                session.persist(city);
                inserted++;
            }

            tx.commit();
            opRepo.markSuccess(opId, inserted);
            return opId;

        } catch (Exception e) {
            if (tx != null) {
                try { tx.rollback(); } catch (Exception ignored) {}
            }
            opRepo.markFailed(opId, humanError(e));
            return opId;
        }
    }

    //DB CHECKS

    private boolean existsCityByName(Session session, String name) {
        Long cnt = session.createQuery(
                        "select count(c.id) from MagicCity c where lower(c.name) = :n",
                        Long.class
                )
                .setParameter("n", normLower(name))
                .uniqueResult();
        return cnt != null && cnt > 0;
    }

    private boolean existsCreatureWithNameInCity(Session session, Long cityId, String name) {
        Long cnt = session.createQuery(
                        "select count(bc.id) from BookCreature bc " +
                                "where bc.creatureLocation.id = :cityId and lower(bc.name) = :n",
                        Long.class
                )
                .setParameter("cityId", cityId)
                .setParameter("n", normLower(name))
                .uniqueResult();
        return cnt != null && cnt > 0;
    }

    //HUMAN ERRORS HELPERS

    private String idx(int i) {
        return "Элемент №" + (i + 1) + ": ";
    }

    private String field(String humanFieldName) {
        return "Поле «" + humanFieldName + "» ";
    }

    private String requireText(String value, String humanFieldName, int i) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(idx(i) + field(humanFieldName) + "не может быть пустым");
        }
        String v = value.trim();
        if (v.length() > 80) {
            throw new IllegalArgumentException(idx(i) + field(humanFieldName) + "не должно быть длиннее 80 символов");
        }
        return v;
    }

    private void ensurePositive(double v, String humanFieldName, int i) {
        // работает и для примитивов
        if (!(v > 0)) {
            throw new IllegalArgumentException(idx(i) + field(humanFieldName) + "должно быть больше 0");
        }
    }

    private void ensurePositive(long v, String humanFieldName, int i) {
        if (v <= 0) {
            throw new IllegalArgumentException(idx(i) + field(humanFieldName) + "должно быть больше 0");
        }
    }

    private void ensurePositive(float v, String humanFieldName, int i) {
        if (!(v > 0)) {
            throw new IllegalArgumentException(idx(i) + field(humanFieldName) + "должно быть больше 0");
        }
    }

    private String normLower(String s) {
        return (s == null) ? "" : s.trim().toLowerCase();
    }

    private String humanError(Exception e) {
        String msg = e.getMessage();
        if (msg != null && !msg.isBlank()) {
            msg = msg.replace("\"", "'").trim();
            if (msg.length() > 300) msg = msg.substring(0, 300);
            return msg;
        }
        return e.getClass().getSimpleName();
    }
}