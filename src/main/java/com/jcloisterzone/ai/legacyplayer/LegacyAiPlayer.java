package com.jcloisterzone.ai.legacyplayer;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.ai.RankingAiPlayer;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.score.ScoreAllCallback;
import com.jcloisterzone.feature.score.ScoreAllFeatureFinder;
import com.jcloisterzone.feature.visitor.score.CityScoreContext;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.feature.visitor.score.FarmScoreContext;
import com.jcloisterzone.feature.visitor.score.RoadScoreContext;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.capability.BuilderCapability;
import com.jcloisterzone.game.capability.BuilderCapability.BuilderState;
import com.jcloisterzone.game.capability.DragonCapability;
import com.jcloisterzone.game.capability.FairyCapability;
import com.jcloisterzone.game.phase.ScorePhase;

public class LegacyAiPlayer extends RankingAiPlayer {

    private static final double TRAPPED_MY_FIGURE_POINTS = -12.0;
    private static final double TRAPPED_ENEMY_FIGURE_POINTS = 3.0;
    private static final double MIN_CHANCE = 0.4;

    protected int packSize, myTurnsLeft;
    protected int enemyPlayers;

    private static final int OPEN_COUNT_ROAD = 0;
    private static final int OPEN_COUNT_CITY = 1;
    private static final int OPEN_COUNT_FARM = 2;
    private static final int OPEN_COUNT_CLOITSTER = 3;

    private int[] openCount = new int[4]; //number of my open objects

    public static EnumSet<Expansion> supportedExpansions() {
        return EnumSet.of(
            Expansion.BASIC,
            Expansion.WINTER,
            Expansion.ABBEY_AND_MAYOR,
            Expansion.INNS_AND_CATHEDRALS,
            Expansion.TRADERS_AND_BUILDERS,
            Expansion.PRINCESS_AND_DRAGON,
            Expansion.KING_AND_SCOUT,
            Expansion.RIVER,
            Expansion.RIVER_II,
            Expansion.GQ11,
            Expansion.CATAPULT,
            //only cards
            Expansion.COUNT,
            Expansion.PLAGUE
        );
    }



    protected void initVars() {
        packSize = getTilePack().totalSize();
        enemyPlayers = game.getAllPlayers().length - 1;
        myTurnsLeft = ((packSize-1) / (enemyPlayers+1)) + 1;
    }

    @Override
    protected double rank() {
        double ranking = 0;
        initVars();

        //trigger score
        game.getPhase().next(ScorePhase.class);
        game.getPhase().enter();

        Arrays.fill(openCount, 0);

        ranking += meepleRating();
        ranking += pointRating();
        ranking += openObjectRating();

        ranking += rankPossibleFeatureConnections();
        ranking += rankConvexity();
        ranking += rankFairy();


        //objectRatings.clear();

        return ranking;
    }

    protected double reducePoints(double points, Player p) {
        if (isMe(p)) return points;
        return -points/enemyPlayers;
    }

    protected double meepleRating() {
        double rating = 0;

        for (Player p : game.getAllPlayers()) {
            double meeplePoints = 0;
            int limit = 0;
            for (Follower f : p.getFollowers()) {
                if (f.isDeployed()) {
                    if (f instanceof SmallFollower) {
                        meeplePoints += 0.15;
                    } else if (f instanceof BigFollower) {
                        meeplePoints += 0.25;
                    }
                    if (++limit == myTurnsLeft) break;
                }
            }
            rating += reducePoints(meeplePoints, p);
        }
        return rating;
    }

    class LegacyAiScoreAllCallback implements ScoreAllCallback {

        private double rank = 0;

        @Override
        public void scoreCastle(Meeple meeple, Castle castle) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletableScoreContext getCompletableScoreContext(Completable completable) {
            //TODO uncomment after invalidate implemeted
//			AiScoreContext ctx = getScoreCache().get(completable);
//			if (ctx != null && ctx.isValid()) {
//				return (CompletableScoreContext) ctx;
//			}
            return new LegacyAiScoreContext(game, completable.getScoreContext(), getScoreCache());
        }

        @Override
        public FarmScoreContext getFarmScoreContext(Farm farm) {
            //TODO uncomment after invalidate implemeted
//			AiScoreContext ctx = getScoreCache().get(farm);
//			if (ctx != null && ctx.isValid()) {
//				return (FarmScoreContext) ctx;
//			}
            return new LegacyAiFarmScoreContext(game, getScoreCache());
        }

