package com.jcloisterzone.ui.controls;

import java.awt.Color;
import java.awt.Image;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import com.jcloisterzone.Player;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.BarnCapability;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.ClothWineGrainCapability;
import com.jcloisterzone.game.capability.DragonCapability;
import com.jcloisterzone.game.capability.FairyCapability;
import com.jcloisterzone.game.capability.GoldminesCapability;
import com.jcloisterzone.game.capability.KingAndRobberBaronCapability;
import com.jcloisterzone.game.capability.LittleBuildingsCapability;
import com.jcloisterzone.game.capability.MageAndWitchCapability;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.game.capability.TunnelCapability;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;
import com.jcloisterzone.ui.resources.ResourceManager;

public class PlayerPanelImageCache {

    private final ResourceManager rm;
    private Map<String, Image> scaledImages = new HashMap<>();

    public PlayerPanelImageCache(Client client, Game game) {
        this.rm = client.getResourceManager();
        scaleImages(game);
    }

    public Image get(Player player, String key) {
        if (player == null) {
            return scaledImages.get(key);
        } else {
            return scaledImages.get(player.getIndex() + key);
        }
    }

    private Image scaleImage(Image img) {
        return new ImageIcon(img.getScaledInstance(30, 30, Image.SCALE_SMOOTH)).getImage();
    }

    private void scaleFigureImages(Player player, Color color, Collection<? extends Meeple> meeples) {
        for (Meeple f : meeples) {
            String key = player.getIndex() + f.getClass().getSimpleName();
            if (!scaledImages.containsKey(key)) {
            	Image img = rm.getLayeredImage(new LayeredImageDescriptor(f.getClass(), color));
                scaledImages.put(key, scaleImage(img));
            }
        }
    }

    private void scaleImages(Game game) {
        for (Player player : game.getAllPlayers()) {
            Color color = player.getColors().getMeepleColor();
            scaleFigureImages(player, color, player.getFollowers());
            scaleFigureImages(player, color, player.getSpecialMeeples());
            if (game.hasCapability(TunnelCapability.class)) {
            	Image tunnelA = rm.getLayeredImage(new LayeredImageDescriptor("player-meeples/tunnel", player.getColors().getMeepleColor()));
            	Image tunnelB = rm.getLayeredImage(new LayeredImageDescriptor("player-meeples/tunnel", player.getColors().getTunnelBColor()));

                scaledImages.put(player.getIndex()+"tunnelA", scaleImage(tunnelA));
                scaledImages.put(player.getIndex()+"tunnelB", scaleImage(tunnelB));
            }
        }
        if (game.hasCapability(TowerCapability.class)) {
            scaledImages.put("towerpiece", scaleImage(rm.getImage("neutral/towerpiece")));
        }
        if (game.hasCapability(KingAndRobberBaronCapability.class)) {
            scaledImages.put("king", scaleImage(rm.getImage("neutral/king")));
            scaledImages.put("robber", scaleImage(rm.getImage("neutral/robber")));
        }
        if (game.hasCapability(BazaarCapability.class)) {
            scaledImages.put("bridge", scaleImage(rm.getImage("neutral/bridge")));
            scaledImages.put("castle", scaleImage(rm.getImage("neutral/castle")));
        }
        if (game.hasCapability(ClothWineGrainCapability.class)) {
            scaledImages.put("cloth", rm.getImage("neutral/cloth"));
            scaledImages.put("grain", rm.getImage("neutral/grain"));
            scaledImages.put("wine", rm.getImage("neutral/wine"));
        }
        if (game.hasCapability(BarnCapability.class)) {
            scaledImages.put("abbey", scaleImage(rm.getAbbeyImage()));
        }
        if (game.hasCapability(LittleBuildingsCapability.class)) {
            scaledImages.put("lb-tower", scaleImage(rm.getImage("neutral/lb-tower")));
            scaledImages.put("lb-house", scaleImage(rm.getImage("neutral/lb-house")));
            scaledImages.put("lb-shed", scaleImage(rm.getImage("neutral/lb-shed")));
        }
        if (game.hasCapability(GoldminesCapability.class)) {
            scaledImages.put("gold", scaleImage(rm.getImage("neutral/gold")));
        }
        if (game.hasCapability(DragonCapability.class)) {
            Image scaled = new ImageIcon(rm.getImage("neutral/dragon").getScaledInstance(42, 42, Image.SCALE_SMOOTH)).getImage();
            scaledImages.put("dragon", scaled);
        }
        if (game.hasCapability(FairyCapability.class)) {
            scaledImages.put("fairy", scaleImage(rm.getImage("neutral/fairy")));
        }
        if (game.hasCapability(MageAndWitchCapability.class)) {
            scaledImages.put("mage", scaleImage(rm.getImage("neutral/mage")));
            scaledImages.put("witch", scaleImage(rm.getImage("neutral/witch")));
        }
    }

}
