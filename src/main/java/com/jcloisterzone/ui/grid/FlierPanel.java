package com.jcloisterzone.ui.grid;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.controls.ControlPanel;
import com.jcloisterzone.ui.controls.FakeComponent;

public class FlierPanel extends FakeComponent {

    private static Font FONT_HEADER = new Font(null, Font.BOLD, 18);

    private JLabel header;
    private JButton rollButton;
    private JLabel rollResult;

    public FlierPanel(Client client) {
        super(client);
    }

    @Override
    public void registerSwingComponents(JComponent parent) {
        header = new JLabel(_("Place follower as a flier"));

        parent.add(header);

        rollButton = new JButton();
        rollButton.setText(_("Roll a dice"));
        rollButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.getServer().rollFlierDice();
            }
        });

        parent.add(rollButton);

        rollResult = new JLabel();
        rollResult.setVisible(false);
        parent.add(rollResult);
    }

    @Override
    public void layoutSwingComponents(JComponent parent) {
        int panelX = client.getGridPanel().getWidth()-ControlPanel.PANEL_WIDTH-getWidth()-60,
            left = panelX + 20;

        header.setBounds(left, 34, ControlPanel.PANEL_WIDTH-10, 30);
        rollButton.setBounds(left, 64, ControlPanel.PANEL_WIDTH-10, 30);
        rollResult.setBounds(left, 64, ControlPanel.PANEL_WIDTH-10, 30);
    }

    public void setFlierDistance(int distance) {
        rollButton.setVisible(false);
        rollResult.setText(_("distance")+" "+distance);
        rollResult.setVisible(true);
    }

    @Override
    public void destroySwingComponents(JComponent parent) {
        parent.remove(header);
        parent.remove(rollButton);
        parent.remove(rollResult);
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
        g2.drawString(_("The Flier"), 20, 24);
    }

}
