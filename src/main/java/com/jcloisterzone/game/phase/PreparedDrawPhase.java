package com.jcloisterzone.game.phase;

import java.util.List;

import com.jcloisterzone.board.Tile;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.message.RandSampleMessage;


public class PreparedDrawPhase extends DrawPhase {

	private static final String END_OF_PACK = ".";

    public PreparedDrawPhase(Game game, Connection conn) {
    	super(game, conn);
    }

    @Override
    public Tile drawTileFromPack(RandSampleMessage msg) {
    	List<String> preparedTiles = game.getPreparedTiles().getDraw();
    	if (preparedTiles.isEmpty())
    	{
    		return super.drawTileFromPack(msg);
    	}
    	String tileId = preparedTiles.remove(0);
    	Tile tile = getTilePack().drawTile(tileId);
    	return tile;
    }

    @Override
    public boolean checkForEarlyPhaseChange() {
    	List<String> drawList = game.getPreparedTiles().getDraw();
    	if(drawList.isEmpty())
    	{
    		return false;
    	}
		String tileId = drawList.get(0);
    	if (tileId.equals(END_OF_PACK)) {
    		next(GameOverPhase.class);
    		return true;
    	}
    	return false;
    }
}
