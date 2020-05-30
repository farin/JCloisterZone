package com.jcloisterzone.ui.dialog;

import static com.jcloisterzone.ui.I18nUtils._tr;

import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.plugin.Plugin;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.component.MultiLineLabel;
import com.jcloisterzone.ui.component.StrechIconPanel;
import com.jcloisterzone.ui.gtk.ThemedJCheckBox;
import com.jcloisterzone.ui.gtk.ThemedJLabel;
import com.jcloisterzone.ui.gtk.ThemedJList;
import com.jcloisterzone.ui.gtk.ThemedJPanel;

import net.miginfocom.swing.MigLayout;

public class PreferencesDialog extends JDialog {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private static final Font HINT_FONT = new Font(null, Font.ITALIC, 10);
    private static final Font PLUGIN_DESCRIPTION_FONT = new Font(null, Font.ITALIC, 11);
    private static final Font PLUGIN_TITLE_FONT = new Font(null, Font.BOLD, 12);

    private final Client client;
    private final Config config;
    private String initialLocale;
    private String initialTheme;

    private JComponent[] tabs;
    private JComponent visibleTab;

    private JLabel languageHint;
    private JLabel themeHint;

    private JComboBox<StringOption> langComboBox;
    private JComboBox<StringOption> themeComboBox;
    private JComboBox<EnumOption<Config.TileRotationControls>> mousePlacementComboBox;
    private JTextField aiPlaceTileDelay;
    private JTextField scoreDisplayDuration;
    private List<PluginModel> pluginRows = new ArrayList<>();

    private static class StringOption {
        private final String key, title;

        public StringOption(String key, String title) {
            this.key = key;
            this.title = title;
        }

