package com.jcloisterzone.ui.grid;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.expansion.BazaarItem;
import com.jcloisterzone.game.expansion.BridgesCastlesBazaarsGame;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.controls.ControlPanel;
import com.jcloisterzone.ui.controls.FakeComponent;
import com.jcloisterzone.ui.controls.MouseListeningRegion;
import com.jcloisterzone.ui.controls.RegionMouseListener;

public class BazaarPanel extends FakeComponent implements RegionMouseListener {

    public static final int PANEL_WIDTH = 250;

    private static Font FONT_HEADER = new Font(null, Font.BOLD, 18);
    private static Font FONT_ACTION = new Font(null, Font.PLAIN, 14);
//
//    private static String BID_LABEL = _("Bid");
//    private static int BID_LABEL_WIDTH;

//    static {
//        Map<TextAttribute, Integer> fontAttributes = new HashMap<TextAttribute, Integer>();
//        fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
//        FONT_BID = (new Font(null, Font.BOLD, 24)).deriveFont(fontAttributes);
//    }

    final BridgesCastlesBazaarsGame bcb;

    private int selectedItem = -1;
    private boolean selectable;
    private boolean bidable;

    private boolean refreshMouseRegions;

    private JButton bidButton;
    private JSpinner bidAmount;
    private SpinnerNumberModel bidAmountModel;


    public BazaarPanel(Client client) {
       super(client);
       bcb = client.getGame().getBridgesCastlesBazaarsGame();

//       FontMetrics fm = client.getFontMetrics(FONT_BID);
//       BID_LABEL_WIDTH = fm.stringWidth(BID_LABEL);

       bidAmountModel = new SpinnerNumberModel(0,0,1,1);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        refreshMouseRegions = true;
        computeBidButtonBounds();
        client.getGridPanel().repaint();
    }

    @Override
    public void registerSwingComponents(JComponent parent) {
         bidButton = new JButton(_("Bid"));
         bidButton.setFont(new Font(null, Font.BOLD, 18));
         bidButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                assert bidable;
                client.getServer().bazaarBid(selectedItem, bidAmountModel.getNumber().intValue());
            }
         });
         bidButton.setVisible(false);
         parent.add(bidButton);

         bidAmount = new JSpinner(bidAmountModel);
         bidAmount.setFont(new Font(null, Font.BOLD, 14));
         bidAmount.setVisible(false);

         parent.add(bidAmount);
    }


    public void setSelectable(boolean selectable) {
        refreshMouseRegions = true;
        this.selectable = selectable;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setBidable(boolean bidable) {
        this.bidable = bidable;
        updateBidRange();
        computeBidButtonBounds();
    }

    private void computeBidButtonBounds() {
        if (!bidable || selectedItem == -1) {
            bidButton.setVisible(false);
            bidAmount.setVisible(false);
            return;
        }

        //TODO hardcoded offset - but no better solution for now
        int bazaarPanelX = client.getGridPanel().getWidth()-ControlPanel.PANEL_WIDTH-BazaarPanel.PANEL_WIDTH-60;
        int width = BazaarPanel.PANEL_WIDTH-160;
        bidButton.setBounds(bazaarPanelX+140, 120+110*selectedItem, width, 30);
        bidButton.setVisible(true);

        bidAmount.setBounds(bazaarPanelX+170, 85+110*selectedItem, width-30, 25);
        bidAmount.setVisible(true);
    }

    public boolean isBidable() {
        return bidable;
    }


    private void updateBidRange() {
        int points = client.getGame().getActivePlayer().getPoints();
        //int points = 0; //DEBUG ONLY

        bidAmountModel.setMaximum(points);
        if (bcb.getCurrentBazaarAuction() == null) {
            bidAmountModel.setMinimum(0);
            bidAmountModel.setValue(0);
        } else {
            int min = bcb.getCurrentBazaarAuction().getCurrentPrice()+1;
            bidAmountModel.setMinimum(min);
            bidAmountModel.setValue(min);
        }
    }


    public void setSelectedItem(int selectedItem) {
        this.selectedItem = selectedItem;
        computeBidButtonBounds();
    }

    public int getSelectedItem() {
        return selectedItem;
    }

    public void forward() {
        if (selectable) {
            selectedItem++;
            if (selectedItem == bcb.getBazaarSupply().length) {
                selectedItem = 0;
            }
            computeBidButtonBounds();
            client.getGridPanel().repaint();
        }
    }

    public void backward() {
        if (selectable) {
            selectedItem--;
            if (selectedItem == 0) {
                selectedItem = bcb.getBazaarSupply().length-1;
            }
            client.getGridPanel().repaint();
        }
    }


    @Override
    public void paintComponent(Graphics2D g2) {
        super.paintComponent(g2);

        GridPanel gp = client.getGridPanel();
        int h = gp.getHeight();

        g2.setColor(ControlPanel.PANEL_BG_COLOR);
        g2.fillRect(0 , 0, PANEL_WIDTH, h);

        g2.setColor(ControlPanel.HEADER_FONT_COLOR);
        g2.setFont(FONT_HEADER);
        g2.drawString(_("Bazaar supply"), 20, 27);

        g2.setColor(Color.BLACK);
        g2.setFont(FONT_ACTION);
        String hint = null;
        if (selectable) {
            hint = _("Select tile for auction.");
        } else {
            if (bidable) {
                hint = _("Place your offer or pass.");
            }
        }

        if (hint != null) {
            g2.drawString(hint, 20, 55);
        }


        int y = 75;

        if (refreshMouseRegions) {
            getMouseRegions().clear();
        }

        //System.out.println("B " + isTransormChanged() + " " + g2.getTransform());
        //System.out.println(g2.getTransform());

        int i = 0;
        for(BazaarItem bi : bcb.getBazaarSupply()) {
            //TOOD caceh supply images ??
            Image img =  client.getTileTheme().getTileImage(bi.getTile().getId());

            if (selectedItem == i) {
                g2.setColor(ControlPanel.PLAYER_BG_COLOR);
                g2.fillRect(0, y-1, BazaarPanel.PANEL_WIDTH, 92);
            }

            if (refreshMouseRegions && selectable) {
                getMouseRegions().add(new MouseListeningRegion(new Rectangle(0, y-1, BazaarPanel.PANEL_WIDTH, 102), this, i));
            }

            g2.drawImage(img, 20, y, 90, 90, null);

            if (bi.getCurrentBidder() != null) {
                Image playerImage = client.getFigureTheme().getFigureImage(SmallFollower.class, client.getPlayerColor(bi.getCurrentBidder()), null);
                //TODO smooth image
                g2.drawImage(playerImage, 130, y+12, 32, 32, null);
//                g2.setColor(Color.BLACK);
//                g2.drawString(bi.getCurrentPrice() + "", 160, y);
            }

            i++;
            y += 110;
        }
        this.refreshMouseRegions = false;
    }

    @Override
    public void mouseClicked(MouseEvent e, MouseListeningRegion origin) {
        Object data = origin.getData();
        if (data instanceof Integer) {
            int idx = (Integer) data;
            if (selectedItem != -1 && selectedItem != idx) {
                selectedItem = idx;
                computeBidButtonBounds();
                client.getGridPanel().repaint();
            }
            return;
        }
        throw new IllegalStateException();
    }
}
