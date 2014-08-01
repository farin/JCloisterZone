package com.jcloisterzone.integration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import com.jcloisterzone.config.Config;
import com.jcloisterzone.event.Event;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.phase.LoadGamePhase;


public class AbstractIntegrationTest {

    public static class EventCatchingGame extends Game {

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
            game.setConfig(new Config());
            LoadGamePhase phase = new LoadGamePhase(game, snapshot, null, null);
            game.getPhases().put(phase.getClass(), phase);
            game.setPhase(phase);
            phase.setSlots(new PlayerSlot[0]);
            phase.startGame();
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
