package ru.itmo.service.specialop;

import ru.itmo.db.BookCreatureRepository;
import ru.itmo.db.MagicCityRepository;
import ru.itmo.model.BookCreature;
import ru.itmo.model.MagicCity;
import ru.itmo.model.util.BookCreatureType;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@ManagedBean(name = "specialOperationsService")
@ApplicationScoped
public class SpecialOperationsService {

    private final BookCreatureRepository creatureRepository = new BookCreatureRepository();
    private final MagicCityRepository cityRepository = new MagicCityRepository();

    /**
     * Вернуть один (любой) объект, значение поля name которого является максимальным.
     */
    public Optional<BookCreature> getCreatureWithMaxName() {
        return creatureRepository.findAll().stream()
                .max(Comparator.comparing(BookCreature::getName));
    }

    /**
     * Вернуть количество объектов, значение поля ring которых меньше заданного.
     * Интерпретация: r.power < maxPower
     */
    public long countCreaturesWithRingPowerLessThan(Integer maxPower) {
        if (maxPower == null) return 0L;

        return creatureRepository.findAll().stream()
                .map(BookCreature::getRing)
                .filter(r -> r != null && r.getPower() != null && r.getPower() < maxPower)
                .count();
    }

    /**
     * Вернуть массив объектов, значение поля attackLevel которых больше заданного.
     */
    public List<BookCreature> getCreaturesWithAttackLevelGreaterThan(Float minAttack) {
        if (minAttack == null) return List.of();

        return creatureRepository.findAll().stream()
                .filter(c -> c.getAttackLevel() != null && c.getAttackLevel() > minAttack)
                .toList();
    }

    /**
     * Найти персонажа с самым сильным кольцом.
     */
    public Optional<BookCreature> getCreatureWithStrongestRing() {
        return creatureRepository.findAll().stream()
                .filter(c -> c.getRing() != null && c.getRing().getPower() != null)
                .max(Comparator.comparingInt(c -> c.getRing().getPower()));
    }

    /**
     * Уничтожить города эльфов.
     * Алгоритм:
     *  - найти все города с governor = ELF
     *  - для каждого города:
     *      - отвязать от него всех существ (setCreatureLocation(null))
     *      - удалить город
     * @return количество уничтоженных городов
     */
    public int destroyElfCities() {
        List<MagicCity> elfCities = cityRepository.findByGovernor(BookCreatureType.ELF);
        if (elfCities.isEmpty()) return 0;

        for (MagicCity city : elfCities) {
            Long cityId = city.getId();

            // 1. находим существ в городе
            List<BookCreature> creatures = creatureRepository.findByCityId(cityId);

            // 2. отвязываем существ
            for (BookCreature creature : creatures) {
                creature.setCreatureLocation(null);
                creatureRepository.update(creature);
            }

            // 3. удаляем сам город
            cityRepository.delete(cityId);
        }

        return elfCities.size();
    }
}