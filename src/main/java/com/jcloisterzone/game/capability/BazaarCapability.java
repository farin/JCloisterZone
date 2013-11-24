package com.jcloisterzone.game.capability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Player;
import com.jcloisterzone.XmlUtils;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class BazaarCapability extends Capability {

    private ArrayList<BazaarItem> bazaarSupply;
    private BazaarItem currentBazaarAuction;
    private Player bazaarTileSelectingPlayer;
    private Player bazaarBiddingPlayer;

    public BazaarCapability(Game game) {
        super(game);
    }

    @Override
    public Object[] backup() {
        return new Object[] {
            (bazaarSupply == null ? null : new ArrayList<>(bazaarSupply)),
            (currentBazaarAuction == null ? null : new BazaarItem(currentBazaarAuction)),
            bazaarTileSelectingPlayer,
            bazaarBiddingPlayer
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public void restore(Object data) {
        Object[] a = (Object[]) data;
        bazaarSupply = a[0] == null ? null : new ArrayList<>((ArrayList<BazaarItem>)a[0]);
        currentBazaarAuction = a[1] == null ? null : new BazaarItem((BazaarItem)a[1]);
        bazaarTileSelectingPlayer = (Player) a[2];
        bazaarBiddingPlayer = (Player) a[3];
    }

    @Override
    public void initTile(Tile tile, Element xml) {
        if (xml.getElementsByTagName("bazaar").getLength() > 0) {
            tile.setTrigger(TileTrigger.BAZAAR);
        }
    }

    @Override
    public void saveToSnapshot(Document doc, Element node) {
        if (bazaarSupply != null) {
            for (BazaarItem bi : bazaarSupply) {
                Element el = doc.createElement("bazaar-supply");
                el.setAttribute("tile", bi.getTile().getId());
                if (bi.getOwner() != null) el.setAttribute("owner", ""+bi.getOwner().getIndex());
                if (bi.getCurrentBidder() != null) el.setAttribute("bidder", ""+bi.getCurrentBidder().getIndex());
                el.setAttribute("price", ""+bi.getCurrentPrice());

                if (currentBazaarAuction == bi) {
                    el.setAttribute("selected", "true");
                }
                node.appendChild(el);
            }
        }
        if (bazaarTileSelectingPlayer != null) {
            Element el = doc.createElement("bazaar-selecting-player");
            el.setAttribute("player", ""+bazaarTileSelectingPlayer.getIndex());
            node.appendChild(el);
        }
        if (bazaarBiddingPlayer != null) {
            Element el = doc.createElement("bazaar-bidding-player");
            el.setAttribute("player", ""+bazaarBiddingPlayer.getIndex());
            node.appendChild(el);
        }
    }



    @Override
    public void loadFromSnapshot(Document doc, Element node) {
        NodeList nl = node.getElementsByTagName("bazaar-supply");
        if (nl.getLength() > 0) {
            bazaarSupply = new ArrayList<BazaarItem>(nl.getLength());
            for (int i = 0; i < nl.getLength(); i++) {
                Element el = (Element) nl.item(i);
                Tile tile = game.getTilePack().drawTile(el.getAttribute("tile"));
                BazaarItem bi = new BazaarItem(tile);
                bazaarSupply.add(bi);
                if (el.hasAttribute("owner")) bi.setOwner(game.getPlayer(Integer.parseInt(el.getAttribute("owner"))));
                if (el.hasAttribute("bidder")) bi.setCurrentBidder(game.getPlayer(Integer.parseInt(el.getAttribute("bidder"))));
                bi.setCurrentPrice(XmlUtils.attributeIntValue(el, "price"));
                if (XmlUtils.attributeBoolValue(el, "selected")) {
                    currentBazaarAuction = bi;
                }
            }
        }

        nl = node.getElementsByTagName("bazaar-selecting-player");
        if (nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
            bazaarTileSelectingPlayer = game.getPlayer(Integer.parseInt(el.getAttribute("player")));
        }
        nl = node.getElementsByTagName("bazaar-bidding-player");
        if (nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
            bazaarBiddingPlayer = game.getPlayer(Integer.parseInt(el.getAttribute("player")));
        }
    }

    public ArrayList<BazaarItem> getBazaarSupply() {
        return bazaarSupply;
    }

    public void setBazaarSupply(ArrayList<BazaarItem> bazaarSupply) {
        this.bazaarSupply = bazaarSupply;
    }

    public Player getBazaarTileSelectingPlayer() {
        return bazaarTileSelectingPlayer;
    }

    public void setBazaarTileSelectingPlayer(Player bazaarTileSelectingPlayer) {
        this.bazaarTileSelectingPlayer = bazaarTileSelectingPlayer;
    }

    public Player getBazaarBiddingPlayer() {
        return bazaarBiddingPlayer;
    }

    public void setBazaarBiddingPlayer(Player bazaarBiddingPlayer) {
        this.bazaarBiddingPlayer = bazaarBiddingPlayer;
    }

    public BazaarItem getCurrentBazaarAuction() {
        return currentBazaarAuction;
    }

    public void setCurrentBazaarAuction(BazaarItem currentBazaarAuction) {
        this.currentBazaarAuction = currentBazaarAuction;
    }

    public boolean hasTileAuctioned(Player p) {
        for (BazaarItem bi : bazaarSupply) {
            if (bi.getOwner() == p) return true;
        }
        return false;
    }

    public Tile drawNextTile() {
        if (bazaarSupply == null) return null;
        Player p = game.getActivePlayer();
        Tile tile = null;
        BazaarItem currentItem = null;
        for (BazaarItem bi : bazaarSupply) {
            if (bi.getOwner() == p) {
                currentItem = bi;
                tile = bi.getTile();
                break;
            }
        }
        bazaarSupply.remove(currentItem);
        if (bazaarSupply.isEmpty()) {
            bazaarSupply = null;
        }
        return tile;
    }

    public List<Tile> getDrawQueue() {
        if (bazaarSupply == null) return Collections.emptyList();
        List<Tile> result = new ArrayList<>();
        Player turnPlayer = game.getTurnPlayer();
        Player p = game.getNextPlayer(turnPlayer);
        while (p != turnPlayer) {
            for (BazaarItem bi : bazaarSupply) {
                if (bi.getOwner() == p) {
                    result.add(bi.getTile());
                    break;
                }
            }
            p = game.getNextPlayer(p);
        }
        return result;
    }

}