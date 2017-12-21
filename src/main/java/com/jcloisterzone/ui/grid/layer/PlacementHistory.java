package com.jcloisterzone.ui.grid.layer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.event.play.PlayerTurnEvent;
import com.jcloisterzone.event.play.TilePlacedEvent;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.grid.GridPanel;

public class PlacementHistory extends AbstractGridLayer {

    private static final Color DEFAULT_COLOR = Color.DARK_GRAY;
    private static final Position ZERO = new Position(0, 0);
    private static final ImmutablePoint POINT = new ImmutablePoint(50,50);
    private static final ImmutablePoint SHADOW_POINT = new ImmutablePoint(51,51);
    private static final AlphaComposite ALPHA_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .6f);


    public PlacementHistory(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    private Player getTriggeringPlayer(GameState state, PlayEvent ev) {
        Integer idx = ev.getMetadata().getTriggeringPlayerIndex();
        return idx == null ? null : state.getPlayers().getPlayer(idx);
    }

    @Override
    public void paint(Graphics2D g) {
        Composite oldComposite = g.getComposite();
        g.setComposite(ALPHA_COMPOSITE);

        GameState state = gc.getGame().getState();
        Player turnPlayer = state.getTurnPlayer();
        int counter = 0;

        boolean breakOnTurnEvent = false;
        boolean turnEventSeen = false;
        Boolean placedCurrentTurn = null;

        ArrayList<TilePlacedEvent> buffer = new ArrayList<>();

        for (PlayEvent ev : state.getEvents().reverseIterator()) {
            if (ev instanceof PlayerTurnEvent) {
                if (breakOnTurnEvent) break;

                turnEventSeen = true;
                if (placedCurrentTurn == null) {
                    placedCurrentTurn = false;
                }

                Player player = ((PlayerTurnEvent)ev).getPlayer();
                if (player != null && player.getPrevPlayer(state).equals(turnPlayer)) {
                    if (placedCurrentTurn) {
                        break;
                    } else {
                        breakOnTurnEvent = true;
                    }
                }
            }

            if (!(ev instanceof TilePlacedEvent)) continue;

            TilePlacedEvent te = (TilePlacedEvent) ev;

            if (placedCurrentTurn == null && !turnEventSeen) {
                placedCurrentTurn = true;
            }

            buffer.add(te);
        }

        while (!buffer.isEmpty()) {
            int lastIdx = buffer.size() - 1;
            TilePlacedEvent te = buffer.get(lastIdx);
            if (te.getMetadata().getTriggeringPlayerIndex() != null) {
                break;
            }
            buffer.remove(lastIdx);
        }

        for (TilePlacedEvent te : buffer) {
            Position pos = te.getPosition();
            Player player = getTriggeringPlayer(state, te);
            String text = String.valueOf(++counter);
            Color color = player != null ?  player.getColors().getFontColor() : DEFAULT_COLOR;

            BufferedImage buf = UiUtils.newTransparentImage(getTileWidth(), getTileHeight());
            Graphics2D gb = (Graphics2D) buf.getGraphics();
            drawAntialiasedTextCentered(gb, text, 80, ZERO, POINT, color, null);
            gb.setComposite(AlphaComposite.DstOver);
            drawAntialiasedTextCentered(gb, text, 80, ZERO, SHADOW_POINT, Color.GRAY, null);
            g.drawImage(buf, null, getOffsetX(pos), getOffsetY(pos));
        }

        g.setComposite(oldComposite);
    }
}
