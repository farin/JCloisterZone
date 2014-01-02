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
import com.jcloisterzone.config.Config.ProfileConfig;
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
    private JComboBox<Object> profiles;
    private JButton profileSave, profileDelete;

    private JButton startGameButton;
    private JPanel expansionPanel;
    private JPanel rulesPanel;
    private JPanel panel;

    private Map<Expansion, JComponent[]> expansionComponents = new HashMap<>();
    private Map<CustomRule, JCheckBox> ruleCheckboxes = new HashMap<>();

    static class Profile implements Comparable<Profile>{
        private final String name;
        private ProfileConfig config;

        public Profile(String name, ProfileConfig config) {
            this.name = name;
            this.config = config;
        }

        @Override
        public int compareTo(Profile o) {
            return name.compareTo(o.name);
        }

        @Override
        public String toString() {
            return name;
        }

        public String getName() {
            return name;
        }

        public ProfileConfig getConfig() {
            return config;
        }

        public void setConfig(ProfileConfig config) {
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

        JPanel profilePanel = new JPanel();
        profilePanel.setBorder(new TitledBorder(null, _("Profiles"),
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        profilePanel.setLayout(new MigLayout());
        panel.add(profilePanel, "west");

        profiles = new JComboBox<Object>(getProfiles());
        profiles.setEditable(true);
        profiles.setSelectedItem("");
        profiles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (profiles.getSelectedItem() instanceof Profile) {
                    loadProfile((Profile) profiles.getSelectedItem());
                }
            }
        });
        profilePanel.add(profiles, "width 160, gapright 10, west");

        profileSave = new JButton(_("Save"));
        profileSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object item = profiles.getSelectedItem();
                if (item instanceof String) {
                    Profile profile = getProfileFor((String) item);
                    if (profile != null) {
                        item = profile;
                    }
                }
                Profile profile = null;
                if (item instanceof String) { //not found matching profile, create new
                    profile = new Profile(((String)item).trim(), createCurrentConfig());
                    profiles.addItem(profile); //TODO insert at
                } else { //profile already exists
                    profile = (Profile) item;
                    profile.setConfig(createCurrentConfig());
                }
                Config config = client.getConfig();
                config.getProfiles().put(profile.getName(), profile.getConfig());
                client.getConfigLoader().save(config);
            }
        });
        profilePanel.add(profileSave, "width 80, gapright 10, west");

        profileDelete = new JButton(_("Delete"));
        profileDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object item = profiles.getSelectedItem();
                if (item instanceof String) {
                    item = getProfileFor((String) item);
                }
                if (item instanceof Profile) {
                    Profile profile = (Profile) item;
                    profiles.removeItem(profile);
                    Config config = client.getConfig();
                    config.getProfiles().remove(profile.getName());
                    client.getConfigLoader().save(config);
                }
            }
        });
        profilePanel.add(profileDelete, "width 80, west");

        playersPanel = new JPanel();
        playersPanel.setBorder(new TitledBorder(null, _("Players"),
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        playersPanel.setLayout(new MigLayout("", "[grow]", ""));

        for (PlayerSlot slot : slots) {
            if (slot != null) {
                playersPanel.add(new CreateGamePlayerPanel(client, mutableSlots, slot, nameProvider), "wrap");
            }
        }
        add(playersPanel, "cell 0 1,grow");

        expansionPanel = new JPanel();
        expansionPanel.setBorder(new TitledBorder(null, _("Expansions"),
                TitledBorder.LEADING, TitledBorder.TOP, null, null));

        TilePackFactory tilePackFactory = new TilePackFactory();
        tilePackFactory.setConfig(client.getConfig());

        expansionPanel.setLayout(new MigLayout("", "[][right]", "[]"));
        for (Expansion exp : Expansion.values()) {
            if (!exp.isEnabled())
                continue;
            JCheckBox chbox = createExpansionCheckbox(exp, mutableSlots);
            if (exp == Expansion.KING_AND_SCOUT || exp == Expansion.INNS_AND_CATHEDRALS || exp == Expansion.FLIER) {
                expansionPanel.add(chbox, "gaptop 10");
            } else {
                expansionPanel.add(chbox, "");
            }
            JLabel expansionSize = new JLabel(tilePackFactory.getExpansionSize(exp)+"");
            expansionSize.setForeground(Color.GRAY);
            expansionPanel.add(expansionSize, "wrap");
            expansionComponents.put(exp, new JComponent[] {chbox, expansionSize});
        }
        add(expansionPanel, "cell 1 1,grow");

        rulesPanel = new JPanel();
        rulesPanel.setBorder(new TitledBorder(null, _("Rules"),
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        rulesPanel.setLayout(new MigLayout("", "[]", "[]"));
        add(rulesPanel, "cell 2 1,grow");

        Expansion prev = Expansion.BASIC;
        for (CustomRule rule : CustomRule.values()) {
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

    private Profile getProfileFor(String name) {
        name = name.trim();
        if ("".equals(name)) return null;

        int count = profiles.getItemCount();
        for (int i = 0; i < count; i++) {
            Profile profile = (Profile) profiles.getItemAt(i);
            if (profile.getName().equals(name)) {
                return profile;
            }
        }
        return null;
    }

    private ProfileConfig createCurrentConfig() {
        List<String> expansions = new ArrayList<>();
        List<String> rules = new ArrayList<>();
        for (Expansion exp : client.getGame().getExpansions()) {
            if (exp == Expansion.BASIC) continue;
            expansions.add(exp.name());
        }
        for (CustomRule rule : client.getGame().getCustomRules()) {
            rules.add(rule.name());
        }
        ProfileConfig config = new ProfileConfig();
        config.setExpansions(expansions);
        config.setRules(rules);
        return config;
    }

    private Profile[] getProfiles() {
        Map<String, ProfileConfig> profileCfg = client.getConfig().getProfiles();
        if (profileCfg == null) {
            return new Profile[0];
        }
        ArrayList<Profile> profiles = new ArrayList<>();
        for (Entry<String, ProfileConfig> e : profileCfg.entrySet()) {
            profiles.add(new Profile(e.getKey(), e.getValue()));
        }
        Collections.sort(profiles);
        return profiles.toArray(new Profile[profiles.size()]);
    }

    private void loadProfile(Profile profile) {
        EnumSet<Expansion> expansions = EnumSet.noneOf(Expansion.class);
        expansions.add(Expansion.BASIC);
        for (String expName : profile.getConfig().getExpansions()) {
            expansions.add(Expansion.valueOf(expName));
        }

        EnumSet<CustomRule> rules = EnumSet.noneOf(CustomRule.class);
        for (String ruleName : profile.getConfig().getRules()) {
            rules.add(CustomRule.valueOf(ruleName));
        }

        client.getServer().updateGameSetup(expansions.toArray(new Expansion[expansions.size()]), rules.toArray(new CustomRule[rules.size()]));
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
        JCheckBox chbox = new JCheckBox(rule.getLabel());
        if (mutableSlots) {
            chbox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JCheckBox chbox = (JCheckBox) e.getSource();
                    client.getServer().updateCustomRule(rule,
                            chbox.isSelected());
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
        if (!exp.isEnabled() || !mutableSlots)
            chbox.setEnabled(false);
        if (exp == Expansion.BASIC) {
            // chbox.setSelected(true);
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
        ruleCheckboxes.get(rule).setSelected(enabled);
    }

    public void updateExpansion(Expansion expansion, Boolean enabled) {
        ((JCheckBox)expansionComponents.get(expansion)[0]).setSelected(enabled);
    }

    public void updateSupportedExpansions(EnumSet<Expansion> expansions) {
        if (expansions == null) {
            expansions = EnumSet.allOf(Expansion.class);
        }
        for (Expansion exp : Expansion.values()) {
            if (exp.isEnabled()) {
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
            CreateGamePlayerPanel panel = (CreateGamePlayerPanel) comp;
            PlayerSlot slot = panel.getSlot();
            if (slot != null && slot.getNumber() == number)
                return panel;
        }
        throw new IllegalArgumentException("Slot " + number + " does not exit.");
    }

    public void updateSlot(PlayerSlot slot) {
        getPlayerPanel(slot.getNumber()).updateSlot(slot);
        onSlotStateChange();
    }

    private void onSlotStateChange() {
        boolean anyPlayerAssigned = false;
        boolean allPlayersAssigned = true;
        ArrayList<Integer> serials = new ArrayList<Integer>();

        for (Component c : playersPanel.getComponents()) {
            CreateGamePlayerPanel playerPanel = (CreateGamePlayerPanel) c;
            PlayerSlot ps = playerPanel.getSlot();
            if (ps == null || ps.getType() == SlotType.OPEN) {
                allPlayersAssigned = false;
            } else {
                anyPlayerAssigned = true;
            }
            if (ps != null && ps.getSerial() != null) {
                serials.add(ps.getSerial());
            } else {
                playerPanel.setSerialText("");
            }
        }
        if (mutableSlots && !serials.isEmpty()) {
            Collections.sort(serials);
            for (Component c : playersPanel.getComponents()) {
                CreateGamePlayerPanel playerPanel = (CreateGamePlayerPanel) c;
                PlayerSlot ps = playerPanel.getSlot();
                if (ps != null && ps.getSerial() != null) {
                    playerPanel.setSerialText(""
                            + (1 + serials.indexOf(ps.getSerial())));
                }
            }
        }

        if (mutableSlots) {
            startGameButton.setEnabled(anyPlayerAssigned);
        } else {
            startGameButton.setEnabled(allPlayersAssigned);
        }
    }

}
