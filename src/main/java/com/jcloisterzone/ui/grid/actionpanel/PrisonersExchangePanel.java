package com.jcloisterzone.ui.grid.actionpanel;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import com.jcloisterzone.action.SelectPrisonerToExchangeAction;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.component.MultiLineLabel;
import com.jcloisterzone.ui.gtk.ThemedJLabel;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;
import com.jcloisterzone.ui.resources.ResourceManager;
import com.jcloisterzone.wsio.message.ExchangeFollowerChoiceMessage;

import net.miginfocom.swing.MigLayout;

public class PrisonersExchangePanel extends ActionInteractionPanel<SelectPrisonerToExchangeAction> {

    public PrisonersExchangePanel(Client client, GameController gc) {
        super(client, gc);
    }

    public void setGameState(GameState state) {
        super.setGameState(state);

        SelectPrisonerToExchangeAction action = getAction();
        setOpaque(true);
        setBackground(gc.getClient().getTheme().getTransparentPanelBg());
        setLayout(new MigLayout("ins 10 20 10 20", "[grow]", ""));

        JLabel label;

        label = new ThemedJLabel(_("Prisoners exchange"));
        label.setFont(CornCirclesPanel.FONT_HEADER);
        label.setForeground(gc.getClient().getTheme().getHeaderFontColor());
        add(label, "wrap, gapbottom 10");

        MultiLineLabel mll = new MultiLineLabel(_("The captured prisoner is going to be"
            + " immediately exchanged. You can select which follower you want to exchange for."));
        add(mll, "wrap, growx, gapbottom 5");

        ResourceManager rm = gc.getGameView().getClient().getResourceManager();

        boolean isActive = state.getActivePlayer().isLocalHuman();

        //HoverMouseListener hover = new HoverMouseListener();

        action.getOptions().forEach(f -> {
            Color color = f.getPlayer().getColors().getMeepleColor();
            LayeredImageDescriptor lid = new LayeredImageDescriptor(f.getClass(), color);
            Image image = rm.getLayeredImage(lid);
            image = image.getScaledInstance(60, 60, Image.SCALE_SMOOTH);

            JButton btn = new JButton();
            btn.setIcon(new ImageIcon(image));
//            btn.setOpaque(true);
//            btn.addMouseListener(hover);
            btn.setEnabled(isActive);
            btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ((JButton)e.getSource()).setEnabled(false);
                    gc.getConnection().send(
                        new ExchangeFollowerChoiceMessage(gc.getGameId(), f.getId())
                    );
                }
            });
            add(btn, "wrap, growx, h 60, gapbottom 5");
        });
    }
}
