package com.jcloisterzone.event;

public class ExprItem {

    private final Integer count;
    private final String name;
    private final int points;

    public ExprItem(Integer count, String name, int points) {
        this.count = count;
        this.name = name;
        this.points = points;
    }

    public ExprItem(String name, int points) {
        this(null, name, points);
    }

    public Integer getCount() {
        return count;
    }

    public String getName() {
        return name;
    }

    public Integer getPoints() {
        return points;
    }
}
