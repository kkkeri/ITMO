package ru.itmo.service.specialop;

import ru.itmo.db.BookCreatureRepository;
import ru.itmo.db.MagicCityRepository;
import ru.itmo.model.BookCreature;
import ru.itmo.model.MagicCity;
import ru.itmo.model.util.BookCreatureType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Named("specialOperationsService")
@ApplicationScoped
public class SpecialOperationsService {

    private final BookCreatureRepository creatureRepository = new BookCreatureRepository();
    private final MagicCityRepository cityRepository = new MagicCityRepository();

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

    public int destroyElfCities() {
        List<MagicCity> elfCities = cityRepository.findByGovernor(BookCreatureType.ELF);
        if (elfCities.isEmpty()) return 0;

        for (MagicCity city : elfCities) {
            Long cityId = city.getId();
            var creatures = creatureRepository.findByCityId(cityId);
            for (BookCreature creature : creatures) {
                creature.setCreatureLocation(null);
                creatureRepository.update(creature);
            }
            cityRepository.delete(cityId);
        }

        return elfCities.size();
    }
}