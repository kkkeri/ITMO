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

    //CRUD

    public Long createCreature(BookCreature creature) {
        if (creature.getCreationDate() == null) {
            creature.setCreationDate(ZonedDateTime.now());
        }

        // подхватываем город (если задан) и валидируем
        attachAndValidateCity(creature);

        //УНИКАЛЬНОСТЬ: имя существа в рамках города
        validateUniqueCreatureNameInCityOnCreate(creature);

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

        // подхватываем город (если задан) и валидируем
        attachAndValidateCity(updated);

        //УНИКАЛЬНОСТЬ: имя существа в рамках города (исключая самого себя)
        validateUniqueCreatureNameInCityOnUpdate(updated);

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

    //Спец операции

    public Optional<BookCreature> getCreatureWithMaxName() {
        return creatureRepository.findAll().stream()
                .max(Comparator.comparing(BookCreature::getName));
    }

    public long countCreaturesWithRingPowerLessThan(Integer maxPower) {
        if (maxPower == null) return 0L;

        return creatureRepository.findAll().stream()
                .map(BookCreature::getRing)
                .filter(r -> r != null && r.getPower() != null && r.getPower() < maxPower)
                .count();
    }

    public List<BookCreature> getCreaturesWithAttackLevelGreaterThan(Float minAttack) {
        if (minAttack == null) return List.of();

        return creatureRepository.findAll().stream()
                .filter(c -> c.getAttackLevel() != null && c.getAttackLevel() > minAttack)
                .toList();
    }

    public Optional<BookCreature> getCreatureWithStrongestRing() {
        return creatureRepository.findAll().stream()
                .filter(c -> c.getRing() != null && c.getRing().getPower() != null)
                .max(Comparator.comparingInt(c -> c.getRing().getPower()));
    }


    private void attachAndValidateCity(BookCreature creature) {
        MagicCity location = creature.getCreatureLocation();
        if (location == null) return;

        Long cityIdObj = location.getId(); // важно: у тебя теперь Long
        if (cityIdObj == null || cityIdObj <= 0) {
            throw new IllegalArgumentException("ID города должен быть положительным числом");
        }

        MagicCity city = cityRepository.findById(cityIdObj)
                .orElseThrow(() -> new IllegalArgumentException("Город с id=" + cityIdObj + " не существует"));

        creature.setCreatureLocation(city);
    }

    /**
     * CREATE: если город задан — проверяем, что в этом городе ещё нет существа с таким именем.
     */
    private void validateUniqueCreatureNameInCityOnCreate(BookCreature creature) {
        MagicCity city = creature.getCreatureLocation();
        if (city == null) return; // правило только "в городе"

        Long cityId = city.getId();
        String name = creature.getName();

        if (creatureRepository.existsByNameInCityIgnoreCase(name, cityId)) {
            throw new IllegalArgumentException(
                    "В городе id=" + cityId + " уже есть существо с именем \"" + name + "\""
            );
        }
    }

    /**
     * UPDATE: проверяем то же самое, но исключаем существо с тем же id.
     */
    private void validateUniqueCreatureNameInCityOnUpdate(BookCreature updated) {
        MagicCity city = updated.getCreatureLocation();
        if (city == null) return;

        Long cityId = city.getId();
        String name = updated.getName();
        Long id = updated.getId();

        if (creatureRepository.existsByNameInCityIgnoreCaseExcludingId(name, cityId, id)) {
            throw new IllegalArgumentException(
                    "В городе id=" + cityId + " уже есть другое существо с именем \"" + name + "\""
            );
        }
    }
}