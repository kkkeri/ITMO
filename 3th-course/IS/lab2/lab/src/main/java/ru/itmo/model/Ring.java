package ru.itmo.model;

public class Ring {
    private String name;      // не null, не пустая, max 80
    private Integer power;    // >0..100, может быть null

    public Ring() {}

    public Ring(String name, Integer power) {
        setName(name);
        setPower(power);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя кольца не может быть пустым");
        }
        if (name.length() > 80) {
            throw new IllegalArgumentException("Имя кольца не должно превышать 80 символов");
        }
        this.name = name;
    }

    public Integer getPower() {
        return power;
    }

    public void setPower(Integer power) {
        // Поле МОЖЕТ быть null, это ок
        if (power != null) {
            if (power <= 0 || power > 100) {
                throw new IllegalArgumentException("Сила кольца должна быть в диапазоне от 1 до 100");
            }
        }
        this.power = power;
    }
}