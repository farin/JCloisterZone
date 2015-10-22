package com.jcloisterzone.ui.grid.layer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.event.TileEvent;
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

    private final LinkedList<PlacementHistoryEntry> entries = new LinkedList<PlacementHistoryEntry>();
    private List<PlacementHistoryEntry> visibleEntries;

    public PlacementHistory(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);

        gc.register(this);
    }

    @Subscribe
    public void handleTileEvent(TileEvent ev) {
    	if (TileEvent.PLACEMENT == ev.getType()) {
    		entries.addFirst(createPlacementHistoryEntry(ev));
    	} else if (TileEvent.REMOVE == ev.getType()) {
    		entries.remove(createPlacementHistoryEntry(ev));
    	}

    	int limit = 0;
    	int ndx = 0;
    	Player lastPlayer = null;
    	Player previousPlayer = null;
    	for (PlacementHistoryEntry entry : entries) {

    		if (ndx == 0) {
    			lastPlayer = previousPlayer = entry.player;
    		}

    		if (lastPlayer == null) {
    			// player can be null (load, river-end)
    			break;
    		}

    		if (previousPlayer != entry.player && lastPlayer == entry.player) {
    			// complete round
    			limit = ndx;
    			break;
    		}

    		previousPlayer = entry.player;
    		ndx++;
    	}

    	if (limit == 0) {
    		limit = getGame().getAllPlayers().length;
    	}

    	visibleEntries = entries.subList(0, Math.min(entries.size(), limit));
    }

    @Override
    public void paint(Graphics2D g) {

    	if (visibleEntries == null) {
    		return;
    	}

    	Composite oldComposite = g.getComposite();
        g.setComposite(ALPHA_COMPOSITE);

        int counter = 0;
    	for (PlacementHistoryEntry entry : visibleEntries) {
    		String text = String.valueOf(++counter);
    		Color color = entry.player != null ?  entry.player.getColors().getFontColor() : DEFAULT_COLOR;

    		BufferedImage buf = UiUtils.newTransparentImage(getSquareSize(), getSquareSize());
    		Graphics2D gb = (Graphics2D) buf.getGraphics();
    		drawAntialiasedTextCentered(gb, text, 80, ZERO, POINT, color, null);
    		gb.setComposite(AlphaComposite.DstOver);
    		drawAntialiasedTextCentered(gb, text, 80, ZERO, SHADOW_POINT, Color.GRAY, null);
    		g.drawImage(buf, null, getOffsetX(entry.position), getOffsetY(entry.position));
    	}

    	g.setComposite(oldComposite);
    }

    private PlacementHistoryEntry createPlacementHistoryEntry(TileEvent tileEvent) {
    	return new PlacementHistoryEntry(tileEvent.getTriggeringPlayer(), tileEvent.getPosition());
    }

	private class PlacementHistoryEntry {
		private final Player player;
		private final Position position;

		public PlacementHistoryEntry(Player player, Position position) {
			this.player = player;
			this.position = position;
		}

		private PlacementHistory getOuterType() {
			return PlacementHistory.this;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(getOuterType(), player, position);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;

			PlacementHistoryEntry that = (PlacementHistoryEntry) obj;

			return Objects.equal(this.getOuterType(), that.getOuterType())
					&& Objects.equal(this.player, that.player)
					&& Objects.equal(this.position, that.position);
		}

	}
}
