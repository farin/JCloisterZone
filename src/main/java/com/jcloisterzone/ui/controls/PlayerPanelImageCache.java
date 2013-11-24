package com.jcloisterzone.ui.controls;

import java.awt.Color;
import java.awt.Image;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import com.jcloisterzone.Player;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.capability.BarnCapability;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.ClothWineGrainCapability;
import com.jcloisterzone.game.capability.KingScoutCapability;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.theme.FigureTheme;

public class PlayerPanelImageCache {

    private final Client client;
    private Map<String, Image> scaledImages = new HashMap<>();

    public PlayerPanelImageCache(Client client) {
        this.client = client;
        scaleImages();
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

    private void scaleImages() {
        FigureTheme theme = client.getFigureTheme();
        for (Player player : client.getGame().getAllPlayers()) {
            Color color = client.getPlayerColor(player);
            scaleFigureImages(player, color, player.getFollowers());
            scaleFigureImages(player, color, player.getSpecialMeeples());
        }
        TowerCapability tower = client.getGame().getCapability(TowerCapability.class);
        if (tower != null) {
            scaledImages.put("towerpiece", scaleImage(theme.getNeutralImage("towerpiece")));
        }
        KingScoutCapability ks = client.getGame().getCapability(KingScoutCapability.class);
        if (ks != null) {
            scaledImages.put("king", scaleImage(theme.getNeutralImage("king")));
            scaledImages.put("robber", scaleImage(theme.getNeutralImage("robber")));
        }
        BazaarCapability bcb = client.getGame().getCapability(BazaarCapability.class);
        if (bcb != null) {
            scaledImages.put("bridge", scaleImage(theme.getNeutralImage("bridge")));
            scaledImages.put("castle", scaleImage(theme.getNeutralImage("castle")));
        }
        ClothWineGrainCapability cwg = client.getGame().getCapability(ClothWineGrainCapability.class);
        if (cwg != null) {
            scaledImages.put("cloth", theme.getNeutralImage("cloth"));
            scaledImages.put("grain", theme.getNeutralImage("grain"));
            scaledImages.put("wine", theme.getNeutralImage("wine"));
        }
        BarnCapability ab = client.getGame().getCapability(BarnCapability.class);
        if (ab != null) {
            scaledImages.put("abbey", scaleImage(client.getResourceManager().getAbbeyImage()));
        }
    }

}