        @Override
        public void scoreFarm(FarmScoreContext ctx, Player player) {
            double points = getFarmPoints((Farm) ctx.getMasterFeature(), player, ctx);
            rank += reducePoints(points, player);
        }

        @Override
        public void scoreBarn(FarmScoreContext ctx, Barn meeple) {
            //prefer barn placement - magic constant
            rank += reducePoints(1.2 * ctx.getBarnPoints(), meeple.getPlayer());
        }

        @Override
        public void scoreCompletableFeature(CompletableScoreContext ctx) {
            rank += rankUnfishedCompletable(ctx.getMasterFeature(), (LegacyAiScoreContext) ctx);
            rank += rankTrappedMeeples((LegacyAiScoreContext) ctx);
            rank += rankSpecialFigures((LegacyAiScoreContext) ctx);
        }

        public double getRanking() {
            return rank;
        }

    }

    protected double pointRating() {
        double rating = 0;

        for (Player p : game.getAllPlayers()) {
            rating += reducePoints(p.getPoints(), p);
        }

        ScoreAllFeatureFinder scoreAll = new ScoreAllFeatureFinder();
        LegacyAiScoreAllCallback callback = new LegacyAiScoreAllCallback();
        scoreAll.scoreAll(game, callback);
        rating += callback.getRanking();

        return rating;
    }


    public static final double[][]  OPEN_PENALTY = {
        { 0.0, 0.2, 0.5, 2.5, 5.5, 9.5, 15.0, 22.0, 30.0 },
        { 0.0, 0.15, 0.3, 1.3, 2.3, 5.3, 10.0, 15.0, 22.0 },
        { 0.0, 5.0, 10.0, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0 },
        { 0.0, 0.0, 0.4, 0.8, 1.2, 2.0, 4.0, 7.0, 11.0 }
    };

    protected double openObjectRating() {
        double rating = 0;

        for (int i = 0; i < OPEN_PENALTY.length; i++ ){
            double penalty;
            //fast fix for strange bug causes ArrayIndexOutOfBoundsException: 9
            if (openCount[i] >= OPEN_PENALTY[i].length) {
                penalty = OPEN_PENALTY[i][OPEN_PENALTY[i].length - 1];
            } else {
                penalty = OPEN_PENALTY[i][openCount[i]];
            }
            if (i == 2) {
                //Farm
                double modifier = (packSize - ((1+enemyPlayers) * 3)) / 20.0;
                if (modifier < 1.0) modifier = 1.0;
                rating -= modifier * penalty;
            } else {
                rating -= penalty;
            }
        }
        return rating;
    }

    /*
    private double rankFeatureConnectionsOn(Position pos, OpenEdge edge, Location loc, boolean isCornerConnection) {
        Tile tile = getBoard().get(pos.add(loc));
        if (tile == null) return 0.0;
        Feature opposite = tile.getFeaturePartOf(loc.rev());
        if (opposite == null || edge.getClass().isInstance(opposite)) return 0.0;

        return 0.0;
    }
    */

    private double rankPossibleFeatureConnections() {
        //FreePlaceInfo[] fpi = board.getFreeNei();
        double rank = 0;

//		for (Entry<Position, OpenEdge> entry : ctx.getOpenEdgesChanceToClose().entrySet()) {
//			OpenEdge edge = entry.getValue();
//			if (edge.chanceToClose < MIN_CHANCE) continue;
//
//			Position pos = entry.getKey();
//			rank += rankFeatureConnectionsOn(pos, edge, edge.location.rotateCW(Rotation.R90), true);
//			rank += rankFeatureConnectionsOn(pos, edge, edge.location.rotateCCW(Rotation.R90), true);
//			rank += rankFeatureConnectionsOn(pos, edge, edge.location, false);
//		}

        /// vvvvvvvvvvvv OLD vvvvvvvvvvvvvvvvvv

//		for (Location loc : Location side) {
//
//		}

        //----- TODO difference 1.6
//		for (Direction d : Direction.sides()) {
//			FreePlaceInfo i = fpi[d.index()];
//			if (i == null) continue;
//
//			double chance = countChance(i.suitableCards);
//			if (chance < MIN_CHANCE) {
//				continue;
//			}
//
//			for (int type = 0; type < 2; type++) {
//				MultiTileFeature placedSO = i.so[type][d.rev().index()];
//				if (placedSO == null) continue;
//
//				int balance = placedSO.getFigureBalance(me);
//				MultiTileFeature comparedSO;
//				//SO next to placed
//				comparedSO = i.so[type][d.prev().index()];
//				rating += connectionRating(placedSO, balance, comparedSO, PLACED_NEXT,i);
//				//second SO next to placed
//				comparedSO = i.so[type][d.next().index()];
//				rating += connectionRating(placedSO, balance, comparedSO, PLACED_NEXT,i);
//				//oppostite SO
//				comparedSO = i.so[type][d.index()];
//				rating += connectionRating(placedSO, balance, comparedSO, PLACED_OPPOSITE,i);
//			}
//		}
        return rank;
    }

