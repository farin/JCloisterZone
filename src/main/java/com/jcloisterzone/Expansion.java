package com.jcloisterzone;

import static com.jcloisterzone.ui.I18nUtils._tr;

import com.jcloisterzone.game.capability.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.game.Capability;
import com.jcloisterzone.plugin.Plugin;

import io.vavr.collection.Vector;

@SuppressWarnings("unchecked")
/**
 * See https://boardgamegeek.com/wiki/page/Carcassonne_series
 */
public class Expansion {

    // Basic sets
    public static Expansion BASIC = new Expansion("BASIC", "BA", _tr("Basic game"),
            new Class[] { StandardGameCapability.class }, ExpansionType.BASIC);

    // Winter branch
    public static Expansion WINTER = new Expansion("WINTER", "WI", _tr("Winter Edition"), ExpansionType.BASIC_EXT);
    @NotImplemented public static Expansion GINGERBREAD_MAN = new Expansion("GINGERBREAD_MAN", "GM", _tr("The Gingerbread Man"), ExpansionType.MINOR);

    // Big expansions
    public static Expansion INNS_AND_CATHEDRALS = new Expansion("INNS_AND_CATHEDRALS", "IC", _tr("Inns & Cathedrals"),
            new Class[] { BigFollowerCapability.class, InnCapability.class, CathedralCapability.class}, ExpansionType.MAJOR);
    public static Expansion TRADERS_AND_BUILDERS = new Expansion("TRADERS_AND_BUILDERS", "TB", _tr("Traders & Builders"),
            new Class[] { PigCapability.class, BuilderCapability.class, TradeGoodsCapability.class, PigHerdCapability.class }, ExpansionType.MAJOR);
    public static Expansion PRINCESS_AND_DRAGON = new Expansion("PRINCESS_AND_DRAGON", "DG", _tr("The Princess & The Dragon"),
            new Class[] { FairyCapability.class, DragonCapability.class, PortalCapability.class, PrincessCapability.class }, ExpansionType.MAJOR);
    public static Expansion TOWER = new Expansion("TOWER", "TO", _tr("The Tower"),
            new Class[] { TowerCapability.class }, ExpansionType.MAJOR);
    public static Expansion ABBEY_AND_MAYOR = new Expansion("ABBEY_AND_MAYOR", "AM", _tr("Abbey & Mayor"),
            new Class[] { AbbeyCapability.class, WagonCapability.class, MayorCapability.class, BarnCapability.class }, ExpansionType.MAJOR);
    @NotImplemented public static Expansion CATAPULT = new Expansion("CATAPULT", "CA", _tr("The Catapult"), ExpansionType.MAJOR);
    public static Expansion BRIDGES_CASTLES_AND_BAZAARS = new Expansion("BRIDGES_CASTLES_AND_BAZAARS", "BB", _tr("Bridges, Castles and Bazaars"),
            new Class[] { BridgeCapability.class, CastleCapability.class, BazaarCapability.class }, ExpansionType.MAJOR);
    public static Expansion HILLS_AND_SHEEP = new Expansion("HILLS_AND_SHEEP", "HS", _tr("Hills and Sheep"),
    		new Class[] { SheepCapability.class, HillCapability.class, VineyardCapability.class }, ExpansionType.MAJOR);
    @NotImplemented public static Expansion UNDER_THE_BIG_TOP = new Expansion("UNDER_THE_BIG_TOP", "UN", _tr("Under the Big Top"), ExpansionType.MAJOR);

