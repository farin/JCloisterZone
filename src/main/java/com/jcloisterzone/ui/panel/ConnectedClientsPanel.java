package com.jcloisterzone.ui.panel;

import static com.jcloisterzone.ui.I18nUtils._tr;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants.ColorConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.ui.Client;
import com.jcloisterzone.wsio.message.ClientUpdateMessage.ClientState;
import com.jcloisterzone.wsio.server.RemoteClient;

import net.miginfocom.swing.MigLayout;

public class ConnectedClientsPanel extends JPanel {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private static Font FONT_TITLE = new Font(null, Font.BOLD, 20);

    private JTextPane connectedClients;

    public ConnectedClientsPanel(Client client, String titleText) {
        Color bg = client.getTheme().getPanelBg();
        if (bg == null) bg = Color.WHITE;

        setLayout(new MigLayout("ins 0, fillx", "[grow]", "[][grow]"));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setBackground(bg);

        JLabel title = new JLabel(titleText);
        title.setFont(FONT_TITLE);
        add(title, "wrap");

        connectedClients = new JTextPane();
        connectedClients.setToolTipText(_tr("Connected clients"));
        connectedClients.setEditable(false);
        connectedClients.setBackground(bg);
        connectedClients.setForeground(client.getTheme().getTextColor());

        add(connectedClients, "wrap, grow, align 0 0");
    }

    public void updateClients(List<RemoteClient> clients) {
        DefaultStyledDocument doc = new DefaultStyledDocument();

        List<RemoteClient> inGameClients = new ArrayList<>();
        int offs = 0;

        try {
            for (RemoteClient rc : clients) {
                if (ClientState.ACTIVE.equals(rc.getState())) {
                    SimpleAttributeSet attrs = new SimpleAttributeSet();
                    String text = rc.getName();
                    doc.insertString(offs, text+"\n", attrs);
                    offs += text.length() + 1;
                }
                if (ClientState.IN_GAME.equals(rc.getState  ())) {
                    inGameClients.add(rc);
                }
            }
            if (!inGameClients.isEmpty()) {
                doc.insertString(offs, "\n", new SimpleAttributeSet());
                offs += 1;
                for (RemoteClient rc : inGameClients) {
                    SimpleAttributeSet attrs = new SimpleAttributeSet();
                    ColorConstants.setForeground(attrs, Color.LIGHT_GRAY);
                    String text = rc.getName();
                    doc.insertString(offs, text+"\n", attrs);
                    offs += text.length() + 1;
                }
            }
        } catch (BadLocationException e) {
            logger.error(e.getMessage(), e); //should never happen
        }

        connectedClients.setDocument(doc);
        connectedClients.repaint();
    }

}
