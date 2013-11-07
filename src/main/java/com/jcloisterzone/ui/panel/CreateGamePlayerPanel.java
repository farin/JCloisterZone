package com.jcloisterzone.ui.panel;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.EnumSet;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import net.miginfocom.swing.MigLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.ai.legacyplayer.LegacyAiPlayer;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.PlayerSlot.SlotType;
import com.jcloisterzone.ui.Client;

public class CreateGamePlayerPanel extends JPanel {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private static final long serialVersionUID = 1436952221307376517L;

    static Font FONT_PLAYER_TYPE = new Font(null, Font.ITALIC, 11);
    static Font FONT_SERIAL = new Font(null, Font.BOLD, 32);

    private PlayerSlot slot;

    private final Client client;
    private boolean mutableSlots;
    private long clientId;

    private JButton icon;
    private JLabel status;
    private JTextField nickname;

    private NicknameUpdater nicknameUpdater;
    private JLabel serialLabel;

    private NameProvider nameProvider;


    /**
     * Create the panel.
     */
    public CreateGamePlayerPanel(final Client client, boolean mutableSlots, PlayerSlot slot, NameProvider nameProvider) {
        this.client = client;
        this.mutableSlots = mutableSlots;
        this.clientId = client.getClientId();
        this.nameProvider = nameProvider;

        setLayout(new MigLayout("", "[][][10px][grow]", "[][]"));

        serialLabel = new JLabel("");
        serialLabel.setHorizontalAlignment(SwingConstants.CENTER);
        serialLabel.setForeground(new Color(180,180,180));
        serialLabel.setFont(FONT_SERIAL);
        add(serialLabel, "cell 0 0 0 2,width 34!,height 60!");

        icon = new JButton();
        icon.addActionListener(mutableSlots ? new MutableIconActionListener() : new ImmutableIconActionListener());
        add(icon, "cell 1 0 1 2,width 60!,height 60!");

        nickname = new JTextField();
        updateNickname(false);
        add(nickname, "cell 3 0,growx,width :200:,gapy 10");

        status = new JLabel("");
        status.setFont(FONT_PLAYER_TYPE);
        add(status, "cell 3 1,growx");

        updateSlot(slot);

        if (mutableSlots) {
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

    private boolean isMySlotBefore(PlayerSlot slot) {
        if (slot == null || this.slot == null) return false;
        if (slot.getOwner() == null || this.slot.getOwner() == null) return false;
        if (slot.getOwner() != clientId || this.slot.getOwner() != clientId) return false;
        if (slot.getType() != this.slot.getType()) return false;
        return true;
    }

    public void updateSlot(PlayerSlot slot) {
        //logger.debug("Updating slot {}", slot);
        if (mutableSlots) {
            updateSlotMutable(slot);
        } else {
            updateSlotImmutable(slot);
        }
    }

    public PlayerSlot getSlot() {
        return slot;
    }

    private void updateNickname(boolean editable) {
        //nickname.setEditable(false);
        nickname.setEnabled(editable);
    }

    private void updateIcon(String iconType, Color color, boolean state) {
        ImageIcon img = new ImageIcon(client.getFigureTheme().getPlayerSlotImage(iconType, color));
        icon.setIcon(img);
        icon.setDisabledIcon(img);
        icon.setEnabled(state);
    }

    public void updateSlotImmutable(PlayerSlot slot) {
        this.slot = slot;
        Color color = client.getPlayerColor(slot);
        switch (slot.getType()) {
            case OPEN:
                status.setText(_("Unassigned player"));
                updateIcon("open", color, true);
                break;
            case PLAYER:
                if (slot.getOwner() == clientId) {
                    status.setText(_("Local player"));
                    updateIcon("local", color, true);
                } else {
                    status.setText(_("Remote player"));
                    updateIcon("remote", color, false);
                }
                break;
            case AI:
                status.setText(_("Computer player"));
                updateIcon("ai", color, false);
                break;
        }
        nickname.setText(slot.getNick());
    }

    public void updateSlotMutable(PlayerSlot slot) {
        boolean myBefore = isMySlotBefore(slot);
        this.slot = slot;
        Color color = client.getPlayerColor(slot);
        switch (slot.getType()) {
            case OPEN:
                status.setText(_("Open player slot"));
                updateIcon("open", color, true);
                updateNickname(false);
                break;
            case PLAYER:
                if (slot.getOwner() != null && slot.getOwner() == clientId) {
                    status.setText(_("Local player"));
                    updateIcon("local", color, true);
                    updateNickname(true);
                } else {
                    status.setText(_("Remote player"));
                    updateIcon("remote", color, false);
                    updateNickname(false);
                }
                break;
            case AI:
                status.setText(_("Computer player"));
                //updateIcon("ai", color, slot.getOwner() == clientId);
                updateIcon("ai", color, true);
                updateNickname(false);
                break;
        }
        if (!myBefore || ! nickname.isEnabled()) { //probably change by me
            nickname.setText(slot.getNick());
        }
        /*if (slot.getSerial() == null) {
            serialLabel.setText("");
        } else {
            serialLabel.setText(slot.getSerial() + "");
        }*/
    }

    public void setSerialText(String text) {
        serialLabel.setText(text);
    }

    class MutableIconActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String nick;
            EnumSet<Expansion> supported = null;
            switch (slot.getType()) {
            case OPEN: //-> PLAYER
                slot.setType(SlotType.PLAYER);
                nick = nameProvider.reserveName(SlotType.PLAYER, slot.getNumber());
                slot.setNick(nick);
                nickname.setText(nick);
                break;
            case PLAYER: //-> AI
                nameProvider.releaseName(SlotType.PLAYER, slot.getNumber());
                slot.setType(SlotType.AI);
                //TODO pryc s hardcoded AI tridou
                slot.setAiClassName(LegacyAiPlayer.class.getName());
                supported = LegacyAiPlayer.supportedExpansions();
                nick = nameProvider.reserveName(SlotType.AI, slot.getNumber());
                slot.setNick(nick);
                nickname.setText(nick);
                break;
            case AI: //-> OPEN
                nameProvider.releaseName(SlotType.AI, slot.getNumber());
                slot.setType(SlotType.OPEN);
                break;
            default:
                return;
            }
            slot.setOwner(clientId);
            client.getServer().updateSlot(slot, supported);
        }
    }

    class ImmutableIconActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            switch (slot.getType()) {
            case OPEN: //-> PLAYER
                slot.setType(SlotType.PLAYER);
                break;
            case PLAYER: //-> OPEN
                slot.setType(SlotType.OPEN);
                break;
            default:
                return;
            }
            slot.setOwner(clientId);
            client.getServer().updateSlot(slot, null);
        }
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
            if (update != null && ! update.equals(slot.getNick())) {
                slot.setNick(update);
                client.getServer().updateSlot(slot, null);
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
