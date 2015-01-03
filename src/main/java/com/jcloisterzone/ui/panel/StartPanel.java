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
import com.jcloisterzone.ui.component.MultiLineLabel;
import com.jcloisterzone.ui.view.ConnectP2PView;
import com.jcloisterzone.ui.view.ConnectPlayOnlineView;

public class StartPanel extends JPanel {

    static Font FONT_LARGE_BUTTON = new Font(null, Font.PLAIN, 25);

    private Client client;

    private HelpPanel helpPanel;

    /**
     * Create the panel.
     */
    public StartPanel() {
        setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setLayout(new MigLayout("", "[center,grow]20[center,grow]", "[]20[]10[]"));

        JLabel lblNewLabel = new JLabel();
        lblNewLabel.setIcon(new ImageIcon(StartPanel.class.getResource("/sysimages/jcloisterzone.png")));
        add(lblNewLabel, "span 2, wrap, center");
        helpPanel = new HelpPanel();
        add(helpPanel, "span 2, wrap, grow, gap 30 30");

        JPanel createPanel = new JPanel();
        createPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        add(createPanel, "grow, width :250:");
        createPanel.setLayout(new MigLayout("", "[grow,center]", "20[40px]20[grow]"));

        JButton btn = new JButton(_("New game"));
        createPanel.add(btn, "wrap, alignx center,aligny top");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.createGame();
            }
        });
        btn.setFont(FONT_LARGE_BUTTON);
        createPanel.add(new MultiLineLabel(
            _("Create a new local or network game. You can play against any number of computer players.")),
        "wrap, grow");


        JPanel connectPanel = new JPanel();
        connectPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        add(connectPanel, "grow, width :250:, wrap");
        connectPanel.setLayout(new MigLayout("", "[grow,center]", "20[40px]20[grow]"));

        btn = new JButton(_("Connect"));
        connectPanel.add(btn, "wrap, alignx center,aligny top");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.mountView(new ConnectP2PView(client));
            }
        });
        btn.setFont(FONT_LARGE_BUTTON);
        connectPanel.add(new MultiLineLabel(
            _("Connect to a remote JCloisterZone application with settled new game.")),
        "wrap, grow");


        JPanel loadPanel = new JPanel();
        loadPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        add(loadPanel, "grow, width :250:");
        loadPanel.setLayout(new MigLayout("", "[grow,center]", "20[40px]20[grow]"));

        btn = new JButton(_("Load game"));
        loadPanel.add(btn, "wrap, alignx center,aligny top");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.handleLoad();
            }
        });
        btn.setFont(FONT_LARGE_BUTTON);
        loadPanel.add(new MultiLineLabel(_("Load from a file previously saved game.")), "wrap, grow");

        JPanel playOnlinePanel = new JPanel();
        playOnlinePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        add(playOnlinePanel, "grow, width :250:, wrap");
        playOnlinePanel.setLayout(new MigLayout("", "[grow,center]", "20[40px]20[grow]"));

        btn = new JButton(_("Play online"));
        playOnlinePanel.add(btn, "wrap, alignx center,aligny top");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.mountView(new ConnectPlayOnlineView(client));
            }
        });
        btn.setFont(FONT_LARGE_BUTTON);
        playOnlinePanel.add(new MultiLineLabel("*** BETA! ***\n\n" + _("Can't host game? Play game using internet and public game server play.jcloisterzoe.com")), "wrap, grow");
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
