package com.jcloisterzone.ui.dialog;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JDialog;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import com.google.common.base.Joiner;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.component.MultiLineLabel;

public class GameSetupDialog extends JDialog {

    public GameSetupDialog(Client client, Game game) {
        super(client);

        setTitle(_("Game setup"));
        UiUtils.centerDialog(this, 600, 400);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        Container pane = getContentPane();
        pane.setLayout(new MigLayout("", "[]", ""));

        Joiner joiner = Joiner.on(", ").skipNulls();
        pane.add(new JLabel(_("Expansions")), "wrap, w 600, gaptop 5");
        MultiLineLabel lExpansion = new MultiLineLabel(joiner.join(game.getExpansions()));
        pane.add(lExpansion, "wrap, w 600");

        joiner = Joiner.on("\n").skipNulls();
        pane.add(new JLabel(_("Rules")), "wrap, w 600, gaptop 10");
        List<String> rules = new ArrayList<>();
        for (Entry<CustomRule, Object> entry : game.getCustomRules().entrySet()) {
            CustomRule rule = entry.getKey();
            if (rule.getType().equals(Boolean.class)) {
                if (rule == CustomRule.RANDOM_SEATING_ORDER) continue;
                if (entry.getValue().equals(Boolean.FALSE)) continue;
                rules.add(rule.getLabel());
            } else {
                rules.add(rule.getLabel() + " = " + entry.getValue());
            }
        }

        MultiLineLabel lRules = new MultiLineLabel(joiner.join(rules));
        pane.add(lRules, "wrap, w 600");
    }

}
