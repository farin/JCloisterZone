package com.jcloisterzone;

import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.capability.*;
import io.vavr.collection.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
/**
 * See https://boardgamegeek.com/wiki/page/Carcassonne_series
 */
public class Expansion {

    // Basic sets
    public static Expansion BASIC = new Expansion("BASIC", "BA", "Basic game",
            new Class[] { }, ExpansionType.BASIC);

    // Winter branch
    public static Expansion WINTER = new Expansion("WINTER", "WI", "Winter Edition", ExpansionType.BASIC_EXT);

    // Big expansions
    public static Expansion INNS_AND_CATHEDRALS = new Expansion("INNS_AND_CATHEDRALS", "IC", "Inns & Cathedrals",
            new Class[] { InnCapability.class, CathedralCapability.class}, ExpansionType.MAJOR);
    public static Expansion TRADERS_AND_BUILDERS = new Expansion("TRADERS_AND_BUILDERS", "TB", "Traders & Builders",
            new Class[] { BuilderCapability.class, TradeGoodsCapability.class, PigHerdCapability.class }, ExpansionType.MAJOR);
    public static Expansion PRINCESS_AND_DRAGON = new Expansion("PRINCESS_AND_DRAGON", "DG", "The Princess & The Dragon",
            new Class[] { FairyCapability.class, DragonCapability.class, PortalCapability.class, PrincessCapability.class }, ExpansionType.MAJOR);
    public static Expansion TOWER = new Expansion("TOWER", "TO", "The Tower",
            new Class[] { TowerCapability.class }, ExpansionType.MAJOR);
    public static Expansion ABBEY_AND_MAYOR = new Expansion("ABBEY_AND_MAYOR", "AM", "Abbey & Mayor",
            new Class[] { AbbeyCapability.class, WagonCapability.class, BarnCapability.class }, ExpansionType.MAJOR);
    // @NotImplemented public static Expansion CATAPULT = new Expansion("CATAPULT", "CA", "The Catapult", ExpansionType.MAJOR);
    public static Expansion BRIDGES_CASTLES_AND_BAZAARS = new Expansion("BRIDGES_CASTLES_AND_BAZAARS", "BB", "Bridges, Castles and Bazaars",
            new Class[] { BridgeCapability.class, CastleCapability.class, BazaarCapability.class }, ExpansionType.MAJOR);
    public static Expansion HILLS_AND_SHEEP = new Expansion("HILLS_AND_SHEEP", "HS", "Hills and Sheep",
    		new Class[] { SheepCapability.class, HillCapability.class, VineyardCapability.class }, ExpansionType.MAJOR);
    // @NotImplemented public static Expansion UNDER_THE_BIG_TOP = new Expansion("UNDER_THE_BIG_TOP", "UN", "Under the Big Top", ExpansionType.MAJOR);

    // Small expansion
    public static Expansion KING_AND_ROBBER_BARON = new Expansion("KING_AND_ROBBER_BARON", "KR", "King and Robber Baron", new Class[] { KingAndRobberBaronCapability.class }, ExpansionType.MINOR);
    public static Expansion RIVER = new Expansion("RIVER", "R1", "The River", new Class[] { RiverCapability.class }, ExpansionType.MINOR);
    public static Expansion RIVER_II = new Expansion("RIVER_II", "R2", "The River II", new Class[] { RiverCapability.class }, ExpansionType.MINOR);
    public static Expansion CATHARS = new Expansion("CATHARS", "SI", "The Cathars / Siege", new Class[] { SiegeCapability.class }, ExpansionType.MINOR);
    public static Expansion BESIEGERS = new Expansion("BESIEGERS", "BE", "The Besiegers", new Class[] { SiegeCapability.class }, ExpansionType.MINOR);
    public static Expansion COUNT = new Expansion("COUNT", "CO", "The Count of Carcassonne", new Class[] { CountCapability.class }, ExpansionType.MINOR);
    public static Expansion GQ11 = new Expansion("GQ11", "GQ", "The Mini Expansion (GQ11)", ExpansionType.MINOR);
    public static Expansion CULT = new Expansion("CULT", "CU", "The Cult",  new Class[] { ShrineCapability.class }, ExpansionType.MINOR);
    public static Expansion TUNNEL = new Expansion("TUNNEL", "TU", "The Tunnel", new Class[] { TunnelCapability.class }, ExpansionType.MINOR);
    public static Expansion CORN_CIRCLES = new Expansion("CORN_CIRCLES", "CC", "The Corn Circles", new Class[] { CornCircleCapability.class }, ExpansionType.MINOR);
    // @NotImplemented public static Expansion PLAGUE = new Expansion("PLAGUE", "PL", "The Plague", ExpansionType.MINOR);
    public static Expansion PHANTOM = new Expansion("PHANTOM", "PH", "The Phantom",  new Class[] { PhantomCapability.class }, ExpansionType.MINOR);
    public static Expansion FESTIVAL = new Expansion("FESTIVAL", "FE", "The Festival (10th an.)", new Class[] { FestivalCapability.class }, ExpansionType.MINOR);
    public static Expansion LITTLE_BUILDINGS = new Expansion("LITTLE_BUILDINGS", "LB", "Little Buildings", new Class[] { LittleBuildingsCapability.class }, ExpansionType.MINOR);
    public static Expansion WIND_ROSE = new Expansion("WIND_ROSE", "WR", "The Wind Rose", new Class[] { WindRoseCapability.class }, ExpansionType.MINOR);
    public static Expansion GERMAN_MONASTERIES = new Expansion("GERMAN_MONASTERIES", "GM", "The German Monasteries", new Class[] { GermanMonasteriesCapability.class }, ExpansionType.MINOR);
//    @NotImplemented public static Expansion CASTLES = new Expansion("CASTLES", "CS", "Castles in Germany", ExpansionType.MINOR);
//    @NotImplemented public static Expansion HALFINGS_I = new Expansion("HALFINGS_I", "H1", "Halfings" + " I", ExpansionType.MINOR);
//    @NotImplemented public static Expansion HALFINGS_II = new Expansion("HALFINGS_II", "H2", "Halfings" + " ÏI", ExpansionType.MINOR);
//    @NotImplemented public static Expansion WATCHTOWER = new Expansion("WATCHTOWER", "WT", "The Watchtower", ExpansionType.MINOR);