        public String getKey() {
            return key;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private static class EnumOption<T extends Enum> {
        private final String title;
        private final T key;

        public EnumOption(T key, String title) {
            this.key = key;
            this.title = title;
        }

        public T getKey() {
            return key;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private void initLocaleOptions(JComboBox<StringOption> comboBox) {
        ArrayList<StringOption> result = new ArrayList<>();
        result.add(new StringOption(null, _tr("Use system language")));
        result.add(new StringOption("ca", "català (ca)"));
        result.add(new StringOption("cs", "čeština (cs)"));
        result.add(new StringOption("de", "deutch (de)"));
        result.add(new StringOption("el", "ελληνικά (el)"));
        result.add(new StringOption("en", "english (en)"));
        result.add(new StringOption("es", "español (es)"));
        result.add(new StringOption("fr", "français (fr)"));
        result.add(new StringOption("hu", "magyar (hu)"));
        result.add(new StringOption("it", "italiano (it)"));
        result.add(new StringOption("ja", "日本語 (ja)"));
        result.add(new StringOption("nl", "nederlands (nl)"));
        result.add(new StringOption("pl", "polski (pl)"));
        result.add(new StringOption("ro", "român (ro)"));
        result.add(new StringOption("ru", "русский (ru)"));
        result.add(new StringOption("sk", "slovenčina (sk)"));
        result.add(new StringOption("zh", "中文 (zh)"));

        boolean match = false;
        for (StringOption opt : result) {
            comboBox.addItem(opt);
            if (Objects.equal(opt.getKey(), config.getLocale())) {
                comboBox.setSelectedItem(opt);
                match = true;
            }
        }
        if (!match) {
            StringOption unknown = new StringOption(config.getLocale(), config.getLocale());
            comboBox.addItem(unknown);
            comboBox.setSelectedItem(unknown);
        }
        initialLocale = config.getLocale();
    }

    private void initThemeOptions(JComboBox<StringOption> comboBox) {
        ArrayList<StringOption> result = new ArrayList<>();
        result.add(new StringOption("light", "Light"));
        result.add(new StringOption("dark", "Dark"));

        boolean match = false;
        for (StringOption opt : result) {
            comboBox.addItem(opt);
            if (Objects.equal(opt.getKey(), config.getTheme())) {
                comboBox.setSelectedItem(opt);
                match = true;
            }
        }
        if (!match) {
            comboBox.setSelectedItem(result.get(0));
        }
        initialTheme = config.getTheme();
    }

    private void initRotateBasedOnMousePositionOptions(JComboBox<EnumOption<Config.TileRotationControls>> comboBox) {
        List<EnumOption> result = new ArrayList<>();
        result.add(new EnumOption(Config.TileRotationControls.TAB_RCLICK, _tr("TAB / right click")));
        result.add(new EnumOption(Config.TileRotationControls.TAB_RCLICK_MOUSEMOVE, _tr("+ mouse over spot") + " " + _tr("(experimental)")));

        for (EnumOption<Config.TileRotationControls> opt : result) {
            comboBox.addItem(opt);
            if (opt.getKey() == config.getTile_rotation()) {
                comboBox.setSelectedItem(opt);
            }
        }
    }

    private String valueOf(Object obj) {
        if (obj == null) return "";
        return obj.toString();
    }

    private Integer intValue(String value) {
        value = value.trim();
        if ("".equals(value)) return null;
        return Integer.parseInt(value);
    }

    private void save() {
        StringOption opt = (StringOption) langComboBox.getSelectedItem();
        config.setLocale(opt.getKey());
        opt = (StringOption) themeComboBox.getSelectedItem();
        config.setTheme(opt.getKey());
        EnumOption<Config.TileRotationControls> mousePlacementOption = (EnumOption<Config.TileRotationControls>) mousePlacementComboBox.getSelectedItem();
        config.setTile_rotation(mousePlacementOption.key);
        //TODO error handling
        config.getAi().setPlace_tile_delay(intValue(aiPlaceTileDelay.getText()));
        config.setScore_display_duration(intValue(scoreDisplayDuration.getText()));

        List<String> enabledPlugins = new ArrayList<>();
        for (PluginModel row : pluginRows) {
            Plugin plugin = row.plugin;
            if (row.isEnabled()) {
                try {
                    plugin.load();
                    plugin.setEnabled(true);
                    enabledPlugins.add(plugin.getRelativePath().toString());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            } else {
                plugin.unload();
                plugin.setEnabled(false);
            }
        }
        config.getPlugins().setEnabled_plugins(enabledPlugins);

        client.saveConfig();
    }

    private JPanel createInerfaceTab() {
        JPanel panel = new ThemedJPanel(new MigLayout("", "[]10px[]", ""));

        panel.add(new ThemedJLabel(_tr("Language")), "alignx trailing");

        langComboBox = new JComboBox<StringOption>();
        langComboBox.setEditable(false);
        initLocaleOptions(langComboBox);
        langComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                @SuppressWarnings("unchecked")
                JComboBox<StringOption> comboBox = (JComboBox<StringOption>) e.getSource();
                StringOption opt = (StringOption) comboBox.getSelectedItem();
                languageHint.setVisible(!Objects.equal(opt.getKey(), initialLocale));
            }
        });
        panel.add(langComboBox, "wrap, growx");

        languageHint = new ThemedJLabel(_tr("To apply new language you must restart the application"));
        languageHint.setVisible(false);
        languageHint.setFont(HINT_FONT);
        languageHint.setForeground(client.getTheme().getHintColor());
        panel.add(languageHint, "sx 2, wrap");

        panel.add(new ThemedJLabel(_tr("Theme")), "alignx trailing");

        themeComboBox = new JComboBox<StringOption>();
        themeComboBox.setEditable(false);
        initThemeOptions(themeComboBox);
        themeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                @SuppressWarnings("unchecked")
                JComboBox<StringOption> comboBox = (JComboBox<StringOption>) e.getSource();
                StringOption opt = (StringOption) comboBox.getSelectedItem();
                themeHint.setVisible(!Objects.equal(opt.getKey(), initialTheme));
            }
        });
        panel.add(themeComboBox, "wrap, growx");

        themeHint = new ThemedJLabel(_tr("To apply new theme you must restart the application"));
        themeHint.setVisible(false);
        themeHint.setFont(HINT_FONT);
        themeHint.setForeground(client.getTheme().getHintColor());
        panel.add(themeHint, "sx 2, wrap");

        panel.add(new ThemedJLabel(_tr("Tile rotation")), "alignx trailing");

        mousePlacementComboBox = new JComboBox();
        initRotateBasedOnMousePositionOptions(mousePlacementComboBox);
        panel.add(mousePlacementComboBox, "wrap, growx");

        panel.add(new ThemedJLabel(_tr("AI placement delay (ms)")), "gaptop 10, alignx trailing");

        aiPlaceTileDelay = new JTextField();
        aiPlaceTileDelay.setText(valueOf(config.getAi().getPlace_tile_delay()));
        panel.add(aiPlaceTileDelay, "wrap, growx");

