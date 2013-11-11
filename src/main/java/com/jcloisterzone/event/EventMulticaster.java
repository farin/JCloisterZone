package com.jcloisterzone.event;

import java.util.EnumSet;
import java.util.EventListener;
import java.util.List;
import java.util.Set;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.UserInterface;
import com.jcloisterzone.action.PlayerAction;
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


public class EventMulticaster implements GameEventListener, UserInterface {

    protected final EventListener a, b;

    protected EventMulticaster(EventListener a, EventListener b) {
        this.b = b;
        this.a = a;
    }

    public static EventListener addListener(EventListener a, EventListener b) {
        if (a == null)  return b;
        if (b == null)  return a;
        return new EventMulticaster(a, b);
    }

    public static EventListener removeListener(EventListener l, EventListener lOld) {
        if (l instanceof EventMulticaster) {
            EventMulticaster mc = (EventMulticaster) l;
            return addListener(removeListener(mc.a, lOld), removeListener(mc.b, lOld));
        } else {
            return l == lOld ? null : l;
        }
    }

    @Override
    public void updateCustomRule(CustomRule rule, Boolean enabled) {
        ((GameEventListener)a).updateCustomRule(rule, enabled);
        ((GameEventListener)b).updateCustomRule(rule, enabled);

    }

    @Override
    public void updateExpansion(Expansion expansion, Boolean enabled) {
        ((GameEventListener)a).updateExpansion(expansion, enabled);
        ((GameEventListener)b).updateExpansion(expansion, enabled);
    }

    @Override
    public void updateSlot(PlayerSlot slot) {
        ((GameEventListener)a).updateSlot(slot);
        ((GameEventListener)b).updateSlot(slot);
    }

    @Override
    public void updateSupportedExpansions(EnumSet<Expansion> expansions) {
        ((GameEventListener)a).updateSupportedExpansions(expansions);
        ((GameEventListener)b).updateSupportedExpansions(expansions);
    }

    @Override
    public void started(Snapshot snapshot) {
        ((GameEventListener)a).started(snapshot);
        ((GameEventListener)b).started(snapshot);
    }

    @Override
    public void playerActivated(Player p, Player p2) {
        ((GameEventListener)a).playerActivated(p, p2);
        ((GameEventListener)b).playerActivated(p, p2);
    }

    @Override
    public void tileDrawn(Tile tile) {
        ((GameEventListener)a).tileDrawn(tile);
        ((GameEventListener)b).tileDrawn(tile);
    }

    @Override
    public void tileDiscarded(Tile tile) {
        ((GameEventListener)a).tileDiscarded(tile);
        ((GameEventListener)b).tileDiscarded(tile);
    }

    @Override
    public void tilePlaced(Tile tile) {
        ((GameEventListener)a).tilePlaced(tile);
        ((GameEventListener)b).tilePlaced(tile);
    }

    @Override
    public void dragonMoved(Position p) {
        ((GameEventListener)a).dragonMoved(p);
        ((GameEventListener)b).dragonMoved(p);

    }

    @Override
    public void fairyMoved(Position p) {
        ((GameEventListener)a).fairyMoved(p);
        ((GameEventListener)b).fairyMoved(p);
    }

    @Override
    public void towerIncreased(Position p, Integer height) {
        ((GameEventListener)a).towerIncreased(p, height);
        ((GameEventListener)b).towerIncreased(p, height);
    }

    @Override
    public void bazaarTileSelected(int supplyIndex, BazaarItem bazaarItem) {
        ((GameEventListener)a).bazaarTileSelected(supplyIndex, bazaarItem);
        ((GameEventListener)b).bazaarTileSelected(supplyIndex, bazaarItem);
    }

    @Override
    public void selectAction(List<PlayerAction> actions, boolean canPass) {
        ((UserInterface)a).selectAction(actions, canPass);
        ((UserInterface)b).selectAction(actions, canPass);
    }

    @Override
    public void selectBazaarTile() {
        ((UserInterface)a).selectBazaarTile();
        ((UserInterface)b).selectBazaarTile();
    }



