package com.jcloisterzone.event;

import com.jcloisterzone.Immutable;
import io.vavr.collection.List;

@Immutable
public class PointsExpression {

    private final String name;
    private final List<ExprItem> items;

    public PointsExpression(String name,  List<ExprItem> items) {
        this.name = name;
        this.items = items;
    }

    public PointsExpression(String name, ExprItem...items) {
        this(name, List.of(items));
    }


    public int getPoints() {
        return items.map(exp -> exp.getPoints()).sum().intValue();
    }

    public String getName() {
        return name;
    }

    public List<ExprItem> getItems() {
        return items;
    }

    public PointsExpression merge(PointsExpression expr) {
        if (expr == null || expr.getPoints() == 0) {
            return this;
        }
        List<ExprItem>  items = this.items.appendAll(expr.items);
        return new PointsExpression(name + '+' + expr.name, items);
    }

    public PointsExpression append(int points, List<ExprItem> items) {
        items = this.items.appendAll(items);
        return new PointsExpression(name, items);
    }
}
