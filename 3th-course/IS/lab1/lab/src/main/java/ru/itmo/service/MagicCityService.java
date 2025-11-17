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
        if (city.getEstablishmentDate() == null) {
            city.setEstablishmentDate(ZonedDateTime.now());
        }
        return cityRepository.save(city);
    }

    public Optional<MagicCity> getById(Long id) {
        return cityRepository.findById(id);
    }

    public List<MagicCity> getAll() {
        return cityRepository.findAll();
    }

    public void updateCity(MagicCity city) {
        cityRepository.update(city);
    }

    public void deleteCityWithReassignment(Long cityId, Long newCityId) {
        MagicCity oldCity = cityRepository.findById(cityId)
                .orElseThrow(() -> new IllegalArgumentException("Город с id=" + cityId + " не найден"));

        List<BookCreature> creatures = creatureRepository.findByCityId(cityId);

        MagicCity newCity = null;
        if (newCityId != null) {
            newCity = cityRepository.findById(newCityId)
                    .orElseThrow(() -> new IllegalArgumentException("Новый город с id=" + newCityId + " не найден"));
        }

        for (BookCreature creature : creatures) {
            creature.setCreatureLocation(newCity);
            creatureRepository.update(creature);
        }

        cityRepository.delete(cityId);
    }

    public List<MagicCity> getElfCities() {
        return cityRepository.findByGovernor(BookCreatureType.ELF);
    }
}