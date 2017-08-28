package com.jcloisterzone.game.capability;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.figure.neutral.Count;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Tuple2;

public class CountCapability extends Capability<Void> {

    public static String QUARTER_ACTION_TILE_ID = "CO.7";
    private static final String[] FORBIDDEN_TILES = new String[] { "CO.6", "CO.7" };

//    private final Map<Player, Integer> receivedPoints = new HashMap<>();
//    private Position quarterPosition;
//
//    // active player for pre score phase
//    private Player moveOutPlayer;

    @Override
    public GameState onStartGame(GameState state) {
        return state.mapNeutralFigures(nf -> nf.setCount(new Count("count.1")));
    }

//    @Override
//    public List<Feature> extendFeatures(Tile tile) {
//        if (QUARTER_ACTION_TILE_ID.equals(tile.getId())) {
//            return Arrays.stream(Location.quarters()).map(location -> {
//                Quarter q = new Quarter();
//                q.setLocation(location);
//                return q;
//            }).collect(Collectors.toList());
//        }
//        return Collections.emptyList();
//    }

    public static boolean isTileForbidden(TileDefinition tile) {
        String id = tile.getId();
        for (String forbidden : FORBIDDEN_TILES) {
            if (forbidden.equals(id)) return true;
        }
        return false;
    }

    @Override
    public boolean isMeepleDeploymentAllowed(GameState state, Position pos) {
        Tuple2<TileDefinition, Rotation> t = state.getPlacedTiles().get(pos).getOrNull();
        return t == null || !isTileForbidden(t._1);
    }

//    @Override
//    public void handleEvent(Event event) {
//       if (event instanceof TileEvent) {
//           tilePlaced((TileEvent) event);
//       }
//       if (event instanceof ScoreEvent) {
//           scoreAssigned((ScoreEvent) event);
//       }
//    }
//
//    private void tilePlaced(TileEvent ev) {
//        Tile tile = ev.getTile();
//        if (ev.getType() == TileEvent.PLACEMENT && QUARTER_ACTION_TILE_ID.equals(tile.getId())) {
//            quarterPosition = tile.getPosition();
//            count.deploy(new FeaturePointer(quarterPosition, Location.QUARTER_CASTLE));
//        }
//    }
//
//    private void scoreAssigned(ScoreEvent ev) {
//        if (ev.getCategory().hasLandscapeSource()) {
//            Player p = ev.getTargetPlayer();
//            int points = ev.getPoints();
//            if (receivedPoints.containsKey(p)) {
//                receivedPoints.put(p, receivedPoints.get(p) + points);
//            } else {
//                receivedPoints.put(p, points);
//            }
//        }
//    }
//
//    public boolean didReceivePoints(Player p) {
//        Integer pts = receivedPoints.get(p);
//        if (pts == null) {
//            return false;
//        }
//        return pts > 0; //undo can cause 0 in map!
//    }
//
//    @Override
//    public void turnPartCleanUp() {
//        receivedPoints.clear();
//    }
//
//    public Count getCount() {
//        return count;
//    }
//
//    public Position getQuarterPosition() {
//        return quarterPosition;
//    }
//
//    public Quarter getQuarter(Location loc) {
//        return (Quarter) game.getBoard().get(quarterPosition).getFeature(loc);
//    }
//
//    public Quarter getQuarterFor(Feature f) {
//        if (f instanceof City) return getQuarter(Location.QUARTER_CASTLE);
//        if (f instanceof Road) return getQuarter(Location.QUARTER_BLACKSMITH);
//        if (f instanceof Cloister) return getQuarter(Location.QUARTER_CATHEDRAL);
//        if (f instanceof Farm) return getQuarter(Location.QUARTER_MARKET);
//        throw new IllegalArgumentException("Illegal feature " + f);
//    }
//
//    public Player getMoveOutPlayer() {
//        return moveOutPlayer;
//    }
//
//    public void setMoveOutPlayer(Player moveOutPlayer) {
//        this.moveOutPlayer = moveOutPlayer;
//    }
}
