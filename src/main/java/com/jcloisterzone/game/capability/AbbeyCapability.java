package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.state.GameState;


/**
 * @model Integer: store playerIndex during final abbey placement turn
 */
public class AbbeyCapability extends Capability<Integer> {

    //private final Set<Player> unusedAbbey = new HashSet<>();
    //private Player abbeyRoundLastPlayer; //when last tile is drawn all players can still place abbey

//    @Override
//    public void initPlayer(Player player) {
//        unusedAbbey.add(player);
//    }

    @Override
    public GameState onStartGame(GameState state) {
        return state.mapPlayers(ps -> ps.setTokenCountForAllPlayers(Token.ABBEY_TILE, 1));
    }

    @Override
    public String getTileGroup(TileDefinition tile) {
        return tile.getId().equals(TileDefinition.ABBEY_TILE_ID) ? "inactive": null;
    }

//    public boolean hasUnusedAbbey(Player player) {
//        return unusedAbbey.contains(player);
//    }
//
//    public void useAbbey(Player player) {
//        if (!unusedAbbey.remove(player)) {
//            throw new IllegalArgumentException("Player alredy used his abbey");
//        }
//    }

//    public void undoUseAbbey(Player player) {
//        unusedAbbey.add(player);
//    }

//    public Player getAbbeyRoundLastPlayer() {
//        return abbeyRoundLastPlayer;
//    }
//
//    public void setAbbeyRoundLastPlayer(Player abbeyRoundLastPlayer) {
//        this.abbeyRoundLastPlayer = abbeyRoundLastPlayer;
//    }
}
