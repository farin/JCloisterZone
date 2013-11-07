package com.jcloisterzone.ui.dialog;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.ui.Client;

public class AmbiguousUndeployDialog extends JDialog {

    //private final Client client;

    private final int ICON_SIZE = 80;

    public AmbiguousUndeployDialog(Client client, List<Meeple> meeples, final AmbiguousUndeployDialogEvent handler) {
        super(client);
        //this.client = client;

        setTitle(_("Undeploy meeple"));
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        Container pane = getContentPane();
        pane.setLayout(new MigLayout("nogrid", "", ""));

        pane.add(new JLabel(_("Select meeple to undeploy")), "wrap, gapbottom 15");

        int gridx = 0;
        for (final Meeple meeple : meeples) {
            Color color = client.getPlayerColor(meeple.getPlayer());
            Image img = client.getFigureTheme().getFigureImage(meeple.getClass(), color, null);
            img = img.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
            JButton button = new JButton(new ImageIcon(img));
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    dispose();
                    handler.meepleTypeSelected(meeple);
                }
            });
            pane.add(button, "cell " + (gridx++) + " 1, height ::" + ICON_SIZE + ", width ::" + ICON_SIZE);
        }

        pack();
        centerDialog(getWidth(), getHeight());
        setVisible(true);
    }

    public static abstract class AmbiguousUndeployDialogEvent {
        public abstract void meepleTypeSelected(Meeple meeple);
    }

    //TODO copy&paste from About dialog
    private void centerDialog(int width, int height) {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();
        int screenHeight = screenSize.height;
        int screenWidth = screenSize.width;
        setBounds(screenWidth / 2 - width / 2, screenHeight / 3 - height / 2, width, height);

    }

}
