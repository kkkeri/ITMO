package ru.itmo.model;

public class Ring {
    private String name; // не null, не пустая
    private Integer power; // > 0

    public Ring() {}

    public Ring(String name, Integer power) {
        setName(name);
        setPower(power);
    }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Имя кольца не может быть пустым");
        this.name = name;
    }

    public Integer getPower() { return power; }
    public void setPower(Integer power) {
        if (power != null && power <= 0)
            throw new IllegalArgumentException("Сила кольца должна быть > 0");
        this.power = power;
    }
}