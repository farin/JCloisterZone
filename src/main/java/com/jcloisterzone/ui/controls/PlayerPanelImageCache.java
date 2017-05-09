package com.jcloisterzone.ui.controls;

import java.awt.Color;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.state.GameState;
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

    private void scaleFigureImages(Player player, Color color, Iterable<? extends Meeple> meeples) {
        for (Meeple f : meeples) {
            String key = player.getIndex() + f.getClass().getSimpleName();
            if (!scaledImages.containsKey(key)) {
                Image img = rm.getLayeredImage(new LayeredImageDescriptor(f.getClass(), color));
                scaledImages.put(key, scaleImage(img));
            }
        }
    }

    private void scaleImages(Game game) {
        GameState state = game.getState();
        for (Player player : state.getPlayers().getPlayers()) {
            Color color = player.getColors().getMeepleColor();
            scaleFigureImages(player, color, player.getFollowers(state));
            scaleFigureImages(player, color, player.getSpecialMeeples(state));

            Image tunnelA = rm.getLayeredImage(new LayeredImageDescriptor("player-meeples/tunnel", player.getColors().getMeepleColor()));
            Image tunnelB = rm.getLayeredImage(new LayeredImageDescriptor("player-meeples/tunnel", player.getColors().getTunnelBColor()));

            scaledImages.put(player.getIndex()+"tunnelA", scaleImage(tunnelA));
            scaledImages.put(player.getIndex()+"tunnelB", scaleImage(tunnelB));

        }

        scaledImages.put("towerpiece", scaleImage(rm.getImage("neutral/towerpiece")));

        scaledImages.put("king", scaleImage(rm.getImage("neutral/king")));
        scaledImages.put("robber", scaleImage(rm.getImage("neutral/robber")));

        scaledImages.put("bridge", scaleImage(rm.getImage("neutral/bridge")));
        scaledImages.put("castle", scaleImage(rm.getImage("neutral/castle")));

        scaledImages.put("cloth", rm.getImage("neutral/cloth"));
        scaledImages.put("grain", rm.getImage("neutral/grain"));
        scaledImages.put("wine", rm.getImage("neutral/wine"));

        scaledImages.put("abbey", scaleImage(rm.getAbbeyImage(Rotation.R0).getImage()));

        scaledImages.put("lb-tower", scaleImage(rm.getImage("neutral/lb-tower")));
        scaledImages.put("lb-house", scaleImage(rm.getImage("neutral/lb-house")));
        scaledImages.put("lb-shed", scaleImage(rm.getImage("neutral/lb-shed")));

        scaledImages.put("gold", scaleImage(rm.getImage("neutral/gold")));

        Image scaled = new ImageIcon(rm.getImage("neutral/dragon").getScaledInstance(42, 42, Image.SCALE_SMOOTH)).getImage();
        scaledImages.put("dragon", scaled);

        scaledImages.put("fairy", scaleImage(rm.getImage("neutral/fairy")));

        scaledImages.put("mage", scaleImage(rm.getImage("neutral/mage")));
        scaledImages.put("witch", scaleImage(rm.getImage("neutral/witch")));
    }
}
