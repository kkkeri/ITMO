package ru.itmo.model;

import java.time.ZonedDateTime;
import ru.itmo.model.util.BookCreatureType;

public class MagicCity {
    private long id;
    private String name;
    private Long area;
    private Long population;
    private ZonedDateTime establishmentDate;
    private BookCreatureType governor; // может быть null
    private boolean capital;
    private double populationDensity;

    public MagicCity() {}

    public MagicCity(String name, Long area, Long population, boolean capital, double density) {
        setName(name);
        setArea(area);
        setPopulation(population);
        setCapital(capital);
        setPopulationDensity(density);
        this.establishmentDate = ZonedDateTime.now();
    }

    // getters/setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Имя города не может быть пустым");
        this.name = name;
    }

    public Long getArea() { return area; }
    public void setArea(Long area) {
        if (area == null || area <= 0)
            throw new IllegalArgumentException("Площадь должна быть > 0");
        this.area = area;
    }

    public Long getPopulation() { return population; }
    public void setPopulation(Long population) {
        if (population == null || population <= 0)
            throw new IllegalArgumentException("Население должно быть > 0");
        this.population = population;
    }

    public ZonedDateTime getEstablishmentDate() { return establishmentDate; }
    // <-- добавлен публичный сеттер для Hibernate
    public void setEstablishmentDate(ZonedDateTime establishmentDate) {
        this.establishmentDate = establishmentDate;
    }

    public BookCreatureType getGovernor() { return governor; }
    public void setGovernor(BookCreatureType governor) { this.governor = governor; }

    public boolean isCapital() { return capital; }
    public void setCapital(boolean capital) { this.capital = capital; }

    public double getPopulationDensity() { return populationDensity; }
    public void setPopulationDensity(double populationDensity) {
        if (populationDensity <= 0)
            throw new IllegalArgumentException("Плотность населения должна быть > 0");
        this.populationDensity = populationDensity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MagicCity)) return false;
        return id == ((MagicCity) o).id;
    }

    @Override
    public int hashCode() { return Long.hashCode(id); }
}