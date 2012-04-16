package com.jcloisterzone.ui.grid;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import com.jcloisterzone.game.expansion.BazaarItem;
import com.jcloisterzone.game.expansion.BridgesCastlesBazaarsGame;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.controls.ControlPanel;
import com.jcloisterzone.ui.controls.FakeComponent;

public class BazaarPanel extends FakeComponent {

    public static final int PANEL_WIDTH = 250;

    private static Font FONT_HEADER = new Font(null, Font.BOLD, 18);

    final BridgesCastlesBazaarsGame bcb;

    //JButton bidButton = new JButton(_("Bid"));


    public BazaarPanel(Client client) {
       super(client);
       bcb = client.getGame().getBridgesCastlesBazaarsGame();

       //bidButton.setBounds(120, 60, 100, 30);
    }

    public void selectBazaarTile() {
        //
    }

    @Override
    public void paintComponent(Graphics2D g2) {
        super.paintComponent(g2);
        AffineTransform origTransform = g2.getTransform();

        GridPanel gp = client.getGridPanel();
        int h = gp.getHeight();

        g2.setColor(ControlPanel.PANEL_BG_COLOR);
        g2.fillRect(0 , 0, PANEL_WIDTH, h);

        g2.setColor(ControlPanel.HEADER_FONT_COLOR);
        g2.setFont(FONT_HEADER);
        g2.drawString(_("Bazaar auction"), 20, 30);

        g2.translate(20, 60);

        //int i = 0;
        for(BazaarItem bi : bcb.getBazaarSupply()) {
            //TOOD caceh supply images ??
            Image img =  client.getTileTheme().getTileImage(bi.getTile().getId());
            g2.drawImage(img, 0, 0, 100, 100, null);

//            if (i == 0) {
//                bidButton.paint(g2);
//            }
//            i++;
            g2.translate(0, 120);
        }



        g2.setTransform(origTransform);
    }

}
