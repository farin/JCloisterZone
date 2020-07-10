package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.action.BazaarBidAction;
import com.jcloisterzone.action.BazaarSelectBuyOrSellAction;
import com.jcloisterzone.action.BazaarSelectTileAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.BazaarCapabilityModel;
import com.jcloisterzone.game.capability.BazaarItem;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.Flag;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.AddPoints;
import com.jcloisterzone.wsio.message.BazaarBidMessage;
import com.jcloisterzone.wsio.message.BazaarBuyOrSellMessage;
import com.jcloisterzone.wsio.message.BazaarBuyOrSellMessage.BuyOrSellOption;
import com.jcloisterzone.wsio.message.PassMessage;

import io.vavr.Tuple2;
import io.vavr.collection.Queue;

@RequiredCapability(BazaarCapability.class)
public class BazaarPhase extends Phase {

    public BazaarPhase(RandomGenerator random) {
        super(random);
    }

    @Override
    public StepResult enter(GameState state) {
        if (!state.hasFlag(Flag.BAZAAR_AUCTION)) {
            return next(state);
        }

        int size = state.getPlayers().length();
        TilePack tilePack = state.getTilePack();

        if (tilePack.size() < size) {
            return next(state);
        }

        Queue<BazaarItem> supply = Queue.empty();

        for (int i = 0; i < size; i++) {
            Tuple2<Tile, TilePack> t = tilePack.drawTile(getRandom());
            state = state.setTilePack(t._2);
            supply = supply.append(new BazaarItem(t._1, 0, null, null));
        }

        Player player = state.getTurnPlayer().getNextPlayer(state);
        BazaarCapabilityModel model = new BazaarCapabilityModel(supply, null, player);

        state = state.setCapabilityModel(BazaarCapability.class, model);

        BazaarSelectTileAction action = new BazaarSelectTileAction(supply.toLinkedSet());
        state = state.setPlayerActions(
            new ActionsState(player, action, false)
        );
        return promote(state);
    }

    private boolean hasTileAssigned(BazaarCapabilityModel model, Player p) {
        for (BazaarItem bi : model.getSupply()) {
            if (p.equals(bi.getOwner())) return true;
        }
        return false;
    }

    @PhaseMessageHandler
    public StepResult bazaarBid(GameState state, BazaarBidMessage msg) {
        int supplyIndex = msg.getSupplyIndex();
        int price = msg.getPrice();

        boolean noAuction = state.getBooleanRule(Rule.BAZAAR_NO_AUCTION);

        Player player = state.getActivePlayer();
        PlayerAction<?> action = state.getPlayerActions().getActions().get();
        boolean isTileSelection = action instanceof BazaarSelectTileAction;

        state = state.mapCapabilityModel(BazaarCapability.class, model -> {
            BazaarItem item = model.getAuctionedItem();

            if (isTileSelection) {
                assert item == null;

                item = model.getSupply().get(supplyIndex);
                model = model.setAuctionedItemIndex(supplyIndex);
                if (noAuction) {
                    assert item.getCurrentPrice() == 0;
                    item = item.setOwner(player);
                    model = model.updateSupplyItem(supplyIndex, item);
                    return model;
                }
            }

            item = item.setCurrentPrice(price);
            item = item.setCurrentBidder(player);
            model = model.updateSupplyItem(supplyIndex, item);

            return model;
        });

        if (noAuction) {
            return nextSelectingPlayer(state);
        } else {
            return nextBidder(state);
        }
    }