    // Small expansion
    public static Expansion KING_AND_ROBBER_BARON = new Expansion("KING_AND_ROBBER_BARON", "KR", _tr("King and Robber Baron"), new Class[] { KingAndRobberBaronCapability.class }, ExpansionType.MINOR);
    public static Expansion RIVER = new Expansion("RIVER", "R1", _tr("The River"), new Class[] { RiverCapability.class }, ExpansionType.MINOR);
    public static Expansion RIVER_II = new Expansion("RIVER_II", "R2", _tr("The River II"), new Class[] { RiverCapability.class }, ExpansionType.MINOR);
    public static Expansion CATHARS = new Expansion("CATHARS", "SI", _tr("The Cathars / Siege"), new Class[] { SiegeCapability.class }, ExpansionType.MINOR);
    public static Expansion BESIEGERS = new Expansion("BESIEGERS", "BE", _tr("The Besiegers"), new Class[] { SiegeCapability.class }, ExpansionType.MINOR);
    public static Expansion COUNT = new Expansion("COUNT", "CO", _tr("The Count of Carcassonne"), new Class[] { CountCapability.class }, ExpansionType.MINOR);
    public static Expansion GQ11 = new Expansion("GQ11", "GQ", _tr("The Mini Expansion (GQ11)"), ExpansionType.MINOR);
    public static Expansion CULT = new Expansion("CULT", "CU", _tr("The Cult"),  new Class[] { ShrineCapability.class }, ExpansionType.MINOR);
    public static Expansion TUNNEL = new Expansion("TUNNEL", "TU", _tr("The Tunnel"), new Class[] { TunnelCapability.class }, ExpansionType.MINOR);
    public static Expansion CORN_CIRCLES = new Expansion("CORN_CIRCLES", "CC", _tr("The Corn Circles"), new Class[] { CornCircleCapability.class }, ExpansionType.MINOR);
    @NotImplemented public static Expansion PLAGUE = new Expansion("PLAGUE", "PL", _tr("The Plague"), ExpansionType.MINOR);
    public static Expansion PHANTOM = new Expansion("PHANTOM", "PH", _tr("The Phantom"),  new Class[] { PhantomCapability.class }, ExpansionType.MINOR);
    public static Expansion FESTIVAL = new Expansion("FESTIVAL", "FE", _tr("The Festival (10th an.)"), new Class[] { FestivalCapability.class }, ExpansionType.MINOR);
    public static Expansion LITTLE_BUILDINGS = new Expansion("LITTLE_BUILDINGS", "LB", _tr("Little Buildings"), new Class[] { LittleBuildingsCapability.class }, ExpansionType.MINOR);
    public static Expansion WIND_ROSE = new Expansion("WIND_ROSE", "WR", _tr("The Wind Rose"), new Class[] { WindRoseCapability.class }, ExpansionType.MINOR);
    public static Expansion GERMAN_MONASTERIES = new Expansion("GERMAN_MONASTERIES", "GM", _tr("The German Monasteries"), new Class[] { GermanMonasteriesCapability.class }, ExpansionType.MINOR);
    @NotImplemented public static Expansion CASTLES = new Expansion("CASTLES", "CS", _tr("Castles in Germany"), ExpansionType.MINOR);
    @NotImplemented public static Expansion HALFINGS_I = new Expansion("HALFINGS_I", "H1", _tr("Halfings") + " I", ExpansionType.MINOR);
    @NotImplemented public static Expansion HALFINGS_II = new Expansion("HALFINGS_II", "H2", _tr("Halfings") + " ÏI", ExpansionType.MINOR);
    @NotImplemented public static Expansion WATCHTOWER = new Expansion("WATCHTOWER", "WT", _tr("The Watchtower"), ExpansionType.MINOR);

    // Minis expansion line
    public static Expansion FLIER = new Expansion("FLIER", "FL", "#1 - " + _tr("The Flier"), new Class[] { FlierCapability.class }, ExpansionType.MINI);
    @NotImplemented public static Expansion MESSAGES = new Expansion("MESSAGES", "ME", "#2 - " + _tr("The Messages"), ExpansionType.MINI);
    public static Expansion FERRIES = new Expansion("FERRIES", "FR", "#3 - " + _tr("The Ferries"), new Class[] { FerriesCapability.class }, ExpansionType.MINI);
    public static Expansion GOLDMINES = new Expansion("GOLDMINES", "GO", "#4 - " + _tr("The Goldmines"), new Class[] { GoldminesCapability.class }, ExpansionType.MINI);
    public static Expansion MAGE_AND_WITCH = new Expansion("MAGE_AND_WITCH", "MW", "#5 - " + _tr("Mage & Witch"), new Class[] { MageAndWitchCapability.class }, ExpansionType.MINI);
    @NotImplemented public static Expansion ROBBERS = new Expansion("ROBBERS", "RO", "#6 - " + _tr("The Robbers"), ExpansionType.MINI);
    public static Expansion CORN_CIRCLES_II = new Expansion("CORN_CIRCLES_II", "C2", "#7 - " + _tr("The Corn Circles II"), new Class[] { CornCircleCapability.class }, ExpansionType.MINI);

