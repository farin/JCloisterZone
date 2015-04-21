package com.jcloisterzone.ui.dialog;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Container;
import java.awt.Image;
import java.awt.Point;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import com.jcloisterzone.board.Tile;
import com.jcloisterzone.ui.Client;

public class DiscardedTilesDialog extends JDialog {

    public static final int ICON_SIZE = 120;

    private final Client client;
    private JPanel panel;
    private JScrollPane scroll;

    public DiscardedTilesDialog(Client client) {
        super(client);
        this.client = client;

        setTitle(_("Discarded tiles"));
        Point p = client.getLocation();
        setLocation(p.x+200,p.y+150);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        Container pane = getContentPane();
        pane.setLayout(new MigLayout("", "[grow]", "[][]"));
        pane.add(new JLabel(_("These tiles have been discarded during the game")), "wrap, growx, gapbottom 10");

        panel = new JPanel();
        scroll = new JScrollPane(panel);
        pane.add(scroll, "wrap, grow, width 400::, height 150::");
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        panel.setLayout(new MigLayout("", "[]", ""));
        pack();
    }

    public void addTile(Tile tile) {
        Image icon = client.getResourceManager().getTileImage(tile).getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_FAST);
        panel.add(new JLabel(new ImageIcon(icon)), "");
        scroll.getViewport().setViewPosition(new Point(panel.getWidth(), 0));
    }

}

