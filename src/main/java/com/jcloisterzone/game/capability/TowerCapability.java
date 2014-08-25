package com.jcloisterzone.game.capability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.XmlUtils;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.TowerPieceAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.MeeplePrisonEvent;
import com.jcloisterzone.event.TowerIncreasedEvent;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.predicate.MeeplePredicates;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;


public final class TowerCapability extends Capability {

    private static final int RANSOM_POINTS = 3;

    private final Set<Position> towers = new HashSet<>();
    private final Map<Player, Integer> towerPieces = new HashMap<>();
    private boolean ransomPaidThisTurn;

    private Position lastIncreasedTower; //needed for persist game in TowerCapturePhase

    //key is Player who keeps follower imprisoned
    //synchronized because of GUI is looking inside
    //TODO fix gui hack
    private final Map<Player, List<Follower>> prisoners = Collections.synchronizedMap(new HashMap<Player, List<Follower>>());

    public TowerCapability(Game game) {
        super(game);
    }

    @Override
    public Object backup() {
        Map<Player, List<Follower>> prisonersCopy = new HashMap<>();
        for (Entry<Player, List<Follower>> entry : prisoners.entrySet()) {
            prisonersCopy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        return new Object[] {
            ransomPaidThisTurn,
            new HashSet<>(towers),
            new HashMap<>(towerPieces),
            prisonersCopy
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public void restore(Object data) {
        Object[] a = (Object[]) data;
        ransomPaidThisTurn = (Boolean) a[0];
        towers.clear();
        towers.addAll((Set<Position>) a[1]);
        towerPieces.clear();
        towerPieces.putAll((Map<Player, Integer>) a[2]);
        for (Entry<Player, List<Follower>> entry : ((Map<Player, List<Follower>>)a[3]).entrySet()) {
            List<Follower> value = prisoners.get(entry.getKey());
            value.clear();
            value.addAll(entry.getValue());
        }
    }

    public void registerTower(Position p) {
        towers.add(p);
    }

    public Set<Position> getTowers() {
        return towers;
    }

    public boolean isRansomPaidThisTurn() {
        return ransomPaidThisTurn;
    }

    public void setRansomPaidThisTurn(boolean ransomPayedThisTurn) {
        this.ransomPaidThisTurn = ransomPayedThisTurn;
    }

    @Override
    public void initPlayer(Player player) {
        int pieces = 0;
        switch(game.getAllPlayers().length) {
        case 1:
        case 2: pieces = 10; break;
        case 3: pieces = 9; break;
        case 4: pieces = 7; break;
        case 5: pieces = 6; break;
        case 6: pieces = 5; break;
        }
        towerPieces.put(player, pieces);
        prisoners.put(player, Collections.synchronizedList(new ArrayList<Follower>()));
    }

    public int getTowerPieces(Player player) {
        return towerPieces.get(player);
    }

    public int setTowerPieces(Player player, int pieces) {
        return towerPieces.put(player, pieces);
    }

    public void decreaseTowerPieces(Player player) {
        int pieces = getTowerPieces(player);
        if (pieces == 0) throw new IllegalStateException("Player has no tower pieces");
        towerPieces.put(player, pieces-1);
    }

    private boolean hasSmallOrBigFollower(Player p) {
        return Iterables.any(p.getFollowers(), Predicates.and(
                MeeplePredicates.inSupply(), MeeplePredicates.instanceOf(SmallFollower.class, BigFollower.class)));
    }

    @Override
    public void prepareActions(List<PlayerAction<?>> actions, Set<FeaturePointer> followerOptions) {
        if (hasSmallOrBigFollower(game.getActivePlayer())) {
            prepareTowerFollowerDeploy(findFollowerActions(actions));
        }
        if (getTowerPieces(game.getActivePlayer()) > 0) {
            Set<Position> availTowers = getOpenTowers(0);
            if (!availTowers.isEmpty()) {
                actions.add(new TowerPieceAction().addAll(availTowers));
            }
        }
    }

    public void prepareTowerFollowerDeploy(List<MeepleAction> followerActions) {
        Set<Position> availableTowers = getOpenTowers(1);
        if (!availableTowers.isEmpty()) {
            for (Position p : availableTowers) {
                if (game.isDeployAllowed(getBoard().get(p), Follower.class)) {
                    for (MeepleAction ma : followerActions) {
                        //only small, big and phantoms are allowed on top of tower
                        if (SmallFollower.class.isAssignableFrom(ma.getMeepleType()) || BigFollower.class.isAssignableFrom(ma.getMeepleType())) {
                            ma.add(new FeaturePointer(p, Location.TOWER));
                        }
                    }
                }
            }
        }
    }

    public void placeTowerPiece(Player player, Position pos) {
        Tower tower = getBoard().get(pos).getTower();
        if (tower  == null) {
            throw new IllegalArgumentException("No tower on tile.");
        }
        if (tower.getMeeple() != null) {
            throw new IllegalArgumentException("The tower is sealed");
        }
        decreaseTowerPieces(player);
        tower.increaseHeight();
        lastIncreasedTower = pos;
        game.post(new TowerIncreasedEvent(player, pos, tower.getHeight()));
    }

    protected Set<Position> getOpenTowers(int minHeight) {
        Set<Position> availTower = new HashSet<>();
        for (Position p : getTowers()) {
            Tower t = getBoard().get(p).getTower();
            if (t.getMeeple() == null && t.getHeight() >= minHeight) {
                availTower.add(p);
            }
        }
        return availTower;
    }

    public Map<Player, List<Follower>> getPrisoners() {
        return prisoners;
    }

    public Position getLastIncreasedTower() {
        return lastIncreasedTower;
    }


    public void setLastIncreasedTower(Position lastIncreasedTower) {
        this.lastIncreasedTower = lastIncreasedTower;
    }


    public boolean hasImprisonedFollower(Player followerOwner) {
        for (Follower m : followerOwner.getFollowers()) {
            if (m.getLocation() == Location.PRISON) return true;
        }
        return false;
    }

    public boolean hasImprisonedFollower(Player followerOwner, Class<? extends Follower> followerClass) {
        for (Follower m : followerOwner.getFollowers()) {
            if (m.getLocation() == Location.PRISON && m.getClass().equals(followerClass)) return true;
        }
        return false;
    }

    public void inprison(Meeple m, Player player) {
        assert m.getLocation() == null;
        prisoners.get(player).add((Follower) m);
        game.post(new MeeplePrisonEvent(m, null, player));
        m.setLocation(Location.PRISON);
    }

    public void payRansom(Integer playerIndexToPay, Class<? extends Follower> meepleType) {
        if (ransomPaidThisTurn) {
            throw new IllegalStateException("Ransom alreasy paid this turn");
        }
        Player opponent = game.getAllPlayers()[playerIndexToPay];

        Iterator<Follower> i = prisoners.get(opponent).iterator();
        while (i.hasNext()) {
            Follower meeple = i.next();
            if (meepleType.isInstance(meeple)) {
                i.remove();
                meeple.clearDeployment();
                opponent.addPoints(RANSOM_POINTS, PointCategory.TOWER_RANSOM);
                ransomPaidThisTurn = true;
                game.getActivePlayer().addPoints(-RANSOM_POINTS, PointCategory.TOWER_RANSOM);
                game.post(new MeeplePrisonEvent(meeple, opponent, null));
                game.getPhase().notifyRansomPaid();
                return;
            }
        }
        throw new IllegalStateException("Opponent has no figure to exchage");
    }

    @Override
    public void turnCleanUp() {
        ransomPaidThisTurn = false;
        lastIncreasedTower = null;
    }

    @Override
    public void saveToSnapshot(Document doc, Element node) {
        node.setAttribute("ransomPaid", ransomPaidThisTurn + "");
        if (lastIncreasedTower != null) {
            Element it = doc.createElement("increased-tower");
            XmlUtils.injectPosition(it, lastIncreasedTower);
            node.appendChild(it);
        }
        for (Position towerPos : towers) {
            Tower tower = getBoard().get(towerPos).getTower();
            Element el = doc.createElement("tower");
            node.appendChild(el);
            XmlUtils.injectPosition(el, towerPos);
            el.setAttribute("height", "" + tower.getHeight());
        }
        for (Player player: game.getAllPlayers()) {
            Element el = doc.createElement("player");
            node.appendChild(el);
            el.setAttribute("index", "" + player.getIndex());
            el.setAttribute("pieces", "" + getTowerPieces(player));
            for (Follower follower : prisoners.get(player)) {
                Element prisoner = doc.createElement("prisoner");
                el.appendChild(prisoner);
                prisoner.setAttribute("player", "" + follower.getPlayer().getIndex());
                prisoner.setAttribute("type", "" + follower.getClass().getName());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadFromSnapshot(Document doc, Element node) {
        ransomPaidThisTurn = Boolean.parseBoolean(node.getAttribute("ransomPaid"));
        NodeList nl = node.getElementsByTagName("increased-tower");
        if (nl.getLength() > 0) {
            lastIncreasedTower = XmlUtils.extractPosition((Element) nl.item(0));
        }
        nl = node.getElementsByTagName("tower");
        for (int i = 0; i < nl.getLength(); i++) {
            Element te = (Element) nl.item(i);
            Position towerPos = XmlUtils.extractPosition(te);
            Tower tower = getBoard().get(towerPos).getTower();
            tower.setHeight(Integer.parseInt(te.getAttribute("height")));
            towers.add(towerPos);
            if (tower.getHeight() > 0) {
                game.post(new TowerIncreasedEvent(null, towerPos, tower.getHeight()));
            }
        }
        nl = node.getElementsByTagName("player");
        for (int i = 0; i < nl.getLength(); i++) {
            Element playerEl = (Element) nl.item(i);
            Player player = game.getPlayer(Integer.parseInt(playerEl.getAttribute("index")));
            towerPieces.put(player, Integer.parseInt(playerEl.getAttribute("pieces")));
            NodeList priosonerNl = playerEl.getElementsByTagName("prisoner");
            for (int j = 0; j < priosonerNl.getLength(); j++) {
                Element prisonerEl = (Element) priosonerNl.item(j);
                int ownerIndex = XmlUtils.attributeIntValue(prisonerEl, "player");
                Class<? extends Meeple> meepleClass = (Class<? extends Meeple>) XmlUtils.classForName(prisonerEl.getAttribute("type"));
                Meeple m = game.getPlayer(ownerIndex).getMeepleFromSupply(meepleClass);
                inprison(m, player);
            }
        }
    }
}
