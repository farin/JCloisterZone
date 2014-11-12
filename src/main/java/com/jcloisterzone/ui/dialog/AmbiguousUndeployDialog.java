package com.jcloisterzone.ui.dialog;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Color;
import java.awt.Container;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.UiUtils;

public class AmbiguousUndeployDialog extends JDialog {

    //private final Client client;

    private final int ICON_SIZE = 80;

    public AmbiguousUndeployDialog(Client client, List<MeeplePointer> pointers, final AmbiguousUndeployDialogEvent handler) {
        super(client);
        //this.client = client;

        setTitle(_("Undeploy meeple"));
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        Container pane = getContentPane();
        pane.setLayout(new MigLayout("nogrid", "", ""));

        pane.add(new JLabel(_("Select meeple to undeploy")), "wrap, gapbottom 15");

        int gridx = 0;
        for (final MeeplePointer pointer : pointers) {
            Color color = pointer.getMeepleOwner().getColors().getMeepleColor();
            Image img = client.getFigureTheme().getFigureImage(pointer.getMeepleType(), color, null);
            img = img.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
            JButton button = new JButton(new ImageIcon(img));
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    dispose();
                    handler.meepleTypeSelected(pointer);
                }
            });
            pane.add(button, "cell " + (gridx++) + " 1, height ::" + ICON_SIZE + ", width ::" + ICON_SIZE);
        }

        pack();
        UiUtils.centerDialog(this, getWidth(), getHeight());
        setVisible(true);
    }

    public static abstract class AmbiguousUndeployDialogEvent {
        public abstract void meepleTypeSelected(MeeplePointer meeple);
    }
}