    protected double rankFairy() {
        if (!game.hasCapability(FairyCapability.class)) return 0;
        FairyCapability fc = game.getCapability(FairyCapability.class);
        Position fairyPos = fc.getFairyPosition();
        if (fairyPos == null) return 0;

        double rating = 0;

//		TODO more sophisticated rating
        for (Meeple meeple : game.getDeployedMeeples()) {
            if (!meeple.at(fairyPos)) continue;
            if (!(meeple instanceof Follower)) continue;
            if (meeple.getFeature() instanceof Castle) continue;

            rating += reducePoints(1.0, meeple.getPlayer());
        }

        return rating;

// 		//OLD legacy impl
//		Set<PlacedFigure> onTile = gc.getPlacedFiguresForTile(board.get(fairyPos.x,fairyPos.y));
//		Set<Player> onePointPlayers = new HashSet<>();
//		for (PlacedFigure pfi : onTile) {
//			if (pfi != null && gi.get().equals(pfi.player)) {
//				onePointPlayers.add(pfi.player);
//			}
//			if (pfi.et == FeatureType.TOWER) continue;
//			LastPlaceInfo last = gc.getBoard().getLastPlacementInfo();
//			if (gc.getBoard().get(last.pos).getTrigger() == TileTrigger.DRAGON) {
//				int dist = last.pos.squareDistance(pfi.position);
//				if (dist == 1) {
//					//figurka hned vedle, heuristika
//					/*Tile lastTile = gc.getBoard().get(pfi.position);
//					if (pfi.et == ElementType.CLOISTER) {
//						//lastTile.getCloister().get ...
//					}*/
//					rating += reducePoints(4, pfi.player); //zatim proste odhadnem cenu figurky na 4 body
//				}
//			}
//		}
//		//kvuli brane a vice figurkam na jednom poli, aby kazdy hrac max +1 za kolo
//
//		for (Player player : onePointPlayers) {
//			rating += reducePoints(0.8, player);
//		}
    }

    protected double rankConvexity() {
        Position pos = game.getCurrentTile().getPosition();
        return 0.001 * getBoard().getAdjacentAndDiagonalTiles(pos).size();
    }

    protected double rankUnfishedCompletable(Completable completable, LegacyAiScoreContext ctx) {
        double rating = 0.0;
        double points = getUnfinishedCompletablePoints(completable, ctx);
        for (Player p : ctx.getMajorOwners()) {
            rating += reducePoints(points, p);
        }
        return rating;
    }

    protected double getUnfinishedCompletablePoints(Completable complatable, LegacyAiScoreContext ctx) {
        if (complatable instanceof City) {
            return getUnfinishedCityPoints((City) complatable, ctx);
        }
        if (complatable instanceof Road) {
            return getUnfinishedRoadPoints((Road) complatable, ctx);
        }
        if (complatable instanceof Cloister) {
            return getUnfinishedCloisterPoints((Cloister) complatable, ctx);
        }
        throw new IllegalArgumentException();
    }

    protected double getUnfinishedCityPoints(City city, LegacyAiScoreContext ctx) {
        double chanceToClose = ctx.getChanceToClose();

        if (chanceToClose > MIN_CHANCE && ctx.getMajorOwners().contains(getPlayer())) {
            openCount[OPEN_COUNT_CITY]++;
        }

        //legacy heuristic
        CityScoreContext cityCtx = (CityScoreContext) ctx.getCompletableScoreContext();
        if (chanceToClose < MIN_CHANCE) {
            return cityCtx.getPoints(false) + 3.0*chanceToClose;
        } else {
            return cityCtx.getPoints(true) - 3.0*(1.0-chanceToClose);
        }
    }

