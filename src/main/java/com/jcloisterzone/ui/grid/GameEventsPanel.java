package com.jcloisterzone.ui.grid;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.event.play.FollowerCaptured;
import com.jcloisterzone.event.play.MeepleDeployed;
import com.jcloisterzone.event.play.MeepleReturned;
import com.jcloisterzone.event.play.NeutralFigureMoved;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.event.play.PlayerTurnEvent;
import com.jcloisterzone.event.play.ScoreEvent;
import com.jcloisterzone.event.play.TileDiscardedEvent;
import com.jcloisterzone.event.play.TilePlacedEvent;
import com.jcloisterzone.event.play.TokenPlacedEvent;
import com.jcloisterzone.event.play.TokenReceivedEvent;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.neutral.Count;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.eventpanel.EventItem;
import com.jcloisterzone.ui.grid.eventpanel.ImageEventItem;
import com.jcloisterzone.ui.grid.eventpanel.ScoreEventItem;
import com.jcloisterzone.ui.grid.layer.EventsOverlayLayer;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;
import com.jcloisterzone.ui.resources.ResourceManager;
import com.jcloisterzone.ui.resources.TileImage;
import com.jcloisterzone.ui.theme.Theme;

import io.vavr.Function1;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Queue;

public class GameEventsPanel extends JPanel {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final int ICON_WIDTH = 30;

    private EventsOverlayLayer eventsOverlayPanel;

    private GameState state;
    private ArrayList<EventItem> model = new ArrayList<>();
    private Integer mouseOverIdx;
    private int skipItems;

    Map<Class<? extends PlayEvent>, Function1<PlayEvent, EventItem>> mapping;

    protected final Theme theme;
    protected final ResourceManager rm;

    private Color turnColor, triggeringColor;

