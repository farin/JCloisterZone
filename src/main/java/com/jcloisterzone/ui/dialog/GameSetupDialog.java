package com.jcloisterzone.ui.dialog;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;

import com.google.common.base.Joiner;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.component.MultiLineLabel;
import com.jcloisterzone.ui.gtk.ThemedJLabel;

import io.vavr.Tuple2;
import net.miginfocom.swing.MigLayout;

public class GameSetupDialog extends JDialog {

    public GameSetupDialog(Client client, Game game) {
        super(client);

        setTitle(_("Game setup"));
        UiUtils.centerDialog(this, 600, 400);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        Container pane = getContentPane();
        pane.setBackground(client.getTheme().getPanelBg());
        pane.setLayout(new MigLayout("", "[]", ""));

        Joiner joiner = Joiner.on(", ").skipNulls();
        pane.add(new ThemedJLabel(_("Expansions")), "wrap, w 600, gaptop 5");
        MultiLineLabel lExpansion = new MultiLineLabel(joiner.join(game.getSetup().getExpansions()));
        pane.add(lExpansion, "wrap, w 600");

        joiner = Joiner.on("\n").skipNulls();
        pane.add(new ThemedJLabel(_("Rules")), "wrap, w 600, gaptop 10");
        List<String> rules = new ArrayList<>();
        for (Tuple2<CustomRule, Object> t : game.getSetup().getRules()) {
            CustomRule rule = t._1;
            if (rule.getType().equals(Boolean.class)) {
                if (rule == CustomRule.RANDOM_SEATING_ORDER) continue;
                if (t._2.equals(Boolean.FALSE)) continue;
                rules.add(rule.getLabel());
            } else {
                rules.add(rule.getLabel() + " = " + t._2);
            }
        }

        MultiLineLabel lRules = new MultiLineLabel(joiner.join(rules));
        pane.add(lRules, "wrap, w 600");
    }

}
