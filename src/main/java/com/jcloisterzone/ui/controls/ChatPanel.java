package com.jcloisterzone.ui.controls;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleConstants.ColorConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import com.jcloisterzone.Player;
import com.jcloisterzone.rmi.mina.ClientStub;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.grid.GridPanel;

public class ChatPanel extends FakeComponent {

    public static final int CHAT_WIDTH = 250;

    private JComponent parent; //move to FakeComponent? hack to realoyot from inside class

    //private boolean hasFocus = true;
    private JTextField input;
    private JTextPane messagesPane;
    private final Deque<ChatMessage> formattedMessages = new ArrayDeque<>();

    public ChatPanel(Client client) {
        super(client);
    }

    private void clean() {
        input.setText("");
        client.requestFocus();
    }

    private Player getSendingPlayer() {
        Player result = null, active = client.getGame().getActivePlayer();
        for (Player player : client.getGame().getAllPlayers()) {
            boolean isLocal  = ((ClientStub)Proxy.getInvocationHandler(client.getServer())).isLocalPlayer(player);
            if (isLocal) {
                if (result == null) result = player;
                if (player.equals(active)) return player;
            }
        }
        return result;
    }

    @Override
    public void registerSwingComponents(JComponent parent) {
        this.parent = parent;
        input = new JTextField();
        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    clean();
                }
            }
        });
        input.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String msg = input.getText();
                if (!"".equals(msg)) {
                    client.getServer().chatMessage(getSendingPlayer().getIndex(), msg);
                }
                clean();
            }
        });
        input.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                client.getGridPanel().repaint();
            }

            @Override
            public void focusGained(FocusEvent e) {
                client.getGridPanel().repaint();
            }
        });

        input.setOpaque(false);
        input.setBackground(new Color(255, 255, 255, 8));

        messagesPane = new JTextPane();
        messagesPane.setEditorKit(new WrapEditorKit());
        messagesPane.setFocusable(false);
        messagesPane.setOpaque(false);

        parent.add(input);
        parent.add(messagesPane);
    }

    @Override
    public void destroySwingComponents(JComponent parent) {
        parent.remove(input);
    }

    @Override
    public void layoutSwingComponents(JComponent parent) {
        //    input.setBounds(10, 10, 180, 25);
        //    messagesPane.setBounds(10, 45, 240, parent.getHeight() - 55);
        input.setBounds(10, parent.getHeight() - 35, CHAT_WIDTH-20, 25);

        messagesPane.setSize(CHAT_WIDTH-20, Short.MAX_VALUE);
        //int height = Math.min(messagesPane.getPreferredSize().height, parent.getHeight() - 55);
        int height = messagesPane.getPreferredSize().height;
        messagesPane.setBounds(10, parent.getHeight() - 30 - height, CHAT_WIDTH-20, height);
    }

    @Override
    public void paintComponent(Graphics2D g2) {
        GridPanel gp = client.getGridPanel();
        int h = gp.getHeight();

        g2.setColor(ControlPanel.PANEL_BG_COLOR);

        if (input.hasFocus()) {
            g2.fillRect(0, 0, CHAT_WIDTH, h);
        } else {
            g2.fillRect(0, gp.getHeight() - 45, CHAT_WIDTH, 45);
        }
    }

    public JTextField getInput() {
        return input;
    }

    public void displayChatMessage(Player player, String message) {
        ChatMessage fm = new ChatMessage(player, message);
        formattedMessages.addLast(fm);

        DefaultStyledDocument doc = new DefaultStyledDocument();
        int offset = 0;
        try {
            for (ChatMessage msg : formattedMessages) {
                SimpleAttributeSet attrs = new SimpleAttributeSet();
                ColorConstants.setForeground(attrs, client.getPlayerColor(msg.player));

                doc.insertString(offset, msg.player.getNick() + ": ", attrs);
                offset += msg.player.getNick().length() + 2;
                doc.insertString(offset, msg.message + "\n", null);
                offset += msg.message.length() + 1;
            }
        } catch (BadLocationException e) {
            e.printStackTrace(); //should never happen
        }
        messagesPane.setDocument(doc);
        layoutSwingComponents(parent);
    }

    static class ChatMessage {
        Player player;
        String message;

        public ChatMessage(Player player, String message) {
            this.player = player;
            this.message = message;
        }
    }

    static class WrapEditorKit extends StyledEditorKit {
        ViewFactory defaultFactory=new WrapColumnFactory();
        public ViewFactory getViewFactory() {
            return defaultFactory;
        }

    }

    static class WrapColumnFactory implements ViewFactory {
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new WrapLabelView(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                    return new ParagraphView(elem);
                } else if (kind.equals(AbstractDocument.SectionElementName)) {
                    return new BoxView(elem, View.Y_AXIS);
                } else if (kind.equals(StyleConstants.ComponentElementName)) {
                    return new ComponentView(elem);
                } else if (kind.equals(StyleConstants.IconElementName)) {
                    return new IconView(elem);
                }
            }

            // default to text display
            return new LabelView(elem);
        }
    }

    static class WrapLabelView extends LabelView {
        public WrapLabelView(Element elem) {
            super(elem);
        }

        public float getMinimumSpan(int axis) {
            switch (axis) {
                case View.X_AXIS:
                    return 0;
                case View.Y_AXIS:
                    return super.getMinimumSpan(axis);
                default:
                    throw new IllegalArgumentException("Invalid axis: " + axis);
            }
        }

    }
}
