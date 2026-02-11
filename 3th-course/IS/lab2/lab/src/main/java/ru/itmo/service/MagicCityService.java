package ru.itmo.service;

import ru.itmo.db.BookCreatureRepository;
import ru.itmo.db.MagicCityRepository;
import ru.itmo.model.BookCreature;
import ru.itmo.model.MagicCity;
import ru.itmo.model.util.BookCreatureType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Named("magicCityService")
@ApplicationScoped
public class MagicCityService {

    private final MagicCityRepository cityRepository = new MagicCityRepository();
    private final BookCreatureRepository creatureRepository = new BookCreatureRepository();

    public Long createCity(MagicCity city) {
        if (city == null) throw new IllegalArgumentException("Город не задан");

        if (city.getEstablishmentDate() == null) {
            city.setEstablishmentDate(ZonedDateTime.now());
        }

        // теперь уникальность + insert атомарно
        return cityRepository.saveIfUnique(city);
    }

    public Optional<MagicCity> getById(Long id) {
        return cityRepository.findById(id);
    }

    public List<MagicCity> getAll() {
        return cityRepository.findAll();
    }

    public void updateCity(MagicCity updated) {
        if (updated == null || updated.getId() == null) {
            throw new IllegalArgumentException("Некорректные данные для обновления города");
        }

        MagicCity existing = cityRepository.findById(updated.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Город с id=" + updated.getId() + " не найден"));

        // establishmentDate защищаем: не даём менять с клиента
        updated.setEstablishmentDate(existing.getEstablishmentDate());

        // программная уникальность имени (кроме текущего id)
        ensureUniqueCityNameForUpdate(updated.getId(), updated.getName());

        cityRepository.update(updated);
    }

    /**
     * удалить город и перепривязать существ на другой город (или отвязать, если newCityId == null).
     */
    public void deleteCityWithReassignment(Long cityId, Long newCityId) {
        if (cityId == null) {
            throw new IllegalArgumentException("cityId обязателен");
        }

        MagicCity oldCity = cityRepository.findById(cityId)
                .orElseThrow(() -> new IllegalArgumentException("Город с id=" + cityId + " не найден"));

        List<BookCreature> creatures = creatureRepository.findByCityId(cityId);

        MagicCity newCity = null;
        if (newCityId != null) {
            if (newCityId.equals(cityId)) {
                throw new IllegalArgumentException("Нельзя перепривязать на тот же самый город");
            }
            newCity = cityRepository.findById(newCityId)
                    .orElseThrow(() -> new IllegalArgumentException("Новый город с id=" + newCityId + " не найден"));
        }

        for (BookCreature creature : creatures) {
            creature.setCreatureLocation(newCity);
            creatureRepository.update(creature);
        }

        cityRepository.delete(oldCity.getId());
    }

    public List<MagicCity> getElfCities() {
        return cityRepository.findByGovernor(BookCreatureType.ELF);
    }

    /**
     * Спецоперация: уничтожить все города эльфов.
     * Перед удалением отвязываем всех существ от этих городов.
     */
    public int destroyElfCities() {
        List<MagicCity> elfCities = getElfCities();
        if (elfCities.isEmpty()) {
            return 0;
        }

        for (MagicCity city : elfCities) {
            Long cityId = city.getId();

            List<BookCreature> creatures = creatureRepository.findByCityId(cityId);
            for (BookCreature creature : creatures) {
                creature.setCreatureLocation(null);
                creatureRepository.update(creature);
            }

            cityRepository.delete(cityId);
        }

        return elfCities.size();
    }

    // ===================== helpers =====================

    private void ensureUniqueCityNameForCreate(String name) {
        String normalized = normalizeName(name);
        if (cityRepository.existsByNameIgnoreCase(normalized)) {
            throw new IllegalArgumentException("Город с названием \"" + normalized + "\" уже существует");
        }
    }

    private void ensureUniqueCityNameForUpdate(Long id, String name) {
        String normalized = normalizeName(name);
        if (cityRepository.existsByNameIgnoreCaseExceptId(normalized, id)) {
            throw new IllegalArgumentException("Город с названием \"" + normalized + "\" уже существует");
        }
    }

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