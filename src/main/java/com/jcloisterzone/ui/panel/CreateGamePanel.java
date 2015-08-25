package com.jcloisterzone.ui.panel;

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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import net.miginfocom.swing.MigLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.board.TilePackFactory;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.config.Config.PresetConfig;
import com.jcloisterzone.event.setup.ExpansionChangedEvent;
import com.jcloisterzone.event.setup.PlayerSlotChangeEvent;
import com.jcloisterzone.event.setup.RuleChangeEvent;
import com.jcloisterzone.event.setup.SupportedExpansionsChangeEvent;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.component.TextPrompt;
import com.jcloisterzone.ui.component.TextPrompt.Show;
import com.jcloisterzone.wsio.message.SetExpansionMessage;
import com.jcloisterzone.wsio.message.SetRuleMessage;
import com.jcloisterzone.wsio.message.StartGameMessage;

import static com.jcloisterzone.ui.I18nUtils._;

public class CreateGamePanel extends JPanel {

    private static final long serialVersionUID = -8993000662700228625L;

    static Font FONT_RULE_SECTION = new Font(null, Font.ITALIC, 13);

    private final Client client;
    private final Game game;
    private final GameController gc;
    private boolean mutableSlots;

    private JPanel playersPanel;

    private JComboBox<Object> presets;
    private JButton presetSave, presetDelete;

    private JButton leaveGameButton, startGameButton;
    private JPanel expansionPanel;
    private JPanel rulesPanel;
    private JPanel header;

    private JCheckBox timeLimitChbox;
    private JSpinner timeLimitSpinner;
    private SpinnerNumberModel timeLimitModel;

    private Map<Expansion, JComponent[]> expansionComponents = new HashMap<>();
    private Map<CustomRule, JCheckBox> ruleCheckboxes = new HashMap<>();

    protected final transient Logger logger = LoggerFactory
            .getLogger(getClass());

