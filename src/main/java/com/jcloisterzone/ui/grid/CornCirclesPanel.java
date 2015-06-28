package com.jcloisterzone.ui.grid;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.jcloisterzone.feature.TileFeature;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.controls.ControlPanel;

import static com.jcloisterzone.ui.I18nUtils._;

@InteractionPanel
public class CornCirclesPanel extends JPanel {

    public static Font FONT_HEADER = new Font(null, Font.BOLD, 18);

    private JButton deploymentOption, removalOption;

    public CornCirclesPanel(final GameController gc) {
        setOpaque(true);
        setBackground(ControlPanel.PANEL_BG_COLOR);
        setLayout(new MigLayout("ins 10 20 10 20", "[grow]", ""));

        JLabel label;

        label = new JLabel(_("Corn circle"));
        label.setFont(FONT_HEADER);
        label.setForeground(ControlPanel.HEADER_FONT_COLOR);
        add(label, "wrap, gapbottom 10");

        label = new JLabel(_("Each player…"));
        add(label, "wrap, gapbottom 5");

        boolean isActive = gc.getGame().getActivePlayer().isLocalHuman();

        deploymentOption = new JButton();
        deploymentOption.setText(_("may deploy additional follower"));
        deploymentOption.setEnabled(isActive);
        deploymentOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gc.getRmiProxy().cornCiclesRemoveOrDeploy(false);
            }
        });
        add(deploymentOption, "wrap, growx, h 40, gapbottom 5");

        removalOption = new JButton();
        removalOption.setText(_("must remove follower"));
        removalOption.setEnabled(isActive);
        removalOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gc.getRmiProxy().cornCiclesRemoveOrDeploy(true);
            }
        });
        add(removalOption, "wrap, growx, h 40, gapbottom 5");

        String feature = TileFeature.getLocalizedNamefor (gc.getGame().getCurrentTile().getCornCircle());
        label = new JLabel(_("on/from a {0}.", feature.toLowerCase()));
        add(label, "wrap");
    }
}
