package com.jcloisterzone.game.capability;

import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.feature.Watchtower;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.event.ScoreEvent.ReceivedPoints;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;

import org.w3c.dom.Element;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;

public class WatchtowersCapability extends Capability<Void> {

	private static final long serialVersionUID = 1L;

    @Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) {
        Vector<Element> watchtowers = XMLUtils.getElementStreamByTagName(tileElements, "watchtower").toVector();
		if (watchtowers.size() == 0) {
			return tile;
		}
		if (watchtowers.size() == 1) {
			String type = watchtowers.get().getAttribute("type");
            Watchtower.WatchtowerType watchtowerType = Watchtower.WatchtowerType.forType(type);
            Watchtower watchtower = new Watchtower(watchtowerType);
            tile = tile.setInitialFeatures(tile.getInitialFeatures().put(Location.TOWER, watchtower));
        }
        return tile;
    }
    @Override
    public List<ReceivedPoints> appendFiguresBonusPoints(GameState state, List<ReceivedPoints> bonusPoints, Scoreable feature, boolean isFinal) {
    	
    	if (isFinal) {
        	// No extra points on end of game
            return bonusPoints;
    	}
    	if (!((feature instanceof Road) || (feature instanceof City))) {
            return bonusPoints;
        }
    	
    	Stream<Meeple> featureMeeples = feature.getMeeples(state);

    	Position pos = null;
		boolean isWatchtower = false;
    	
    	for(Meeple meeple: featureMeeples) {
    	    pos = meeple.getPosition(state);

    	    Tuple2<Location, Feature> tfs  = state.getTileFeatures2(pos).filter(tf -> tf._2 instanceof Watchtower).getOrNull();

	    	if (tfs != null) {
	    		Location loc = tfs._1;
	    		Watchtower watchtower = (Watchtower) tfs._2;
	    		int cities = 0;
	    		int meeples = 0;
	    		int monasteries = 0;
	    		int roads = 0;
	    		int pennants = 0;
	    		
	    		Set<Position> positions = state.getAdjacentAndDiagonalTiles2(pos).map(Tuple2::_2).map(pt -> pt.getPosition()).toSet().add(pos);
	    		
	    		for (Position p : positions) {
	            	Stream<Tuple2<Location, Feature>> features = state.getTileFeatures2(p);
	            	
	            	if (features.filter(tf -> tf._2 instanceof Road).length()>0) {
	            		roads += 1;
	            	}

	            	monasteries += features.filter(tf -> tf._2 instanceof Cloister).length();

	            	List<City> tileCities = state.getPlacedTile(p)
	            			.getTile()
	            			.getInitialFeatures()
		                    .filter(t -> t._2 instanceof City)
		                    .map(Tuple2::_2)
		                    .map(f -> (City) f)
		                    .toList();
	            	
	            	if (tileCities.length()>0) {
	            		cities += 1;
	            		
	            		for(City city: tileCities) {
	            			pennants += city.getPennants();
	            		}
	            	}
	            	
	                meeples += state.getDeployedMeeples()
	                        .filter(mt -> mt._1 instanceof Follower)
	                        .filter(mt -> mt._2.getPosition().equals(p))
	                        .length();

	            }

                int count = 0;

                if (watchtower.getType() == Watchtower.WatchtowerType.CITY) {
              
                	count = cities;
              
				} else if (watchtower.getType() == Watchtower.WatchtowerType.MONASTERY) {
				  
				  	count = monasteries;
				
				} else if (watchtower.getType() == Watchtower.WatchtowerType.ROAD) {
				  
				  	count = roads;
				
				} else if (watchtower.getType() == Watchtower.WatchtowerType.MEEPLE) {
				  
				  	count = meeples;
				
				} else if (watchtower.getType() == Watchtower.WatchtowerType.PENNANT) {
				
				  	count = pennants;
				}

                int points = count * watchtower.getType().getPoints();
                
                Map<String, Integer> args = HashMap.of(
                	watchtower.getType().getType(), count
                );

                if (points>0) {
                	
                	bonusPoints = bonusPoints.append(new ReceivedPoints(new PointsExpression(points,"watchtower",args), meeple.getPlayer(), new FeaturePointer(pos, loc)));
//                	bonusPoints = bonusPoints.append(new ReceivedPoints(new PointsExpression(points,"watchtower",args), meeple.getPlayer(), meeple.getDeployment(state)));
                }
	    	}
	    }

    	return bonusPoints;
    }
}
