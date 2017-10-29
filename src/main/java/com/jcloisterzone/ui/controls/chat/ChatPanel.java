package com.jcloisterzone.ui.controls.chat;

import static com.jcloisterzone.ui.I18nUtils._tr;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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

import javax.swing.JFrame;
import javax.swing.JPanel;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.event.ChatEvent;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.component.TextPrompt;
import com.jcloisterzone.ui.component.TextPrompt.Show;
import com.jcloisterzone.wsio.message.PostChatMessage;

import net.miginfocom.swing.MigLayout;

public abstract class ChatPanel extends JPanel implements WindowStateListener {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final int DISPLAY_MESSAGES_INTERVAL = 9000;

    protected final Client client;

    private boolean hidingMode;
    private boolean forceFocus;
    private boolean messageReceivedWhileIconified;
    private JTextField input;
    private JTextPane messagesPane;
    private final Deque<ReceivedChatMessage> formattedMessages = new ArrayDeque<>();
    private Timer repaintTimer;


    public ChatPanel(final Client client) {
        this.client = client;

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
                    client.getConnection().send(createPostChatMessage(msg));
                }
                clean();
            }
        });
        input.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                updateMessaagesVisibility();
            }

            @Override
            public void focusGained(FocusEvent e) {
                updateMessaagesVisibility();
            }
        });

        input.setOpaque(false);
        input.setBackground(client.getTheme().getTransparentInputBg());
        Color textColor = client.getTheme().getTextColor();
        if (textColor != null) {
            input.setForeground(client.getTheme().getTextColor());
            input.setCaretColor(client.getTheme().getTextColor());
        }
        TextPrompt tp = new TextPrompt(_tr("Type to chat"), input);
        tp.setShow(Show.FOCUS_LOST);
        tp.changeStyle(Font.ITALIC);
        tp.changeAlpha(0.4f);

        messagesPane = new JTextPane();
        messagesPane.setEditorKit(new WrapEditorKit());
        messagesPane.setFocusable(false);
        messagesPane.setOpaque(false);

        setBackground(client.getTheme().getTransparentPanelBg());
        setLayout(new MigLayout(""));
        add(messagesPane, "pos 10 n (100%-10) (100%-35)");
        add(input, "pos 10 (100%-35) (100%-10) (100%-10)");
    }

    public void initHidingMode() {
        hidingMode = true;
        repaintTimer = new Timer(DISPLAY_MESSAGES_INTERVAL, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                forceFocus = false;
                repaintTimer.stop();
                updateMessaagesVisibility();
            }
        });
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                repaintTimer.stop();
            }
        });
        if (forceFocus) {
            repaintTimer.start();
        }

        setBackground(new Color(0, 0, 0, 0));
        input.setBackground(client.getTheme().getInputBg());
        updateMessaagesVisibility();
    }

    private void updateMessaagesVisibility() {
        messagesPane.setVisible(!hidingMode || !isFolded());
        if (getParent() == null) {
            repaint();
        } else {
            getParent().repaint();
        }
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(client.getTheme().getTransparentPanelBg());
        if (messagesPane.isVisible()) {
            g2.fillRect(0, 0, getWidth(), getHeight());
        } else {
            g2.fillRect(0, getHeight() - 40, getWidth(), 40);
        }
        super.paint(g);
    }

    abstract protected ReceivedChatMessage createReceivedMessage(ChatEvent ev);
    abstract protected PostChatMessage createPostChatMessage(String msg);

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
        client.getContentPane().repaint(); //need to repain whote grid pane
    }

    @Override
    public void windowStateChanged(WindowEvent e) {
        if (e.getOldState() == JFrame.ICONIFIED && messageReceivedWhileIconified) {
            setForceFocus();
            messageReceivedWhileIconified = false;
        }
    }

    private boolean isFolded() {
        return !forceFocus && !input.hasFocus();
    }

    public JTextField getInput() {
        return input;
    }

    public JTextPane getMessagesPane() {
        return messagesPane;
    }

    private void setForceFocus() {
        if (repaintTimer == null) return;
        if (repaintTimer.isRunning()) {
            repaintTimer.restart();
        } else {
            forceFocus = true;
            updateMessaagesVisibility();
            repaintTimer.start();
        }
    }

    @Subscribe
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

                Color textColor = client.getTheme().getTextColor();
                if (textColor == null) {
                    attrs = null;
                } else {
                    attrs = new SimpleAttributeSet();
                    ColorConstants.setForeground(attrs, client.getTheme().getTextColor());
                }
                doc.insertString(offset, text + "\n", attrs);
                offset += text.length() + 1;
            }
        } catch (BadLocationException e) {
            logger.error(e.getMessage(), e); //should never happen
        }
        messagesPane.setDocument(doc);
        repaint();
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
        @Override
        public ViewFactory getViewFactory() {
            return defaultFactory;
        }

    }

    static class WrapColumnFactory implements ViewFactory {
        @Override
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

        @Override
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
