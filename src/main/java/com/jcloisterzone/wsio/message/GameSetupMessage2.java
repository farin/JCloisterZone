package com.jcloisterzone.wsio.message;


import com.jcloisterzone.wsio.WsMessageCommand;

import java.util.ArrayList;
import java.util.Map;


@WsMessageCommand("GAME_SETUP_2")
public class GameSetupMessage2 extends AbstractWsMessage implements WsMessage {

    private Map<String, Integer> sets;
    private Map<String, Object> elements;
    private Map<String, Object> rules;
    private Map<String, Object> timer;
    private ArrayList<Slot> slots;

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

    public ArrayList<Slot> getSlots() {
        return slots;
    }

    public static class Slot {
        private int number;
        private String state;
        private String name;
        private Integer order;

        public int getNumber() {
            return number;
        }

        public String getState() {
            return state;
        }

        public String getName() {
            return name;
        }

        public Integer getOrder() {
            return order;
        }
    }
}
