package com.jcloisterzone.event;

import java.util.EnumSet;
import java.util.EventListener;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.capability.BazaarItem;
import com.jcloisterzone.game.phase.Phase;

//TODO change event system - allow adding events withou signature change
public interface GameEventListener extends EventListener {

    void updateSlot(PlayerSlot slot);
    void updateExpansion(Expansion expansion, Boolean enabled);
    void updateCustomRule(CustomRule rule, Boolean enabled);
    void updateSupportedExpansions(EnumSet<Expansion> expansions);

    void started(Snapshot snapshot);

    void playerActivated(Player turnPlayer, Player activePlayer);

    //void scoreAssigned(int score, PlacedFigure pf); //TODO not used, revise use or delete
    void ransomPaid(Player from, Player to, Follower meeple);
    //void scoreAssigned(int score, Tile tile, Player player); //non-feature score (fairy?)

    void tileDrawn(Tile tile);

    void tileDiscarded(Tile tile);
    void tilePlaced(Tile tile);

    void dragonMoved(Position p);
    void fairyMoved(Position p);
    void towerIncreased(Position p, Integer height);

    void tunnelPiecePlaced(Player player, Position p, Location d, boolean isSecondPiece);

    void gameOver();

    void phaseEntered(Phase phase);

    //feature events
    void completed(Completable feature, CompletableScoreContext ctx);

    //TODO ad cattegory, pack into object adne merge ?
    void scored(Feature feature, int points, String label, Meeple meeple, boolean isFinal);
    void scored(Position position, Player player, int points, String label, boolean isFinal);

    //meeple events
    void deployed(Meeple meeple);
    void undeployed(Meeple meeple);

    //BB events
    void bridgeDeployed(Position pos, Location loc);
    void castleDeployed(Castle castle1, Castle castle2);
    void bazaarTileSelected(int supplyIndex, BazaarItem bazaarItem);
    void bazaarAuctionsEnded();

    void plagueSpread();
    void chatMessage(Player player, String message);
}
