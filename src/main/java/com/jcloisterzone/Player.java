package com.jcloisterzone;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.PlayerSlot;


/**
 * Represents one player in game. Contains information about figures, points and
 * control informations.<br>
 *
 * @author Roman Krejcik
 */
public class Player implements Serializable {

    private static final long serialVersionUID = -7276471952562769832L;

    private int points;
    private final Map<PointCategory, Integer> pointStats = Maps.newHashMap();

    private List<Follower> followers = new ArrayList<Follower>(SmallFollower.QUANTITY + 3);
    private List<Special> specialMeeples = new ArrayList<Special>(3);

    final private String nick;
    final private int index;
    private PlayerSlot slot;

    public Player(String nick, int index, PlayerSlot slot) {
        this.nick = nick;
        this.index = index;
        this.slot = slot;
    }

    public void addMeeple(Meeple meeple) {
        if (meeple instanceof Follower) {
            followers.add((Follower) meeple);
        } else {
            specialMeeples.add((Special) meeple);
        }
    }

    public List<Follower> getFollowers() {
        return followers;
    }

    public List<Special> getSpecialMeeples() {
        return specialMeeples;
    }

    public boolean hasSpecialMeeple(Class<? extends Special> clazz) {
        assert !Modifier.isAbstract(clazz.getModifiers());
        for (Special m : specialMeeples) {
            if (!m.isDeployed() && clazz.equals(m.getClass())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasFollower() {
        for (Follower m : followers) {
            if (!m.isDeployed()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasFollower(Class<? extends Follower> clazz) {
        assert !Modifier.isAbstract(clazz.getModifiers());
        for (Follower m : followers) {
            //no instance of! bacause phantom is subclass of small follower
            if (!m.isDeployed() && clazz.equals(m.getClass())) {
                return true;
            }
        }
        return false;
    }


    public Meeple getUndeployedMeeple(Class<? extends Meeple> clazz) {
        assert !Modifier.isAbstract(clazz.getModifiers());
        if (Follower.class.isAssignableFrom(clazz)) {
            for(Follower m : followers) {
                if (!m.isDeployed() && clazz.equals(m.getClass())) return m;
            }
        } else {
            for(Special m : specialMeeples) {
                if (!m.isDeployed() && clazz.equals(m.getClass())) return m;
            }
        }
        return null;
    }

    public void addPoints(int points, PointCategory category) {
        this.points += points;
        if (pointStats.containsKey(category)) {
            pointStats.put(category, pointStats.get(category) + points);
        } else {
            pointStats.put(category, points);
        }
    }

    public int getPoints() {
        return points;
    }

    public String getNick() {
        return nick;
    }

    @Override
    public String toString() {
        return nick + " " + points;
    }

    public int getIndex() {
        return index;
    }

    public Long getOwnerId() {
        return slot.getOwner();
    }

    public PlayerSlot getSlot() {
        return slot;
    }
    public void setSlot(PlayerSlot slot) {
        this.slot = slot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof Player) {
            if (((Player) o).index == index && index != -1)
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (index == -1) {
            return nick.hashCode();
        } else {
            return index;
        }
    }

    public void setPoints(int points) {
        this.points = points;
    }


    public int getPointsInCategory(PointCategory cat) {
        Integer points = pointStats.get(cat);
        return points == null ? 0 : points;
    }

}
