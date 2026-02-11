package ru.itmo.web.dto;

public class RingDto {
    private String name;
    private Integer power; // может быть null

    public RingDto() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getPower() { return power; }
    public void setPower(Integer power) { this.power = power; }
}