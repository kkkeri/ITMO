package ru.itmo.web.dto;

import ru.itmo.model.util.BookCreatureType;

import java.time.ZonedDateTime;

public class CityImportItem {
    private String name;
    private Long area;
    private Long population;
    private ZonedDateTime establishmentDate;
    private BookCreatureType governor; // может быть null
    private boolean capital;
    private double populationDensity;

    public CityImportItem() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getArea() { return area; }
    public void setArea(Long area) { this.area = area; }

    public Long getPopulation() { return population; }
    public void setPopulation(Long population) { this.population = population; }

    public ZonedDateTime getEstablishmentDate() { return establishmentDate; }
    public void setEstablishmentDate(ZonedDateTime establishmentDate) { this.establishmentDate = establishmentDate; }

    public BookCreatureType getGovernor() { return governor; }
    public void setGovernor(BookCreatureType governor) { this.governor = governor; }

    public boolean isCapital() { return capital; }
    public void setCapital(boolean capital) { this.capital = capital; }

    public double getPopulationDensity() { return populationDensity; }
    public void setPopulationDensity(double populationDensity) { this.populationDensity = populationDensity; }
}