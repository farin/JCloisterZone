package com.jcloisterzone.ui.controls.action;

import java.awt.Color;
import java.awt.Image;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.FairyNextToAction;
import com.jcloisterzone.action.FairyOnTileAction;
import com.jcloisterzone.action.GoldPieceAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.PrincessAction;
import com.jcloisterzone.action.TowerPieceAction;
import com.jcloisterzone.action.TunnelAction;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.figure.Mayor;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Phantom;
import com.jcloisterzone.figure.Pig;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.Wagon;
import com.jcloisterzone.ui.annotations.LinkedImage;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;
import com.jcloisterzone.ui.resources.ResourceManager;

public class ActionWrapper implements Comparable<ActionWrapper> {

    private final PlayerAction<?> action;

    public ActionWrapper(PlayerAction<?> action) {
        assert action != null;
        this.action = action;
    }

    public PlayerAction<?> getAction() {
        return action;
    }

    public Image getImage(ResourceManager rm, Player player, boolean active) {
        return getImage(rm, player != null && active ? player.getColors().getMeepleColor() : Color.GRAY);
    }

    protected Image getImage(ResourceManager rm, Color color) {
        if (!action.getClass().isAnnotationPresent(LinkedImage.class)) {
            throw new UnsupportedOperationException(
                String.format("Annotate %s with LinkedImage or override getImage()", action.getClass().getSimpleName())
            );
        }
        LinkedImage disp = action.getClass().getAnnotation(LinkedImage.class);
        return rm.getLayeredImage(new LayeredImageDescriptor(disp.value(), color));
    }

    public int getOrderingKey() {
        if (action instanceof MeepleAction) {
            Class<? extends Meeple> meepleType = ((MeepleAction) action).getMeepleType();
            if (meepleType.equals(SmallFollower.class)) return 9;
            if (meepleType.equals(BigFollower.class)) return 10;
            if (meepleType.equals(Barn.class)) return 10;
            if (meepleType.equals(Wagon.class)) return 12;
            if (meepleType.equals(Mayor.class)) return 13;
            if (meepleType.equals(Builder.class)) return 14;
            if (meepleType.equals(Pig.class)) return 15;
            if (meepleType.equals(Phantom.class)) return 16;
            return 19;
        }

        if (action instanceof PrincessAction) return 1;
        if (action instanceof TowerPieceAction) return 20;
        if (action instanceof GoldPieceAction) return 29;
        if (action instanceof FairyNextToAction) return 30;
        if (action instanceof FairyOnTileAction) return 30;
        if (action instanceof TunnelAction) {
            return ((TunnelAction)action).isSecondTunnelPiece() ? 41 : 40;
        }
        return 50;
    }

    @Override
    public int compareTo(ActionWrapper o) {
        return getOrderingKey() - o.getOrderingKey();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '(' + getAction().toString() + ')';
    }

}
