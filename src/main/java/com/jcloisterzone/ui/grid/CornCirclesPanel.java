package com.jcloisterzone.ui.grid;

import static com.jcloisterzone.ui.I18nUtils._;

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

public class CornCirclesPanel extends JPanel {

    private static Font FONT_HEADER = new Font(null, Font.BOLD, 18);

    private JButton deploymentOption, removalOption;

    public CornCirclesPanel(final GameController gc) {
    	setOpaque(true);
        setBackground(ControlPanel.PANEL_BG_COLOR);
        setLayout(new MigLayout("", "[grow]", ""));

        JLabel label;

        label = new JLabel(_("Corn circle"));
        label.setFont(FONT_HEADER);
        label.setForeground(ControlPanel.HEADER_FONT_COLOR);
        add(label, "wrap, gaptop 5, gapbottom 15");

        label = new JLabel(_("Each player…"));
        add(label, "wrap");

        deploymentOption = new JButton();
        //deploymentOption.setFont(FONT_BUTTON);
        deploymentOption.setText(_("may deploy additional follower"));
        deploymentOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gc.getRmiProxy().cornCiclesRemoveOrDeploy(false);
                GridPanel gridPanel = gc.getGameView().getGridPanel();
                gridPanel.remove(CornCirclesPanel.this);
                gridPanel.revalidate();
            }
        });
        add(deploymentOption, "wrap, growx, h 40");

        removalOption = new JButton();
        removalOption.setText(_("must remove follower"));
        removalOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	gc.getRmiProxy().cornCiclesRemoveOrDeploy(true);
            	GridPanel gridPanel = gc.getGameView().getGridPanel();
                gridPanel.remove(CornCirclesPanel.this);
                gridPanel.revalidate();
            }
        });
        add(removalOption, "wrap, growx, h 40");

        String feature = TileFeature.getLocalizedNamefor (gc.getGame().getCurrentTile().getCornCircle());
        label = new JLabel(_("on/from a {0}.", feature.toLowerCase()));
        add(label, "wrap");
    }
}
