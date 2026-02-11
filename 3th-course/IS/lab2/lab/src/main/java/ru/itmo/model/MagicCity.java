package ru.itmo.model;

import java.time.ZonedDateTime;
import ru.itmo.model.util.BookCreatureType;

public class MagicCity {
    private Long id;

    // ✅ optimistic locking version
    private Long version;

    private String name;
    private Long area;
    private Long population;
    private ZonedDateTime establishmentDate;
    private BookCreatureType governor;
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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    //version getter/setter
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Название не может быть пустым");
        }
        if (name.length() > 80) {
            throw new IllegalArgumentException("Название не должно быть длиннее 80 символов");
        }
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
        MagicCity other = (MagicCity) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return (id == null) ? 0 : Long.hashCode(id);
    }
}