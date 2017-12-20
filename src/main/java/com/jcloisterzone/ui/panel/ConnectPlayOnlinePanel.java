package com.jcloisterzone.ui.panel;

import static com.jcloisterzone.ui.I18nUtils._tr;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.ConnectException;
import java.nio.channels.UnresolvedAddressException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.config.Config;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.gtk.ThemedJLabel;
import com.jcloisterzone.ui.gtk.ThemedJPanel;
import com.jcloisterzone.ui.view.StartView;

import net.miginfocom.swing.MigLayout;


public class ConnectPlayOnlinePanel extends ThemedJPanel {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Client client;

    private JTextField nickField;
    private JButton btnConnect, btnBack;
    private JLabel message;

    /**
     * Create the panel.
     */
    public ConnectPlayOnlinePanel(Client client) {
        this.client = client;
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnConnect.setEnabled(false); //TODO change to Interrupt button
                message.setForeground(Color.BLACK);
                message.setText(_tr("Connecting") + "...");
                saveClientName();
                connect();
            }
        };

        if (!client.getTheme().isDark()) {
            setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        }

        setLayout(new MigLayout("", "[80.00][][grow]", "[][][][][]"));

        JLabel helpLabel = new ThemedJLabel("Enter your nickname");
        add(helpLabel, "cell 0 0,spanx 3");

        JLabel hostLabel = new ThemedJLabel(_tr("Nickname"));
        add(hostLabel, "cell 0 1,alignx left,aligny top, gaptop 10");


        nickField = new JTextField();
        nickField.addActionListener(actionListener);
        add(nickField, "cell 1 1,spanx 2,growx, width 250::");
        nickField .setColumns(20);
        nickField.setText(getDefaultNick());

        btnConnect = new JButton(_tr("Connect"));
        btnConnect.addActionListener(actionListener);
        add(btnConnect, "cell 1 2");

        btnBack = new JButton(_tr("Back"));
        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConnectPlayOnlinePanel.this.client.mountView(new StartView(ConnectPlayOnlinePanel.this.client));
            }
        });
        add(btnBack, "cell 2 2");

        message = new ThemedJLabel("");
        message.setForeground(Color.BLACK);
        add(message, "cell 1 3,spanx 2, height 20");
    }

    private String getDefaultNick() {
        //for development is useful to change nick with system property
        if (System.getProperty("nick") != null) {
            return System.getProperty("nick");
        }
        String name = client.getConfig().getClient_name();
        name = name == null ? "" : name.trim();
        if (name.equals("")) name = System.getProperty("user.name");
        return name;

    }

    private void saveClientName() {
        if (System.getProperty("nick") != null) return;
        String nick = nickField.getText().trim();
        if (nick.equals(System.getProperty("user.name"))) return;
        Config cfg = client.getConfig();
        cfg.setClient_name(nick);
        client.saveConfig();
    }

    public void onWebsocketError(Exception ex) {
        message.setForeground(Color.RED);
        btnConnect.setEnabled(true);

        if (ex instanceof UnresolvedAddressException) {
            message.setText( _tr("Connection failed. Unknown host."));
        } else if (ex instanceof ConnectException && ex.getMessage().contains("Connection refused")) {
            message.setText( _tr("Connection refused."));
        } else {
            message.setText( _tr("Connection failed.") + " (" + ex.getMessage() + ")");
            logger.info(ex.getMessage(), ex);
        }
    }

    private void connect() {
        try {
            String nick = nickField.getText().trim();
            client.connectPlayOnline(nick);
            return;
        } catch (NumberFormatException nfe) {
            message.setText( _tr("Invalid port number."));
        }

        message.setForeground(Color.RED);
        btnConnect.setEnabled(true);
    }
}
