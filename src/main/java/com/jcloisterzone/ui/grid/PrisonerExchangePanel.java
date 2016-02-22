package com.jcloisterzone.ui.grid;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.controls.PlayerPanelImageCache;
import com.jcloisterzone.ui.gtk.ThemedJLabel;
import com.jcloisterzone.ui.gtk.ThemedJPanel;

import static com.jcloisterzone.ui.I18nUtils._;

@InteractionPanel
public class PrisonerExchangePanel extends JPanel {
    final Client client;
    final GameController gc;
    final TowerCapability towerCap;
    final PlayerPanelImageCache cache;    

    private static final Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);
    public static Font FONT_HEADER = new Font(null, Font.BOLD, 18);

    private List<FollowerItem> options;

    public PrisonerExchangePanel(Client client, GameController gc, PlayerPanelImageCache cache) {
        this.client = client;
        this.gc = gc;
        this.cache = cache;
        this.towerCap = gc.getGame().getCapability(TowerCapability.class);

        setOpaque(true);
        setBackground(gc.getClient().getTheme().getTransparentPanelBg());
        setLayout(new MigLayout("ins 10", "[grow]", ""));

        JLabel label = new ThemedJLabel(_("Exchange prisoners"));
        label.setFont(FONT_HEADER);
        label.setForeground(gc.getClient().getTheme().getHeaderFontColor());
        add(label, "wrap");

        int lastPos = towerCap.getPrisoners().get(gc.getGame().getActivePlayer()).size() - 1;
        add(new FollowerItem(towerCap.getPrisoners().get(gc.getGame().getActivePlayer()).get(lastPos)), "wrap, gapbottom 5, growx, h 50");
        label = new ThemedJLabel(_("Choose your follower to exchange:"));
        add(label, "wrap");

        List<Follower> prisoners = towerCap.getPrisoners().get(towerCap.getPrisoners().get(gc.getGame().getActivePlayer()).get(lastPos).getPlayer());
        options = new ArrayList<FollowerItem>();
        for (Follower f : prisoners) {
            if (f.getPlayer() == gc.getGame().getActivePlayer()) {
                options.add(new FollowerItem(f));
                add(options.get(options.size() - 1), "wrap, gap 0, growx, h 50");
            }
        }
    }

    class FollowerItem extends ThemedJPanel {
        final Follower follower;

        public FollowerItem(Follower follower) {
            this.follower = follower;
            setOpaque(false);
            setBackground(TRANSPARENT_COLOR);
            setLayout(new MigLayout("ins 0", "", ""));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (follower.getPlayer() == gc.getGame().getActivePlayer()) {
                        gc.getRmiProxy().exchangePrisoners(follower.getClass());
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            Image img = cache.get(follower.getPlayer(), follower.getClass().getSimpleName());
            g2.drawImage(img, 20, 20, null);
        }
    }

    public void updateChoices() {
        
        for (FollowerItem o: options) {
            int lastPos = towerCap.getPrisoners().get(gc.getGame().getActivePlayer()).size() - 1;
            if (!towerCap.getPrisoners().get(towerCap.getPrisoners().get(gc.getGame().getActivePlayer()).get(lastPos).getPlayer()).contains(o.follower)) {
                remove(o);
            }
        }
    }
}
