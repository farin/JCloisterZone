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

import com.jcloisterzone.event.play.*;
import com.jcloisterzone.ui.grid.eventpanel.RansomPaidEventItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.neutral.Count;
import com.jcloisterzone.figure.neutral.Dragon;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.GoldminesCapability.GoldToken;
import com.jcloisterzone.game.capability.TunnelCapability.Tunnel;
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
import io.vavr.collection.Vector;

public class GameEventsPanel extends JPanel {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final int ICON_WIDTH = 30;
    public static final int ICON_HEIGHT = 30;

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
        mapping = mapping.put(MeepleReturned.class, this::processMeepleReturnedEvent);
        mapping = mapping.put(FollowerCaptured.class, this::processFollowerCapturedEvent);
        mapping = mapping.put(ScoreEvent.class, this::processScoreEvent);
        mapping = mapping.put(NeutralFigureMoved.class, this::processNeutralFigureMoved);
        mapping = mapping.put(NeutralFigureReturned.class, this::processNeutralFigureReturned);
        mapping = mapping.put(TokenPlacedEvent.class, this::processTokenPlacedEvent);
        mapping = mapping.put(TokenReceivedEvent.class, this::processTokenReceivedEvent);
        mapping = mapping.put(CastleCreated.class, this::processCastleCreatedEvent);
        mapping = mapping.put(RansomPaidEvent.class, this::processRansomPaidEvent);
        mapping = mapping.put(PrisonersExchangeEvent.class, ev -> null);
        mapping = mapping.put(DoubleTurnEvent.class, ev -> null);
        mapping = mapping.put(FlierRollEvent.class, ev -> null);
    }

    private EventItem processTilePlacedEvent(PlayEvent _ev) {
        TilePlacedEvent ev = (TilePlacedEvent) _ev;
        TileImage img = rm.getTileImage(ev.getTile().getId(), ev.getRotation());
        ImageEventItem item = new ImageEventItem(ev, turnColor, triggeringColor);
        item.setImage(img.getImage());

        item.setHighlightedPositions(Vector.of(ev.getPosition()));
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

    private EventItem processFollowerCapturedEvent(PlayEvent _ev) {
        FollowerCaptured ev = (FollowerCaptured) _ev;
        ImageEventItem item = getMeepleItem(ev, ev.getFollower(), ev.getFrom().asFeaturePointer());
        item.setDrawCross(true);
        return item;
    }

    private EventItem processMeepleReturnedEvent(PlayEvent _ev) {
        MeepleReturned ev = (MeepleReturned) _ev;
        if (!ev.isForced()) {
            return null;
        }
        ImageEventItem item = getMeepleItem(ev, ev.getMeeple(), ev.getFrom().asFeaturePointer());
        item.setDrawCross(true);
        return item;
    }

    private EventItem processScoreEvent(PlayEvent _ev) {
        ScoreEvent ev = (ScoreEvent) _ev;
        ScoreEventItem item = new ScoreEventItem(theme, ev, turnColor, triggeringColor);
        if (ev.getSource() != null) {
            item.setHighlightedPositions(ev.getSource().toVector());
        } else if (ev.getCategory() == PointCategory.FAIRY || ev.getFeaturePointer() == null) {
            item.setHighlightedPositions(Vector.of(ev.getPosition()));
        } else {
            Feature feature = state.getFeature(ev.getFeaturePointer());
            item.setHighlightedFeature(feature);
        }
        return item;
    }

    private EventItem processRansomPaidEvent(PlayEvent _ev) {
        RansomPaidEvent ev = (RansomPaidEvent) _ev;
        RansomPaidEventItem item = new RansomPaidEventItem(theme, ev, turnColor, triggeringColor);
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
            Position from = ev.getFrom() == null ? null : ev.getFrom().getPosition();
            Position to = ev.getTo() == null ? null : ev.getTo().getPosition();
            Vector<Position> positions = Vector.empty();
            if (from != null) {
                positions = positions.append(from);
            }
            if (to != null && (from == null || !to.equals(from))) {
                positions = positions.append(to);
            }
            item.setHighlightedPositions(positions);
        }
        return item;
    }

    private EventItem processNeutralFigureReturned(PlayEvent _ev) {
        NeutralFigureReturned ev = (NeutralFigureReturned) _ev;
        Image img = rm.getImage("neutral/" + ev.getNeutralFigure().getClass().getSimpleName().toLowerCase());
        ImageEventItem item = new ImageEventItem(ev, turnColor, triggeringColor);
        item.setImage(img);
        item.setDrawCross(true);
        Feature feature = state.getFeature(ev.getFrom().asFeaturePointer());
        item.setHighlightedFeature(feature);
        return item;
    }

    private EventItem processTokenPlacedEvent(PlayEvent _ev) {
        TokenPlacedEvent ev = (TokenPlacedEvent) _ev;
        Token token = ev.getToken();

        if (token == GoldToken.GOLD) {
            // gold placement on board is obvious and only recevied gold should be notified
            return null;
        }

        ImageEventItem item = new ImageEventItem(ev, turnColor, triggeringColor);

        if (token instanceof Tunnel) {
            Player player = state.getPlayers().getPlayer(ev.getMetadata().getTriggeringPlayerIndex());
            java.util.Map<Tunnel, Color> tunnelColors = player.getColors().getTunnelColors();
            Image img = rm.getLayeredImage(
                new LayeredImageDescriptor("player-meeples/tunnel", tunnelColors.get(token))
             );
            item.setImage(img);
        } else {
            item.setImage(rm.getImage("neutral/" + token.name().toLowerCase()));
        }

        item.setHighlightedPositions(Vector.of(ev.getPointer().getPosition()));
        return item;
    }

    private EventItem processTokenReceivedEvent(PlayEvent _ev) {
        TokenReceivedEvent ev = (TokenReceivedEvent) _ev;
        Color playerColor = getMeepleColor(ev.getPlayer());
        Image img = rm.getImage("neutral/" + ev.getToken().name().toLowerCase());
        ImageEventItem item = new ImageEventItem(ev, turnColor, playerColor);
        item.setImage(img);

        if (ev.getSourceFeature() != null) {
            item.setHighlightedFeature(ev.getSourceFeature() );
        }
        if (ev.getSourcePositions() != null) {
            item.setHighlightedPositions(ev.getSourcePositions());
        }
        return item;
    }

    private EventItem processCastleCreatedEvent(PlayEvent _ev) {
        CastleCreated ev = (CastleCreated) _ev;
        Image img = rm.getImage("neutral/castle");
        ImageEventItem item = new ImageEventItem(ev, turnColor, triggeringColor);
        item.setImage(img);
        item.setHighlightedFeature(ev.getCastle());
        return item;
    }

    private ImageEventItem getMeepleItem(PlayEvent ev, Meeple meeple, FeaturePointer fp) {
        Image img = rm.getLayeredImage(new LayeredImageDescriptor(meeple.getClass(), meeple.getPlayer().getColors().getMeepleColor()));
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
        Vector<Position> positions = item.getHighlightedPositions();
        Feature feature = item.getHighlightedFeature();
        if (positions != null) {
            eventsOverlayPanel.setHighlightedPositions(state, positions);
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
        boolean finalScoring = false;
        EventItem dragonItem = null;

        for (PlayEvent ev : events) {
            if (ev instanceof PlayerTurnEvent) {
                turnColor = getMeepleColor(((PlayerTurnEvent) ev).getPlayer());
                ignore = false;
                dragonItem = null;
                continue;
            }
            if (ignore) {
                continue;
            }

            if (dragonItem != null && isDragonMoveEvent(ev)) {
                Vector<Position> positions = dragonItem.getHighlightedPositions();
                positions = positions.append(((NeutralFigureMoved)ev).getTo().getPosition());
                dragonItem.setHighlightedPositions(positions);
                continue;
            }

            if (!finalScoring) {
                if (ev instanceof ScoreEvent && ((ScoreEvent) ev).isFinal()) {
                    turnColor = Color.GRAY;
                    triggeringColor = null;
                    finalScoring = true;
                } else {
                    Integer idx = ev.getMetadata().getTriggeringPlayerIndex();
                    if (idx == null) {
                        triggeringColor = turnColor;
                    } else {
                        triggeringColor = getMeepleColor(state.getPlayers().getPlayer(idx));
                    }
                }
            }

            Function1<PlayEvent, EventItem> fn = mapping.get(ev.getClass()).getOrNull();
            if (fn == null) {
                logger.warn("Unhandled event {}", ev.getClass());
            } else {
                EventItem item = fn.apply(ev);
                if (item == null) {
                    continue;
                }
                if (isDragonMoveEvent(ev)) {
                    dragonItem = item;
                }
                model.add(item);
            }
        }
        return model;
    }

    private boolean isDragonMoveEvent(PlayEvent ev) {
        return ev instanceof NeutralFigureMoved && ((NeutralFigureMoved)ev).getNeutralFigure() instanceof Dragon;
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
            Color top = item.getTurnColor();
            Color bottom = item.getColor();
            if (top.equals(bottom)) {
                g2.setColor(top);
                g2.fillRect(0, -5, ICON_WIDTH, 4);
            } else {
                g2.setColor(top);
                g2.fillRect(0, -5, ICON_WIDTH, 2);
                g2.setColor(bottom);
                g2.fillRect(0, -3, ICON_WIDTH, 3); //1px overlap
            }
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
