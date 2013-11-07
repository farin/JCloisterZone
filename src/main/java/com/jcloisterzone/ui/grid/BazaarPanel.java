package com.jcloisterzone.ui.grid;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.BazaarItem;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.controls.ControlPanel;
import com.jcloisterzone.ui.controls.FakeComponent;
import com.jcloisterzone.ui.controls.MouseListeningRegion;
import com.jcloisterzone.ui.controls.RegionMouseListener;

public class BazaarPanel extends FakeComponent implements RegionMouseListener, ForwardBackwardListener {

    private static Font FONT_HEADER = new Font(null, Font.BOLD, 18);
    private static Font FONT_BUTTON = new Font(null, Font.BOLD, 12);
    private static Font FONT_ACTION = new Font(null, Font.PLAIN, 12);

    public static enum BazaarPanelState { INACTIVE, SELECT_TILE, MAKE_BID, BUY_OR_SELL};

    final BazaarCapability bcb;

    private int selectedItem = -1;
    private BazaarPanelState state = BazaarPanelState.INACTIVE;

    private boolean refreshMouseRegions;
    private final boolean noAuction;

    private JLabel hint, bidAmountLabel;
    private JButton leftButton, rightButton;
    private JSpinner bidAmount;
    private SpinnerNumberModel bidAmountModel;


    public BazaarPanel(Client client) {
       super(client);
       noAuction = client.getGame().hasRule(CustomRule.BAZAAR_NO_AUCTION);
       bcb = client.getGame().getCapability(BazaarCapability.class);
       bidAmountModel = new SpinnerNumberModel(0,0,1,1);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        refreshMouseRegions = true;
        super.componentResized(e);
    }

