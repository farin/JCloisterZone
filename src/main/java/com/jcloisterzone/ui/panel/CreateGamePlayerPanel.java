package com.jcloisterzone.ui.panel;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.EnumSet;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.ai.DummyAiPlayer;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.PlayerSlot.SlotState;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.gtk.ThemedJLabel;
import com.jcloisterzone.ui.gtk.ThemedJPanel;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;
import com.jcloisterzone.wsio.message.LeaveSlotMessage;
import com.jcloisterzone.wsio.message.TakeSlotMessage;

import net.miginfocom.swing.MigLayout;

public class CreateGamePlayerPanel extends ThemedJPanel {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private static final long serialVersionUID = 1436952221307376517L;

    static Font FONT_PLAYER_TYPE = new Font(null, Font.ITALIC, 11);
    static Font FONT_SERIAL = new Font(null, Font.BOLD, 32);

    private final PlayerSlot slot;
    private final PlayerSlot[] slots;
    private boolean ownSlot = false;

    private final Client client;
    private final GameController gc;
    private boolean mutableSlots;
    private boolean channel;

    private JButton icon;
    private JLabel status;
    private JTextField nickname;

    private NicknameUpdater nicknameUpdater;
    private JLabel serialLabel;

    private NameProvider nameProvider;


    /**
     * Create the panel.
     */
    public CreateGamePlayerPanel(Client client, GameController gc, boolean mutableSlots, PlayerSlot slot, PlayerSlot[] slots) {
        this.slot = slot;
        this.slots = slots;
        this.client = client;
        this.gc = gc;
        this.mutableSlots = mutableSlots;
        this.channel = gc.getChannel() != null;

        setLayout(new MigLayout("", "[][][10px][grow]", "[][]"));

        serialLabel = new ThemedJLabel("");
        serialLabel.setHorizontalAlignment(SwingConstants.CENTER);
        serialLabel.setForeground(new Color(180,180,180));
        serialLabel.setFont(FONT_SERIAL);
        add(serialLabel, "cell 0 0 0 2,width 34!,height 60!");

        icon = new JButton();
        icon.addActionListener(mutableSlots ? new MutableIconActionListener() : new ImmutableIconActionListener());
        add(icon, "cell 1 0 1 2,width 60!,height 60!");

        nickname = new JTextField();
        nickname.setDisabledTextColor(Color.BLACK);
        updateNickname(false);
        add(nickname, "cell 3 0,growx,width :200:,gapy 10");

        status = new ThemedJLabel("");
        status.setFont(FONT_PLAYER_TYPE);
        add(status, "cell 3 1,growx");

        updateSlot();

        if (mutableSlots && !channel) {
            nicknameUpdater = new NicknameUpdater();
            nicknameUpdater.setName("NickUpdater"+slot.getNumber());
            nicknameUpdater.start();
            nickname.addCaretListener(nicknameUpdater);
            nickname.addFocusListener(nicknameUpdater);
        }
    }

    public void disposePanel() {
        if (nicknameUpdater != null) {
            nicknameUpdater.setStopped(true);
            nicknameUpdater = null;
        }
    }


    public void updateSlot() {
        //logger.debug("Updating slot {}", slot);
        if (mutableSlots) {
            updateSlotMutable();
        } else {
            updateSlotImmutable();
        }
        ownSlot = slot.isOwn();
    }

    public PlayerSlot getSlot() {
        return slot;
    }

    private void updateNickname(boolean editable) {
        nickname.setEnabled(editable && !channel);
    }

    private void updateIcon(String iconType, Color color, boolean state) {
        Image imgRes = client.getResourceManager().getLayeredImage(new LayeredImageDescriptor("player-slot/" + iconType, color));
        imgRes = imgRes.getScaledInstance(60, 60, Image.SCALE_SMOOTH);
        ImageIcon img = new ImageIcon(imgRes);
        icon.setIcon(img);
        icon.setDisabledIcon(img);
        icon.setEnabled(state);
    }

    public void updateSlotImmutable() {
        Color color = slot.getColors().getMeepleColor();
        if (!slot.isOccupied()) {
            status.setText(_("Unassigned player"));
            updateIcon("open", color, true);
        } else if (!slot.isAi()) {
            if (slot.isOwn()) {
                status.setText(_("Local player"));
                updateIcon("local", color, true);
            } else {
                status.setText(_("Remote player"));
                updateIcon("remote", color, false);
            }
        } else {
            if (slot.isOwn()) {
                status.setText(_("Computer player"));
                updateIcon("ai", color, false);
            } else {
                status.setText(_("Remote computer player"));
                updateIcon("remote_ai", color, false);
            }
        }
        nickname.setText(slot.getNickname());
    }

