package com.jcloisterzone.game;

import static com.jcloisterzone.ui.I18nUtils._;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Objects;
import com.jcloisterzone.Application;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.VersionComparator;
import com.jcloisterzone.XmlUtils;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.PlayerSlot.SlotType;
import com.jcloisterzone.game.phase.Phase;


public class Snapshot implements Serializable {

    protected transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final String COMPATIBLE_FROM = "2.3";

    private Document doc;
    private Element root;

    private Map<Position, Element> tileElemens; //write cache

    private boolean gzipOutput = true;


    public Snapshot(Game game, long clientId) {
        createRootStructure(game);
        createRuleElements(game);
        createExpansionElements(game);
        createCapabilityElements(game);
        createPlayerElements(game, clientId);
        createTileElements(game);
        createMeepleElements(game);
    }

    public Snapshot(File savedGame) throws IOException, SAXException {
        try {
            load(new GZIPInputStream(new FileInputStream(savedGame)));
        } catch (IOException e) {
            if ("Not in GZIP format".equals(e.getMessage())) {
                load(new FileInputStream(savedGame));
            } else {
                throw e;
            }
        }
    }

    public boolean isGzipOutput() {
        return gzipOutput;
    }

    public void setGzipOutput(boolean gzipOutput) {
        this.gzipOutput = gzipOutput;
    }


    private void createRootStructure(Game game) {
        doc = XmlUtils.newDocument();
        root = doc.createElement("game");
        root.setAttribute("app-version", Application.VERSION);
        root.setAttribute("phase", game.getPhase().getClass().getName());
        doc.appendChild(root);

    }

    private void createRuleElements(Game game) {
        for (CustomRule cr : game.getCustomRules()) {
            Element el = doc.createElement("rule");
            el.setAttribute("name", cr.name());
            root.appendChild(el);
        }
    }

    private void createExpansionElements(Game game) {
        for (Expansion exp : game.getExpansions()) {
            Element el = doc.createElement("expansion");
            el.setAttribute("name", exp.name());
            root.appendChild(el);
        }
    }

    private void createCapabilityElements(Game game) {
        for (Capability cap : game.getCapabilities()) {
            Element el = doc.createElement("capability");
            el.setAttribute("name", cap.getClass().getName());
            root.appendChild(el);
            cap.saveToSnapshot(doc, el);
        }
    }


    private void createPlayerElements(Game game, long clientId) {
        Element parent = doc.createElement("players");
        parent.setAttribute("turn", "" + game.getTurnPlayer().getIndex());
        root.appendChild(parent);
        for (Player p : game.getAllPlayers()) {
            Element el = doc.createElement("player");
            el.setAttribute("name", p.getNick());
            el.setAttribute("points", "" + p.getPoints());
            el.setAttribute("slot", "" + p.getSlot().getNumber());
            if (Objects.equal(p.getOwnerId(),clientId)) {
                el.setAttribute("local", "true");
            }
            if (p.getSlot().getType() == SlotType.AI) {
                el.setAttribute("ai-class", p.getSlot().getAiClassName());
            }
            parent.appendChild(el);
        }
    }

    private void createTileElements(Game game) {
        tileElemens = new HashMap<Position, Element>();
        Element parent = doc.createElement("tiles");
        if (game.getCurrentTile() != null) {
            parent.setAttribute("next", game.getCurrentTile().getId());
        }
        root.appendChild(parent);
        for (String group : game.getTilePack().getGroups()) {
            Element el = doc.createElement("group");
            el.setAttribute("name", group);
            el.setAttribute("active", "" + game.getTilePack().isGroupActive(group));
            parent.appendChild(el);
        }
        for (Tile tile : game.getBoard().getAllTiles()) {
            Element el = doc.createElement("tile");
            el.setAttribute("name", tile.getId());
            el.setAttribute("rotation", tile.getRotation().name());
            XmlUtils.injectPosition(el, tile.getPosition());
            parent.appendChild(el);
            tileElemens.put(tile.getPosition(), el);
            game.saveTileToSnapshot(tile, doc, el);
        }
        for (Tile tile : game.getBoard().getDiscardedTiles()) {
            Element el = doc.createElement("discard");
            el.setAttribute("name", tile.getId());
            parent.appendChild(el);
        }
    }

    private void createMeepleElements(Game game) {
        for (Meeple m : game.getDeployedMeeples()) {
            Element tileEl = tileElemens.get(m.getPosition());
            Element el = doc.createElement("meeple");
            el.setAttribute("player", "" + m.getPlayer().getIndex());
            el.setAttribute("type", "" + m.getClass().getName());
            el.setAttribute("loc", "" + m.getLocation());
            tileEl.appendChild(el);
        }
    }

    public void save(File file) throws TransformerException, IOException {
        OutputStream os = new FileOutputStream(file);
        StreamResult streamResult;
        if (gzipOutput) {
            streamResult = new StreamResult(new GZIPOutputStream(os));
        } else {
            streamResult = new StreamResult(os);
        }
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer serializer = tf.newTransformer();
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        serializer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        serializer.setOutputProperty(OutputKeys.METHOD, "xml");
        serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");
        serializer.transform(new DOMSource(doc), streamResult);
        streamResult.getOutputStream().close();
    }


    public void load(InputStream is) throws SnapshotCorruptedException {
        doc = XmlUtils.parseDocument(is);
        root = doc.getDocumentElement();
        String snapshotVersion = root.getAttribute("app-version");
        if (!snapshotVersion.equals(Application.VERSION)) { //first check simple equality (useful for dev version without numbers)
            if ((new VersionComparator()).compare(snapshotVersion, Snapshot.COMPATIBLE_FROM) < 0) {
                throw new SnapshotVersionException("Saved game is not compatible with current JCloisterZone application. (saved in "+snapshotVersion+")");
            }
        }
    }

