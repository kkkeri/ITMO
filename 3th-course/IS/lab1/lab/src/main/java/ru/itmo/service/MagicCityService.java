package ru.itmo.service;

import ru.itmo.db.BookCreatureRepository;
import ru.itmo.db.MagicCityRepository;
import ru.itmo.model.BookCreature;
import ru.itmo.model.MagicCity;
import ru.itmo.model.util.BookCreatureType;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@ManagedBean(name = "magicCityService")
@ApplicationScoped
public class MagicCityService {

    // Репозитории создаём вручную, без @Inject
    private final MagicCityRepository cityRepository = new MagicCityRepository();
    private final BookCreatureRepository creatureRepository = new BookCreatureRepository();

    /**
     * Создание нового города.
     * Здесь, при необходимости, выставляем establishmentDate,
     * если он ещё не установлен.
     */
    public Long createCity(MagicCity city) {
        if (city.getEstablishmentDate() == null) {
            city.setEstablishmentDate(ZonedDateTime.now());
        }
        return cityRepository.save(city);
    }

    /**
     * Получение города по id.
     */
    public Optional<MagicCity> getById(Long id) {
        return cityRepository.findById(id);
    }

    /**
     * Получение всех городов (для таблиц, выпадающих списков и т.д.).
     */
    public List<MagicCity> getAll() {
        return cityRepository.findAll();
    }

    /**
     * Обновление города.
     */
    public void updateCity(MagicCity city) {
        cityRepository.update(city);
    }

    /**
     * Удаление города С УЧЁТОМ перепривязки существ.
     *
     * @param cityId    id удаляемого города
     * @param newCityId id города, к которому нужно перепривязать существ
     *                  или null, если существа должны остаться без города.
     */
    public void deleteCityWithReassignment(Long cityId, Long newCityId) {
        // 1. Находим удаляемый город
        MagicCity oldCity = cityRepository.findById(cityId)
                .orElseThrow(() -> new IllegalArgumentException("Город с id=" + cityId + " не найден"));

        // 2. Находим всех существ, живущих в этом городе
        List<BookCreature> creatures = creatureRepository.findByCityId(cityId);

        // 3. Определяем новый город (или null)
        MagicCity newCity = null;
        if (newCityId != null) {
            newCity = cityRepository.findById(newCityId)
                    .orElseThrow(() -> new IllegalArgumentException("Новый город с id=" + newCityId + " не найден"));
        }

        // 4. Перепривязываем существ: либо к новому городу, либо на null
        for (BookCreature creature : creatures) {
            creature.setCreatureLocation(newCity);  // newCity может быть null — в модели это разрешено
            creatureRepository.update(creature);
        }

        // 5. Удаляем старый город
        cityRepository.delete(cityId);
    }

    /**
     * Получить все города, у которых правитель (governor) — ELF.
     * Пригодится для спец-операции "уничтожить города эльфов".
     */
    public List<MagicCity> getElfCities() {
        return cityRepository.findByGovernor(BookCreatureType.ELF);
    }
}