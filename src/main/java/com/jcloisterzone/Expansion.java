package com.jcloisterzone;

import static com.jcloisterzone.ui.I18nUtils._;

import com.jcloisterzone.game.ExpandedGame;
import com.jcloisterzone.game.expansion.AbbeyAndMayorGame;
import com.jcloisterzone.game.expansion.BridgesCastlesBazaarsGame;
import com.jcloisterzone.game.expansion.CatharsGame;
import com.jcloisterzone.game.expansion.CountGame;
import com.jcloisterzone.game.expansion.CultGame;
import com.jcloisterzone.game.expansion.FestivalGame;
import com.jcloisterzone.game.expansion.InnsAndCathedralsGame;
import com.jcloisterzone.game.expansion.KingAndScoutGame;
import com.jcloisterzone.game.expansion.PrincessAndDragonGame;
import com.jcloisterzone.game.expansion.RiverGame;
import com.jcloisterzone.game.expansion.RiverIIGame;
import com.jcloisterzone.game.expansion.TowerGame;
import com.jcloisterzone.game.expansion.TradersAndBuildersGame;
import com.jcloisterzone.game.expansion.TunnelGame;

public enum Expansion {
    //Basic sets
    BASIC("BA", _("Basic game")),
    WHEEL_OF_FORTUNE("WF", _("Wheel of Fortune"), false),

    //Big expansions
    INNS_AND_CATHEDRALS("IC", _("Inns & Cathedrals"), InnsAndCathedralsGame.class),
    TRADERS_AND_BUILDERS("TB", _("Traders & Builders"), TradersAndBuildersGame.class),
    PRINCESS_AND_DRAGON("DG", _("The Princess & the Dragon"), PrincessAndDragonGame.class),
    TOWER("TO", _("The Tower"), TowerGame.class),
    ABBEY_AND_MAYOR("AM", _("Abbey & Mayor"), AbbeyAndMayorGame.class),
    CATAPULT("CA", _("The Catapult") + " (" + _("tiles only") + ")"),
    BRIDGES_CASTLES_AND_BAZAARS("BB", _("Bridges, Castles and Bazaars"), BridgesCastlesBazaarsGame.class),

    //Small expansion
    KING_AND_SCOUT("KS", _("King and Scout"), KingAndScoutGame.class),
    RIVER("R1", _("The River"), RiverGame.class),
    RIVER_II("R2", _("The River II"), RiverIIGame.class),
    CATHARS("SI", _("The Cathars"), CatharsGame.class), //cathars aka siege
    COUNT("CO", _("The Count of Carcassonne") + " (" + _("tiles only") + ")", CountGame.class),
    GQ11("GQ", _("The Mini Expansion (GQ11)")),
    CULT("CU", _("The Cult"), CultGame.class),
    TUNNEL("TU", _("The Tunnel"), TunnelGame.class),
    CROP_CIRCLES("CC", _("Crop Circles") + " (" + _("tiles only") + ")"),
    PLAGUE("PL", _("The Plague") + " (" + _("tiles only") + ")"),
    ENTOURAGE("EN", _("The Entourage"), false),
    PHANTOM("PH", _("The Phantom"), false),
    FESTIVAL("FE", _("The Festival (10th an.)"), FestivalGame.class),

    //promo/one tile expansions
    LA_PORXADA("PH", _("La porxada"), false),
    SCHOOL("SC", _("The school"), false);

    String code;
    String label;
    boolean enabled;
    Class<? extends ExpandedGame> expandedBy;

    Expansion(String code, String label) {
        this(code, label, null, true);
    }
    Expansion(String code, String label, Class<? extends ExpandedGame> expandedBy) {
        this(code, label, expandedBy, true);
    }
    Expansion(String code, String label, boolean expandedBy) {
        this(code, label, null, expandedBy);
    }

    Expansion(String code, String label, Class<? extends ExpandedGame> expandedBy, boolean enabled) {
        this.code = code;
        this.label = label;
        this.expandedBy = expandedBy;
        this.enabled = enabled;
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