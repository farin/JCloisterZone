package com.jcloisterzone.game.phase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ClassToInstanceMap;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.ai.AiPlayer;
import com.jcloisterzone.ai.AiUserInterfaceAdapter;
import com.jcloisterzone.board.DefaultTilePack;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TilePackFactory;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.PlayerSlot.SlotType;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.rmi.ServerIF;


public class CreateGamePhase extends ServerAwarePhase {

    private final class PlayerSlotComparator implements Comparator<PlayerSlot> {
        @Override
        public int compare(PlayerSlot o1, PlayerSlot o2) {
            if (o1.getSerial() == null) {
                return o2.getSerial() == null ? 0 : 1;
            }
            if (o2.getSerial() == null) return -1;
            if (o1.getSerial() < o2.getSerial()) return -1;
            if (o1.getSerial() > o2.getSerial()) return 1;
            return 0;
        }
    }

    protected PlayerSlot[] slots;

    public CreateGamePhase(Game game, ServerIF server) {
        super(game, server);
    }

    public void setSlots(PlayerSlot[] slots) {
        this.slots = slots;
    }

    public PlayerSlot[] getPlayerSlots() {
        return slots;
    }

    @Override
    public void updateCustomRule(CustomRule rule, Boolean enabled) {
        if (enabled) {
            game.getCustomRules().add(rule);
        } else {
            game.getCustomRules().remove(rule);
        }
        game.fireGameEvent().updateCustomRule(rule, enabled);
    }

    @Override
    public void updateExpansion(Expansion expansion, Boolean enabled) {
        if (enabled) {
            game.getExpansions().add(expansion);
        } else {
            game.getExpansions().remove(expansion);
        }
        game.fireGameEvent().updateExpansion(expansion, enabled);
    }

    @Override
    public void updateSlot(PlayerSlot slot) {
        slots[slot.getNumber()] = slot;
        game.fireGameEvent().updateSlot(slot);
    }

    @Override
    public void updateSupportedExpansions(EnumSet<Expansion> expansions) {
        game.fireGameEvent().updateSupportedExpansions(expansions);
    }


    private Phase addPhase(Phase next, Phase phase) {
        if (!phase.isActive()) return next;

        ClassToInstanceMap<Phase> phases = game.getPhases();
        phases.put(phase.getClass(), phase);
        if (next != null) {
            phase.setDefaultNext(next);
        }
        return phase;
    }

    protected void preparePhases() {
        Phase next = null;
        //if there isn't assignment - phase is out of standard flow
               addPhase(next, new GameOverPhase(game));
        next = addPhase(next, new CleanUpPhase(game));
        next = addPhase(next, new BazaarPhase(game, getServer()));
        next = addPhase(next, new CornCirclePhase(game));
        next = addPhase(next, new EscapePhase(game));
        next = addPhase(next, new WagonPhase(game));
        next = addPhase(next, new ScorePhase(game));
        next = addPhase(next, new CastlePhase(game));
               addPhase(next, new DragonMovePhase(game));
        next = addPhase(next, new DragonPhase(game));
        next = addPhase(next, new PhantomPhase(game));
               addPhase(next, new TowerCapturePhase(game));
               addPhase(next, new FlierActionPhase(game));
        next = addPhase(next, new ActionPhase(game));
        next = addPhase(next, new PlaguePhase(game));
        next = addPhase(next, new TilePhase(game));
        next = addPhase(next, new DrawPhase(game, getServer()));
        next = addPhase(next, new AbbeyPhase(game));
        next = addPhase(next, new FairyPhase(game));
        setDefaultNext(next); //set next phase for this (CreateGamePhase) instance
        game.getPhases().get(CleanUpPhase.class).setDefaultNext(next); //after last phase, the first is default
    }

    private void createPlayers() {
        List<Player> players = new ArrayList<>();
        Arrays.sort(slots, new PlayerSlotComparator());
        for (int i = 0; i < slots.length; i++) {
            PlayerSlot slot = slots[i];
            if (slot.isOccupied()) {
                Player player = new Player(slot.getNick(), i, slot);
                players.add(player);
            }
        }
        if (players.isEmpty()) {
            throw new IllegalStateException("No players in game");
        }
        game.setPlayers(players, 0);
    }

    protected Snapshot getSnapshot() {
        return null;
    }

    protected void initializePlayersMeeples() {
        for (Player player : game.getAllPlayers()) {
            for (int i = 0; i < SmallFollower.QUANTITY; i++) {
                player.addMeeple(new SmallFollower(game, player));
            }
            game.initPlayer(player);
        }
    }

    protected void preparePlayers() {
        createPlayers();
        initializePlayersMeeples();
    }

    protected void prepareTilePack() {
        TilePackFactory tilePackFactory = new TilePackFactory();
        tilePackFactory.setGame(game);
        tilePackFactory.setConfig(game.getConfig());
        tilePackFactory.setExpansions(game.getExpansions());
        game.setTilePack(tilePackFactory.createTilePack());
        getTilePack().activateGroup("default");
        getTilePack().activateGroup("count");
        getTilePack().activateGroup("wind-rose-initial");
        game.begin();
    }

    protected void preplaceTiles() {
        for (Tile preplaced : ((DefaultTilePack)getTilePack()).drawPrePlacedActiveTiles()) {
            game.getBoard().add(preplaced, preplaced.getPosition(), true);
            game.getBoard().mergeFeatures(preplaced);
            game.fireGameEvent().tilePlaced(preplaced);
        }
    }

    protected void prepareAiPlayers() {
        for (PlayerSlot slot : slots) {
            if (slot != null && slot.getType() == SlotType.AI && isLocalSlot(slot)) {
                try {
                    AiPlayer ai = (AiPlayer) Class.forName(slot.getAiClassName()).newInstance();
                    ai.setGame(game);
                    ai.setServer(getServer());
                    for (Player player : game.getAllPlayers()) {
                        if (player.getSlot().getNumber() == slot.getNumber()) {
                            ai.setPlayer(player);
                            break;
                        }
                    }
                    game.addUserInterface(new AiUserInterfaceAdapter(ai));
                    logger.info("AI player created - " + slot.getAiClassName());
                } catch (Exception e) {
                    logger.error("Unable to create AI player", e);
                }
            }
        }
    }

    protected void prepareCapabilities() {
        for (Expansion exp : game.getExpansions()) {
            game.getCapabilityClasses().addAll(Arrays.asList(exp.getCapabilities()));
        }

        String offVal = game.getConfig().get("debug", "off_capabilities");
        Set<Class<? extends Capability>> off = new HashSet<>();
        if (offVal != null) {
            for (String tok : offVal.split(",")) {
                tok = tok.trim();
                try {
                    String className = "com.jcloisterzone.game.capability."+tok+"Capability";
                    @SuppressWarnings("unchecked")
                    Class<? extends Capability> clazz = (Class<? extends Capability>) Class.forName(className);
                    off.add(clazz);
                } catch (Exception e) {
                    logger.warn("Invalid capability name: " + tok, e);
                }
            }
        }
        game.getCapabilityClasses().removeAll(off);
    }

    @Override
    public void startGame() {
        //temporary code should be configured by player as rules
        prepareCapabilities();

        game.start();
        preparePlayers();
        preparePhases();
        prepareTilePack();
        prepareAiPlayers();

        game.fireGameEvent().started(getSnapshot());
        preplaceTiles();
        game.fireGameEvent().playerActivated(game.getTurnPlayer(), getActivePlayer());

        next();
    }

}