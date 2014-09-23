package com.jcloisterzone.ui.panel;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.ConnectException;
import java.nio.channels.UnresolvedAddressException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.config.Config;
import com.jcloisterzone.ui.Client;


public class ConnectPlayOnlinePanel extends JPanel implements ConnectPanel {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Client client;

    private JTextField nickField;
    private JButton btnConnect;
    private JLabel message;

    /**
     * Create the panel.
     */
    public ConnectPlayOnlinePanel(Client client) {
        this.client = client;
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btnConnect.setEnabled(false); //TODO change to Interrupt button
                message.setForeground(Color.BLACK);
                message.setText(_("Connecting") + "...");
                saveClientName();
                connect();
            }
        };

        setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));

        setLayout(new MigLayout("", "[80.00][grow]", "[][][][][]"));

        JLabel helpLabel = new JLabel("Enter your nickname");
        add(helpLabel, "cell 0 0 2 1");

        JLabel hostLabel = new JLabel(_("Nickname"));
        add(hostLabel, "cell 0 1,alignx left,aligny top, gaptop 10");


        nickField = new JTextField();
        nickField.addActionListener(actionListener);
        add(nickField, "cell 1 1,growx, width 250::");
        nickField .setColumns(20);
        nickField.setText(getDefaultNick());

        btnConnect = new JButton(_("Connect"));
        btnConnect.addActionListener(actionListener);
        add(btnConnect, "cell 1 2");

        message = new JLabel("");
        message.setForeground(Color.BLACK);
        add(message, "cell 1 3, height 20");
    }

    private String getDefaultNick() {
        String name = client.getConfig().getClient_name();
        name = name == null ? "" : name.trim();
        if (name.equals("")) name = System.getProperty("user.name");
        return name;

    }

    private void saveClientName() {
        Config cfg = client.getConfig();
        String nick = nickField.getText().trim();
        cfg.setClient_name(nick);
        client.saveConfig();
    }

    public void onWebsocketError(Exception ex) {
        message.setForeground(Color.RED);
        btnConnect.setEnabled(true);
        if (ex instanceof UnresolvedAddressException) {
            message.setText( _("Connection failed. Unknown host."));
        } else if (ex instanceof ConnectException && "Connection refused: connect".equals(ex.getMessage())) {
            message.setText( _("Connection refused."));
        } else {
            message.setText( _("Connection failed.") + " (" + ex.getMessage() + ")");
            logger.warn(ex.getMessage(), ex);
        }

    }

    private void connect() {
        try {
            String nick = nickField.getText().trim();
            client.connectPlayOnline(nick);
            return;
        } catch (NumberFormatException nfe) {
            message.setText( _("Invalid port number."));
        }

        message.setForeground(Color.RED);
        btnConnect.setEnabled(true);
    }
}
