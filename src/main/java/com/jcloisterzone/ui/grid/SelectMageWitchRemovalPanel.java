package com.jcloisterzone.ui.grid;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.jcloisterzone.figure.neutral.Mage;
import com.jcloisterzone.figure.neutral.Witch;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.component.MultiLineLabel;
import com.jcloisterzone.ui.controls.ControlPanel;

public class SelectMageWitchRemovalPanel extends JPanel {


    public SelectMageWitchRemovalPanel(final GameController gc) {
        setOpaque(true);
        setBackground(ControlPanel.PANEL_BG_COLOR);
        setLayout(new MigLayout("ins 10 20 10 20", "[grow]", ""));

        JLabel label;

        label = new JLabel(_("Mage and Witch"));
        label.setFont(CornCirclesPanel.FONT_HEADER);
        label.setForeground(ControlPanel.HEADER_FONT_COLOR);
        add(label, "wrap, gapbottom 10");

        MultiLineLabel mll = new MultiLineLabel(_("It's not possible to place mage or witch because there isn't an unfinished feature. Select what figure do you want to remove from board?"));
        add(mll, "wrap, growx, gapbottom 5");

        boolean isActive = gc.getGame().getActivePlayer().isLocalHuman();

        JButton btn = new JButton();
        btn.setText(_("Remove the mage."));
        btn.setEnabled(isActive);
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((JButton)e.getSource()).setEnabled(false);
                gc.getRmiProxy().moveNeutralFigure(null, Mage.class);
            }
        });
        add(btn, "wrap, growx, h 40, gapbottom 5");

        btn = new JButton();
        btn.setText(_("Remove the witch."));
        btn.setEnabled(isActive);
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((JButton)e.getSource()).setEnabled(false);
                gc.getRmiProxy().moveNeutralFigure(null, Witch.class);
            }
        });
        add(btn, "wrap, growx, h 40, gapbottom 5");
    }

}
