package com.jcloisterzone.ui.grid;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import com.jcloisterzone.feature.TileFeature;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.controls.ControlPanel;
import com.jcloisterzone.ui.controls.FakeComponent;

public class CornCirclesPanel extends FakeComponent {

    private static Font FONT_HEADER = new Font(null, Font.BOLD, 18);

    private JLabel header, footer;
    private JButton deploymentOption, removalOption;

    public CornCirclesPanel(Client client) {
        super(client);
    }

    @Override
    public void registerSwingComponents(JComponent parent) {


        header = new JLabel(_("Each player…"));
        parent.add(header);

        deploymentOption = new JButton();
        //deploymentOption.setFont(FONT_BUTTON);
        deploymentOption.setText(_("may deploy additional follower"));
        deploymentOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.getServer().cornCiclesRemoveOrDeploy(false);
                client.getGridPanel().setSecondPanel(null);
            }
        });
        parent.add(deploymentOption);

        removalOption = new JButton();
        removalOption.setText(_("must remove follower"));
        removalOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.getServer().cornCiclesRemoveOrDeploy(true);
                client.getGridPanel().setSecondPanel(null);
            }
        });


        parent.add(removalOption);

        String feature = TileFeature.getLocalizedNamefor (client.getGame().getCurrentTile().getCornCircle());
        footer = new JLabel(_("on/from a {0}.", feature.toLowerCase()));
        parent.add(footer);
    }

    @Override
    public void layoutSwingComponents(JComponent parent) {
        int panelX = client.getGridPanel().getWidth()-ControlPanel.PANEL_WIDTH-getWidth()-60,
            left = panelX + 20;

        header.setBounds(left, 34, ControlPanel.PANEL_WIDTH-10, 30);
        deploymentOption.setBounds(left, 64, ControlPanel.PANEL_WIDTH-10, 30);
        removalOption.setBounds(left, 104, ControlPanel.PANEL_WIDTH-10, 30);
        footer.setBounds(left, 134, ControlPanel.PANEL_WIDTH-10, 30);
    }

    @Override
    public void destroySwingComponents(JComponent parent) {
        parent.remove(deploymentOption);
        parent.remove(removalOption);
        parent.remove(header);
        parent.remove(footer);
    }

    @Override
    public void paintComponent(Graphics2D g2) {
        super.paintComponent(g2);

        GridPanel gp = client.getGridPanel();
        int h = gp.getHeight();

        g2.setColor(ControlPanel.PANEL_BG_COLOR);
        g2.fillRect(0 , 0, getWidth(), h);

        g2.setColor(ControlPanel.HEADER_FONT_COLOR);
        g2.setFont(FONT_HEADER);
        g2.drawString(_("Corn circle"), 20, 24);
    }

}
