package com.jcloisterzone.integration;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.junit.Test;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.event.MeepleEvent;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.event.TowerIncreasedEvent;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.phase.ActionPhase;
import com.jcloisterzone.game.phase.Phase;
import com.jcloisterzone.game.phase.TilePhase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UndoableTest extends AbstractIntegrationTest {


    @Test
    public void placeTileUndo() {
        EventCatchingGame game = createGame("/saved-games/tilePhase.jcz");
        assertTrue(game.getPhase() instanceof TilePhase);

        String s1 = snapshotGame(game);

        Phase phase = game.getPhase();
        phase.placeTile(Rotation.R90, new Position(-2, 0));
        assertEquals(1, game.events.size());

        TileEvent ev = (TileEvent) game.events.get(0);
        ev.undo(game);
        game.setPhase(phase);

        String s2 = snapshotGame(game);
        assertEquals(s1, s2);
    }

    @Test
    public void placeFolloweUndo() {
        EventCatchingGame game = createGame("/saved-games/actionPhase.jcz");
        assertTrue(game.getPhase() instanceof ActionPhase);

        String s1 = snapshotGame(game);
        Phase phase = game.getPhase();
        phase.deployMeeple(new Position(1, -3), Location.NE, BigFollower.class);
        assertEquals(1, game.events.size());

        MeepleEvent ev = (MeepleEvent) game.events.get(0);
        ev.undo(game);
        game.setPhase(phase);

        String s2 = snapshotGame(game);
        assertEquals(s1, s2);
    }

    @Test
    public void placeFolloweOnTowerUndo() {
        EventCatchingGame game = createGame("/saved-games/actionPhase.jcz");
        assertTrue(game.getPhase() instanceof ActionPhase);

        String s1 = snapshotGame(game);
        Phase phase = game.getPhase();
        phase.deployMeeple(new Position(1, -2), Location.TOWER, SmallFollower.class);
        assertEquals(1, game.events.size());

        MeepleEvent ev = (MeepleEvent) game.events.get(0);
        ev.undo(game);
        game.setPhase(phase);

        String s2 = snapshotGame(game);
        assertEquals(s1, s2);
    }

    @Test
    public void placeTowerPieceUndo() {
        EventCatchingGame game = createGame("/saved-games/actionPhase.jcz");
        assertTrue(game.getPhase() instanceof ActionPhase);

        String s1 = snapshotGame(game);
        Phase phase = game.getPhase();
        phase.placeTowerPiece(new Position(1, -2));
        assertEquals(2, game.events.size()); //TowerIncreasedEvent, SelectActionEvent

        TowerIncreasedEvent ev = (TowerIncreasedEvent) game.events.get(0);
        ev.undo(game);
        game.setPhase(phase);

        String s2 = snapshotGame(game);
        assertEquals(s1, s2);
    }

}
