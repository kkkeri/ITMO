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

    public void updateCity(MagicCity updated) {
        MagicCity existing = cityRepository.findById(updated.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Город с id=" + updated.getId() + " не найден"));

        updated.setEstablishmentDate(existing.getEstablishmentDate());

        cityRepository.update(updated);
    }

    // удалить город и перепривязать существ на другой город (или отвязать, если newCityId == null).

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

        cityRepository.delete(oldCity.getId());
    }

    public List<MagicCity> getElfCities() {
        return cityRepository.findByGovernor(BookCreatureType.ELF);
    }

    //Спецоперация: уничтожить все города эльфов. Перед удалением отвязываем всех существ от этих городов.

    public int destroyElfCities() {
        List<MagicCity> elfCities = getElfCities();
        if (elfCities.isEmpty()) {
            return 0;
        }

        for (MagicCity city : elfCities) {
            Long cityId = city.getId();

            // находим всех существ, живущих в этом городе
            List<BookCreature> creatures = creatureRepository.findByCityId(cityId);
            for (BookCreature creature : creatures) {
                creature.setCreatureLocation(null); // отвязываем город
                creatureRepository.update(creature);
            }

            // удаляем сам город
            cityRepository.delete(cityId);
        }

        return elfCities.size();
    }
}