package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.AcrobatsScoreAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.ExprItem;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.event.ScoreEvent;
import com.jcloisterzone.feature.Acrobats;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.reducers.AddPoints;
import com.jcloisterzone.reducers.UndeployMeeples;

import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;

import java.util.Random;

import org.w3c.dom.Element;

public class AcrobatsCapability extends Capability<Void> {

	private static final long serialVersionUID = 1L;

	private static final int ACROBATS_COUNT = 3;
	
    @Override
    public Tile initTile(GameState state, Tile tile, Element tileElement) {
        Vector<Element> acrobatsEl = XMLUtils.getElementStreamByTagName(tileElement, "acrobats").toVector();
        if (acrobatsEl.size() == 0) {
            return tile;
        }
        if (acrobatsEl.size() == 1) {
            Location direction = Location.valueOf(acrobatsEl.get().getAttribute("direction"));
            Acrobats acrobats = new Acrobats(Acrobats.INITIAL_PLACE, direction);
            return tile.setInitialFeatures(tile.getInitialFeatures().put(acrobats.getPlace(), acrobats));
        }
        throw new IllegalStateException("multiple <acrobats> elements");
    }

    @Override
    public GameState onActionPhaseEntered(GameState state) {
        ActionsState actions = state.getPlayerActions();
        HashSet fullPlaces = HashSet.empty();
        Player active = state.getActivePlayer();

        PlacedTile lastPlaced = state.getLastPlaced();
        Position currentTilePos = lastPlaced.getPosition();
        
        Stream<Acrobats> acrobats = state.getFeatures(Acrobats.class);
        List<FeaturePointer> acrobatsPlaces = List.empty();
        Vector<Meeple> availMeeples = active.getMeeplesFromSupply(state, Vector.of(SmallFollower.class));

        // Not allow to place Acrobat on tile with Bridge
        Set<Position> placedBridgesPositions = (state.hasCapability(VineyardCapability.class) ? state.getCapabilityModel(BridgeCapability.class).map(fp -> fp.getPosition()) : HashSet.empty());
        
        // When Magic Portal, allow place also to all Acrobats Space
        Boolean isMagicPortal = state.hasCapability(PortalCapability.class) && lastPlaced.getTile().hasModifier(PortalCapability.MAGIC_PORTAL);

        for(Acrobats acrobat : acrobats) {
            int count = state.getDeployedMeeples().filter((m, fp) -> {
            	return (acrobat.getPlaces().contains(fp));
            }).length();
            acrobatsPlaces = acrobatsPlaces.appendAll(acrobat.getPlaces());
            if (count == ACROBATS_COUNT) {
              fullPlaces = fullPlaces.union(acrobat.getPlaces().toSet());
            } else {
              for(Meeple meeple : availMeeples) {
            	  List<FeaturePointer> _places = acrobat.getPlaces().filter(fp -> {
            		Position apos = fp.getPosition();
      	            return !placedBridgesPositions.contains(apos) && (isMagicPortal || Math.abs(currentTilePos.x - apos.x) <= 1 && Math.abs(currentTilePos.y - apos.y) <= 1);
            	  });
            	  if (_places.length()>0) {
            		  actions = actions.appendAction(new MeepleAction(meeple, _places.toSet())).mergeMeepleActions();
            		  state = state.setPlayerActions(actions);
            	  }
              }
            }
        }
        // Hack remove Acrobats placement to all other types of meeple
        var currentActions = state.getPlayerActions().getActions();
        List<FeaturePointer> _acrobatsPlaces = acrobatsPlaces;
        Vector<PlayerAction<?>> filteredActions = currentActions.map(a -> {
        			if (!(a instanceof MeepleAction) || ((MeepleAction) a).getMeepleType().equals(SmallFollower.class)) {
        				return a;
        			} else {
        				MeepleAction ma = (MeepleAction) a;
        		        ma = ma.excludeOptions(_acrobatsPlaces.toSet());
        				return ma;
        			}
        		});
        actions = actions.setActions(filteredActions);
		state = state.setPlayerActions(actions);

        if (fullPlaces.length()>0) {
            actions = actions.appendAction(new AcrobatsScoreAction(fullPlaces));
            state = state.setPlayerActions(actions);
        }
        return state;
    }

    @Override
    public GameState onFinalScoring(GameState state) {
        for (Acrobats acrobats : state.getFeatures(Acrobats.class)) {
        	state = scoreAcrobats(state, acrobats, false);
        }
        return state;
    }
    
    public GameState scoreAcrobats(GameState state, Acrobats acrobats, Boolean undeployMeeples) {
		LinkedHashMap<Meeple, FeaturePointer> affectedMeeples = state.getDeployedMeeples().filter((m, fp) -> {
			return acrobats.getPlaces().contains(fp);
        });
        if (affectedMeeples.length()>0) {
            for (Tuple2<Meeple, FeaturePointer> t : affectedMeeples) {
                PointsExpression expr = new PointsExpression("acrobats", new ExprItem(1, "acrobats", 5));
                var receivedPoints= new ScoreEvent.ReceivedPoints(expr, t._1.getPlayer(), t._2);
                state = (new AddPoints(receivedPoints, true)).apply(state);
                if (undeployMeeples) {
                    state = (new UndeployMeeples(state.getFeature(t._2), false)).apply(state);
                }
        	}
        }
    	return state;
    }
}