    @Override
    public void registerSwingComponents(JComponent parent) {
         hint = new JLabel();
         hint.setFont(FONT_ACTION);
         parent.add(hint);

         leftButton = new JButton();
         leftButton.setFont(FONT_BUTTON);
         leftButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                switch (state) {
                case SELECT_TILE:
                case MAKE_BID:
                    client.getServer().bazaarBid(selectedItem, bidAmountModel.getNumber().intValue());
                    break;
                case BUY_OR_SELL:
                    client.getServer().bazaarBuyOrSell(true);
                    break;
                }

            }
         });
         leftButton.setMargin(new Insets(1,1,1,1));
         leftButton.setVisible(false);
         parent.add(leftButton);

         rightButton = new JButton();
         rightButton.setFont(FONT_BUTTON);
         rightButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                switch (state) {
                case SELECT_TILE:
                case MAKE_BID:
                    client.getServer().pass();
                    break;
                case BUY_OR_SELL:
                    client.getServer().bazaarBuyOrSell(false);
                    break;
                }
            }
         });
         rightButton.setMargin(new Insets(1,1,1,1));
         rightButton.setVisible(false);
         parent.add(rightButton);

         if (!noAuction) {
             bidAmount = new JSpinner(bidAmountModel);
             bidAmount.setFont(new Font(null, Font.BOLD, 14));
             bidAmount.setVisible(false);
             parent.add(bidAmount);

             bidAmountLabel = new JLabel();
             parent.add(bidAmountLabel);
         }
    }

    @Override
    public void destroySwingComponents(JComponent parent) {
        parent.remove(hint);
        parent.remove(leftButton);
        parent.remove(rightButton);
        if (bidAmount != null) {
            parent.remove(bidAmount);
            parent.remove(bidAmountLabel);
        }
    }


    public BazaarPanelState getState() {
        return state;
    }

    public void setState(BazaarPanelState state) {
        this.state = state;
        refreshMouseRegions = true;
        switch (state) {
        case INACTIVE:
            hint.setText("");
            break;
        case SELECT_TILE:
            if (noAuction) {
                hint.setText( _("Choose your tile."));
            } else {
                hint.setText( _("<html>Choose tile for next auction<br>and make initial offer.</html>"));
                updateBidRange();
            }
            break;
        case MAKE_BID:
            hint.setText( _("Raise bid or pass."));
            updateBidRange();
            break;
        case BUY_OR_SELL:
            hint.setText(_("Buy or sell tile from latest bidder."));
            break;
        }
        if (bidAmountLabel != null) {
            if (state == BazaarPanelState.BUY_OR_SELL) {
                bidAmountLabel.setText(bcb.getCurrentBazaarAuction().getCurrentPrice() + "  " + _("points"));
            } else {
                bidAmountLabel.setText(_("points"));
            }
        }

        layoutSwingComponents(client.getGridPanel());
    }


    @Override
    public void layoutSwingComponents(JComponent parent) {
        //TODO hardcoded offset - but no better solution for now
        int bazaarPanelX = parent.getWidth()-ControlPanel.PANEL_WIDTH-getWidth()-60;
        int y = getRowY(selectedItem);

        hint.setBounds(bazaarPanelX+20, 24, ControlPanel.PANEL_WIDTH-10, 50);

        if (state == BazaarPanelState.SELECT_TILE) {
            leftButton.setBounds(bazaarPanelX+130, y+55, 100, 25);
        } else {
            leftButton.setBounds(bazaarPanelX+118, y+55, 60, 25);
            rightButton.setBounds(bazaarPanelX+182, y+55, 60, 25);
        }

        if (bidAmount != null) {
            //bidAmount.setBounds(bazaarPanelX+170, y+10, BazaarPanel.PANEL_WIDTH-190, 25);
            bidAmount.setBounds(bazaarPanelX+130, y+15, 50, 24);
            if (state == BazaarPanelState.BUY_OR_SELL) {
                bidAmountLabel.setBounds(bazaarPanelX+130, y+15, 130, 24);
            } else {
                bidAmountLabel.setBounds(bazaarPanelX+190, y+15, 70, 24);
            }
        }

        switch (state) {
        case BUY_OR_SELL:
            leftButton.setText(_("Buy"));
            rightButton.setText(_("Sell"));
            leftButton.setVisible(true);
            rightButton.setVisible(true);
            if (bidAmount != null) {
                bidAmount.setVisible(false);
                bidAmountLabel.setVisible(true);
            }
            break;
        case SELECT_TILE:
            leftButton.setText(_("Select"));
            leftButton.setVisible(true);
            rightButton.setVisible(false);
            if (bidAmount != null) {
                bidAmount.setVisible(true);
                bidAmountLabel.setVisible(true);
            }
            break;
        case MAKE_BID:
            leftButton.setText(_("Bid"));
            rightButton.setText(_("Pass"));
            leftButton.setVisible(true);
            rightButton.setVisible(true);
            if (bidAmount != null) {
                bidAmount.setVisible(true);
                bidAmountLabel.setVisible(true);
            }
            break;
        default:
            leftButton.setVisible(false);
            rightButton.setVisible(false);
            if (bidAmount != null) {
                bidAmount.setVisible(false);
                bidAmountLabel.setVisible(false);
            }
            break;
        }
    }


    private void updateBidRange() {
        //int points = client.getGame().getActivePlayer().getPoints();
        //bidAmountModel.setMaximum(points);
        bidAmountModel.setMaximum(999);

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
        layoutSwingComponents(client.getGridPanel());
    }

    public int getSelectedItem() {
        return selectedItem;
    }

    public void forward() {
        if (state == BazaarPanelState.SELECT_TILE) {
            ArrayList<BazaarItem> supply = bcb.getBazaarSupply();
            do {
                selectedItem++;
                if (selectedItem == supply.size()) {
                    selectedItem = 0;
                }
            } while (supply.get(selectedItem).getOwner() != null);
            layoutSwingComponents(client.getGridPanel());
            client.getGridPanel().repaint();
        }
    }

    public void backward() {
        if (state == BazaarPanelState.SELECT_TILE) {
            ArrayList<BazaarItem> supply = bcb.getBazaarSupply();
            do {
                selectedItem--;
                if (selectedItem == 0) {
                    selectedItem = supply.size()-1;
                }
            } while (supply.get(selectedItem).getOwner() != null);
            layoutSwingComponents(client.getGridPanel());
            client.getGridPanel().repaint();
        }
    }

    private int getRowY(int item) {
        return 75 + 110 * item;
    }

    @Override
    public void paintComponent(Graphics2D g2) {
        super.paintComponent(g2);

        if (bcb.getBazaarSupply() == null) return;

        GridPanel gp = client.getGridPanel();
        int h = gp.getHeight();

        g2.setColor(ControlPanel.PANEL_BG_COLOR);
        g2.fillRect(0 , 0, getWidth(), h);

        g2.setColor(ControlPanel.HEADER_FONT_COLOR);
        g2.setFont(FONT_HEADER);
        g2.drawString(_("Bazaar supply"), 20, 24);

        int y = 75;

        if (refreshMouseRegions) {
            getMouseRegions().clear();
        }

        int i = 0;
        for (BazaarItem bi : bcb.getBazaarSupply()) {
            //TOOD cache supply images ??
            Image img =  client.getResourceManager().getTileImage(bi.getTile());

            if (selectedItem == i) {
                g2.setColor(ControlPanel.PLAYER_BG_COLOR);
                g2.fillRect(0, y-1, getWidth(), 92);
            }

            if (refreshMouseRegions && state == BazaarPanelState.SELECT_TILE && bi.getOwner() == null) {
                getMouseRegions().add(new MouseListeningRegion(new Rectangle(0, y-1, getWidth(), 102), this, i));
            }

            g2.drawImage(img, 20, y, 90, 90, null);

            if (bi.getCurrentBidder() == null && bi.getOwner() != null) {
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
                layoutSwingComponents(client.getGridPanel());
                client.getGridPanel().repaint();
            }
            return;
        }
        throw new IllegalStateException();
    }

    @Override
    public void mouseEntered(MouseEvent e, MouseListeningRegion origin) {
    }

    @Override
    public void mouseExited(MouseEvent e, MouseListeningRegion origin) {
    }
}