    @Override
    public void makeBazaarBid(int supplyIndex) {
        ((UserInterface)a).makeBazaarBid(supplyIndex);
        ((UserInterface)b).makeBazaarBid(supplyIndex);
    }

    @Override
    public void selectBuyOrSellBazaarOffer(int supplyIndex) {
        ((UserInterface)a).selectBuyOrSellBazaarOffer(supplyIndex);
        ((UserInterface)b).selectBuyOrSellBazaarOffer(supplyIndex);
    }

    @Override
    public void selectCornCircleOption() {
        ((UserInterface)a).selectCornCircleOption();
        ((UserInterface)b).selectCornCircleOption();
    }

    @Override
    public void selectDragonMove(Set<Position> positions, int movesLeft) {
        ((UserInterface)a).selectDragonMove(positions, movesLeft);
        ((UserInterface)b).selectDragonMove(positions, movesLeft);
    }

    @Override
    public void showWarning(String title, String message) {
        ((UserInterface)a).showWarning(title, message);
        ((UserInterface)b).showWarning(title, message);
    }

    @Override
    public void gameOver() {
        ((GameEventListener)a).gameOver();
        ((GameEventListener)b).gameOver();
    }

    @Override
    public void phaseEntered(Phase phase) {
        ((GameEventListener)a).phaseEntered(phase);
        ((GameEventListener)b).phaseEntered(phase);
    }

    @Override
    public void ransomPaid(Player from, Player to, Follower ft) {
        ((GameEventListener)a).ransomPaid(from, to, ft);
        ((GameEventListener)b).ransomPaid(from, to, ft);
    }

    @Override
    public void tunnelPiecePlaced(Player player, Position p, Location d, boolean isSecondPiece) {
        ((GameEventListener)a).tunnelPiecePlaced(player, p, d, isSecondPiece);
        ((GameEventListener)b).tunnelPiecePlaced(player, p, d, isSecondPiece);
    }

    @Override
    public void deployed(Meeple m) {
        ((GameEventListener)a).deployed(m);
        ((GameEventListener)b).deployed(m);
    }


    @Override
    public void undeployed(Meeple m) {
        ((GameEventListener)a).undeployed(m);
        ((GameEventListener)b).undeployed(m);
    }

    @Override
    public void completed(Completable feature, CompletableScoreContext ctx) {
        ((GameEventListener)a).completed(feature, ctx);
        ((GameEventListener)b).completed(feature, ctx);
    }

    @Override
    public void scored(Feature feature, int points, String label, Meeple meeple, boolean isFinal) {
        ((GameEventListener)a).scored(feature, points, label, meeple, isFinal);
        ((GameEventListener)b).scored(feature, points, label, meeple, isFinal);
    }

    @Override
    public void scored(Position position, Player player, int points, String label, boolean isFinal) {
        ((GameEventListener)a).scored(position, player, points, label, isFinal);
        ((GameEventListener)b).scored(position, player, points, label, isFinal);
    }

    @Override
    public void bridgeDeployed(Position pos, Location loc) {
        ((GameEventListener)a).bridgeDeployed(pos, loc);
        ((GameEventListener)b).bridgeDeployed(pos, loc);
    }

    @Override
    public void castleDeployed(Castle castle1, Castle castle2) {
        ((GameEventListener)a).castleDeployed(castle1, castle2);
        ((GameEventListener)b).castleDeployed(castle1, castle2);
    }

    @Override
    public void bazaarAuctionsEnded() {
        ((GameEventListener)a).bazaarAuctionsEnded();
        ((GameEventListener)b).bazaarAuctionsEnded();
    }

    @Override
    public void plagueSpread() {
        ((GameEventListener)a).plagueSpread();
        ((GameEventListener)b).plagueSpread();
    }

    @Override
    public void chatMessage(Player player, String message) {
        ((GameEventListener)a).chatMessage(player, message);
        ((GameEventListener)b).chatMessage(player, message);

    }

    @Override
    public String toString() {
        return String.valueOf(a) + "," + String.valueOf(b);
    }

}
