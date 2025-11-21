package ru.itmo.model;

import ru.itmo.model.util.BookCreatureType;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.ZonedDateTime;

public class BookCreature {
    private long id;
    private String name;
    private Coordinates coordinates;
    private ZonedDateTime creationDate;
    private long age;

    @Enumerated(EnumType.STRING)
    @Column(name = "creature_type")
    private BookCreatureType creatureType;

    private MagicCity creatureLocation;
    private Float attackLevel;
    private Ring ring;

    public BookCreature() {}

    public BookCreature(String name, Coordinates coordinates, long age, BookCreatureType creatureType, Float attackLevel) {
        setName(name);
        setCoordinates(coordinates);
        setAge(age);
        setCreatureType(creatureType);
        setAttackLevel(attackLevel);
        this.creationDate = ZonedDateTime.now();
    }

    public long getId() { return id; }

    public void setId(long id) {
        if (id <= 0) throw new IllegalArgumentException("ID должен быть больше 0");
        this.id = id;
    }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Имя не может быть пустым");
        this.name = name;
    }

    public Coordinates getCoordinates() { return coordinates; }
    public void setCoordinates(Coordinates coordinates) {
        if (coordinates == null)
            throw new IllegalArgumentException("Координаты не могут быть null");
        this.coordinates = coordinates;
    }

    public ZonedDateTime getCreationDate() { return creationDate; }
    // добавлен публичный сеттер для Hibernate
    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public long getAge() { return age; }
    public void setAge(long age) {
        if (age <= 0) throw new IllegalArgumentException("Возраст должен быть > 0");
        this.age = age;
    }

    public BookCreatureType getCreatureType() { return creatureType; }
    public void setCreatureType(BookCreatureType creatureType) {
        if (creatureType == null)
            throw new IllegalArgumentException("Тип существа обязателен");
        this.creatureType = creatureType;
    }

    public MagicCity getCreatureLocation() { return creatureLocation; }
    public void setCreatureLocation(MagicCity creatureLocation) { this.creatureLocation = creatureLocation; }

    public Float getAttackLevel() { return attackLevel; }
    public void setAttackLevel(Float attackLevel) {
        if (attackLevel == null || attackLevel <= 0)
            throw new IllegalArgumentException("Уровень атаки должен быть > 0");
        this.attackLevel = attackLevel;
    }

    public Ring getRing() { return ring; }
    public void setRing(Ring ring) { this.ring = ring; }

    // --- equals / hashCode ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookCreature)) return false;
        return id == ((BookCreature) o).id;
    }

    @Override
    public int hashCode() { return Long.hashCode(id); }

    @Override
    public String toString() {
        return "BookCreature{id=" + id + ", name='" + name + "', type=" + creatureType + "}";
    }
}