        panel.add(new ThemedJLabel(_tr("Score display duration (sec)")), "alignx trailing");
        scoreDisplayDuration = new JTextField();
        scoreDisplayDuration.setText(valueOf(config.getScore_display_duration()));
        panel.add(scoreDisplayDuration, "wrap, growx");

        return panel;
    }

    private class PluginModel {
        private final Plugin plugin;
        private boolean enabled;
        private Image icon;

        public PluginModel(Plugin plugin) {
           this.plugin = plugin;
           enabled = plugin.isEnabled();
           icon = plugin.getIcon();
        }

        public Image getIcon() {
            return icon;
        }

        public String getTitle() {
            return plugin.getTitle();
        }

        public String getDescription() {
            return plugin.getDescription();
        }

        public boolean isReadOnly() {
           return plugin.isDefault();
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    private class PluginPanel extends ThemedJPanel {
        private final PluginModel model;
        private JCheckBox chbox;

        public PluginPanel(PluginModel model) {
            super(new MigLayout("fillx", "[][][grow]"));
            this.model = model;

            add(new StrechIconPanel(model.getIcon()), "w 120!, h 120!, sy 2, gapright 10");

            chbox = new ThemedJCheckBox();
            chbox.setSelected(model.isEnabled());
            if (model.isReadOnly()) {
                chbox.setEnabled(false);
            } else {
                chbox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        PluginPanel.this.model.setEnabled(chbox.isSelected());
                    }
                });
            }
            add(chbox, "");

            JLabel label = new ThemedJLabel(model.getTitle());
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setFont(PLUGIN_TITLE_FONT);
            add(label, "alignx left, wrap");


            MultiLineLabel desc = new MultiLineLabel(model.getDescription());
            desc.setFont(PLUGIN_DESCRIPTION_FONT);
            add(desc, "sx 2, growx, aligny top");
        }
    }

    private JComponent createPluginsTab() {
        JPanel panel = new ThemedJPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        ArrayList<Plugin> arr = new ArrayList<Plugin>(client.getPlugins());
        ListIterator<Plugin> li = arr.listIterator(arr.size());

        // Iterate in reverse.
        while(li.hasPrevious()) {
            PluginModel row = new PluginModel(li.previous());
            //row.render(panel);
            pluginRows.add(row);
            panel.add(new PluginPanel(row));
        }

        JScrollPane scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return scrollPane;
    }

    public PreferencesDialog(Client client) {
        super(client);
        this.client = client;
        this.config = client.getConfig();
        setTitle(_tr("Preferences"));
        setModalityType(ModalityType.DOCUMENT_MODAL);
        UiUtils.centerDialog(this, 650, Math.min(client.getHeight(), 600));

        getContentPane().setLayout(new MigLayout("ins 0", "[][grow]", "[grow][]"));
        getContentPane().setBackground(client.getTheme().getPanelBg());

        tabs = new JComponent[] {
           createInerfaceTab(),
           createPluginsTab()
        };
        visibleTab = tabs[0];

        JList<String> tabList = new ThemedJList<String>(new String[] {_tr("Interface"), _tr("Plugins")});
        tabList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabList.setLayoutOrientation(JList.VERTICAL);
        tabList.setSelectedIndex(0);
        tabList.setBorder(new EmptyBorder(4,4,4,4));
        tabList.addListSelectionListener(new ListSelectionListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void valueChanged(ListSelectionEvent e) {
               JList<String> tabList = (JList<String>) e.getSource();
               int idx = tabList.getSelectedIndex();
               getContentPane().remove(visibleTab);
               visibleTab = tabs[idx];
               getContentPane().add(visibleTab, "cell 1 0, aligny top, grow");
               revalidate();
               repaint();
            }
        });
        getContentPane().add(tabList, "cell 0 0, growy, w 160!");
        getContentPane().add(visibleTab, "cell 1 0, aligny top, grow");

        JPanel buttonBox = new ThemedJPanel(new MigLayout("fill", "[grow][][]", "[]"));
        JButton cancel = new JButton(_tr("Cancel"));
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        buttonBox.add(cancel, "skip 1");
        JButton ok = new JButton(_tr("Save"));
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
                dispose();
            }
        });
        buttonBox.add(ok, "");
        getContentPane().add(buttonBox, "cell 0 1,gaptop 5, spanx 2, growx");

        //TODO move config location here from About Dialog

        setVisible(true);
    }

}
