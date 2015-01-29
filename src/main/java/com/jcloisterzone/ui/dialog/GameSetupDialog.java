package com.jcloisterzone.ui.dialog;

import java.awt.Container;

import javax.swing.JDialog;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import com.google.common.base.Joiner;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.component.MultiLineLabel;

import static com.jcloisterzone.ui.I18nUtils._;

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
        MultiLineLabel lRules = new MultiLineLabel(joiner.join(game.getCustomRules()));
        pane.add(lRules, "wrap, w 600");
    }

}
