package com.jcloisterzone.ui.panel;

import static com.jcloisterzone.ui.I18nUtils._tr;

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

import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.component.MultiLineLabel;
import com.jcloisterzone.ui.gtk.ThemedJLabel;
import com.jcloisterzone.ui.gtk.ThemedJPanel;
import com.jcloisterzone.ui.view.ConnectP2PView;
import com.jcloisterzone.ui.view.ConnectPlayOnlineView;

import net.miginfocom.swing.MigLayout;

public class StartPanel extends ThemedJPanel {

    static Font FONT_LARGE_BUTTON = new Font(null, Font.PLAIN, 25);

    private HelpPanel helpPanel;

    /**
     * Create the panel.
     */
    public StartPanel(final Client client) {
        if (!client.getTheme().isDark()) { //HACK
            setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        }
        setLayout(new MigLayout("", "[center,grow]20[center,grow]", "[]20[]10[]"));


        JLabel lblNewLabel = new JLabel();
        if (client.getTheme().isDark()) {
            lblNewLabel.setIcon(new ImageIcon(StartPanel.class.getResource("/sysimages/jcloisterzone-dark.png")));
        } else {
            lblNewLabel.setIcon(new ImageIcon(StartPanel.class.getResource("/sysimages/jcloisterzone.png")));
        }
        add(lblNewLabel, "span 2, wrap, center");
        helpPanel = new HelpPanel();
        add(helpPanel, "span 2, wrap, grow, gap 30 30");

        JPanel playHostedPanel = new ThemedJPanel();
        if (!client.getTheme().isDark()) { //HACK
            playHostedPanel.setBorder(new TitledBorder(
                UIManager.getBorder("TitledBorder.border"),  "", TitledBorder.LEADING,
                TitledBorder.TOP, null, new Color(0, 0, 0)));
        }

        add(playHostedPanel, "grow 2, width :500:");
        playHostedPanel.setLayout(new MigLayout("", "[grow,center]", "20[40px]20[grow]"));

        playHostedPanel.add(new MultiLineLabel(
          _tr("Create a new game or continue a previously saved one. A game will be hosted on your computer and other players may connect during game set up. " +
            "You can also play only against any number of computer players.")),
            "wrap, grow");


        JPanel btnPanel = new ThemedJPanel();
        btnPanel.setLayout(new MigLayout("", "[]30[]30[]", "[]"));
        playHostedPanel.add(btnPanel, "wrap");

        JButton btn = new JButton(_tr("New game"));
        btnPanel.add(btn, "aligny top");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.createGame();
            }
        });
        btn.setFont(FONT_LARGE_BUTTON);

        btnPanel.add(new ThemedJLabel(_tr("or")));

        btn = new JButton(_tr("Load game"));
        btnPanel.add(btn, "aligny top");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.handleLoad();
            }
        });
        btn.setFont(FONT_LARGE_BUTTON);

        playHostedPanel.add(new MultiLineLabel(
                _tr("You can also connect to remote JCloisterZone application hosting a game. Connect when game is created but not yet started.")),
            "wrap, grow, gaptop 15");

        btn = new JButton(_tr("Connect"));
        playHostedPanel.add(btn, "wrap, alignx center,aligny top");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.mountView(new ConnectP2PView(client));
            }
        });
        btn.setFont(FONT_LARGE_BUTTON);

        JPanel playOnlinePanel = new ThemedJPanel();
        if (!client.getTheme().isDark()) { //HACK
            playOnlinePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        }
        add(playOnlinePanel, "grow, width :250:, wrap");
        playOnlinePanel.setLayout(new MigLayout("", "[grow,center]", "20[40px]20[grow]"));

        playOnlinePanel.add(new MultiLineLabel(
          _tr("Connect to other players and play with them using internet connection and public game server play.jcloisterzone.com.")), "wrap, grow");

        btn = new JButton(_tr("Play online"));
        playOnlinePanel.add(btn, "wrap, alignx center, aligny top");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.mountView(new ConnectPlayOnlineView(client));
            }
        });
        btn.setFont(FONT_LARGE_BUTTON);

    }

    public HelpPanel getHelpPanel() {
        return helpPanel;
    }

}
