package com.jcloisterzone.ui.dialog;

import java.awt.Button;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.google.common.base.Objects;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.UiUtils;

import static com.jcloisterzone.ui.I18nUtils._;

public class PreferencesDialog extends JDialog {

    private Font hintFont = new Font(null, Font.ITALIC, 10);

    private final Client client;
    private final Config config;
    private String initialLocale;

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
        client.saveConfig();
    }

    public PreferencesDialog(Client client) {
        this.client = client;
        this.config = client.getConfig();
        setTitle(_("Preferences"));
        UiUtils.centerDialog(this, 600, 300);

        getContentPane().setLayout(new MigLayout("", "[]10px[]", "[][][]"));

        getContentPane().add(new JLabel(_("Language")), "alignx trailing");

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
        getContentPane().add(langComboBox, "wrap, growx");

        languageHint = new JLabel(_("To apply new language you must restart the application"));
        languageHint.setVisible(false);
        languageHint.setFont(hintFont);
        getContentPane().add(languageHint, "skip 1, wrap");

        getContentPane().add(new JLabel(_("AI placement delay (ms)")), "gaptop 10, alignx trailing");

        aiPlaceTileDelay = new JTextField();
        aiPlaceTileDelay.setText(valueOf(config.getAi_place_tile_delay()));
        getContentPane().add(aiPlaceTileDelay, "wrap, growx");

        getContentPane().add(new JLabel(_("Score display duration (sec)")), "alignx trailing");
        scoreDisplayDuration = new JTextField();
        scoreDisplayDuration.setText(valueOf(config.getScore_display_duration()));
        getContentPane().add(scoreDisplayDuration, "wrap, growx");

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
        getContentPane().add(buttonBox, "gaptop 5, spanx 2, growx");

        //TODO move config location here from About Dialog

        setVisible(true);
    }

}