    public void updateSlotMutable() {
        Color color = slot.getColors().getMeepleColor();
        if (!slot.isOccupied()) {
            status.setText(_("Open player slot"));
            updateIcon("open", color, true);
            updateNickname(false);
        } else if (!slot.isAi()) {
            if (slot.isOwn()) {
                status.setText(_("Local player"));
                updateIcon("local", color, true);
                updateNickname(true);
            } else {
                status.setText(_("Remote player"));
                updateIcon("remote", color, false);
                updateNickname(false);
            }
        } else {
            if (slot.isOwn()) {
                status.setText(_("Computer player"));
                updateIcon("ai", color, true);
            } else {
                status.setText(_("Remote computer player"));
                updateIcon("remote_ai", color, false);
            }
            updateNickname(false);
        }

        if (!ownSlot || !nickname.isEnabled()) { //probably change by me
            nickname.setText(slot.getNickname());
        }
    }

    public void setSerialText(String text) {
        serialLabel.setText(text);
    }

    class MutableIconActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String nick;
            boolean skipPlayer = false;
            if (channel && !"true".equals(System.getProperty("allowHotSeatOnlineGame"))) {
                for (PlayerSlot other : slots) {
                    if (other == slot) continue;
                    if (other.isOwn() && !other.isAi()) skipPlayer = true;
                }
            }

            if (!slot.isOccupied() && !skipPlayer) {  // open --> player
                if (channel) {
                    nick = gc.getConnection().getNickname();
                } else {
                    nick = nameProvider.reserveName(false, slot.getNumber());
                }
                slot.setNickname(nick);
                nickname.setText(nick);
                slot.setState(SlotState.OWN);
                sendTakeSlotMessage(slot);
            } else if (!slot.isAi()) { //player --> ai
                nameProvider.releaseName(false, slot.getNumber());
                //TODO get out hardcoded AI class
                slot.setAiClassName(DummyAiPlayer.class.getName());
                nick = nameProvider.reserveName(true, slot.getNumber());
                slot.setNickname(nick);
                nickname.setText(nick);
                slot.setState(SlotState.OWN);
                sendTakeSlotMessage(slot);
            } else { //ai --> open
                nameProvider.releaseName(true, slot.getNumber());
                slot.setNickname(null);
                slot.setAiClassName(null);
                slot.setState(SlotState.OPEN);
                sendLeaveSlotMessage(slot);
            }
        }
    }

    class ImmutableIconActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (slot.isOccupied()) {  //player --> open
                slot.setState(SlotState.OPEN);
                sendLeaveSlotMessage(slot);
            } else { // open --> player
                slot.setState(SlotState.OWN);
                sendTakeSlotMessage(slot);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void sendTakeSlotMessage(PlayerSlot slot) {
        TakeSlotMessage msg = new TakeSlotMessage(slot.getNumber(), slot.getNickname());
        msg.setAiClassName(slot.getAiClassName());
        if (slot.getAiClassName() != null) {
            try {
                EnumSet<Expansion> supported = (EnumSet<Expansion>) Class.forName(slot.getAiClassName()).getMethod("supportedExpansions").invoke(null);
                msg.setSupportedExpansions(supported.toArray(new Expansion[supported.size()]));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        gc.getConnection().send(msg);
    }

    private void sendLeaveSlotMessage(PlayerSlot slot) {
        LeaveSlotMessage msg = new LeaveSlotMessage(slot.getNumber());
        gc.getConnection().send(msg);
    }


    public NameProvider getNameProvider() {
        return nameProvider;
    }

    public void setNameProvider(NameProvider nameProvider) {
        this.nameProvider = nameProvider;
    }

    class NicknameUpdater extends Thread implements CaretListener, FocusListener {

        private boolean stopped;
        private String update;

        @Override
        public void caretUpdate(CaretEvent e) {
            JTextField field = (JTextField) e.getSource();
            update = field.getText();
        }

        @Override
        public void focusGained(FocusEvent e) {
            //do nothing
        }

        @Override
        public void focusLost(FocusEvent e) {
            JTextField field = (JTextField) e.getSource();
            update = field.getText();
        }


        private void requestUpdate() {
            if (slot.isOwn() && update != null && !update.equals(slot.getNickname())) {
                slot.setNickname(update);
                sendTakeSlotMessage(slot);
                update = null;
            }
        }

        public void setStopped(boolean stopped) {
            this.stopped = stopped;
        }

        @Override
        public void run() {
            while (!stopped) {
                if (update != null) {
                    requestUpdate();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    //pass
                }
            }
        }
    }
}
