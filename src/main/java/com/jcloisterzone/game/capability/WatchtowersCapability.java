package com.jcloisterzone.game.capability;

import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.feature.Castle;
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

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;
import static com.jcloisterzone.XMLUtils.attributeIntValue;

public class WatchtowersCapability extends Capability<Void> {

	private static final long serialVersionUID = 1L;

    @Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) {
        Vector<Element> watchtowers = XMLUtils.getElementStreamByTagName(tileElements, "watchtower").toVector();
		if (watchtowers.size() == 0) {
			return tile;
		}
		if (watchtowers.size() == 1) {
            Map<String, Integer> modifiers = HashMap.empty();
            modifiers = modifiers.put("bonus-city",attributeIntValue(watchtowers.get(),"bonus-city",0));
            modifiers = modifiers.put("bonus-meeple",attributeIntValue(watchtowers.get(),"bonus-meeple",0));
            modifiers = modifiers.put("bonus-monastery",attributeIntValue(watchtowers.get(),"bonus-monastery",0));
            modifiers = modifiers.put("bonus-pennant",attributeIntValue(watchtowers.get(),"bonus-pennant",0));
            modifiers = modifiers.put("bonus-road",attributeIntValue(watchtowers.get(),"bonus-road",0));
            Watchtower watchtower = new Watchtower(modifiers);
            Stream<Location> locations = XMLUtils.contentAsLocations(watchtowers.get());
            if (locations.size() == 1) {
            	tile = tile.setInitialFeatures(tile.getInitialFeatures().put(locations.get(), watchtower));
            }
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
	                        .filter(mt -> {
	    	                    Feature f = state.getFeature(mt._2);
	                        	return (!(mt._2.getLocation().isCityOfCarcassonneQuarter() || f instanceof Castle));
	                        })
	                        .length();

	            }

                int points = 0;
                
                Map<String, Integer> args = HashMap.empty();
                
                if (cities>0) {
                	points += cities * watchtower.getModifiers().getOrElse("bonus-city",0);
                	args = args.put("bonusCity",watchtower.getModifiers().getOrElse("bonus-city",0));
                	args = args.put("city",cities);
                }

                if (pennants>0) {
                	points += pennants * watchtower.getModifiers().getOrElse("bonus-pennant",0);
                	args = args.put("bonusPennant",watchtower.getModifiers().getOrElse("bonus-pennant",0));
                	args = args.put("pennant",pennants);
                }

                if (monasteries>0) {
                	points += monasteries * watchtower.getModifiers().getOrElse("bonus-monastery",0);
                	args = args.put("bonusMonastery",watchtower.getModifiers().getOrElse("bonus-monastery",0));
                	args = args.put("monastery",monasteries);
                }

                if (roads>0) {
                	points += roads * watchtower.getModifiers().getOrElse("bonus-road",0);
                	args = args.put("bonusRoad",watchtower.getModifiers().getOrElse("bonus-road",0));
                	args = args.put("road",roads);
                }

                if (meeples>0) {
                	points += meeples * watchtower.getModifiers().getOrElse("bonus-meeple",0);
                	args = args.put("bonusMeeple",watchtower.getModifiers().getOrElse("bonus-meeple",0));
                	args = args.put("meeple",meeples);
                }

                if (points>0) {
                	bonusPoints = bonusPoints.append(new ReceivedPoints(new PointsExpression(points,"watchtower",args), meeple.getPlayer(), new FeaturePointer(pos, loc)));
                }
	    	}
	    }

    	return bonusPoints;
    }
}
