package com.jcloisterzone.ui.dialog;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;

import com.google.common.base.Objects;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.plugin.Plugin;
import com.jcloisterzone.ui.plugin.PluginType;

import static com.jcloisterzone.ui.I18nUtils._;

public class PreferencesDialog extends JDialog {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private Font hintFont = new Font(null, Font.ITALIC, 10);

    private final Client client;
    private final Config config;
    private String initialLocale;

    private JPanel[] tabs;
    private JPanel visibleTab;

    private JLabel languageHint;

    private JComboBox<LocaleOption> langComboBox;
    private JTextField aiPlaceTileDelay;
    private JTextField scoreDisplayDuration;

    private static class LocaleOption {
        private final String locale, title;

        public LocaleOption(String locale, String title) {
            this.locale = locale;
            this.title = title;
        }

        public String getLocale() {
            return locale;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private void initLocaleOptions(JComboBox<LocaleOption> comboBox) {
        ArrayList<LocaleOption> result = new ArrayList<>();
        result.add(new LocaleOption(null, _("Use system language")));
        result.add(new LocaleOption("cs", "čeština (cs)"));
        result.add(new LocaleOption("de", "deutch (de)"));
        result.add(new LocaleOption("el", "ελληνικά (el)"));
        result.add(new LocaleOption("en", "english (en)"));
        result.add(new LocaleOption("es", "español (es)"));
        result.add(new LocaleOption("fr", "français (fr)"));
        result.add(new LocaleOption("hu", "magyar (hu)"));
        result.add(new LocaleOption("it", "italiano (it)"));
        result.add(new LocaleOption("nl", "nederlands (nl)"));
        result.add(new LocaleOption("pl", "polski (pl)"));
        result.add(new LocaleOption("ro", "român (ro)"));
        result.add(new LocaleOption("ru", "русский (ru)"));
        result.add(new LocaleOption("sk", "slovenčina (sk)"));

        boolean match = false;
        for (LocaleOption opt : result) {
            comboBox.addItem(opt);
            if (Objects.equal(opt.getLocale(), config.getLocale())) {
                comboBox.setSelectedItem(opt);
                match = true;
            }
        }
        if (!match) {
            LocaleOption unknown = new LocaleOption(config.getLocale(), config.getLocale());
            comboBox.addItem(unknown);
            comboBox.setSelectedItem(unknown);
        }
        initialLocale = config.getLocale();
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
        LocaleOption opt = (LocaleOption) langComboBox.getSelectedItem();
        config.setLocale(opt.getLocale());
        //TODO error handling
        config.setAi_place_tile_delay(intValue(aiPlaceTileDelay.getText()));
        config.setScore_display_duration(intValue(scoreDisplayDuration.getText()));

        List<String> enabledPlugins = new ArrayList<>();
        for (Plugin plugin : client.getPlugins()) {
            if (plugin.isEnabled() || plugin.getType() == PluginType.DEFAULT_GRF_SET) {
                enabledPlugins.add(plugin.getRelativePath());
            }
        }
        config.setPlugins(enabledPlugins);

        client.saveConfig();
    }

    private JPanel createInerfaceTab() {
        JPanel panel = new JPanel(new MigLayout("", "[]10px[]", ""));

        panel.add(new JLabel(_("Language")), "alignx trailing");

        langComboBox = new JComboBox<LocaleOption>();
        langComboBox.setEditable(false);
        initLocaleOptions(langComboBox);
        langComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                @SuppressWarnings("unchecked")
                JComboBox<LocaleOption> comboBox = (JComboBox<LocaleOption>) e.getSource();
                LocaleOption opt = (LocaleOption) comboBox.getSelectedItem();
                languageHint.setVisible(opt.getLocale() != initialLocale);
            }
        });
        panel.add(langComboBox, "wrap, growx");

        languageHint = new JLabel(_("To apply new language you must restart the application"));
        languageHint.setVisible(false);
        languageHint.setFont(hintFont);
        panel.add(languageHint, "skip 1, wrap");

        panel.add(new JLabel(_("AI placement delay (ms)")), "gaptop 10, alignx trailing");

        aiPlaceTileDelay = new JTextField();
        aiPlaceTileDelay.setText(valueOf(config.getAi_place_tile_delay()));
        panel.add(aiPlaceTileDelay, "wrap, growx");

        panel.add(new JLabel(_("Score display duration (sec)")), "alignx trailing");
        scoreDisplayDuration = new JTextField();
        scoreDisplayDuration.setText(valueOf(config.getScore_display_duration()));
        panel.add(scoreDisplayDuration, "wrap, growx");

        return panel;
    }

    private class PluginRow {
        private final Plugin plugin;

        public PluginRow(Plugin plugin) {
           this.plugin = plugin;
        }

        public void render(JPanel panel) {
            JCheckBox chbox = new JCheckBox();
            chbox.setSelected(plugin.isEnabled());
            if (plugin.getType() == PluginType.DEFAULT_GRF_SET) {
                plugin.setEnabled(false);
            }
            chbox.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 JCheckBox chbox = (JCheckBox) e.getSource();
                 plugin.setEnabled(chbox.isSelected());
             }
            });
            panel.add(chbox, "");

            JLabel label = new JLabel(plugin.getTitle());
            panel.add(label, "wrap");
        }
    }

    private JPanel createPluginsTab() {
        JPanel panel = new JPanel(new MigLayout());

        ArrayList<Plugin> arr = new ArrayList<Plugin>(client.getPlugins());
        ListIterator<Plugin> li = arr.listIterator(arr.size());

        // Iterate in reverse.
        while(li.hasPrevious()) {
          (new PluginRow(li.previous())).render(panel);
        }

        return panel;
    }

    public PreferencesDialog(Client client) {
        super(client);
        this.client = client;
        this.config = client.getConfig();
        setTitle(_("Preferences"));
        setModalityType(ModalityType.DOCUMENT_MODAL);
        UiUtils.centerDialog(this, 600, 300);

        getContentPane().setLayout(new MigLayout("ins 0", "[][grow]", "[grow][]"));

        tabs = new JPanel[] {
           createInerfaceTab(),
           createPluginsTab()
        };
        visibleTab = tabs[0];

        JList<String> tabList = new JList<String>(new String[] {_("Interface"), _("Plugins")});
        tabList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabList.setLayoutOrientation(JList.VERTICAL);
        tabList.setSelectedIndex(0);
        tabList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
               JList tabList = (JList) e.getSource();
               int idx = tabList.getSelectedIndex();
               getContentPane().remove(visibleTab);
               visibleTab = tabs[idx];
               getContentPane().add(visibleTab, "cell 1 0, aligny top");
               revalidate();
               repaint();
            }
        });
        getContentPane().add(tabList, "cell 0 0, growy, w 160");
        getContentPane().add(visibleTab, "cell 1 0, aligny top");

        JPanel buttonBox = new JPanel(new MigLayout("fill", "[grow][][]", "[]"));
        JButton cancel = new JButton(_("Cancel"));
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        buttonBox.add(cancel, "skip 1");
        JButton ok = new JButton(_("Save"));
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
