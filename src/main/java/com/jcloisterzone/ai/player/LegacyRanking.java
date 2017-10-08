package com.jcloisterzone.ai.player;

import com.jcloisterzone.Player;
import com.jcloisterzone.ai.GameStateRanking;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.game.state.PlayersState;

class LegacyRanking implements GameStateRanking {

    private final Player me;

    // ranking context
    private PlacedTile lastPlaced;
    private GameState state;

    public LegacyRanking(Player me) {
        super();
        this.me = me;
    }

    @Override
    public Double apply(GameState state) {
        double ranking = 0.0;

        this.state = state;
        lastPlaced = state.getLastPlaced();

        ranking += ratePoints();
        ranking += rateMeeples();
        ranking += rateBoardShape();

        return ranking;
    }

    private double ratePoints() {
        double r = 0.0;
        PlayersState ps = state.getPlayers();
        for (Player player : ps.getPlayers()) {
            double q = player == me ? 1.0 : -1.0;
            r += q * ps.getScore().get(player.getIndex()).getPoints();
        }
        return r;
    }

    private double rateMeeples() {
        double r = 0.0;
        for (Player player : state.getPlayers().getPlayers()) {
            double q = player == me ? 1.0 : -1.0;
            for (Follower f : player.getFollowers(state).filter(f -> f.isInSupply(state))) {
                //instanceof cannot be used because of Phantom
                if (f.getClass().equals(SmallFollower.class)) {
                    r += q * 0.15;
                } else {
                    r += q * 0.25;
                }
            }
        }
        return r;
    }

    private double rateBoardShape() {
        return 0.0001 * state.getAdjacentTiles2(lastPlaced.getPosition()).size();
    }

}