package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.event.GameStateChangeEvent;
import com.jcloisterzone.event.ScoreEvent;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.score.ScoreAllCallback;
import com.jcloisterzone.feature.score.ScoreAllFeatureFinder;
import com.jcloisterzone.feature.score.ScoringStrategy;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.feature.visitor.score.FarmScoreContext;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.FairyCapability;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.message.GameOverMessage;
import com.jcloisterzone.wsio.message.ToggleClockMessage;


public class GameOverPhase extends ServerAwarePhase implements ScoreAllCallback, ScoringStrategy {

    public GameOverPhase(Game game, GameController controller) {
        super(game, controller);
    }

    @Override
    public void enter() {
        if (isLocalPlayer(game.getTurnPlayer())) {
            //invoke only by single client
            getConnection().send(new ToggleClockMessage(game.getGameId(), null));
            getConnection().send(new GameOverMessage(game.getGameId()));
        }

        FairyCapability fairyCap = game.getCapability(FairyCapability.class);
        if (fairyCap != null) {
            //erase position to not affect final scoring
            fairyCap.setFairyPosition(null);
        }

        ScoreAllFeatureFinder scoreAll = new ScoreAllFeatureFinder();
        scoreAll.scoreAll(game, this);

        game.finalScoring(this);
        game.post(new GameStateChangeEvent(GameStateChangeEvent.GAME_OVER));
    }

    @Override
    public void scoreCastle(Meeple meeple, Castle castle) {
        ScoreEvent ev = new ScoreEvent(meeple.getFeature(), 0, PointCategory.CASTLE, meeple);
        ev.setFinal(true);
        game.post(ev);
    }

    @Override
    public void scoreFarm(FarmScoreContext ctx, Player p) {
        int points = ctx.getPoints(p);
        game.scoreFeature(points, ctx, p);
    }

    @Override
    public void scoreBarn(FarmScoreContext ctx, Barn meeple) {
        int points = ctx.getBarnPoints();
        meeple.getPlayer().addPoints(points, PointCategory.FARM);
        ScoreEvent ev = new ScoreEvent(meeple.getFeature(), points, PointCategory.FARM, meeple);
        ev.setFinal(true);
        game.post(ev);
    }


    @Override
    public void scoreCompletableFeature(CompletableScoreContext ctx) {
        game.scoreCompletableFeature(ctx);
    }

    @Override
    public CompletableScoreContext getCompletableScoreContext(Completable completable) {
        return completable.getScoreContext();
    }

    @Override
    public FarmScoreContext getFarmScoreContext(Farm farm) {
        return farm.getScoreContext();
    }

    @Override
    public Player getActivePlayer() {
        return null;
    }

    @Override
    public void addPoints(Player player, int points, PointCategory category) {
        player.addPoints(points, category);
    }

}
