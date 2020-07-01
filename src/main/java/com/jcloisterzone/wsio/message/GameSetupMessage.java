package com.jcloisterzone.wsio.message;


import com.jcloisterzone.wsio.WsMessageCommand;

import java.util.ArrayList;
import java.util.Map;


@WsMessageCommand("GAME_SETUP")
public class GameSetupMessage extends AbstractWsMessage implements WsMessage {

    private Map<String, Integer> sets;
    private Map<String, Object> elements;
    private Map<String, Object> rules;
    private Map<String, Object> timer;
    private ArrayList<PlayerSetup> players;

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

    public ArrayList<PlayerSetup> getPlayers() {
        return players;
    }

    public static class PlayerSetup {
        private int slot;
        private String state;
        private String name;

        public int getSlot() {
            return slot;
        }

        public String getState() {
            return state;
        }

        public String getName() {
            return name;
        }
    }
}
