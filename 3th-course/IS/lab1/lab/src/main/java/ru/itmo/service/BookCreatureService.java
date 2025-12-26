package ru.itmo.service;

import ru.itmo.db.BookCreatureRepository;
import ru.itmo.db.MagicCityRepository;
import ru.itmo.model.BookCreature;
import ru.itmo.model.MagicCity;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Named("bookCreatureService")
@ApplicationScoped
public class BookCreatureService {

    private final BookCreatureRepository creatureRepository = new BookCreatureRepository();
    private final MagicCityRepository cityRepository = new MagicCityRepository();

    // crud

    public Long createCreature(BookCreature creature) {
        if (creature.getCreationDate() == null) {
            creature.setCreationDate(ZonedDateTime.now());
        }

        // проверяем и подхватываем город, если он указан
        attachAndValidateCity(creature);

        return creatureRepository.save(creature);
    }

    public Optional<BookCreature> getById(Long id) {
        return creatureRepository.findById(id);
    }

    public List<BookCreature> getAll() {
        return creatureRepository.findAll();
    }

    public void updateCreature(BookCreature updated) {
        BookCreature existing = creatureRepository.findById(updated.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Существо с id=" + updated.getId() + " не найдено"));

        // дата создания должна остаться как в БД
        updated.setCreationDate(existing.getCreationDate());

        // проверяем и подхватываем город, если он указан
        attachAndValidateCity(updated);

        creatureRepository.update(updated);
    }

    public void deleteCreature(Long id) {
        creatureRepository.delete(id);
    }

    public List<BookCreature> searchByName(String substring) {
        if (substring == null || substring.isBlank()) {
            return getAll();
        }
        return creatureRepository.findByNameContains(substring);
    }

    // спец операции по существам

    // вернуть одно (любое) существо с максимальным name (по алфавиту)
    public Optional<BookCreature> getCreatureWithMaxName() {
        return creatureRepository.findAll().stream()
                .max(Comparator.comparing(BookCreature::getName));
    }

    // количество существ, у которых есть кольцо и ring.power < maxPower
    public long countCreaturesWithRingPowerLessThan(Integer maxPower) {
        if (maxPower == null) {
            return 0L;
        }

        return creatureRepository.findAll().stream()
                .map(BookCreature::getRing)
                .filter(r -> r != null && r.getPower() != null && r.getPower() < maxPower)
                .count();
    }

    // все существа, у которых attackLevel > minAttack
    public List<BookCreature> getCreaturesWithAttackLevelGreaterThan(Float minAttack) {
        if (minAttack == null) {
            return List.of();
        }

        return creatureRepository.findAll().stream()
                .filter(c -> c.getAttackLevel() != null && c.getAttackLevel() > minAttack)
                .toList();
    }

    // Существо с самым сильным кольцом (максимальный ring.power)
    public Optional<BookCreature> getCreatureWithStrongestRing() {
        return creatureRepository.findAll().stream()
                .filter(c -> c.getRing() != null && c.getRing().getPower() != null)
                .max(Comparator.comparingInt(c -> c.getRing().getPower()));
    }

    //вспомогательный метод проверки города

    private void attachAndValidateCity(BookCreature creature) {
        MagicCity location = creature.getCreatureLocation();
        if (location == null) {
            return; // город не задан — ничего не проверяем
        }

        long cityId = location.getId();
        if (cityId <= 0) {
            throw new IllegalArgumentException("ID города должен быть положительным числом");
        }

        MagicCity city = cityRepository.findById(cityId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Город с id=" + cityId + " не существует"));

        // подменяем "пустую заглушку {id=...}" на реальный объект из БД
        creature.setCreatureLocation(city);
    }
}