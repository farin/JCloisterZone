package com.jcloisterzone.ui.grid.actionpanel;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.gtk.ThemedJLabel;
import com.jcloisterzone.wsio.message.CornCircleRemoveOrDeployMessage;
import com.jcloisterzone.wsio.message.CornCircleRemoveOrDeployMessage.CornCircleOption;

import net.miginfocom.swing.MigLayout;



public class CornCirclesPanel extends ActionInteractionPanel<PlayerAction<?>> {

    public static Font FONT_HEADER = new Font(null, Font.BOLD, 18);

    private JButton deploymentOption, removalOption;

    public CornCirclesPanel(Client client, GameController gc) {
        super(client, gc);
        setOpaque(true);
        setBackground(gc.getClient().getTheme().getTransparentPanelBg());
        setLayout(new MigLayout("ins 10 20 10 20", "[grow]", ""));

        JLabel label;

        label = new ThemedJLabel(_("Corn circle"));
        label.setFont(FONT_HEADER);
        label.setForeground(gc.getClient().getTheme().getHeaderFontColor());
        add(label, "wrap, gapbottom 10");

        label = new ThemedJLabel(_("Each player…"));
        add(label, "wrap, gapbottom 5");

        boolean isActive = gc.getGame().getState().getActivePlayer().isLocalHuman();

        deploymentOption = new JButton();
        deploymentOption.setText(_("may deploy additional follower"));
        deploymentOption.setEnabled(isActive);
        deploymentOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gc.getConnection().send(
                    new CornCircleRemoveOrDeployMessage(CornCircleOption.DEPLOY)
                );
            }
        });
        add(deploymentOption, "wrap, growx, h 40, gapbottom 5");

        removalOption = new JButton();
        removalOption.setText(_("must remove follower"));
        removalOption.setEnabled(isActive);
        removalOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gc.getConnection().send(
                    new CornCircleRemoveOrDeployMessage(CornCircleOption.REMOVE)
                );
            }
        });
        add(removalOption, "wrap, growx, h 40, gapbottom 5");

        Tile tile = gc.getGame().getState().getLastPlaced().getTile();
        String feature = Feature.getLocalizedNamefor(tile.getCornCircle());
        label = new ThemedJLabel(_("on/from a {0}.", feature.toLowerCase()));
        add(label, "wrap");
    }
}
