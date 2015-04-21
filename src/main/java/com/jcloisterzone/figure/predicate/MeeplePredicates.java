package com.jcloisterzone.figure.predicate;

import com.google.common.base.Predicate;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.Meeple;

public final class MeeplePredicates {

    private static final Predicate<Meeple> DEPLOYED = new Predicate<Meeple>() {
        @Override
        public boolean apply(Meeple m) {
            return m.isDeployed();
        }
    };
    private static final Predicate<Meeple> IN_SUPPLY = new Predicate<Meeple>() {
        @Override
        public boolean apply(Meeple m) {
            return m.isInSupply();
        }
    };

    public static Predicate<Meeple> deployed() {
        return DEPLOYED;
    }

    public static Predicate<Meeple> inSupply() {
        return IN_SUPPLY;
    }

    public static Predicate<Meeple> at(final Position p) {
        return new Predicate<Meeple>() {
            @Override
            public boolean apply(Meeple m) {
                return m.at(p);
            }
        };
    }

    public static Predicate<Meeple> type(final Class<? extends Meeple> clazz) {
        return new Predicate<Meeple>() {
            @Override
            public boolean apply(Meeple m) {
                return clazz.equals(m.getClass());
            }
        };
    }

    public static Predicate<Meeple> type(final Class<? extends Meeple> clazz1, final Class<? extends Meeple> clazz2) {
        return new Predicate<Meeple>() {
            @Override
            public boolean apply(Meeple m) {
                return clazz1.equals(m.getClass()) || clazz2.equals(m.getClass());
            }
        };
    }

    public static Predicate<Meeple> instanceOf(final Class<? extends Meeple> clazz1, final Class<? extends Meeple> clazz2) {
        return new Predicate<Meeple>() {
            @Override
            public boolean apply(Meeple m) {
                return clazz1.isInstance(m) || clazz2.isInstance(m);
            }
        };
    }
}
