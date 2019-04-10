package com.jcloisterzone.ui.grid.actionpanel;

import static com.jcloisterzone.ui.I18nUtils._tr;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import com.jcloisterzone.action.FlockAction;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.gtk.ThemedJLabel;
import com.jcloisterzone.wsio.message.FlockMessage;
import com.jcloisterzone.wsio.message.FlockMessage.FlockOption;

import net.miginfocom.swing.MigLayout;

public class FlockPanel extends ActionInteractionPanel<FlockAction> {

	private JButton expandOption, scoreOption;


	public FlockPanel(Client client, GameController gc) {
		super(client, gc);
		setOpaque(true);
        setBackground(gc.getClient().getTheme().getTransparentPanelBg());
        setLayout(new MigLayout("ins 10 20 10 20", "[grow]", ""));

        JLabel label;

        label = new ThemedJLabel(_tr("Shepherd"));
        label.setFont(FONT_HEADER);
        label.setForeground(gc.getClient().getTheme().getHeaderFontColor());
        add(label, "wrap, gapbottom 10");

        label = new ThemedJLabel(_tr("Farm with shepherd was expanded. Select one choice."));
        add(label, "wrap, gapbottom 5");

        boolean isActive = gc.getGame().getState().getActivePlayer().isLocalHuman();

        expandOption = new JButton();
        expandOption.setText(_tr("Expand the flock of sheep."));
        expandOption.setEnabled(isActive);
        expandOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gc.getConnection().send(
                    new FlockMessage(FlockOption.EXPAND)
                );
            }
        });
        add(expandOption, "wrap, growx, h 40, gapbottom 5");

        scoreOption = new JButton();
        scoreOption.setText(_tr("Herd the flock into the stable."));
        scoreOption.setEnabled(isActive);
        scoreOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gc.getConnection().send(
                	new FlockMessage(FlockOption.SCORE)
                );
            }
        });
        add(scoreOption, "wrap, growx, h 40, gapbottom 5");

//        Tile tile = gc.getGame().getState().getLastPlaced().getTile();
//        String feature = Feature.getLocalizedNamefor(tile.getCornCircle());
//        label = new ThemedJLabel(_tr("on/from a {0}.", feature.toLowerCase()));
//        add(label, "wrap");
	}

}