    // Minis expansion line
    public static Expansion FLIER = new Expansion("FLIER", "FL", "#1 - " + "The Flier", new Class[] { FlierCapability.class }, ExpansionType.MINI);
    // @NotImplemented public static Expansion MESSAGES = new Expansion("MESSAGES", "ME", "#2 - " + "The Messages", ExpansionType.MINI);
    public static Expansion FERRIES = new Expansion("FERRIES", "FR", "#3 - " + "The Ferries", new Class[] { FerriesCapability.class }, ExpansionType.MINI);
    public static Expansion GOLDMINES = new Expansion("GOLDMINES", "GO", "#4 - " + "The Goldmines", new Class[] { GoldminesCapability.class }, ExpansionType.MINI);
    public static Expansion MAGE_AND_WITCH = new Expansion("MAGE_AND_WITCH", "MW", "#5 - " + "Mage & Witch", new Class[] { MageAndWitchCapability.class }, ExpansionType.MINI);
    // @NotImplemented public static Expansion ROBBERS = new Expansion("ROBBERS", "RO", "#6 - " + "The Robbers", ExpansionType.MINI);
    public static Expansion CORN_CIRCLES_II = new Expansion("CORN_CIRCLES_II", "C2", "#7 - " + "The Corn Circles II", new Class[] { CornCircleCapability.class }, ExpansionType.MINI);

    // Promo/one tile expansions
//    @NotImplemented public static Expansion SCHOOL = new Expansion("SCHOOL", "SC", "The School", ExpansionType.PROMO);
//    @NotImplemented public static Expansion LA_PORXADA = new Expansion("LA_PORXADA", "PX", "La Porxada", ExpansionType.PROMO);
    public static Expansion RUSSIAN_PROMOS = new Expansion("RUSSIAN_PROMOS", "RP", "Russian Promos", new Class[] { YagaCapability.class }, ExpansionType.PROMO);
    public static Expansion DARMSTADT = new Expansion("DARMSTADT", "DA", "Darmstadt Promo", new Class[] { ChurchCapability.class }, ExpansionType.PROMO);
    public static Expansion LABYRINTH = new Expansion("LABYRINTH", "LA", "Labyrinth", new Class[] { LabyrinthCapability.class }, ExpansionType.PROMO);
    public static Expansion SPIEL_DOCH = new Expansion("SPIEL_DOCH", "SD", "Spiel Doch", new Class[] {}, ExpansionType.PROMO);


    @NotImplemented public static Expansion _MISSING_PLACEHOLDER = new Expansion("(Unknown expansion)", "??", "(Unknown expansion)", ExpansionType.UNKNOWN);

    private static Vector<Expansion> _values = Vector.of(
        BASIC, WINTER,
        INNS_AND_CATHEDRALS, TRADERS_AND_BUILDERS, PRINCESS_AND_DRAGON, TOWER,
        ABBEY_AND_MAYOR, BRIDGES_CASTLES_AND_BAZAARS, HILLS_AND_SHEEP,
        KING_AND_ROBBER_BARON, RIVER, RIVER_II, CATHARS, BESIEGERS, COUNT, GQ11, CULT, TUNNEL,
        CORN_CIRCLES, PHANTOM, FESTIVAL, LITTLE_BUILDINGS, WIND_ROSE, GERMAN_MONASTERIES,
        FLIER, FERRIES, GOLDMINES, MAGE_AND_WITCH, CORN_CIRCLES_II,
        RUSSIAN_PROMOS, DARMSTADT, LABYRINTH, SPIEL_DOCH
    );

    private final String name;
    private final String code;
    private final String label;
    private final Class<? extends Capability<?>>[] capabilities;
    private final ExpansionType type;

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

    public static void register(Expansion exp) {
        Logger logger = LoggerFactory.getLogger(Expansion.class);
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
        _values = _values.remove(exp);
    }
}
