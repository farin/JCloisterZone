package com.jcloisterzone;

/** represents set of players with rights to something */
public class PlayerRestriction {

    final Player include;
    final Player exclude;

    private PlayerRestriction(Player include, Player exclude) {
        this.include = include;
        this.exclude = exclude;
    }

    public static PlayerRestriction any() {
        return new PlayerRestriction(null, null);
    }

    public static PlayerRestriction only(Player p) {
        return new PlayerRestriction(p, null);
    }

    public static PlayerRestriction except(Player p) {
        return new PlayerRestriction(null, p);
    }

    public boolean isAllowed(Player p) {
        if (exclude != null && p.equals(exclude)) return false;
        if (include != null && !p.equals(include)) return false;
        return true;
    }

}
