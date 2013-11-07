package com.jcloisterzone.ui.panel;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.MultiLineLabel;

public class StartPanel extends JPanel {

    static Font FONT_LARGE_BUTTON = new Font(null, Font.PLAIN, 25);

    private Client client;

    private HelpPanel helpPanel;

    /**
     * Create the panel.
     */
    public StartPanel() {
        setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setLayout(new MigLayout("", "[center, grow]20[center, grow]20[center, grow]", "[]20[]10[]"));

        JLabel lblNewLabel = new JLabel();
        lblNewLabel.setIcon(new ImageIcon(StartPanel.class.getResource("/sysimages/jcloisterzone.png")));
        add(lblNewLabel, "span 3, wrap, center");
        helpPanel = new HelpPanel();
        add(helpPanel, "span 3, wrap, grow, gap 30 30");

        JPanel createPanel = new JPanel();
        createPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        add(createPanel, "grow, width :250:");
                createPanel.setLayout(new MigLayout("", "[grow,center]", "20[40px]20[grow]"));

                JButton create = new JButton(_("New game"));
                createPanel.add(create, "wrap, alignx center,aligny top");
                create.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        client.createGame();
                    }
                });
                create.setFont(FONT_LARGE_BUTTON);
                createPanel.add(new MultiLineLabel(
                    _("Create a new local or network game. You can play against any number of computer players.")),
                "wrap, grow");


        JPanel connectPanel = new JPanel();
        connectPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        add(connectPanel, "grow, width :250:");
                connectPanel.setLayout(new MigLayout("", "[grow,center]", "20[40px]20[grow]"));

                JButton connect = new JButton(_("Connect"));
                connectPanel.add(connect, "wrap, alignx center,aligny top");
                connect.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        client.showConnectGamePanel();
                    }
                });
                connect.setFont(FONT_LARGE_BUTTON);
                connectPanel.add(new MultiLineLabel(
                    _("Connect to a remote JCloisterZone application with settled new game.")),
                "wrap, grow");


        JPanel loadPanel = new JPanel();
        loadPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        add(loadPanel, "grow, width :250:");
                loadPanel.setLayout(new MigLayout("", "[grow,center]", "20[40px]20[grow]"));

                JButton load = new JButton(_("Load game"));
                loadPanel.add(load, "wrap, alignx center,aligny top");
                load.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        client.handleLoad();
                    }
                });
                load.setFont(FONT_LARGE_BUTTON);
                loadPanel.add(new MultiLineLabel(
                    _("Load from a file previously saved game.")),
                "wrap, grow");
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public HelpPanel getHelpPanel() {
        return helpPanel;
    }

}
