package ru.itmo.web.dto;

public class CoordinatesDto {
    private double x;
    private Float y; // не null по предметной области

    public CoordinatesDto() {}

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public Float getY() { return y; }
    public void setY(Float y) { this.y = y; }
}