    // Promo/one tile expansions
    @NotImplemented public static Expansion SCHOOL = new Expansion("SCHOOL", "SC", _tr("The School"), ExpansionType.PROMO);
    @NotImplemented public static Expansion LA_PORXADA = new Expansion("LA_PORXADA", "PX", _tr("La Porxada"), ExpansionType.PROMO);
    public static Expansion RUSSIAN_PROMOS = new Expansion("RUSSIAN_PROMOS", "RP", _tr("Russian Promos"), new Class[] { YagaCapability.class }, ExpansionType.PROMO);
    public static Expansion DARMSTADT = new Expansion("DARMSTADT", "DA", _tr("Darmstadt Promo"), new Class[] { ChurchCapability.class }, ExpansionType.PROMO);
    public static Expansion LABYRINTH = new Expansion("LABYRINTH", "LA", _tr("Labyrinth"), new Class[] { LabyrinthCapability.class }, ExpansionType.PROMO);

    @NotImplemented public static Expansion _MISSING_PLACEHOLDER = new Expansion("(Unknown expansion)", "??", _tr("(Unknown expansion)"), ExpansionType.UNKNOWN);

    private static Vector<Expansion> _values = Vector.of(
        BASIC, WINTER,
        INNS_AND_CATHEDRALS, TRADERS_AND_BUILDERS, PRINCESS_AND_DRAGON, TOWER,
        ABBEY_AND_MAYOR, BRIDGES_CASTLES_AND_BAZAARS, HILLS_AND_SHEEP,
        KING_AND_ROBBER_BARON, RIVER, RIVER_II, CATHARS, BESIEGERS, COUNT, GQ11, CULT, TUNNEL,
        CORN_CIRCLES, PHANTOM, FESTIVAL, LITTLE_BUILDINGS, WIND_ROSE, GERMAN_MONASTERIES,
        FLIER, FERRIES, GOLDMINES, MAGE_AND_WITCH, CORN_CIRCLES_II,
        RUSSIAN_PROMOS, DARMSTADT, LABYRINTH
    );

    private final String name;
    private final String code;
    private final String label;
    private final Class<? extends Capability<?>>[] capabilities;
    private final ExpansionType type;
    private Plugin origin;

    public Expansion(String name, String code, String label, ExpansionType type) {
        this(name, code, label, null, type);
    }

    public Expansion(String name, String code, String label, Class<? extends Capability<?>>[] capabilities,
            ExpansionType type) {
        this.name = name;
        this.code = code;
        this.label = label;
        this.capabilities = capabilities == null ? new Class[0] : capabilities;
        this.type = type;
    }

    /** Returns all implemented expansion */
    public static Vector<Expansion> values() {
        return _values;
    }

    public String name() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public ExpansionType getType() {
        return type;
    }

    public Plugin getOrigin() {
        return origin;
    }

    public Class<? extends Capability<?>>[] getCapabilities() {
        return capabilities;
    }

    @Override
    public String toString() {
        return label;
    }

    public static Expansion valueOf(String name) {
        for (Expansion exp : values()) {
            if (exp.name.equals(name)) return exp;
        }
        return _MISSING_PLACEHOLDER;
    }

    public static Expansion valueOfCode(String code) {
        for (Expansion exp : values()) {
            if (exp.code.equals(code)) return exp;
        }
        return _MISSING_PLACEHOLDER;
    }

    public static void register(Expansion exp, Plugin origin) {
        Logger logger = LoggerFactory.getLogger(Expansion.class);
        exp.origin = origin;
        for (Expansion other : _values) {
            if (other.name.equals(exp.name)) {
                logger.warn("Expansion {} is already registered.", exp.name);
                return;
            }
            if (other.code.equals(exp.code)) {
                logger.warn("Expansion {} is already registered.", exp.code);
                return;
            }
        }
        _values = _values.append(exp);
        logger.info("Expansion {} has been registered.", exp.name());
    }

    public static void unregister(Expansion exp) {
        assert exp.origin != null;
        _values = _values.remove(exp);
    }
}
