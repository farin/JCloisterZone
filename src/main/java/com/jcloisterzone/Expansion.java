package com.jcloisterzone;

import static com.jcloisterzone.game.Capability.*;
import static com.jcloisterzone.ui.I18nUtils._;

import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.ExpandedGame;
import com.jcloisterzone.game.expansion.AbbeyAndMayorGame;
import com.jcloisterzone.game.expansion.BridgesCastlesBazaarsGame;
import com.jcloisterzone.game.expansion.CatharsGame;
import com.jcloisterzone.game.expansion.CornCirclesGame;
import com.jcloisterzone.game.expansion.CountGame;
import com.jcloisterzone.game.expansion.CultGame;
import com.jcloisterzone.game.expansion.FestivalGame;
import com.jcloisterzone.game.expansion.FlierGame;
import com.jcloisterzone.game.expansion.InnsAndCathedralsGame;
import com.jcloisterzone.game.expansion.KingAndScoutGame;
import com.jcloisterzone.game.expansion.PhantomGame;
import com.jcloisterzone.game.expansion.PlagueGame;
import com.jcloisterzone.game.expansion.PrincessAndDragonGame;
import com.jcloisterzone.game.expansion.RiverGame;
import com.jcloisterzone.game.expansion.RiverIIGame;
import com.jcloisterzone.game.expansion.TowerGame;
import com.jcloisterzone.game.expansion.TradersAndBuildersGame;
import com.jcloisterzone.game.expansion.TunnelGame;

public enum Expansion {
    //Basic sets
    BASIC("BA", _("Basic game"), null,
        new Capability[] { FARM_PLACEMENT }),
    WINTER("WI", _("Winter Edition")),
    WHEEL_OF_FORTUNE("WF", _("Wheel of Fortune"), false),

    //Big expansions
    INNS_AND_CATHEDRALS("IC", _("Inns & Cathedrals"), InnsAndCathedralsGame.class,
            new Capability[] { BIG_FOLLOWER /*,INN, CATHEDRAL*/ }),
    TRADERS_AND_BUILDERS("TB", _("Traders & Builders"), TradersAndBuildersGame.class,
            new Capability[] { PIG, BUILDER/*, CITY_RESOURCE*/ }),
    PRINCESS_AND_DRAGON("DG", _("The Princess & the Dragon"), PrincessAndDragonGame.class,
            new Capability[] { FAIRY, DRAGON /*, PRINCESS, MAGIC_GATE*/ }),
    TOWER("TO", _("The Tower"), TowerGame.class),
    ABBEY_AND_MAYOR("AM", _("Abbey & Mayor"), AbbeyAndMayorGame.class,
            new Capability[] { ABBEY }),
    CATAPULT("CA", _("The Catapult") + " (" + _("tiles only") + ")"),
    BRIDGES_CASTLES_AND_BAZAARS("BB", _("Bridges, Castles and Bazaars"), BridgesCastlesBazaarsGame.class),

    //Small expansion
    KING_AND_SCOUT("KS", _("King and Scout"), KingAndScoutGame.class),
    RIVER("R1", _("The River"), RiverGame.class),
    RIVER_II("R2", _("The River II"), RiverIIGame.class),
    CATHARS("SI", _("The Cathars / Siege"), CatharsGame.class), //cathars aka siege
    COUNT("CO", _("The Count of Carcassonne") + " (" + _("tiles only") + ")", CountGame.class),
    GQ11("GQ", _("The Mini Expansion (GQ11)")),
    CULT("CU", _("The Cult"), CultGame.class),
    TUNNEL("TU", _("The Tunnel"), TunnelGame.class),
    CORN_CIRCLES("CC", _("The Corn Circles"), CornCirclesGame.class),
    PLAGUE("PL", _("The Plague") + " (" + _("tiles only") + ")", PlagueGame.class),
    PHANTOM("PH", _("The Phantom"), PhantomGame.class),
    FESTIVAL("FE", _("The Festival (10th an.)"), FestivalGame.class),
    HOUSES("LB", _("Little Buildings"), false),
    WIND_ROSE("WR", _("The Wind Rose"), false),

    //minis expansion line
    FLIER("FL", "#1 - " + _("The Flier"), FlierGame.class),
    MESSAGES("ME", "#2 - " + _("The Messages"), false),
    FERRIES("FR", "#3 - " + _("The Ferries"), false),
    GOLDMINES("GO", "#4 - " + _("The Goldmines"), false),
    MAGE_WITCH("MW", "#5 - " + _("Mage & Witch"), false),
    ROBBER("RO", "#6 - " + _("The Robber"), false),
    CORN_CIRCLES_II("C2", "#7 - " + _("The Corn circles II"), CornCirclesGame.class); //shares expanded game class with CC!

    //promo/one tile expansions
    //LA_PORXADA("PX", _("La porxada"), false),
    //SCHOOL("SC", _("The school"), false);

    String code;
    String label;
    boolean enabled = true;
    Class<? extends ExpandedGame> expandedBy;
    Capability[] capabilities;

    Expansion(String code, String label) {
        this(code, label, null, null);
    }
    Expansion(String code, String label, Class<? extends ExpandedGame> expandedBy) {
        this(code, label, expandedBy, null);
    }

    Expansion(String code, String label, boolean enabled) {
        this(code, label, null, null);
        this.enabled = enabled;
    }

    Expansion(String code, String label, Class<? extends ExpandedGame> expandedBy, Capability[] capabilities) {
        this.code = code;
        this.label = label;
        this.expandedBy = expandedBy;
        this.capabilities = capabilities == null ? new Capability[0] : capabilities;
    }

    public String getCode() {
        return code;
    }

    public boolean isEnabled() {
        return enabled;
    }
    public Class<? extends ExpandedGame> getExpandedBy() {
        return expandedBy;
    }

    public Capability[] getCapabilities() {
        return capabilities;
    }

    @Override
    public String toString() {
        return label;
    }

    public static Expansion valueOfCode(String code) {
        for(Expansion exp : values()) {
            if (exp.code.equals(code)) return exp;
        }
        return null;
    }
}