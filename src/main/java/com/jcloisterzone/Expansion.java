package com.jcloisterzone;

import static com.jcloisterzone.game.Capability.ABBEY;
import static com.jcloisterzone.game.Capability.BARN;
import static com.jcloisterzone.game.Capability.BAZAAR;
import static com.jcloisterzone.game.Capability.BIG_FOLLOWER;
import static com.jcloisterzone.game.Capability.BRIDGE;
import static com.jcloisterzone.game.Capability.BUILDER;
import static com.jcloisterzone.game.Capability.CASTLE;
import static com.jcloisterzone.game.Capability.CATHEDRAL;
import static com.jcloisterzone.game.Capability.CLOTH_WINE_GRAIN;
import static com.jcloisterzone.game.Capability.DRAGON;
import static com.jcloisterzone.game.Capability.FAIRY;
import static com.jcloisterzone.game.Capability.FARM_PLACEMENT;
import static com.jcloisterzone.game.Capability.INN;
import static com.jcloisterzone.game.Capability.MAYOR;
import static com.jcloisterzone.game.Capability.PIG;
import static com.jcloisterzone.game.Capability.PORTAL;
import static com.jcloisterzone.game.Capability.PRINCESS;
import static com.jcloisterzone.game.Capability.WAGON;
import static com.jcloisterzone.ui.I18nUtils._;

import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.GameExtension;
import com.jcloisterzone.game.expansion.CatharsGame;
import com.jcloisterzone.game.expansion.CornCirclesGame;
import com.jcloisterzone.game.expansion.CountGame;
import com.jcloisterzone.game.expansion.CultGame;
import com.jcloisterzone.game.expansion.FestivalGame;
import com.jcloisterzone.game.expansion.FlierGame;
import com.jcloisterzone.game.expansion.KingAndScoutGame;
import com.jcloisterzone.game.expansion.PhantomGame;
import com.jcloisterzone.game.expansion.PlagueGame;
import com.jcloisterzone.game.expansion.RiverGame;
import com.jcloisterzone.game.expansion.RiverIIGame;
import com.jcloisterzone.game.expansion.TunnelGame;

public enum Expansion {
    //Basic sets
    BASIC("BA", _("Basic game"), null,
        new Capability[] { FARM_PLACEMENT }),
    WINTER("WI", _("Winter Edition")),
    WHEEL_OF_FORTUNE("WF", _("Wheel of Fortune"), false),

    //Big expansions
    INNS_AND_CATHEDRALS("IC", _("Inns & Cathedrals"),
            new Capability[] { BIG_FOLLOWER ,INN, CATHEDRAL }),
    TRADERS_AND_BUILDERS("TB", _("Traders & Builders"),
            new Capability[] { PIG, BUILDER, CLOTH_WINE_GRAIN }),
    PRINCESS_AND_DRAGON("DG", _("The Princess & the Dragon"),
            new Capability[] { FAIRY, DRAGON, PORTAL, PRINCESS }),
    TOWER("TO", _("The Tower"),
            new Capability[] { Capability.TOWER }),
    ABBEY_AND_MAYOR("AM", _("Abbey & Mayor"),
            new Capability[] { ABBEY, WAGON, MAYOR, BARN }),
    CATAPULT("CA", _("The Catapult") + " (" + _("tiles only") + ")"),
    BRIDGES_CASTLES_AND_BAZAARS("BB", _("Bridges, Castles and Bazaars"),
            new Capability[] { BRIDGE, CASTLE, BAZAAR }),

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
    Class<? extends GameExtension> impl;
    Capability[] capabilities;

    Expansion(String code, String label) {
        this(code, label, null, null);
    }
    Expansion(String code, String label, Class<? extends GameExtension> impl) {
        this(code, label, impl, null);
    }

    Expansion(String code, String label, boolean enabled) {
        this(code, label, null, null);
        this.enabled = enabled;
    }

    Expansion(String code, String label, Capability[] capabilities) {
        this(code, label, null, capabilities);
    }

    Expansion(String code, String label, Class<? extends GameExtension> impl, Capability[] capabilities) {
        this.code = code;
        this.label = label;
        this.impl = impl;
        this.capabilities = capabilities == null ? new Capability[0] : capabilities;
    }

    public String getCode() {
        return code;
    }

    public boolean isEnabled() {
        return enabled;
    }
    public Class<? extends GameExtension> getImplemetedBy() {
        return impl;
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