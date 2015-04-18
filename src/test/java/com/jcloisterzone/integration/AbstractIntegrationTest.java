package com.jcloisterzone.integration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.jcloisterzone.event.Event;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.phase.LoadGamePhase;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.MutedConnection;


public class AbstractIntegrationTest {

    public static class EventCatchingGame extends Game {

        public EventCatchingGame() {
            super("12345678");
        }

        public List<Event> events = new ArrayList<>();

        @Override
        public void post(Event event) {
            events.add(event);
            super.post(event);
        }
    }

    protected EventCatchingGame createGame(String save) {
        try {
            URI uri = getClass().getResource(save).toURI();
            Snapshot snapshot = new Snapshot(new File(uri));
            EventCatchingGame game = (EventCatchingGame) snapshot.asGame(new EventCatchingGame());
            GameController gc = new GameController(null, game);
            gc.setConnection(new MutedConnection(null));
            LoadGamePhase phase = new LoadGamePhase(game, snapshot, gc);
            game.getPhases().put(phase.getClass(), phase);
            game.setPhase(phase);
            phase.setSlots(new PlayerSlot[0]);
            phase.startGame(false);
            game.events.clear();
            return game;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected String snapshotGame(Game game) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Snapshot snapshot = new Snapshot(game);
            snapshot.setGzipOutput(false);
            snapshot.save(os);
            return os.toString("utf-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
