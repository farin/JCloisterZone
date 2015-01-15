package com.jcloisterzone.ui.grid;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import net.miginfocom.swing.MigLayout;

import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.BazaarItem;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.controls.ControlPanel;

import static com.jcloisterzone.ui.I18nUtils._;

@InteractionPanel
public class BazaarPanel extends JPanel implements ForwardBackwardListener {

    private static Font FONT_HEADER = new Font(null, Font.BOLD, 18);
    private static Font FONT_BUTTON = new Font(null, Font.BOLD, 12);
    private static Font FONT_ACTION = new Font(null, Font.PLAIN, 12);

    private static final Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);

    public static enum BazaarPanelState { INACTIVE, SELECT_TILE, MAKE_BID, BUY_OR_SELL};

    final Client client;
    final GameController gc;
    final BazaarCapability bcb;

    private int selectedItem = -1;
    private BazaarPanelState state = BazaarPanelState.INACTIVE;

    private BazaarItemPanel itemPanels[];
    private final boolean noAuction;

    private JLabel hint;

    private OverlayPanel overlay;


    public BazaarPanel(Client client, GameController gc) {
       this.client = client;
       this.gc = gc;

       noAuction = gc.getGame().hasRule(CustomRule.BAZAAR_NO_AUCTION);
       bcb = gc.getGame().getCapability(BazaarCapability.class);


       setOpaque(true);
       setBackground(ControlPanel.PANEL_BG_COLOR);
       setLayout(new MigLayout("ins 0", "[grow]", ""));

       JLabel label;

       label = new JLabel(_("Bazaar supply"));
       label.setFont(FONT_HEADER);
       label.setForeground(ControlPanel.HEADER_FONT_COLOR);
       add(label, "wrap, gap 20 20 10 5");

       hint = new JLabel();
       hint.setFont(FONT_ACTION);
       add(hint, "wrap, gap 20 20 0 5");

       itemPanels = new BazaarItemPanel[bcb.getBazaarSupply().size()];
       int idx = 0;
       for (BazaarItem bi : bcb.getBazaarSupply()) {
           itemPanels[idx] = new BazaarItemPanel(idx, bi);
           add(itemPanels[idx], "wrap, gap 0, growx, h 92");
           idx++;
       }

       overlay = new OverlayPanel();
    }

    class BazaarItemPanel extends JPanel {
        final BazaarItem bi;
        final int idx;

        public BazaarItemPanel(int idx, BazaarItem bi) {
            this.idx = idx;
            this.bi = bi;
            setOpaque(false);
            setBackground(TRANSPARENT_COLOR);
            setLayout(new MigLayout("ins 0", "", ""));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    setSelectedItem(BazaarItemPanel.this.idx);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            Image img =  client.getResourceManager().getTileImage(bi.getTile());

            if (selectedItem == idx) {
                g2.setColor(ControlPanel.PLAYER_BG_COLOR);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            g2.drawImage(img, 20, 0, 90, 90, null);

            if (bi.getCurrentBidder() == null && bi.getOwner() != null) {
                Image playerImage = client.getFigureTheme().getFigureImage(SmallFollower.class, bi.getOwner().getColors().getMeepleColor(), null);
                //TODO smooth image
                g2.drawImage(playerImage, 140, 12, 64, 64, null);
            }
        }
    }

    class OverlayPanel extends JPanel {
        private JLabel bidAmountLabel;
        private JButton leftButton, rightButton;
        private JSpinner bidAmount;
        private SpinnerNumberModel bidAmountModel;


        public OverlayPanel() {
            setBackground(TRANSPARENT_COLOR);
            setOpaque(false);
            setLayout(new MigLayout("ins 0", "", ""));
            bidAmountModel = new SpinnerNumberModel(0, 0, 1, 1);

            if (!noAuction) {
                bidAmount = new JSpinner(bidAmountModel);
                bidAmount.setFont(new Font(null, Font.BOLD, 14));
                // bidAmount.setVisible(false);
                add(bidAmount, "pos 20 15");

                bidAmountLabel = new JLabel();
                add(bidAmountLabel);
            }

            leftButton = new JButton();
            leftButton.setFont(FONT_BUTTON);
            leftButton.setMargin(new Insets(1,1,1,1));
            leftButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    switch (state) {
                    case SELECT_TILE:
                    case MAKE_BID:
                        gc.getRmiProxy().bazaarBid(selectedItem, bidAmountModel.getNumber().intValue());
                        break;
                    case BUY_OR_SELL:
                        gc.getRmiProxy().bazaarBuyOrSell(true);
                        break;
                    }

                }
            });
            add(leftButton);

            rightButton = new JButton();
            rightButton.setFont(FONT_BUTTON);
            rightButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    switch (state) {
                    case SELECT_TILE:
                    case MAKE_BID:
                        gc.getRmiProxy().pass();
                        break;
                    case BUY_OR_SELL:
                        gc.getRmiProxy().bazaarBuyOrSell(false);
                        break;
                    }
                }
            });
            rightButton.setMargin(new Insets(1,1,1,1));
            add(rightButton);
        }

        public void setState(BazaarPanelState state) {
            MigLayout layout = (MigLayout)getLayout();
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
                    layout.setComponentConstraints(bidAmountLabel, "pos 20 20");
                } else {
                    bidAmountLabel.setText(_("points"));
                    layout.setComponentConstraints(bidAmountLabel, "pos 75 20");
                }
            }

            if (state == BazaarPanelState.SELECT_TILE) {
                layout.setComponentConstraints(leftButton, "pos 20 55 120 80");
            } else {
                layout.setComponentConstraints(leftButton, "pos 8 55 68 80");
                layout.setComponentConstraints(rightButton, "pos 72 55 132 80");
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

            revalidate();
        }
    }

    public BazaarPanelState getState() {
        return state;
    }

    public void setState(BazaarPanelState state) {
        this.state = state;
        overlay.setState(state);
        revalidate();
        gc.getGameView().getGridPanel().repaint(); //must repaint whole panel to avoid ghost bg
    }


    private void updateBidRange() {
        overlay.bidAmountModel.setMaximum(999);

        if (bcb.getCurrentBazaarAuction() == null) {
            overlay.bidAmountModel.setMinimum(0);
            overlay.bidAmountModel.setValue(0);
        } else {
            int min = bcb.getCurrentBazaarAuction().getCurrentPrice()+1;
            overlay.bidAmountModel.setMinimum(min);
            overlay.bidAmountModel.setValue(min);
        }
    }


    public void setSelectedItem(int selectedItem) {
        if (this.selectedItem != -1) {
            itemPanels[selectedItem].remove(overlay);
        }
        this.selectedItem = selectedItem;
        if (selectedItem != -1) {
            itemPanels[selectedItem].add(overlay, "gapleft 110, grow x, h 100%");
        }
        revalidate();
        gc.getGameView().getGridPanel().repaint(); //must repaint whole panel to avoid ghost bg
    }

    public int getSelectedItem() {
        return selectedItem;
    }

    @Override
    public void forward() {
        if (state == BazaarPanelState.SELECT_TILE) {
            int selected = selectedItem;
            ArrayList<BazaarItem> supply = bcb.getBazaarSupply();
            do {
                selected++;
                if (selected == supply.size()) {
                    selected = 0;
                }
            } while (supply.get(selected).getOwner() != null);
            setSelectedItem(selected);
        }
    }

    @Override
    public void backward() {
        if (state == BazaarPanelState.SELECT_TILE) {
            int selected = selectedItem;
            ArrayList<BazaarItem> supply = bcb.getBazaarSupply();
            do {
                selected--;
                if (selected == 0) {
                    selected = supply.size()-1;
                }
            } while (supply.get(selected).getOwner() != null);
            setSelectedItem(selected);
        }
    }
}
