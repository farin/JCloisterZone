package com.jcloisterzone.ui.panel;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.board.TilePackFactory;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.config.Config.PresetConfig;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.PlayerSlot.SlotType;
import com.jcloisterzone.ui.Client;

public class CreateGamePanel extends JPanel {

    private static final long serialVersionUID = -8993000662700228625L;

    static Font FONT_RULE_SECTION = new Font(null, Font.ITALIC, 13);

    private final Client client;
    private boolean mutableSlots;

    private JPanel playersPanel;
    // private JLabel helpText;
    private JComboBox<Object> presets;
    private JButton presetSave, presetDelete;

    private JButton startGameButton;
    private JPanel expansionPanel;
    private JPanel rulesPanel;
    private JPanel panel;

    private Map<Expansion, JComponent[]> expansionComponents = new HashMap<>();
    private Map<CustomRule, JCheckBox> ruleCheckboxes = new HashMap<>();

    static class Preset implements Comparable<Preset>{
        private final String name;
        private PresetConfig config;

        public Preset(String name, PresetConfig config) {
            this.name = name;
            this.config = config;
        }

        @Override
        public int compareTo(Preset o) {
            return name.compareTo(o.name);
        }

        @Override
        public String toString() {
            return name;
        }

        public String getName() {
            return name;
        }

        public PresetConfig getConfig() {
            return config;
        }

        public void setConfig(PresetConfig config) {
            this.config = config;
        }
    }

