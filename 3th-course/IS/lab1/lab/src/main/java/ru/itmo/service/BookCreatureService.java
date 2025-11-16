package ru.itmo.service;

import ru.itmo.db.BookCreatureRepository;
import ru.itmo.model.BookCreature;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ApplicationScoped;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@ManagedBean(name = "bookCreatureService")
@ApplicationScoped
public class BookCreatureService {

    private final BookCreatureRepository creatureRepository = new BookCreatureRepository();

    /**
     * Создание нового существа.
     * Здесь мы гарантируем, что creationDate будет установлен
     * и вся логика создания выполняется на серверной стороне.
     */
    public Long createCreature(BookCreature creature) {
        if (creature.getCreationDate() == null) {
            creature.setCreationDate(ZonedDateTime.now());
        }
        return creatureRepository.save(creature);
    }

    /** Получение существа по id. */
    public Optional<BookCreature> getById(Long id) {
        return creatureRepository.findById(id);
    }

    /** Получение всех существ. */
    public List<BookCreature> getAll() {
        return creatureRepository.findAll();
    }

    /** Обновление существующего объекта. */
    public void updateCreature(BookCreature creature) {
        creatureRepository.update(creature);
    }

    /** Удаление существа по id. */
    public void deleteCreature(Long id) {
        creatureRepository.delete(id);
    }

    /** Поиск существ по части имени. */
    public List<BookCreature> searchByName(String substring) {
        if (substring == null || substring.isBlank()) {
            return getAll();
        }
        return creatureRepository.findByNameContains(substring);
    }
}