    public GameEventsPanel(GameController gc) {
        theme = gc.getClient().getTheme();
        setBackground(theme.getEventsBg());

        rm = gc.getClient().getResourceManager();

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int idx = skipItems + e.getX() / ICON_WIDTH;
                setMouseOverIdx(idx < model.size() ? idx : null);
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                setMouseOverIdx(null);
            }
        });

        mapping = HashMap.empty();
        mapping = mapping.put(TilePlacedEvent.class, this::processTilePlacedEvent);
        mapping = mapping.put(TileDiscardedEvent.class, this::processTileDiscardedEvent);
        mapping = mapping.put(MeepleDeployed.class, this::processMeepleDeployedEvent);
        mapping = mapping.put(FollowerCaptured.class, this::processFollowerCaptured);
        mapping = mapping.put(ScoreEvent.class, this::processScoreEvent);
        mapping = mapping.put(NeutralFigureMoved.class, this::processNeutralFigureMoved);
        mapping = mapping.put(TokenPlacedEvent.class, this::processTokenPlacedEvent);
        mapping = mapping.put(TokenReceivedEvent.class, this::processTokenReceivedEvent);
    }

    private EventItem processTilePlacedEvent(PlayEvent _ev) {
        TilePlacedEvent ev = (TilePlacedEvent) _ev;
        TileImage img = rm.getTileImage(ev.getTile().getId(), ev.getRotation());
        ImageEventItem item = new ImageEventItem(ev, turnColor, triggeringColor);
        item.setImage(img.getImage());

        item.setHighlightedPosition(ev.getPosition());
        return item;
    }

    private EventItem processTileDiscardedEvent(PlayEvent _ev) {
        TileDiscardedEvent ev = (TileDiscardedEvent) _ev;
        TileImage img = rm.getTileImage(ev.getTile().getId(), Rotation.R0);
        ImageEventItem item = new ImageEventItem(ev, turnColor, triggeringColor);
        item.setImage(img.getImage());
        item.setDrawCross(true);
        return item;
    }

    private EventItem processMeepleDeployedEvent(PlayEvent _ev) {
        MeepleDeployed ev = (MeepleDeployed) _ev;
        return getMeepleItem(ev, ev.getMeeple(), ev.getPointer().asFeaturePointer());
    }

    private EventItem processFollowerCaptured(PlayEvent _ev) {
        FollowerCaptured ev = (FollowerCaptured) _ev;
        ImageEventItem item = getMeepleItem(ev, ev.getFollower(), ev.getFrom().asFeaturePointer());
        item.setDrawCross(true);
        return item;
    }

    private EventItem processScoreEvent(PlayEvent _ev) {
        ScoreEvent ev = (ScoreEvent) _ev;
        ScoreEventItem item = new ScoreEventItem(theme, ev, turnColor, triggeringColor);

        Feature feature = state.getFeature(ev.getFeaturePointer());
        item.setHighlightedFeature(feature);
        return item;
    }

    private EventItem processNeutralFigureMoved(PlayEvent _ev) {
        NeutralFigureMoved ev = (NeutralFigureMoved) _ev;
        Image img = rm.getImage("neutral/" + ev.getNeutralFigure().getClass().getSimpleName().toLowerCase());
        ImageEventItem item = new ImageEventItem(ev, turnColor, triggeringColor);
        item.setImage(img);

        if (ev.getNeutralFigure() instanceof Count) {
            Feature feature = state.getFeature(ev.getTo().asFeaturePointer());
            item.setHighlightedFeature(feature);
        } else {
            item.setHighlightedPosition(ev.getTo().getPosition());
        }
        return item;
    }

    private EventItem processTokenPlacedEvent(PlayEvent _ev) {
        TokenPlacedEvent ev = (TokenPlacedEvent) _ev;
        Image img = rm.getImage("neutral/" + ev.getToken().name().toLowerCase());
        ImageEventItem item = new ImageEventItem(ev, turnColor, triggeringColor);
        item.setImage(img);

        item.setHighlightedPosition(ev.getPointer().getPosition());
        return item;
    }

    private EventItem processTokenReceivedEvent(PlayEvent _ev) {
        TokenReceivedEvent ev = (TokenReceivedEvent) _ev;
        Image img = rm.getImage("neutral/" + ev.getToken().name().toLowerCase());
        ImageEventItem item = new ImageEventItem(ev, turnColor, triggeringColor);
        item.setImage(img);

        if (ev.getSourceFeature() != null) {
            item.setHighlightedFeature(ev.getSourceFeature() );
        }
        if (ev.getSourcePosition() != null) {
            item.setHighlightedPosition(ev.getSourcePosition());
        }
        return item;
    }



    private ImageEventItem getMeepleItem(PlayEvent ev, Meeple meeple, FeaturePointer fp) {
        Image img = rm.getLayeredImage(new LayeredImageDescriptor(meeple.getClass(), triggeringColor));
        ImageEventItem item = new ImageEventItem(ev, turnColor, triggeringColor);
        item.setImage(img);
        item.setPadding(2);

        Feature feature = state.getFeature(fp);
        item.setHighlightedFeature(feature);
        return item;
    }


    public void setMouseOverIdx(Integer mouseOverIdx) {
        if (this.mouseOverIdx == mouseOverIdx) {
            return;
        }

        this.mouseOverIdx = mouseOverIdx;
        if (mouseOverIdx == null) {
            eventsOverlayPanel.clearHighlight();
            return;
        }
        EventItem item = model.get(mouseOverIdx);
        Position pos = item.getHighlightedPosition();
        Feature feature = item.getHighlightedFeature();
        if (pos != null) {
            eventsOverlayPanel.setHighlightedPosition(state, pos);
        } else if (feature != null) {
            eventsOverlayPanel.setHighlightedFeature(state, feature);
        } else {
            eventsOverlayPanel.clearHighlight();
        }
    }

    public void handleGameChanged(GameChangedEvent ev) {
        state = ev.getCurrentState();
        model = prepareModel(state, ev.getCurrentState().getEvents());
        repaint();
    }

    private ArrayList<EventItem> prepareModel(GameState state, Queue<PlayEvent> events) {
        ArrayList<EventItem> model = new ArrayList<>();

        turnColor = Color.GRAY;
        triggeringColor = null;

        boolean ignore = true;

        for (PlayEvent ev : events) {
            if (ev instanceof MeepleReturned) {
                continue;
            }
            if (ev instanceof PlayerTurnEvent) {
                turnColor = getMeepleColor(((PlayerTurnEvent) ev).getPlayer());
                ignore = false;
                continue;
            }
            if (ignore) {
                continue;
            }

            Integer idx = ev.getMetadata().getTriggeringPlayerIndex();
            if (idx == null) {
                triggeringColor = turnColor;
            } else {
                triggeringColor = getMeepleColor(state.getPlayers().getPlayer(idx));
            }

            Function1<PlayEvent, EventItem> fn = mapping.get(ev.getClass()).getOrNull();
            if (fn == null) {
                logger.warn("Unhandled event {}", ev.getClass());
            } else {
                model.add(fn.apply(ev));
            }
        }
        return model;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        int size = model.size();
        skipItems = Math.max(0, size - getWidth() / ICON_WIDTH);

        Graphics2D g2 = (Graphics2D) g;
        AffineTransform orig = g2.getTransform();
        g2.translate(0, 5);

        for (int i = skipItems; i < size; i++) {
            EventItem item = model.get(i);
            g2.setColor(item.getTurnColor());
            g2.fillRect(0, -5, ICON_WIDTH, 2);
            g2.setColor(item.getColor());
            g2.fillRect(0, -3, ICON_WIDTH, 2);
            item.draw(g2);

            g2.translate(ICON_WIDTH, 0);
        }
        g2.setTransform(orig);
    }

    private Color getMeepleColor(Player player) {
        return player.getColors().getMeepleColor();
    }

    public EventsOverlayLayer getEventsOverlayPanel() {
        return eventsOverlayPanel;
    }

    public void setEventsOverlayPanel(EventsOverlayLayer eventsOverlayPanel) {
        this.eventsOverlayPanel = eventsOverlayPanel;
    }
}