    static class Preset implements Comparable<Preset> {
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
    public CreateGamePanel(final Client client, final GameController gc, boolean mutableSlots, PlayerSlot[] slots) {
        this.client = client;
        this.gc = gc;
        this.game = gc.getGame();
        this.mutableSlots = mutableSlots;
        NameProvider nameProvider = new NameProvider(client.getConfig());

        setLayout(new MigLayout("", "[grow]", "[][grow]"));
        add(header = new JPanel(), "cell 0 0, growx");

        header.setLayout(new MigLayout("", "[grow]"));

        startGameButton = new JButton(_("Start game"));
        startGameButton.setFont(new Font(null, Font.PLAIN, 25));
        header.add(startGameButton, "width 240, h 40, east");

        if (gc.getChannel() != null) {
            leaveGameButton = new JButton(_("Leave game"));
            header.add(leaveGameButton, "h pref!, gapx 10px 10px, east");

            leaveGameButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    gc.leaveGame();
                }
            });
        }

        startGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.getConnection().send(new StartGameMessage(game.getGameId()));
            }
        });

        if (mutableSlots) {
            header.add(createPresetPanel(), "west");
        }

        JPanel scrolled = new JPanel();
        scrolled.setLayout(new MigLayout("", "[][grow][grow]", "[grow]"));


        playersPanel = new JPanel();
        playersPanel.setBorder(new TitledBorder(null, _("Players"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
        playersPanel.setLayout(new MigLayout("", "[grow]", ""));

        if (mutableSlots) {
            JLabel hint = new JLabel(_("Click twice on a slot button to add a computer player."));
            hint.setFont(new Font(null, Font.ITALIC, 11));
            hint.setForeground(Color.DARK_GRAY);
            playersPanel.add(hint, "aligny bottom, gapbottom 5, wrap");
        }

        for (PlayerSlot slot : slots) {
            if (slot != null) {
                CreateGamePlayerPanel panel = new CreateGamePlayerPanel(client, game, gc.getChannel() != null, mutableSlots, slot, slots);
                panel.setNameProvider(nameProvider);
                playersPanel.add(panel, "wrap");
            }
        }
        if (mutableSlots) {
            JCheckBox randomSeating = createRuleCheckbox(CustomRule.RANDOM_SEATING_ORDER, true);
            playersPanel.add(randomSeating, "wrap, gaptop 10");
            ruleCheckboxes.put(CustomRule.RANDOM_SEATING_ORDER, randomSeating);
        }

        playersPanel.add(createClockPanel(), "wrap, gaptop 10, grow");

        scrolled.add(playersPanel, "cell 0 0, grow");

        expansionPanel = new JPanel();
        expansionPanel.setBorder(new TitledBorder(null, _("Expansions"),
                TitledBorder.LEADING, TitledBorder.TOP, null, null));

        TilePackFactory tilePackFactory = new TilePackFactory();
        tilePackFactory.setConfig(client.getConfig());

        expansionPanel.setLayout(new MigLayout("gapy 1", "[][right]", "[]"));
        for (Expansion exp : Expansion.values()) {
            if (!exp.isImplemented()) continue;
            createExpansionLine(exp, tilePackFactory.getExpansionSize(exp));
        }
        scrolled.add(expansionPanel, "cell 1 0,grow");

        rulesPanel = new JPanel();
        rulesPanel.setBorder(new TitledBorder(null, _("Rules"),
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        rulesPanel.setLayout(new MigLayout("", "[]", "[]"));
        scrolled.add(rulesPanel, "cell 2 0,grow");

        Expansion prev = Expansion.BASIC;
        for (CustomRule rule : CustomRule.values()) {
            if (rule.getExpansion() == null) continue;
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

        JScrollPane scroll = new JScrollPane(scrolled);
        scroll.setViewportBorder(null);  //ubuntu jdk
        scroll.setBorder(BorderFactory.createEmptyBorder()); //win jdk
        add(scroll, "cell 0 1, grow");

        onSlotStateChange();
        startGameButton.requestFocus();
    }

    private JPanel createClockPanel() {
        JPanel clockPanel = new JPanel();
        clockPanel.setBorder(new TitledBorder(null, _("Clock"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
        clockPanel.setLayout(new MigLayout("", "[][][]", ""));

        Integer value = (Integer) game.getCustomRules().get(CustomRule.CLOCK_PLAYER_TIME);
        timeLimitChbox = new JCheckBox(_("player time limit"), value != null);
        timeLimitChbox.setEnabled(mutableSlots);
        timeLimitSpinner = new JSpinner();
        timeLimitModel = new SpinnerNumberModel(value == null ? 20 : value / 60, 0, 300, 1);
        timeLimitSpinner.setModel(timeLimitModel);
        timeLimitSpinner.setEnabled(mutableSlots);
        if (value == null) {
            timeLimitSpinner.setEnabled(false);
        }
        clockPanel.add(timeLimitChbox);
        clockPanel.add(timeLimitSpinner, "w 40");
        clockPanel.add(new JLabel(_("minutes")), "gapleft 4");
        if (mutableSlots) {
            timeLimitChbox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    timeLimitSpinner.setEnabled(timeLimitChbox.isSelected());
                    client.getConnection().send(new SetRuleMessage(game.getGameId(), CustomRule.CLOCK_PLAYER_TIME, timeLimitChbox.isSelected() ? 60 * timeLimitModel.getNumber().intValue() : null));
                }
            });
            timeLimitSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (timeLimitChbox.isSelected()) {
                        Integer value = timeLimitModel.getNumber().intValue() * 60;
                        if (value != game.getCustomRules().get(CustomRule.CLOCK_PLAYER_TIME)) {
                            client.getConnection().send(new SetRuleMessage(game.getGameId(), CustomRule.CLOCK_PLAYER_TIME, value));
                        }
                    }
                }
            });
        }
        return clockPanel;
    }

    private JPanel createPresetPanel() {
        JPanel presetPanel = new JPanel();
        presetPanel.setBorder(new TitledBorder(null, _("Presets"),
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        presetPanel.setLayout(new MigLayout());

        presets = new JComboBox<Object>(getPresets());
        presets.setEditable(true);
        presets.setSelectedItem("");
        presets.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (presets.getSelectedItem() instanceof Preset) {
                    Preset profile = (Preset) presets.getSelectedItem();
                    profile.getConfig().updateGameSetup(client.getConnection(), game.getGameId());
                }
            }
        });
        presetPanel.add(presets, "width 160, gapright 10, west");

        JTextComponent editorComponent = (JTextComponent) presets.getEditor()
                .getEditorComponent();
        TextPrompt tp = new TextPrompt(_("Preset name"), editorComponent);
        tp.setShow(Show.FOCUS_LOST);
        tp.changeStyle(Font.ITALIC);
        tp.changeAlpha(0.4f);

        editorComponent.getDocument().addDocumentListener(
                new DocumentListener() {

                    private void handle(DocumentEvent e) {
                        try {
                            Document doc = e.getDocument();
                            updatePresetButtons(doc.getText(0, doc.getLength()));
                        } catch (BadLocationException ex) {
                            logger.error(ex.getMessage(), ex);
                        }
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        handle(e);
                    }

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        handle(e);
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        handle(e);
                    }
                });

        presetSave = new JButton(_("Save"));
        presetSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object item = presets.getSelectedItem();
                if (item instanceof String) {
                    Preset profile = getPresetFor((String) item);
                    if (profile != null) {
                        item = profile;
                    }
                }
                Preset profile = null;
                if (item instanceof String) { // not found matching profile,
                                                // create new
                    profile = new Preset(((String) item).trim(), createCurrentConfig());
                    presets.addItem(profile); // TODO insert at
                } else { // profile already exists
                    profile = (Preset) item;
                    profile.setConfig(createCurrentConfig());
                }
                Config config = client.getConfig();
                config.getPresets().put(profile.getName(), profile.getConfig());
                client.saveConfig();
                updatePresetButtons(presets.getSelectedItem());
            }
        });
        presetPanel.add(presetSave, "width 80, gapright 10, west");

        presetDelete = new JButton(_("Delete"));
        presetDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object item = presets.getSelectedItem();
                if (item instanceof String) {
                    item = getPresetFor((String) item);
                }
                if (item instanceof Preset) {
                    Preset preset = (Preset) item;
                    presets.removeItem(preset);
                    Config config = client.getConfig();
                    config.getPresets().remove(preset.getName());
                    client.saveConfig();
                    presets.setSelectedItem("");
                    updatePresetButtons("");
                }
            }
        });
        presetPanel.add(presetDelete, "width 80, west");

        updatePresetButtons(presets.getSelectedItem());
        return presetPanel;
    }

    private void updatePresetButtons(Object item) {
        if (item instanceof String) {
            item = ((String) item).trim();
            Preset preset = getPresetFor((String) item);
            if (preset != null)
                item = preset;
        }
        if (item instanceof Preset) {
            presetSave.setEnabled(true);
            presetDelete.setEnabled(true);
        } else {
            presetDelete.setEnabled(false);
            if ("".equals(item)) {
                presetSave.setEnabled(false);
            } else {
                presetSave.setEnabled(true);
            }
        }
    }

    private void createExpansionLine(Expansion exp, int expSize) {
        JCheckBox chbox = createExpansionCheckbox(exp, mutableSlots);
        if (exp == Expansion.KING_AND_ROBBER_BARON
                || exp == Expansion.INNS_AND_CATHEDRALS
                || exp == Expansion.FLIER) {
            expansionPanel.add(chbox, "gaptop 5");
        } else {
            expansionPanel.add(chbox, "");
        }
        JLabel expansionSize = new JLabel(expSize + "");
        expansionSize.setForeground(Color.GRAY);
        expansionPanel.add(expansionSize, "wrap");
        expansionComponents.put(exp, new JComponent[] { chbox, expansionSize });
    }

    private Preset getPresetFor(String name) {
        name = name.trim();
        if ("".equals(name))
            return null;

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
        for (Expansion exp : game.getExpansions()) {
            if (exp == Expansion.BASIC) continue;
            expansions.add(exp.name());
        }
        PresetConfig config = new PresetConfig();
        config.setExpansions(expansions);
        config.setRules(game.getCustomRules());
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

    private JCheckBox createRuleCheckbox(final CustomRule rule,
            boolean mutableSlots) {
        JCheckBox chbox = new JCheckBox(rule.getLabel(), game.getBooleanValue(rule));
        if (mutableSlots) {
            chbox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JCheckBox chbox = (JCheckBox) e.getSource();
                    client.getConnection().send(new SetRuleMessage(game.getGameId(), rule, chbox.isSelected()));
                }
            });
        } else {
            chbox.setEnabled(false);
        }
        return chbox;
    }

    private JCheckBox createExpansionCheckbox(final Expansion exp,
            boolean mutableSlots) {
        JCheckBox chbox = new JCheckBox(exp.toString(), game.hasExpansion(exp));
        if (!exp.isImplemented() || !mutableSlots)
            chbox.setEnabled(false);
        if (exp == Expansion.BASIC) {
            chbox.setEnabled(false);
        }
        if (chbox.isEnabled()) {
            chbox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    final JCheckBox chbox = (JCheckBox) e.getSource();
                    client.getConnection().send(new SetExpansionMessage(game.getGameId(), exp, chbox.isSelected()));
                }
            });
        }
        return chbox;
    }

    public void updateCustomRule(CustomRule rule, Object value) {
        if (rule.getType().equals(Boolean.class)) {
            JCheckBox chbox = ruleCheckboxes.get(rule);
            boolean enabled = value == null ? false : (Boolean) value;
            if (chbox != null && chbox.isSelected() != enabled) {
                chbox.setSelected(enabled);
                UiUtils.highlightComponent(chbox);
            }
            if (rule == CustomRule.RANDOM_SEATING_ORDER) {
                updateSerialLabels();
            }
        } else {
            if (rule == CustomRule.CLOCK_PLAYER_TIME) {
                if (value == null) {
                    timeLimitChbox.setSelected(false);
                    timeLimitSpinner.setEnabled(false);
                } else {
                    timeLimitChbox.setSelected(true);
                    timeLimitSpinner.setEnabled(true);
                    timeLimitModel.setValue(((Integer) value) / 60);
                }
            }
        }
    }

    public void updateExpansion(Expansion expansion, Boolean enabled) {
        JCheckBox chbox = (JCheckBox) expansionComponents.get(expansion)[0];
        if (chbox.isSelected() != enabled) {
            chbox.setSelected(enabled);
            if (expansion != Expansion.BASIC) { // hardcoded exception
                UiUtils.highlightComponent(chbox);
            }
        }
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
                if (slot != null && slot.getNumber() == number)
                    return panel;
            }
        }
        throw new IllegalArgumentException("Slot " + number + " does not exit.");
    }

    public void updateSlot(int number) {
        getPlayerPanel(number).updateSlot();
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
            boolean randomSeating = game.getBooleanValue(CustomRule.RANDOM_SEATING_ORDER);
            for (Component c : playersPanel.getComponents()) {
                if (!(c instanceof CreateGamePlayerPanel)) continue;
                CreateGamePlayerPanel playerPanel = (CreateGamePlayerPanel) c;
                PlayerSlot ps = playerPanel.getSlot();
                if (ps != null && ps.getSerial() != null) {
                    String serial = randomSeating ? "?" : ("" + (1 + serials
                            .indexOf(ps.getSerial())));
                    playerPanel.setSerialText(serial);
                }
            }
        }
    }

    private void onSlotStateChange() {
        int playersAssigned = 0;
        boolean anyHumanPlayersAssigned = false;
        boolean allPlayersAssigned = true;


        for (Component c : playersPanel.getComponents()) {
            if (!(c instanceof CreateGamePlayerPanel)) continue;
            CreateGamePlayerPanel playerPanel = (CreateGamePlayerPanel) c;
            PlayerSlot ps = playerPanel.getSlot();
            if (ps.isOccupied()) {
                playersAssigned++;
                if (!ps.isAi()) {
                    anyHumanPlayersAssigned = true;
                }
            } else {
                allPlayersAssigned = false;
            }
        }
        if (mutableSlots) {
            if (gc.getChannel() == null) {
                startGameButton.setEnabled(playersAssigned > 0);
            } else {
                boolean state;
                if ("true".equals(System.getProperty("allowAiOnlyOnlineGame"))) {
                    state = playersAssigned > 1;
                } else {
                    state = anyHumanPlayersAssigned;
                }

                startGameButton.setEnabled(state);
            }
        } else {
            startGameButton.setEnabled(allPlayersAssigned);
        }

        updateSerialLabels();
    }

    @Subscribe
    public void updateCustomRule(RuleChangeEvent ev) {
        updateCustomRule(ev.getRule(), ev.getValue());
    }

    @Subscribe
    public void updateExpansion(ExpansionChangedEvent ev) {
        updateExpansion(ev.getExpansion(), ev.isEnabled());
    }

    @Subscribe
    public void updateSlot(PlayerSlotChangeEvent ev) {
        updateSlot(ev.getSlot().getNumber());
    }

    @Subscribe
    public void updateSupportedExpansions(SupportedExpansionsChangeEvent ev) {
        updateSupportedExpansions(ev.getExpansions());
    }

}
