package com.jcloisterzone.event;

public class ExprItem {

    private final int count;
    private final String name;
    private final int points;

    public ExprItem(int count, String name, int points) {
        this.count = count;
        this.name = name;
        this.points = points;
    }

    public ExprItem(String name, int points) {
        this(1, name, points);
    }

    public int getCount() {
        return count;
    }

    public String getName() {
        return name;
    }

    public int getPoints() {
        return points;
    }
}