    protected double getUnfinishedRoadPoints(Road road, LegacyAiScoreContext ctx) {
        double chanceToClose = ctx.getChanceToClose();;

        if (chanceToClose > MIN_CHANCE && ctx.getMajorOwners().contains(getPlayer())) {
            openCount[OPEN_COUNT_ROAD]++;
        }

        //legacy heuristic
        RoadScoreContext roadCtx = (RoadScoreContext) ctx.getCompletableScoreContext();
        if (chanceToClose < MIN_CHANCE) {
            return roadCtx.getPoints(false) + 3.0*chanceToClose;
        } else {
            return roadCtx.getPoints(true) - 3.0*(1.0-chanceToClose);
        }

    }

    protected double getUnfinishedCloisterPoints(Cloister cloister, LegacyAiScoreContext ctx) {
        List<Meeple> followers = cloister.getMeeples();
        if (!followers.isEmpty() && isMe(followers.get(0).getPlayer())) {
            openCount[OPEN_COUNT_CLOITSTER]++;
        }
        double chanceToClose = ctx.getChanceToClose();
        int points = ctx.getPoints();
        return points + (9-points)*chanceToClose;
    }

    protected double getFarmPoints(Farm farm, Player p, FarmScoreContext ctx) {
        if (isMe(p)) {
            openCount[OPEN_COUNT_FARM]++;
        }
        return ctx.getPoints(p);
    }

    protected double rankSpecialFigures(LegacyAiScoreContext ctx) {
        double rating = 0.0;
        for (Meeple m : ctx.getSpecialMeeples()) {
            if (m instanceof Builder && isMe(m.getPlayer())) {
                rating += rankBuilder((Builder) m, ctx);
            }
        }
        return rating;
    }

    protected double rankBuilder(Builder builder, LegacyAiScoreContext ctx) {
        if (!ctx.getMajorOwners().contains(getPlayer())) {
            return -3.0; //builder in enemy object penalty
        }
        if (ctx.getChanceToClose() < 0.55) return 0.0;
        double rating = 0.0;
        //builder placed in object
        if (builder.getFeature() instanceof City) {
            rating += 1.5;
        } else {
            rating += 0.5;
        }

        BuilderCapability bc = game.getCapability(BuilderCapability.class);
        //builder used on object
        if (bc.getBuilderState() == BuilderState.ACTIVATED) {
            rating += 3.5;
        }
        return rating;
    }

    private double rankTrappedMeeples(LegacyAiScoreContext ctx) {
        //musi tu byt dolni mez - btw nestaci toto misto hodnoceni figurek, spis asi :)

        //TODO lepe
        if (myTurnsLeft < 8) return 0.0;

        if (ctx.getChanceToClose() > 0.4) return 0.0;

        double rating = 0.0;
        for (Meeple m : ctx.getMeeples()) {
            if (isMe(m.getPlayer())) {
                rating += TRAPPED_MY_FIGURE_POINTS;
            } else {
                rating += TRAPPED_ENEMY_FIGURE_POINTS;
            }
        }
        return (1.0 - ctx.getChanceToClose()) * rating; //no reduce
    }


    @Override
    public void selectDragonMove(Set<Position> positions, int movesLeft) {
        initVars();
        Position dragonPosition = game.getCapability(DragonCapability.class).getDragonPosition();
        double tensionX = 0, tensionY = 0;

        for (Meeple m : game.getDeployedMeeples()) {
            int distance = dragonPosition.squareDistance(m.getPosition());
            if (distance == 0 || distance > movesLeft) continue;
            if (m.getFeature() instanceof Castle) continue;

            double weight = 1.0 / (distance * distance);
            if (isMe(m.getPlayer())) {
                weight *= -0.8;	//co takhle 0.8
            }
            tensionX += weight * (m.getPosition().x - dragonPosition.x);
            tensionY += weight * (m.getPosition().y - dragonPosition.y);
        }

        double minDiff = Double.MAX_VALUE;
        Position result = null;
        for (Position p : positions) {
            double diff =
                Math.abs(p.x - dragonPosition.x - tensionX) + Math.abs(p.y - dragonPosition.y - tensionY);
            if (diff < minDiff) {
                minDiff = diff;
                result = p;
            }
        }
        logger.info("Selected dragon move: {}", result);
        getServer().moveDragon(result);
    }

}
