package com.jcloisterzone.ui.panel;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import net.miginfocom.swing.MigLayout;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.jcloisterzone.wsio.server.RemoteClient;

public class ConnectedClientsPanel extends JPanel {

	private static Font FONT_TITLE = new Font(null, Font.BOLD, 20);

	private JTextPane connectedClients;

	public ConnectedClientsPanel(String titleText) {
		setLayout(new MigLayout("ins 0", "[grow]", "[][grow]"));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setBackground(Color.WHITE);

		JLabel title = new JLabel(titleText);
		title.setFont(FONT_TITLE);
		add(title, "wrap");
		//add(new JLabel(_("Connected clients")+":"), "wrap");
		connectedClients = new JTextPane();
		connectedClients.setToolTipText(_("Connected clients"));
		connectedClients.setEditable(false);
		add(connectedClients, "wrap, align 0 0");
	}

	public void updateClients(RemoteClient[] clients) {
		connectedClients.setText(Joiner.on("\n").join(
            Collections2.transform(Arrays.asList(clients), new Function<RemoteClient, String>() {
                @Override
                public String apply(RemoteClient input) {
                    return input.getName();
                }
        })));
	}

}
