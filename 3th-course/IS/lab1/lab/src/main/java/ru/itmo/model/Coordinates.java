package ru.itmo.model;

public class Coordinates {
    private double x;
    private Float y;

    public Coordinates() {}

    public Coordinates(double x, Float y) {
        this.x = x;
        setY(y);
    }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public Float getY() { return y; }
    public void setY(Float y) {
        if (y == null)
            throw new IllegalArgumentException("Y не может быть null");
        this.y = y;
    }
}