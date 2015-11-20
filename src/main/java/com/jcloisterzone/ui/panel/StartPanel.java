package com.jcloisterzone.ui.panel;

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
import com.jcloisterzone.ui.theme.Theme;
import com.jcloisterzone.ui.view.ConnectP2PView;
import com.jcloisterzone.ui.view.ConnectPlayOnlineView;

import static com.jcloisterzone.ui.I18nUtils._;

public class StartPanel extends JPanel {

    static Font FONT_LARGE_BUTTON = new Font(null, Font.PLAIN, 25);

    private HelpPanel helpPanel;

    /**
     * Create the panel.
     */
    public StartPanel(final Client client) {
    	Color bgColor = client.getTheme().getPanelBg();
        setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setLayout(new MigLayout("", "[center,grow]20[center,grow]", "[]20[]10[]"));
        setBackground(bgColor);

        JLabel lblNewLabel = new JLabel();
        lblNewLabel.setIcon(new ImageIcon(StartPanel.class.getResource("/sysimages/jcloisterzone.png")));
        add(lblNewLabel, "span 2, wrap, center");
        helpPanel = new HelpPanel();
        helpPanel.setBackground(bgColor);
        add(helpPanel, "span 2, wrap, grow, gap 30 30");

        JPanel playHostedPanel = new JPanel();
        playHostedPanel.setBackground(bgColor);
        playHostedPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),  "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));

        add(playHostedPanel, "grow 2, width :500:");
        playHostedPanel.setBackground(bgColor);
        playHostedPanel.setLayout(new MigLayout("", "[grow,center]", "20[40px]20[grow]"));

        playHostedPanel.add(new MultiLineLabel(
          _("Create a new game or continue a previously saved one. A game will be hosted on your computer and other players may connect during game set up. " +
            "You can also play only against any number of computer players.")),
            "wrap, grow");


        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new MigLayout("", "[]30[]30[]", "[]"));
        btnPanel.setBackground(bgColor);
        playHostedPanel.add(btnPanel, "wrap");

        JButton btn = new JButton(_("New game"));
        btnPanel.add(btn, "aligny top");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.createGame();
            }
        });
        btn.setFont(FONT_LARGE_BUTTON);

        btnPanel.add(new JLabel(_("or")));

        btn = new JButton(_("Load game"));
        btnPanel.add(btn, "aligny top");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.handleLoad();
            }
        });
        btn.setFont(FONT_LARGE_BUTTON);

        playHostedPanel.add(new MultiLineLabel(
                _("You can also connect to remote JCloisterZone application hosting a game. Connect when game is created but not yet started.")),
            "wrap, grow, gaptop 15");

        btn = new JButton(_("Connect"));
        playHostedPanel.add(btn, "wrap, alignx center,aligny top");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.mountView(new ConnectP2PView(client));
            }
        });
        btn.setFont(FONT_LARGE_BUTTON);

        JPanel playOnlinePanel = new JPanel();
        playOnlinePanel.setBackground(bgColor);
        playOnlinePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        add(playOnlinePanel, "grow, width :250:, wrap");
        playOnlinePanel.setLayout(new MigLayout("", "[grow,center]", "20[40px]20[grow]"));

        playOnlinePanel.add(new MultiLineLabel(
          _("Connect to other players and play with them using internet connection and public game server play.jcloisterzone.com.")), "wrap, grow");

        btn = new JButton(_("Play online"));
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
