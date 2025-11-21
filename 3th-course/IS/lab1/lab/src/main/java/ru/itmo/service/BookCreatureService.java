package ru.itmo.service;

import ru.itmo.db.BookCreatureRepository;
import ru.itmo.model.BookCreature;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Named("bookCreatureService")
@ApplicationScoped
public class BookCreatureService {

    private final BookCreatureRepository creatureRepository = new BookCreatureRepository();

    public Long createCreature(BookCreature creature) {
        if (creature.getCreationDate() == null) {
            creature.setCreationDate(ZonedDateTime.now());
        }
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
        // сохраняем дату создания из БД
        updated.setCreationDate(existing.getCreationDate());

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
}