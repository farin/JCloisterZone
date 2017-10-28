package com.jcloisterzone.ui.panel;

import static com.jcloisterzone.ui.I18nUtils._tr;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.ConnectException;
import java.nio.channels.UnresolvedAddressException;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.config.Config;
import com.jcloisterzone.config.ConfigLoader;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.gtk.ThemedJLabel;
import com.jcloisterzone.ui.gtk.ThemedJPanel;
import com.jcloisterzone.ui.view.StartView;

import net.miginfocom.swing.MigLayout;


public class ConnectGamePanel extends ThemedJPanel {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Client client;

    private JTextField hostField;
    private JTextField portField;
    private JButton btnConnect, btnBack;
    private JLabel message;

    /**
     * Create the panel.
     */
    public ConnectGamePanel(Client client) {
        this.client = client;
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnConnect.setEnabled(false); //TODO change to Interrupt button
                message.setForeground(Color.BLACK);
                message.setText(_tr("Connecting") + "...");
                String port = portField.getText().trim();
                if (port.equals("")) {
                     portField.setText(ConnectGamePanel.this.client.getConfig().getPort() + "");
                }
                //(new AsyncConnect()).start();
                saveHistory();
                connect();
            }
        };

        if (!client.getTheme().isDark()) {
            setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        }

        setLayout(new MigLayout("", "[80.00][][grow]", "[][][][][]"));

        JLabel helpLabel = new ThemedJLabel("Enter remote host address.");
        add(helpLabel, "cell 0 0,spanx 3");

        JLabel hostLabel = new ThemedJLabel(_tr("Host"));
        add(hostLabel, "cell 0 1,alignx left,aligny top, gaptop 10");

        String[] hostPost = getDefaultHostPort();

        hostField = new JTextField();
        hostField.addActionListener(actionListener);
        add(hostField, "cell 1 1,spanx 2,growx, width 250::");
        hostField.setColumns(10);
        hostField.setText(hostPost[0]);

        JLabel portLabel = new ThemedJLabel(_tr("Port"));
        add(portLabel, "cell 0 2,alignx left, gaptop 5");

        portField = new JTextField();
        portField.addActionListener(actionListener);
        add(portField, "cell 1 2,spanx 2,growx, width 250::");
        portField.setColumns(10);
        portField.setText(hostPost[1]);

        btnConnect = new JButton(_tr("Connect"));
        btnConnect.addActionListener(actionListener);
        add(btnConnect, "cell 1 3");

        btnBack = new JButton(_tr("Back"));
        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConnectGamePanel.this.client.mountView(new StartView(ConnectGamePanel.this.client));
            }
        });
        add(btnBack, "cell 2 3");

        message = new ThemedJLabel("");
        message.setForeground(Color.BLACK);
        add(message, "cell 1 4, spanx 2, height 20");
    }

    private String[] getDefaultHostPort() {
        int port = client.getConfig().getPort() == null ? ConfigLoader.DEFAULT_PORT : client.getConfig().getPort();
        List<String> history = client.getConfig().getConnection_history();
        if (history == null || history.isEmpty()) {
            return new String[] {"", port + ""};
        } else {
            String[] hp = history.get(0).split(":");
            if (hp.length > 1) return hp;
            return new String[] {hp[0], port + ""};
        }
    }

    private void saveHistory() {
        Config cfg = client.getConfig();
        String record = hostField.getText().trim();
        String port = portField.getText().trim();
        if (!port.equals(client.getConfig().getPort() + "")) {
            record = record + ":" + port;
        }
        cfg.setConnection_history(Collections.singletonList(record));
        client.saveConfig();
    }

    public void onWebsocketError(Exception ex) {
        message.setForeground(Color.RED);
        btnConnect.setEnabled(true);
        if (ex instanceof UnresolvedAddressException) {
            message.setText( _tr("Connection failed. Unknown host."));
        } else if (ex instanceof ConnectException && "Connection refused: connect".equals(ex.getMessage())) {
            message.setText( _tr("Connection refused."));
        } else {
            message.setText( _tr("Connection failed.") + " (" + ex.getMessage() + ")");
            logger.warn(ex.getMessage(), ex);
        }

    }

    private void connect() {
        try {
            String hostname = hostField.getText().trim();
            String portStr = portField.getText().trim();
            int port = Integer.parseInt(portStr);
            client.connect(hostname, port);
            return;
        } catch (NumberFormatException nfe) {
            message.setText( _tr("Invalid port number."));
        }

        message.setForeground(Color.RED);
        btnConnect.setEnabled(true);
    }
}
