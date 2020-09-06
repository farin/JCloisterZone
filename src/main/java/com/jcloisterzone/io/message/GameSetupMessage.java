package com.jcloisterzone.io.message;


import com.jcloisterzone.io.MessageCommand;

import java.util.ArrayList;
import java.util.Map;


@MessageCommand("GAME_SETUP")
public class GameSetupMessage extends AbstractMessage implements Message {

    private Map<String, Integer> sets;
    private Map<String, Object> elements;
    private Map<String, Object> rules;
    private Map<String, Object> timer;
    private int players;
    private ArrayList<PlacedTileItem> start;
    private Map<String, Object> gameAnnotations;
    private String initialSeed;

    public Map<String, Integer> getSets() {
        return sets;
    }

    public Map<String, Object> getElements() {
        return elements;
    }

    public Map<String, Object> getRules() {
        return rules;
    }

    public Map<String, Object> getTimer() {
        return timer;
    }

    public int getPlayers() {
        return players;
    }

    public ArrayList<PlacedTileItem> getStart() {
        return start;
    }

    public Map<String, Object> getGameAnnotations() {
        return gameAnnotations;
    }

    public String getInitialSeed() {
        return initialSeed;
    }


    public static class PlacedTileItem {
        private String tile;
        private int x;
        private int y;
        private int rotation;

        public String getTile() {
            return tile;
        }
        public int getX() {
            return x;
        }
        public int getY() {
            return y;
        }
        public int getRotation() {
            return rotation;
        }
    }
}