    /**
     * Create the panel.
     */
    public CreateGamePanel(final Client client, boolean mutableSlots, PlayerSlot[] slots) {
        this.client = client;
        this.mutableSlots = mutableSlots;
        NameProvider nameProvider = new NameProvider(client.getConfig());

        setLayout(new MigLayout("", "[][grow][grow]", "[][grow]"));

        panel = new JPanel();
        add(panel, "cell 0 0 3 1,grow");
        panel.setLayout(new MigLayout("", "[grow][]", "[]"));

        // helpText = new JLabel("[TODO HELP TEXT]");
        // panel.add(helpText, "growx");
        // helpText.setText(_("The game has been created. Remote clients can connect now."));

        startGameButton = new JButton(_("Start game"));
        startGameButton.setIcon(new ImageIcon(CreateGamePanel.class
                .getResource("/sysimages/endTurn.png")));
        panel.add(startGameButton, "width 100, h 40, east");

        startGameButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                client.getServer().startGame();
            }
        });

        if (mutableSlots) {
            panel.add(createProfilePanel(), "west");
        }

        playersPanel = new JPanel();
        playersPanel.setBorder(new TitledBorder(null, _("Players"),
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        playersPanel.setLayout(new MigLayout("", "[grow]", ""));

        for (PlayerSlot slot : slots) {
            if (slot != null) {
                playersPanel.add(new CreateGamePlayerPanel(client, mutableSlots, slot, nameProvider), "wrap");
            }
        }
        if (mutableSlots) {
            JCheckBox randomSeating = createRuleCheckbox(CustomRule.RANDOM_SEATING_ORDER, true);
            playersPanel.add(randomSeating, "wrap, gaptop 10");
            ruleCheckboxes.put(CustomRule.RANDOM_SEATING_ORDER, randomSeating);
        }

        add(playersPanel, "cell 0 1,grow");

        expansionPanel = new JPanel();
        expansionPanel.setBorder(new TitledBorder(null, _("Expansions"),
                TitledBorder.LEADING, TitledBorder.TOP, null, null));

        TilePackFactory tilePackFactory = new TilePackFactory();
        tilePackFactory.setConfig(client.getConfig());

        expansionPanel.setLayout(new MigLayout("", "[][right]", "[]"));
        for (Expansion exp : Expansion.values()) {
            if (!exp.isImplemented()) continue;
            createExpansionLine(exp, tilePackFactory.getExpansionSize(exp));
        }
        add(expansionPanel, "cell 1 1,grow");

        rulesPanel = new JPanel();
        rulesPanel.setBorder(new TitledBorder(null, _("Rules"),
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        rulesPanel.setLayout(new MigLayout("", "[]", "[]"));
        add(rulesPanel, "cell 2 1,grow");

        Expansion prev = Expansion.BASIC;
        for (CustomRule rule : CustomRule.values()) {
            if (rule == CustomRule.RANDOM_SEATING_ORDER) continue;
            if (prev != rule.getExpansion()) {
                prev = rule.getExpansion();
                JLabel label = new JLabel(prev.toString());
                label.setFont(FONT_RULE_SECTION);
                rulesPanel.add(label, "wrap, growx, gaptop 10, gapbottom 7");
            }
            JCheckBox chbox = createRuleCheckbox(rule, mutableSlots);
            rulesPanel.add(chbox, "wrap");
            ruleCheckboxes.put(rule, chbox);
        }

        onSlotStateChange();
        startGameButton.requestFocus();
    }

    private JPanel createProfilePanel() {
        JPanel profilePanel = new JPanel();
        profilePanel.setBorder(new TitledBorder(null, _("Presets"),
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        profilePanel.setLayout(new MigLayout());


        presets = new JComboBox<Object>(getPresets());
        presets.setEditable(true);
        presets.setSelectedItem("");
        presets.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (presets.getSelectedItem() instanceof Preset) {
                    Preset profile = (Preset) presets.getSelectedItem();
                    profile.getConfig().updateGameSetup(client.getServer());
                }
            }
        });
        profilePanel.add(presets, "width 160, gapright 10, west");

        presetSave = new JButton(_("Save"));
        presetSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object item = presets.getSelectedItem();
                if (item instanceof String) {
                    Preset profile = getProfileFor((String) item);
                    if (profile != null) {
                        item = profile;
                    }
                }
                Preset profile = null;
                if (item instanceof String) { //not found matching profile, create new
                    profile = new Preset(((String)item).trim(), createCurrentConfig());
                    presets.addItem(profile); //TODO insert at
                } else { //profile already exists
                    profile = (Preset) item;
                    profile.setConfig(createCurrentConfig());
                }
                Config config = client.getConfig();
                config.getPresets().put(profile.getName(), profile.getConfig());
                client.saveConfig();
            }
        });
        profilePanel.add(presetSave, "width 80, gapright 10, west");

        presetDelete = new JButton(_("Delete"));
        presetDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object item = presets.getSelectedItem();
                if (item instanceof String) {
                    item = getProfileFor((String) item);
                }
                if (item instanceof Preset) {
                    Preset profile = (Preset) item;
                    presets.removeItem(profile);
                    Config config = client.getConfig();
                    config.getPresets().remove(profile.getName());
                    client.saveConfig();
                }
            }
        });
        profilePanel.add(presetDelete, "width 80, west");
        return profilePanel;
    }

    private void createExpansionLine(Expansion exp, int expSize) {
        JCheckBox chbox = createExpansionCheckbox(exp, mutableSlots);
        if (exp == Expansion.KING_AND_ROBBER_BARON || exp == Expansion.INNS_AND_CATHEDRALS || exp == Expansion.FLIER) {
            expansionPanel.add(chbox, "gaptop 10");
        } else {
            expansionPanel.add(chbox, "");
        }
        JLabel expansionSize = new JLabel(expSize+"");
        expansionSize.setForeground(Color.GRAY);
        expansionPanel.add(expansionSize, "wrap");
        expansionComponents.put(exp, new JComponent[] {chbox, expansionSize});
    }

    private Preset getProfileFor(String name) {
        name = name.trim();
        if ("".equals(name)) return null;

        int count = presets.getItemCount();
        for (int i = 0; i < count; i++) {
            Preset profile = (Preset) presets.getItemAt(i);
            if (profile.getName().equals(name)) {
                return profile;
            }
        }
        return null;
    }

    private PresetConfig createCurrentConfig() {
        List<String> expansions = new ArrayList<>();
        List<String> rules = new ArrayList<>();
        for (Expansion exp : client.getGame().getExpansions()) {
            if (exp == Expansion.BASIC) continue;
            expansions.add(exp.name());
        }
        for (CustomRule rule : client.getGame().getCustomRules()) {
            rules.add(rule.name());
        }
        PresetConfig config = new PresetConfig();
        config.setExpansions(expansions);
        config.setRules(rules);
        return config;
    }

    private Preset[] getPresets() {
        Map<String, PresetConfig> presetCfg = client.getConfig().getPresets();
        if (presetCfg == null) {
            return new Preset[0];
        }
        ArrayList<Preset> profiles = new ArrayList<>();
        for (Entry<String, PresetConfig> e : presetCfg.entrySet()) {
            profiles.add(new Preset(e.getKey(), e.getValue()));
        }
        Collections.sort(profiles);
        return profiles.toArray(new Preset[profiles.size()]);
    }

    public void disposePanel() {
        for (Component comp : playersPanel.getComponents()) {
            if (comp instanceof CreateGamePlayerPanel) {
                ((CreateGamePlayerPanel) comp).disposePanel();
            }
        }
    }

    private JCheckBox createRuleCheckbox(final CustomRule rule, boolean mutableSlots) {
        JCheckBox chbox = new JCheckBox(rule.getLabel());
        if (mutableSlots) {
            chbox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JCheckBox chbox = (JCheckBox) e.getSource();
                    client.getServer().updateCustomRule(rule, chbox.isSelected());
                }
            });
        } else {
            chbox.setEnabled(false);
        }
        return chbox;
    }

    private JCheckBox createExpansionCheckbox(final Expansion exp,
            boolean mutableSlots) {
        JCheckBox chbox = new JCheckBox(exp.toString());
        if (!exp.isImplemented() || !mutableSlots)
            chbox.setEnabled(false);
        if (exp == Expansion.BASIC) {
            chbox.setEnabled(false);
        }
        if (chbox.isEnabled()) {
            chbox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JCheckBox chbox = (JCheckBox) e.getSource();
                    client.getServer().updateExpansion(exp, chbox.isSelected());
                }
            });
        }
        return chbox;
    }

    public void updateCustomRule(CustomRule rule, Boolean enabled) {
        JCheckBox chbox = ruleCheckboxes.get(rule);
        if (chbox != null) {
            chbox.setSelected(enabled);
        }
        if (rule == CustomRule.RANDOM_SEATING_ORDER) {
            updateSerialLabels();
        }
    }

    public void updateExpansion(Expansion expansion, Boolean enabled) {
        ((JCheckBox)expansionComponents.get(expansion)[0]).setSelected(enabled);
    }

    public void updateSupportedExpansions(EnumSet<Expansion> expansions) {
        if (expansions == null) {
            expansions = EnumSet.allOf(Expansion.class);
        }
        for (Expansion exp : Expansion.values()) {
            if (exp.isImplemented()) {
                boolean isSupported = expansions.contains(exp);
                JComponent[] components = expansionComponents.get(exp);
                for (JComponent comp : components) {
                    comp.setVisible(isSupported);
                }
            }
        }
    }

    private CreateGamePlayerPanel getPlayerPanel(int number) {
        for (Component comp : playersPanel.getComponents()) {
            if (comp instanceof CreateGamePlayerPanel) {
                CreateGamePlayerPanel panel = (CreateGamePlayerPanel) comp;
                PlayerSlot slot = panel.getSlot();
                if (slot != null && slot.getNumber() == number) return panel;
            }
        }
        throw new IllegalArgumentException("Slot " + number + " does not exit.");
    }

    public void updateSlot(PlayerSlot slot) {
        getPlayerPanel(slot.getNumber()).updateSlot(slot);
        onSlotStateChange();
    }

    private void updateSerialLabels() {
        ArrayList<Integer> serials = new ArrayList<Integer>();

        for (Component c : playersPanel.getComponents()) {
            if (!(c instanceof CreateGamePlayerPanel)) continue;
            CreateGamePlayerPanel playerPanel = (CreateGamePlayerPanel) c;
            PlayerSlot ps = playerPanel.getSlot();
            if (ps != null && ps.getSerial() != null) {
                serials.add(ps.getSerial());
            } else {
                playerPanel.setSerialText("");
            }
        }
        if (mutableSlots && !serials.isEmpty()) {
            Collections.sort(serials);
            boolean randomSeating = client.getGame().hasRule(CustomRule.RANDOM_SEATING_ORDER);
            for (Component c : playersPanel.getComponents()) {
                if (!(c instanceof CreateGamePlayerPanel)) continue;
                CreateGamePlayerPanel playerPanel = (CreateGamePlayerPanel) c;
                PlayerSlot ps = playerPanel.getSlot();
                if (ps != null && ps.getSerial() != null) {
                    String serial = randomSeating ? "?" : ("" + (1 + serials.indexOf(ps.getSerial())));
                    playerPanel.setSerialText(serial);
                }
            }
        }
    }

    private void onSlotStateChange() {
        boolean anyPlayerAssigned = false;
        boolean allPlayersAssigned = true;


        for (Component c : playersPanel.getComponents()) {
            if (!(c instanceof CreateGamePlayerPanel)) continue;
            CreateGamePlayerPanel playerPanel = (CreateGamePlayerPanel) c;
            PlayerSlot ps = playerPanel.getSlot();
            if (ps == null || ps.getType() == SlotType.OPEN) {
                allPlayersAssigned = false;
            } else {
                anyPlayerAssigned = true;
            }
        }
        if (mutableSlots) {
            startGameButton.setEnabled(anyPlayerAssigned);
        } else {
            startGameButton.setEnabled(allPlayersAssigned);
        }

        updateSerialLabels();
    }

}
