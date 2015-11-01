package com.jcloisterzone.ui.controls;

import java.awt.Color;
import java.awt.Image;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Rotation;
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
import com.jcloisterzone.ui.theme.FigureTheme;

public class PlayerPanelImageCache {

    private final Client client;
    private Map<String, Image> scaledImages = new HashMap<>();

    public PlayerPanelImageCache(Client client, Game game) {
        this.client = client;
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
        FigureTheme theme = client.getFigureTheme();
        //Image img = theme.getFigureImage(type, color, null);
        for (Meeple f : meeples) {
            String key = player.getIndex() + f.getClass().getSimpleName();
            if (!scaledImages.containsKey(key)) {
                scaledImages.put(key, scaleImage(theme.getFigureImage(f.getClass(), color, null)));
            }
        }
    }

    private void scaleImages(Game game) {
        FigureTheme theme = client.getFigureTheme();
        for (Player player : game.getAllPlayers()) {
            Color color = player.getColors().getMeepleColor();
            scaleFigureImages(player, color, player.getFollowers());
            scaleFigureImages(player, color, player.getSpecialMeeples());
            if (game.hasCapability(TunnelCapability.class)) {
                scaledImages.put(player.getIndex()+"tunnelA", scaleImage(theme.getTunnelImage(player.getColors().getMeepleColor())));
                scaledImages.put(player.getIndex()+"tunnelB", scaleImage(theme.getTunnelImage(player.getColors().getTunnelBColor())));
            }
        }
        if (game.hasCapability(TowerCapability.class)) {
            scaledImages.put("towerpiece", scaleImage(theme.getNeutralImage("towerpiece")));
        }
        if (game.hasCapability(KingAndRobberBaronCapability.class)) {
            scaledImages.put("king", scaleImage(theme.getNeutralImage("king")));
            scaledImages.put("robber", scaleImage(theme.getNeutralImage("robber")));
        }
        if (game.hasCapability(BazaarCapability.class)) {
            scaledImages.put("bridge", scaleImage(theme.getNeutralImage("bridge")));
            scaledImages.put("castle", scaleImage(theme.getNeutralImage("castle")));
        }
        if (game.hasCapability(ClothWineGrainCapability.class)) {
            scaledImages.put("cloth", theme.getNeutralImage("cloth"));
            scaledImages.put("grain", theme.getNeutralImage("grain"));
            scaledImages.put("wine", theme.getNeutralImage("wine"));
        }
        if (game.hasCapability(BarnCapability.class)) {
            scaledImages.put("abbey", scaleImage(client.getResourceManager().getAbbeyImage(Rotation.R0)));
        }
        if (game.hasCapability(LittleBuildingsCapability.class)) {
            scaledImages.put("lb-tower", scaleImage(theme.getNeutralImage("lb-tower")));
            scaledImages.put("lb-house", scaleImage(theme.getNeutralImage("lb-house")));
            scaledImages.put("lb-shed", scaleImage(theme.getNeutralImage("lb-shed")));
        }
        if (game.hasCapability(GoldminesCapability.class)) {
            scaledImages.put("gold", scaleImage(theme.getNeutralImage("gold")));
        }
        if (game.hasCapability(DragonCapability.class)) {
            Image scaled = new ImageIcon(theme.getNeutralImage("dragon").getScaledInstance(42, 42, Image.SCALE_SMOOTH)).getImage();
            scaledImages.put("dragon", scaled);
        }
        if (game.hasCapability(FairyCapability.class)) {
            scaledImages.put("fairy", scaleImage(theme.getNeutralImage("fairy")));
        }
        if (game.hasCapability(MageAndWitchCapability.class)) {
            scaledImages.put("mage", scaleImage(theme.getNeutralImage("mage")));
            scaledImages.put("witch", scaleImage(theme.getNeutralImage("witch")));
        }
    }

}
