package com.jcloisterzone.ui.grid.actionpanel;

import static com.jcloisterzone.ui.I18nUtils._tr;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Function;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jcloisterzone.action.FlockAction;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Shepherd;
import com.jcloisterzone.game.capability.SheepCapability;
import com.jcloisterzone.game.capability.SheepCapability.SheepToken;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.gtk.ThemedJLabel;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;
import com.jcloisterzone.wsio.message.FlockMessage;
import com.jcloisterzone.wsio.message.FlockMessage.FlockOption;

import io.vavr.collection.List;
import io.vavr.collection.Map;
import net.miginfocom.swing.MigLayout;

public class FlockPanel extends ActionInteractionPanel<FlockAction> {

	private static final Color TRANSPARENT = new Color(0,0,0,0);

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

        label = new ThemedJLabel(_tr("Farm with shepherd was expanded."));
        add(label, "wrap, gapbottom 5");

        GameState state = gc.getGame().getState();
        Shepherd shepherd = (Shepherd) state.getTurnPlayer().getSpecialMeeples(state).find(m -> m instanceof Shepherd).getOrNull();
		FeaturePointer shepherdFp = shepherd.getDeployment(state);
		Farm farm = (Farm) state.getFeature(shepherdFp);
		Map<Meeple, FeaturePointer> shephersOnFarm = state.getDeployedMeeples().filter((m, fp) -> m instanceof Shepherd && farm.getPlaces().contains(fp));

		int i = 0;
		JPanel farmContent = new JPanel();
		farmContent.setOpaque(true);
		farmContent.setBackground(TRANSPARENT);
		farmContent.setLayout(new MigLayout("ins 5 5 5 5", "[][][][][]", ""));
		for (Meeple m : shephersOnFarm.keySet()) {
			 Color color = m.getPlayer().getColors().getMeepleColor();
	         LayeredImageDescriptor lid = new LayeredImageDescriptor(m.getClass(), color);
	         Image image = client.getResourceManager().getLayeredImage(lid);
	         farmContent.add(createIconLabel(image), (++i % 5 == 0) ? "wrap": "");
		}

		Map<FeaturePointer, List<SheepToken>> placedTokens = state.getCapabilityModel(SheepCapability.class);
		int points = 0;
		for (SheepToken token : shephersOnFarm.values().flatMap(fp -> placedTokens.get(fp).get())) {
			points += token.sheepCount();
			Image image = client.getResourceManager().getImage("neutral/" + token.name().toLowerCase());
			farmContent.add(createIconLabel(image), (++i % 5 == 0) ? "wrap": "");
		}
		add(farmContent, "wrap, gapbottom 5");


        label = new ThemedJLabel(_tr("You can draw another sheep/wolf token from a bag."));
        add(label, "wrap, gapbottom 0");

        int remainingTokens = SheepCapability.TOKENS_COUNT - placedTokens.values().flatMap(Function.identity()).map(SheepToken::sheepCount).sum().intValue();
        label = new ThemedJLabel(_tr("({0} tokens left in bag)", remainingTokens));
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

        label = new ThemedJLabel(_tr("Or score the flock for {0} points.", points));
        add(label, "wrap, gapbottom 5");

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
	}

	private JLabel createIconLabel(Image image) {
		image = image.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
		JLabel icon = new JLabel(new ImageIcon(image));
        icon.setOpaque(true);
        icon.setBackground(TRANSPARENT);
        return icon;
	}
}
