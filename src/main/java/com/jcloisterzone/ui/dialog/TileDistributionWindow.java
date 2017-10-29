package com.jcloisterzone.ui.dialog;

import static com.jcloisterzone.ui.I18nUtils._tr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TilePackBuilder;
import com.jcloisterzone.board.TilePackBuilder.TileCount;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.WrapLayout;
import com.jcloisterzone.ui.gtk.ThemedJList;
import com.jcloisterzone.ui.gtk.ThemedJPanel;
import com.jcloisterzone.ui.resources.TileImage;
import com.jcloisterzone.ui.theme.Theme;


public class TileDistributionWindow extends JFrame {

    final Client client;
    final JScrollPane scrollPane;
    final JPanel content = new ThemedJPanel();
    final TilePackBuilder tilePackBuilder;

    private static Font FONT = new Font("Dialog", Font.PLAIN, 26);

    public static final int SIZE = 160;
    public static final int BANNER = 34;

    public TileDistributionWindow(Client client) {
        super(_tr("Tile Distribution"));
        this.client = client;

        tilePackBuilder = new TilePackBuilder();
        tilePackBuilder.setConfig(client.getConfig());

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        UiUtils.centerDialog(this, Math.min(client.getWidth(), 890), client.getHeight() - 40);
        getContentPane().setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(client.getTheme().getPanelBg());

        final JList<Expansion> list = new ThemedJList<Expansion>(getImplementedExpansions());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setPreferredSize(new Dimension(200, client.getHeight()));
        list.setSelectedValue(Expansion.BASIC, true);
        list.setBorder(new EmptyBorder(4,4,4,4));
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                fillContent(list.getSelectedValue());
            }

        });

        getContentPane().add(list, BorderLayout.WEST);

        content.setLayout(new WrapLayout(WrapLayout.LEFT, 3, 3));

        scrollPane = new JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        fillContent(Expansion.BASIC);
        setVisible(true);
    }

    private Expansion[] getImplementedExpansions() {
        List<Expansion> exps = new ArrayList<>();
        for (Expansion exp : Expansion.values()) {
            if (exp != Expansion.PHANTOM && exp != Expansion.LITTLE_BUILDINGS) {
                exps.add(exp);
            }
        }
        return exps.toArray(new Expansion[exps.size()]);
    }

    private void fillContent(Expansion exp) {
        content.removeAll();
        Dimension dim = new Dimension(SIZE, SIZE+BANNER);
        for (TileCount tc : tilePackBuilder.getExpansionTiles(exp)) {
            TileLabel tileLabel = new TileLabel(client.getTheme(), exp, tc);
            tileLabel.setPreferredSize(dim);
            content.add(tileLabel);
        }
        content.revalidate();
        scrollPane.repaint();
    }

    private class TileLabel extends JPanel {
        private final TileImage image;
        private final Theme theme;
        private String count;

        public TileLabel(Theme theme, Expansion exp, TileCount tc) {
            this.theme = theme;
            this.image = client.getResourceManager().getTileImage(tc.tileId, Rotation.R0);
            this.count = tc.count == null ? "" : tc.count + "";
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            if (image != null) {
                g2.drawImage(image.getImage(), 0, 0, SIZE, SIZE, this);
            }
            Color bgColor = theme.getTileDistCountBg();
            g2.setColor(bgColor == null ? Color.WHITE : bgColor);
            g2.fillRect(0, SIZE, SIZE, BANNER);
            Color textColor = theme.getTextColor();
            g2.setColor(textColor == null ? Color.BLACK : textColor);
            g2.setFont(FONT);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.drawString(count, SIZE/2, SIZE+BANNER-8);
        }
    }
}