    private StepResult nextBidder(GameState state) {
        Player nextBidder = state.getActivePlayer();
        BazaarCapabilityModel model = state.getCapabilityModel(BazaarCapability.class);
        BazaarItem item = model.getAuctionedItem();
        Player tileSelectingPlayer = model.getTileSelectingPlayer();

        do {
            nextBidder = nextBidder.getNextPlayer(state);
            if (nextBidder.equals(tileSelectingPlayer)) {
                //all players makes bid
                if (tileSelectingPlayer.equals(item.getCurrentBidder())) {
                    return buyOrSell(state, BuyOrSellOption.BUY);
                } else {
                    BazaarSelectBuyOrSellAction action = new BazaarSelectBuyOrSellAction();
                    ActionsState as = new ActionsState(nextBidder, action, false);
                    return promote(state.setPlayerActions(as));
                }
            }
        } while (hasTileAssigned(model, nextBidder));

        BazaarBidAction action = new BazaarBidAction();
        ActionsState as = new ActionsState(nextBidder, action, false);
        return promote(state.setPlayerActions(as));
    }

    private StepResult nextSelectingPlayer(GameState state) {
        BazaarCapabilityModel model = state.getCapabilityModel(BazaarCapability.class);
        Player currentSelectingPlayer = model.getTileSelectingPlayer();
        Player player = currentSelectingPlayer;

        model = model.setAuctionedItemIndex(null);

        do {
            player = player.getNextPlayer(state);
            if (!hasTileAssigned(model, player)) {
                model = model.setTileSelectingPlayer(player);

                state = state.setCapabilityModel(BazaarCapability.class, model);

                BazaarSelectTileAction action = new BazaarSelectTileAction(model.getSupply().toLinkedSet());
                state = state.setPlayerActions(
                    new ActionsState(player, action, false)
                );
                return promote(state);
            }
        } while (player != currentSelectingPlayer);

        //all tiles has been auctioned
        Queue<BazaarItem> supply =  model.getSupply();

        model = model.setSupply(
            state.getPlayers().getPlayersBeginWith(
                state.getTurnPlayer().getNextPlayer(state)
            )
            .map(p ->
                supply.find(bi -> bi.getOwner().equals(p)).get()
            )
            .toQueue()
        );
        model = model.setAuctionedItemIndex(null);
        model = model.setTileSelectingPlayer(null);

        state = state.setCapabilityModel(BazaarCapability.class, model);
        return next(state);
    }

    @Override
    @PhaseMessageHandler
    public StepResult handlePass(GameState state, PassMessage msg) {
        BazaarCapabilityModel model = state.getCapabilityModel(BazaarCapability.class);
        Player p = state.getActivePlayer();

        if (p.equals(model.getTileSelectingPlayer())) {
            throw new IllegalStateException("Tile selecting player is not allowed to pass");
        }
        return nextBidder(state);
    }

    @PhaseMessageHandler
    public StepResult handleBazaarBuyOrSellMessage(GameState state, BazaarBuyOrSellMessage msg) {
        return buyOrSell(state, msg.getValue());
    }

    private StepResult buyOrSell(GameState state, BuyOrSellOption option) {
        BazaarCapabilityModel model = state.getCapabilityModel(BazaarCapability.class);

        BazaarItem bi = model.getAuctionedItem();
        int points = bi.getCurrentPrice();
        Player pSelecting = model.getTileSelectingPlayer();
        Player pBidding = bi.getCurrentBidder();

        assert !pSelecting.equals(pBidding) || option == BuyOrSellOption.BUY; //if same, buy is flag expected
        if (option == BuyOrSellOption.SELL) points *= -1;

        state = (new AddPoints(pSelecting, -points, PointCategory.BAZAAR_AUCTION)).apply(state);
        if (!pSelecting.equals(pBidding)) {
            state = (new AddPoints(pBidding, points, PointCategory.BAZAAR_AUCTION)).apply(state);
        }

        bi = bi.setOwner(option == BuyOrSellOption.BUY ? pSelecting : pBidding);
        bi = bi.setCurrentBidder(null);

        model = model.updateSupplyItem(model.getAuctionedItemIndex(), bi);
        state = state.setCapabilityModel(BazaarCapability.class, model);

        return nextSelectingPlayer(state);
    }
}
