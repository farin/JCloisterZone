package com.jcloisterzone.game.save;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jcloisterzone.Application;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.GameSetup;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.wsio.message.WsReplayableMessage;


public class SavedGame implements Serializable {

    private static final long serialVersionUID = 1L;

    private String gameId;
    private String name;
    private String appVersion;
    private long initialSeed;
    private Date created;
    private List<SavedGamePlayerSlot> slots;
    private long[] clocks;
    private SavedGameSetup setup;
    private List<WsReplayableMessage> replay;
    private HashMap<String, Object> annotations;

    public SavedGame() {
    }

    public SavedGame(Game game) {
        gameId = game.getGameId();
        name = game.getName();
        appVersion = Application.VERSION;
        initialSeed = game.getInitialSeed();
        created = new Date();
        slots = new ArrayList<>();
        for (PlayerSlot slot : game.getPlayerSlots()) {
            if (slot != null && slot.isOccupied()) {
                slots.add(new SavedGamePlayerSlot(
                    slot.getNumber(),
                    slot.getSerial(),
                    slot.getClientId(),
                    slot.getNickname(),
                    slot.getAiClassName()
                ));
            }
        }
        clocks = game.getClocks().map(c -> c.getTime()).toJavaStream().mapToLong(Long::longValue).toArray();
        setup = new SavedGameSetup();
        setup.setExpansions(game.getSetup().getExpansions().toJavaSet());
        setup.setRules(game.getSetup().getRules().toJavaMap());
        replay = game.getReplay().reverse().toJavaList();
    }

    public SavedGameSetup getSetup() {
        return setup;
    }

    public void setSetup(SavedGameSetup setup) {
        this.setup = setup;
    }

    public List<WsReplayableMessage> getReplay() {
        return replay;
    }

    public void setReplay(List<WsReplayableMessage> replay) {
        this.replay = replay;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public long getInitialSeed() {
        return initialSeed;
    }

    public void setInitialSeed(long initialSeed) {
        this.initialSeed = initialSeed;
    }

    public List<SavedGamePlayerSlot> getSlots() {
        return slots;
    }

    public void setSlots(List<SavedGamePlayerSlot> slots) {
        this.slots = slots;
    }


    public long[] getClocks() {
        return clocks;
    }

    public void setClocks(long[] clocks) {
        this.clocks = clocks;
    }

    public HashMap<String, Object> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(HashMap<String, Object> annotations) {
        this.annotations = annotations;
    }



    public static class SavedGamePlayerSlot {
        private final int number;
        private Integer serial;
        private String clientId;
        private String nickname;
        private String aiClassName;

        public SavedGamePlayerSlot(int number, Integer serial, String clientId, String nickname, String aiClassName) {
            this.number = number;
            this.serial = serial;
            this.clientId = clientId;
            this.nickname = nickname;
            this.aiClassName = aiClassName;
        }

        public Integer getSerial() {
            return serial;
        }

        public void setSerial(Integer serial) {
            this.serial = serial;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getAiClassName() {
            return aiClassName;
        }

        public void setAiClassName(String aiClassName) {
            this.aiClassName = aiClassName;
        }

        public int getNumber() {
            return number;
        }
    }

    public static class SavedGameSetup {
        private Set<Expansion> expansions;
        private Map<CustomRule, Object> rules;

        public Set<Expansion> getExpansions() {
            return expansions;
        }

        public void setExpansions(Set<Expansion> expansions) {
            this.expansions = expansions;
        }

        public Map<CustomRule, Object> getRules() {
            return rules;
        }

        public void setRules(Map<CustomRule, Object> rules) {
            this.rules = rules;
        }

        public GameSetup asGameSetup() {
            return new GameSetup(
                io.vavr.collection.HashSet.ofAll(expansions),
                io.vavr.collection.HashMap.ofAll(rules)
            );
        }
    }
}
