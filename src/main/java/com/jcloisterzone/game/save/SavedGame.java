package com.jcloisterzone.game.save;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.JsonAdapter;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.PlayerClock;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.GameSetup;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.ui.JCloisterZone;
import com.jcloisterzone.wsio.message.WsReplayableMessage;
import com.jcloisterzone.wsio.message.adapters.CapabilitiesSetAdapter;
import com.jcloisterzone.wsio.message.adapters.ExpansionMapAdapter;
import com.jcloisterzone.wsio.message.adapters.RulesMapAdapter;


/**
 * Represents a saved game. This includes information of all players as well as replay data.
 */
public class SavedGame implements Serializable {

    private static final long serialVersionUID = 1L;

    private String gameId;
    private String name;
    private String appVersion;
    private long initialSeed;
    private Date created;
    private List<SavedGamePlayerSlot> slots;
    private long clock;
    private SavedGameSetup setup;
    private List<WsReplayableMessage> replay;
    private HashMap<String, Object> annotations;

    /**
     * Instantiates an empty {@code SavedGame}.
     */
    public SavedGame() {
    }

    /**
     * Instantiates a {@code SavedGame} with the data of {@code game}.
     *
     * @param game the game to store
     */
    public SavedGame(Game game) {
        gameId = game.getGameId();
        name = game.getName();
        appVersion = JCloisterZone.VERSION;
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
        clock = System.currentTimeMillis() - game.getClockStart();
        setup = new SavedGameSetup();
        setup.setExpansions(game.getSetup().getExpansions().toJavaMap());
        setup.setRules(game.getSetup().getRules().toJavaMap());
        setup.setCapabilities(game.getSetup().getCapabilities().toJavaSet());
        replay = game.getReplay().reverse().toJavaList();
    }

    /**
     * Gets setup data. This includes rules, expansions and map.
     *
     * @return the setup data
     */
    public SavedGameSetup getSetup() {
        return setup;
    }

    /**
     * Sets setup data.
     *
     * @param setup the setup data
     */
    public void setSetup(SavedGameSetup setup) {
        this.setup = setup;
    }

    /**
     * Gets replay data.
     *
     * @return the replay data
     */
    public List<WsReplayableMessage> getReplay() {
        return replay;
    }

    /**
     * Sets replay data.
     *
     * @param replay the replay data
     */
    public void setReplay(List<WsReplayableMessage> replay) {
        this.replay = replay;
    }

    /**
     * Gets the game id.
     *
     * @return the game id
     */
    public String getGameId() {
        return gameId;
    }

    /**
     * Sets the game id.
     *
     * @param gameId the game id
     */
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    /**
     * Gets game name.
     *
     * @return the game name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets game name.
     *
     * @param name the game name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the app version.
     *
     * @return the app version
     */
    public String getAppVersion() {
        return appVersion;
    }

    /**
     * Sets the app version.
     *
     * @param appVersion the app version
     */
    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    /**
     * Gets the date of creation.
     *
     * @return the date of creation
     */
    public Date getCreated() {
        return created;
    }

    /**
     * Sets the date of creation.
     *
     * @param created the date of creation
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * Gets the initial randomness seed.
     *
     * @return the initial randomness seed
     */
    public long getInitialSeed() {
        return initialSeed;
    }

    /**
     * Sets the initial randomness seed.
     *
     * @param initialSeed the initial randomness seed
     */
    public void setInitialSeed(long initialSeed) {
        this.initialSeed = initialSeed;
    }

    /**
     * Gets players slots data.
     *
     * @return the players slots data
     */
    public List<SavedGamePlayerSlot> getSlots() {
        return slots;
    }

    /**
     * Sets players slots data.
     *
     * @param slots the players slots data
     */
    public void setSlots(List<SavedGamePlayerSlot> slots) {
        this.slots = slots;
    }


    /**
     * Gets the clocks of players.
     *
     * @return the game clock (number of ms from start)
     */
    public long getClock() {
        return clock;
    }

    /**
     * Sets the clocks of players.
     *
     * @param clock the the clocks of players
     */
    public void setClock(long clock) {
        this.clock = clock;
    }

