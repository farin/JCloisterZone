package com.jcloisterzone.ui.controls;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
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
import com.jcloisterzone.event.ChatEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.phase.CreateGamePhase;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.component.TextPrompt;
import com.jcloisterzone.ui.component.TextPrompt.Show;
import com.jcloisterzone.wsio.message.PostChatMessage;

public class ChatPanel extends FakeComponent implements WindowStateListener {

    public static final int CHAT_WIDTH = 250;
    public static final int DISPLAY_MESSAGES_INTERVAL = 9000;

    private JComponent parent; //TODO move to FakeComponent? hack to re-layout from inside class
    private final Game game;

    private boolean forceFocus;
    private boolean messageReceivedWhileIconified;
    private JTextField input;
    private JTextPane messagesPane;
    private final Deque<ReceivedChatMessage> formattedMessages = new ArrayDeque<>();
    private final Timer repaintTimer;


    public ChatPanel(Client client, Game game) {
        super(client);
        this.game = game;
        repaintTimer = new Timer(DISPLAY_MESSAGES_INTERVAL, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                forceFocus = false;
                parent.repaint();
                repaintTimer.stop();
            }
        });
    }


    public void setParent(JComponent parent) {
        this.parent = parent;
    }

    public void activateChat() {
        input.setFocusable(true);
        input.requestFocusInWindow();
        //prevent key event propagate to input - but still not 100%
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                input.requestFocusInWindow();
            }
        });
    }

    private void clean() {
        input.setText("");
        input.setFocusable(false);
        client.requestFocusInWindow();
    }



    @Override
    public void registerSwingComponents(JComponent parent) {
        this.parent = parent;
        input = new JTextField();
        //prevent unintended focus (by window activate etc. - allow focus just on direct click)
        input.setFocusable(false);
        input.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                activateChat();
            }
        });
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
                    forceFocus = true; //prevent panel flashing
                    client.getConnection().send(new PostChatMessage(game.getGameId(), msg));
                    //client.getServer().chatMessage(getSendingPlayer().getIndex(), msg);
                }
                clean();
            }
        });
        input.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                if (ChatPanel.this.parent != null) {
                    ChatPanel.this.parent.repaint();
                }
            }

            @Override
            public void focusGained(FocusEvent e) {
                messagesPane.setVisible(true);
                ChatPanel.this.parent.repaint();
            }
        });

        input.setOpaque(false);
        input.setBackground(new Color(255, 255, 255, 8));
        TextPrompt tp = new TextPrompt(_("Type to chat"), input);
        tp.setShow(Show.FOCUS_LOST);
        tp.changeStyle(Font.ITALIC);
        tp.changeAlpha(0.4f);

        messagesPane = new JTextPane();
        messagesPane.setEditorKit(new WrapEditorKit());
        messagesPane.setFocusable(false);
        messagesPane.setOpaque(false);

        parent.add(input);
        parent.add(messagesPane);

        client.addWindowStateListener(this);
    }

    @Override
    public void destroySwingComponents(JComponent parent) {
        parent.remove(input);
        client.removeWindowStateListener(this);
    }

    @Override
    public void windowStateChanged(WindowEvent e) {
        if (e.getOldState() == JFrame.ICONIFIED && messageReceivedWhileIconified) {
            setForceFocus();
            messageReceivedWhileIconified = false;
        }
    }

    @Override
    public void layoutSwingComponents(JComponent parent) {
        input.setBounds(10, parent.getHeight() - 35, CHAT_WIDTH-20, 25);

        messagesPane.setSize(CHAT_WIDTH-20, Short.MAX_VALUE);
        int height = messagesPane.getPreferredSize().height;
        messagesPane.setBounds(10, parent.getHeight() - 30 - height, CHAT_WIDTH-20, height);
    }

    private boolean isFolded() {
        return !forceFocus && !input.hasFocus();
    }

    @Override
    public void paintComponent(Graphics2D g2) {
        int h = parent.getHeight();

        g2.setColor(ControlPanel.PANEL_BG_COLOR);
        if (isFolded()) {
            if (messagesPane.isVisible()) messagesPane.setVisible(false);
            g2.fillRect(0, parent.getHeight() - 45, CHAT_WIDTH, 45);
        } else {
            if (!messagesPane.isVisible()) messagesPane.setVisible(true);
            g2.fillRect(0, 0, CHAT_WIDTH, h);
        }
    }

    public JTextField getInput() {
        return input;
    }

    public JTextPane getMessagesPane() {
        return messagesPane;
    }

    private void setForceFocus() {
        if (repaintTimer.isRunning()) {
            repaintTimer.restart();
        } else {
            forceFocus = true;
            parent.repaint();
            repaintTimer.start();
        }
    }

    /**
     * More then one client can play from one seat. Find local player, prefer
     * active, than human player, latest is AI.
     *
     * @return
     */
    private ReceivedChatMessage createReceivedMessage(ChatEvent ev) {
        String nick = ev.getRemoteClient().getName();
        Color color = Color.DARK_GRAY;

        if (game.isStarted()) {
            Player selected = null, active = game.getActivePlayer();
            for (Player player : game.getAllPlayers()) {
                if (player.getSlot().getClientId().equals(ev.getRemoteClient().getClientId())) {
                    if (selected == null) {
                        selected = player;
                    } else {
                        if (selected.getSlot().getAiClassName() != null
                                && player.getSlot().getAiClassName() == null) {
                            // prefer real user for remote inactive client with
                            // more players
                            selected = player;
                        }
                    }
                    if (player.equals(active)) {
                        selected = player;
                        break;
                    }
                }
            }
            if (selected != null) {
                nick = selected.getNick();
                color = selected.getColors().getFontColor();
            }
        } else {
             PlayerSlot[] slots = ((CreateGamePhase) game.getPhase()).getPlayerSlots();
             for (PlayerSlot slot: slots) {
                 if (ev.getRemoteClient().getClientId().equals(slot.getClientId()) && !slot.getNickname().equals("")) {
                     nick = slot.getNickname();
                     color = slot.getColors().getFontColor();
                     break;
                 }
             }
        }
        return new ReceivedChatMessage(ev, nick, color);
    }

    public void displayChatMessage(ChatEvent ev) {
        ReceivedChatMessage fm = createReceivedMessage(ev);
        formattedMessages.addLast(fm);

        if (client.getState() == JFrame.ICONIFIED) {
            messageReceivedWhileIconified = true;
        } else {
            setForceFocus();
        }

        DefaultStyledDocument doc = new DefaultStyledDocument();
        int offset = 0;
        try {
            for (ReceivedChatMessage msg : formattedMessages) {
                SimpleAttributeSet attrs = new SimpleAttributeSet();
                ColorConstants.setForeground(attrs, msg.color);

                String nick = msg.nickname;
                String text = msg.ev.getText();
                doc.insertString(offset, nick + ": ", attrs);
                offset += nick.length() + 2;
                doc.insertString(offset, text + "\n", null);
                offset += text.length() + 1;
            }
        } catch (BadLocationException e) {
            e.printStackTrace(); //should never happen
        }
        messagesPane.setDocument(doc);
        layoutSwingComponents(parent);
    }


    static class ReceivedChatMessage {
        final ChatEvent ev;
        final String nickname;
        final Color color;
        final long time;

        public ReceivedChatMessage(ChatEvent ev, String nickname, Color color) {
            this.ev = ev;
            this.color = color;
            this.nickname = nickname;
            this.time = System.currentTimeMillis();
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
