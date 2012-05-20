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
    private static Font FONT_BUTTON = new Font(null, Font.BOLD, 15);
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
    private boolean buyOrSell;

    private boolean refreshMouseRegions;

    private JButton leftButton, rightButton;
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
         leftButton = new JButton();
         leftButton.setFont(FONT_BUTTON);
         leftButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
            	assert bidable ^ buyOrSell;
                if (bidable) {
                	client.getServer().bazaarBid(selectedItem, bidAmountModel.getNumber().intValue());
                }
                if (buyOrSell) {
                	client.getServer().bazaarBuyOrSell(true);
                }

            }
         });
         leftButton.setVisible(false);
         parent.add(leftButton);

         rightButton = new JButton();
         rightButton.setFont(FONT_BUTTON);
         rightButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
            	assert bidable ^ buyOrSell;
                if (bidable) {
                	client.getServer().pass();
                }
                if (buyOrSell) {
                	client.getServer().bazaarBuyOrSell(false);
                }
            }
         });
         rightButton.setVisible(false);
         parent.add(rightButton);

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

    public boolean isBuyOrSell() {
		return buyOrSell;
	}

	public void setBuyOrSell(boolean buyOrSell) {
		this.buyOrSell = buyOrSell;
		computeBidButtonBounds();
	}



	private void computeBidButtonBounds() {
		//TODO hardcoded offset - but no better solution for now
		int bazaarPanelX = client.getGridPanel().getWidth()-ControlPanel.PANEL_WIDTH-BazaarPanel.PANEL_WIDTH-60;
		int y = getRowY(selectedItem);

		leftButton.setBounds(bazaarPanelX+115, y+55, 70, 30);
		rightButton.setBounds(bazaarPanelX+180, y+55, 70,30);

		if (buyOrSell) {
			leftButton.setText(_("Buy"));
			rightButton.setText(_("Sell"));
			leftButton.setVisible(true);
		    rightButton.setVisible(true);
			bidAmount.setVisible(false);
		} else if (bidable) {
			leftButton.setText(_("Bid"));
			rightButton.setText(_("Pass"));
			leftButton.setVisible(true);
		    rightButton.setVisible(!selectable);
			bidAmount.setBounds(bazaarPanelX+170, y+10, BazaarPanel.PANEL_WIDTH-190, 25);
			bidAmount.setVisible(true);
        } else {
        	leftButton.setVisible(false);
        	rightButton.setVisible(false);
        	bidAmount.setVisible(false);
        }
    }

    public boolean isBidable() {
        return bidable;
    }


    private void updateBidRange() {
        //int points = client.getGame().getActivePlayer().getPoints();
        int points = 100; //DEBUG ONLY

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
        	BazaarItem[] supply = bcb.getBazaarSupply();
        	do {
	            selectedItem++;
	            if (selectedItem == supply.length) {
	                selectedItem = 0;
	            }
        	} while (supply[selectedItem].getOwner() != null);
            computeBidButtonBounds();
            client.getGridPanel().repaint();
        }
    }

    public void backward() {
        if (selectable) {
        	BazaarItem[] supply = bcb.getBazaarSupply();
        	do {
	            selectedItem--;
	            if (selectedItem == 0) {
	                selectedItem = supply.length-1;
	            }
        	} while (supply[selectedItem].getOwner() != null);
        	computeBidButtonBounds();
            client.getGridPanel().repaint();
        }
    }

    private int getRowY(int item) {
		return 75 + 110 * item;
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
        } else if (bidable) {
            hint = _("Place your offer or pass.");
        } else if (buyOrSell) {
        	//TODO parameterized message
        	hint = _("Buy or sell tile from latest bidder.");
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
            } else if (bi.getOwner() != null) {
            	Image playerImage = client.getFigureTheme().getFigureImage(SmallFollower.class, client.getPlayerColor(bi.getOwner()), null);
            	//TODO smooth image
                g2.drawImage(playerImage, 140, y+12, 64, 64, null);
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
