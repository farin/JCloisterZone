package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileModifier;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.game.state.PlayersState;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Vector;

import org.w3c.dom.Element;

/** model contains last placed tile with carousel */
public class CarouselCapability extends Capability<PlacedTile> {

	public static class CarouselModifier extends TileModifier {
		private String rotation;

		CarouselModifier(String rotation) {
			super("Carousel" + rotation);
			this.rotation = rotation;
		}

		public String getRotation() {
			return rotation;
		}
	}

    public static final Map<String, CarouselModifier> CAROUSELS = HashMap.of(
    	"regular", new CarouselModifier("regular"),
    	"reverse", new CarouselModifier("reverse")
    );
	   
	private static final long serialVersionUID = 1L;

    @Override
    public Tile initTile(GameState state, Tile tile, Element tileElement) {
        Vector<Element> carouselEl = XMLUtils.getElementStreamByTagName(tileElement, "carousel").toVector();
        if (carouselEl.size() == 0) {
            return tile;
        }
        if (carouselEl.size() == 1) {
            String rotation = carouselEl.get().getAttribute("rotation");
          	tile = tile.addTileModifier(CAROUSELS.get(rotation).getOrElseThrow(IllegalArgumentException::new));
            return tile;
        }
        throw new IllegalStateException("multiple <carousel> elements");
    }


    @Override
    public GameState onTilePlaced(GameState state, PlacedTile pt) {
    	String rotation = pt.getTile().getTileModifiers()
    			.find(m -> m instanceof CarouselModifier)
    			.map(m -> ((CarouselModifier)m).getRotation())
    			.getOrNull();
        if (rotation == null) {
            return state;
        }
        String currentRotation = null;
        if (getModel(state) != null) {
            currentRotation = getModel(state).getTile().getTileModifiers()
        			.find(m -> m instanceof CarouselModifier)
        			.map(m -> ((CarouselModifier)m).getRotation())
        			.getOrNull();
        }
        
        state = setModel(state, pt);
        
        if ((currentRotation == null && rotation.equals("regular"))
        	|| (currentRotation != null && rotation.equals(currentRotation))
        ) {
            // No change of rotation
        	return state;
        }

        return state.setNextPlayerIncrement(-1 * state.getNextPlayerIncrement());
//        Player turnPlayer = state.getTurnPlayer();
//        PlayersState ps = state.getPlayers();
//        System.out.println(turnPlayer);
//        System.out.println(ps.getPlayers());
//        Array<Player> newPlayersOrder = ps.getPlayers().reverse();
//        for (Player p : ps.getPlayers()) {
//            System.out.println(p);
//        }
//        System.out.println(newPlayersOrder);
////        if (biggestCityCompleted != null) {
////            for (Player p : ps.getPlayers()) {
////                ps = ps.setTokenCount(p.getIndex(), BiggestFeatureAward.KING, p.equals(turnPlayer) ? 1 : 0);
////            }
////            TokenReceivedEvent ev = new TokenReceivedEvent(
////                    PlayEventMeta.createWithActivePlayer(state), turnPlayer, BiggestFeatureAward.KING, 1);
////            ev.setSourceFeature(biggestCityCompleted);
////            state = state.appendEvent(ev);
////        }
//    	return state.setPlayers(ps.setPlayers(newPlayersOrder));
    }
}