    /**
     * Gets the annotations.
     *
     * @return the annotations
     */
    public HashMap<String, Object> getAnnotations() {
        return annotations;
    }

    /**
     * Sets the annotations.
     *
     * @param annotations the annotations
     */
    public void setAnnotations(HashMap<String, Object> annotations) {
        this.annotations = annotations;
    }


    /**
     * Represents a save player slot
     */
    public static class SavedGamePlayerSlot {
        private final int number;
        private Integer serial;
        private String clientId;
        private String nickname;
        private String aiClassName;

        /**
         * Instantiates a new {@code SavedGamePlayerSlot} given the necessary data.
         *
         * @param number      the number
         * @param serial      the serial
         * @param clientId    the client id
         * @param nickname    the nickname
         * @param aiClassName the ai class name
         */
        public SavedGamePlayerSlot(int number, Integer serial, String clientId, String nickname, String aiClassName) {
            this.number = number;
            this.serial = serial;
            this.clientId = clientId;
            this.nickname = nickname;
            this.aiClassName = aiClassName;
        }

        /**
         * Gets the serial.
         *
         * @return the serial
         */
        public Integer getSerial() {
            return serial;
        }

        /**
         * Sets the serial.
         *
         * @param serial the serial
         */
        public void setSerial(Integer serial) {
            this.serial = serial;
        }

        /**
         * Gets the player client id.
         *
         * @return the client id
         */
        public String getClientId() {
            return clientId;
        }

        /**
         * Sets the player client id.
         *
         * @param clientId the client id
         */
        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        /**
         * Gets the player nickname.
         *
         * @return the player nickname
         */
        public String getNickname() {
            return nickname;
        }

        /**
         * Sets the player nickname.
         *
         * @param nickname the player nickname
         */
        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        /**
         * Gets the class name of the AI.
         *
         * @return the AI class name, if any, {@code null} otherwise
         */
        public String getAiClassName() {
            return aiClassName;
        }

        /**
         * Sets the class name of the AI.
         *
         * @param aiClassName the AI class name
         */
        public void setAiClassName(String aiClassName) {
            this.aiClassName = aiClassName;
        }

        /**
         * Gets the slot number.
         *
         * @return the slot number
         */
        public int getNumber() {
            return number;
        }
    }

    /**
     * Represents a saved game setup.
     */
    public static class SavedGameSetup {
        @JsonAdapter(ExpansionMapAdapter.class)
        private Map<Expansion, Integer> expansions;
        @JsonAdapter(RulesMapAdapter.class)
        private Map<Rule, Object> rules;
        @JsonAdapter(CapabilitiesSetAdapter.class)
        private Set<Class<? extends Capability<?>>> capabilities;

        /**
         * Gets the expansions.
         *
         * @return the expansions
         */
        public Map<Expansion, Integer> getExpansions() {
            return expansions;
        }

        /**
         * Sets the expansions.
         *
         * @param expansions the expansions
         */
        public void setExpansions(Map<Expansion, Integer> expansions) {
            this.expansions = expansions;
        }

        /**
         * Gets the rules.
         *
         * @return the rules
         */
        public Map<Rule, Object> getRules() {
            return rules;
        }

        /**
         * Sets the rules.
         *
         * @param rules the rules
         */
        public void setRules(Map<Rule, Object> rules) {
            this.rules = rules;
        }

        /**
         * Gets the capabilities.
         *
         * @return the capabilities
         */
        public Set<Class<? extends Capability<?>>> getCapabilities() {
            return capabilities;
        }

        /**
         * Sets the capabilities.
         *
         * @param capabilities the capabilities
         */
        public void setCapabilities(Set<Class<? extends Capability<?>>> capabilities) {
            this.capabilities = capabilities;
        }

        /**
         * Returns a {@link GameSetup} instance based on {@code this}.
         *
         * @return the game setup instance
         */
        public GameSetup asGameSetup() {
            return new GameSetup(
                io.vavr.collection.HashMap.ofAll(expansions),
                io.vavr.collection.HashSet.ofAll(capabilities),
                io.vavr.collection.HashMap.ofAll(rules)
            );
        }
    }
}
