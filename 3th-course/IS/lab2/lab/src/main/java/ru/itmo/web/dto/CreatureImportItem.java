package ru.itmo.web.dto;

import ru.itmo.model.util.BookCreatureType;

public class CreatureImportItem {
    private String name;
    private CoordinatesDto coordinates;
    private long age;
    private BookCreatureType creatureType;
    private Long creatureLocationId;   // ссылка на существующий город (может быть null)
    private Float attackLevel;
    private RingDto ring;              // может быть null

    public CreatureImportItem() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CoordinatesDto getCoordinates() { return coordinates; }
    public void setCoordinates(CoordinatesDto coordinates) { this.coordinates = coordinates; }

    public long getAge() { return age; }
    public void setAge(long age) { this.age = age; }

    public BookCreatureType getCreatureType() { return creatureType; }
    public void setCreatureType(BookCreatureType creatureType) { this.creatureType = creatureType; }

    public Long getCreatureLocationId() { return creatureLocationId; }
    public void setCreatureLocationId(Long creatureLocationId) { this.creatureLocationId = creatureLocationId; }

    public Float getAttackLevel() { return attackLevel; }
    public void setAttackLevel(Float attackLevel) { this.attackLevel = attackLevel; }

    public RingDto getRing() { return ring; }
    public void setRing(RingDto ring) { this.ring = ring; }
}