    private NodeList getSecondLevelElelents(String first, String second) {
        return ((Element)root.getElementsByTagName(first).item(0)).getElementsByTagName(second);
    }

    public Set<Expansion> getExpansions() {
        Set<Expansion> result = new HashSet<>();
        NodeList nl = root.getElementsByTagName("expansion");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            Expansion exp = Expansion.valueOf(el.getAttribute("name"));
            result.add(exp);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public void loadCapabilities(Game game) {
        NodeList nl = root.getElementsByTagName("capability");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            String capabilityName = el.getAttribute("name");
            try {
                //TODO instances should be created here, not in load phase
                Class<? extends Capability> capabilityClass = (Class<? extends Capability>) Class.forName(capabilityName);
                Capability capability = game.getCapability(capabilityClass);
                capability.loadFromSnapshot(doc, el);
            } catch (Exception e) {
                logger.error("Incompatible or corrupted snapshot. Problem with stored expansion: " + capabilityName, e);
                game.getUserInterface().showWarning(_("Load error"), _("Saved game is incompatible or file is corrupted. Game couldn't work properly."));
            }
        }
    }

    public Set<CustomRule> getCustomRules() {
        Set<CustomRule> result = new HashSet<>();
        NodeList nl = root.getElementsByTagName("rule");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            CustomRule rule = CustomRule.valueOf(el.getAttribute("name"));
            result.add(rule);
        }
        return result;
    }

    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        NodeList nl = getSecondLevelElelents("players", "player");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            PlayerSlot slot = new PlayerSlot(Integer.parseInt(el.getAttribute("slot")));
            Player p = new Player(el.getAttribute("name"), i, slot);
            p.setPoints(Integer.parseInt(el.getAttribute("points")));
            if (el.hasAttribute("ai-class")) {
                slot.setType(SlotType.AI);
                String aiClassName = el.getAttribute("ai-class");
                slot.setAiClassName(aiClassName);
            } else {
                if (el.hasAttribute("local")) {
                    slot.setType(SlotType.PLAYER);
                }
            }
            players.add(p);
        }
        return players;
    }

    public PlayerSlot[] getPlayerSlots() {
        List<Player> players = getPlayers();
        int maxSlotNumber = 0;
        for (Player player : players) {
            int slotNumber = player.getSlot().getNumber();
            if (slotNumber > maxSlotNumber) maxSlotNumber = slotNumber;
        }

        PlayerSlot[] slots = new PlayerSlot[maxSlotNumber+1];
        for (int i = 0; i < slots.length; i++) {
            for (Player player : players) {
                PlayerSlot slot = player.getSlot();
                if (slot.getNumber() == i) {
                    slot.setNick(player.getNick());
                    slots[i] = slot;
                    break;
                }
            }
        }
        return slots;
    }

    public int getTurnPlayer() {
        Element el = (Element) doc.getElementsByTagName("players").item(0);
        return Integer.parseInt(el.getAttribute("turn"));
    }

    public List<String> getActiveGroups() {
        List<String> result = new ArrayList<>();
        NodeList nl = getSecondLevelElelents("tiles", "group");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            if (Boolean.parseBoolean(el.getAttribute("active"))) {
                result.add(el.getAttribute("name"));
            }
        }
        return result;
    }

    public List<String> getDiscardedTiles() {
        List<String> result = new ArrayList<>();
        NodeList nl = getSecondLevelElelents("tiles", "discard");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            result.add(el.getAttribute("name"));
        }
        return result;
    }

    public Rotation extractTileRotation(Element el) {
        return Rotation.valueOf(el.getAttribute("rotation"));
    }

    @SuppressWarnings("unchecked")
    public List<Meeple> extractTileMeeples(Element tileEl, Game game, Position pos) throws SnapshotCorruptedException {
        NodeList nl = tileEl.getElementsByTagName("meeple");
        List<Meeple> result = new ArrayList<>();
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            Location loc = Location.valueOf(el.getAttribute("loc"));
            Class<? extends Meeple> mt = (Class<? extends Meeple>) XmlUtils.classForName(el.getAttribute("type"));
            int playerIndex = Integer.parseInt(el.getAttribute("player"));
            Meeple meeple = game.getPlayer(playerIndex).getMeepleFromSupply(mt);
            meeple.setLocation(loc);
            meeple.setPosition(pos);
            //don't set feature here. Feature must be set after meeple deployment to correct replace ref during merge
            result.add(meeple);
        }
        return result;
    }

    public NodeList getTileElements() {
        return getSecondLevelElelents("tiles", "tile");
    }

    public String getNextTile() {
        Element el = (Element) root.getElementsByTagName("tiles").item(0);
        return el.getAttribute("next");
    }

    @SuppressWarnings("unchecked")
    public Class<? extends Phase> getActivePhase() throws SnapshotCorruptedException {
        return (Class<? extends Phase>) XmlUtils.classForName(root.getAttribute("phase"));
    }

    public Game asGame() {
        Game game = new Game();
        game.getExpansions().addAll(getExpansions());
        game.getCustomRules().addAll(getCustomRules());
        game.setPlayers(getPlayers(), getTurnPlayer());
        return game;
    }

    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        String xml = (String) stream.readObject();
        InputStream is = new ByteArrayInputStream(xml.getBytes());
        load(is);
        logger = LoggerFactory.getLogger(getClass());
    }

    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        //StreamResult streamResult = new StreamResult(new ZipOutputStream(os));
        StreamResult streamResult = new StreamResult(os);
        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            Transformer serializer = tf.newTransformer();
            serializer.transform(new DOMSource(doc), streamResult);
            stream.writeObject(os.toString());
        } catch (TransformerException e) {
            throw new IOException(e);
        }
    }

}
