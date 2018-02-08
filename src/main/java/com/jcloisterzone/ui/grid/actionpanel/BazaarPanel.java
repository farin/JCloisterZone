package com.jcloisterzone.ui.grid.actionpanel;

import static com.jcloisterzone.ui.I18nUtils._tr;

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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.jcloisterzone.action.BazaarBidAction;
import com.jcloisterzone.action.BazaarSelectBuyOrSellAction;
import com.jcloisterzone.action.BazaarSelectTileAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.BazaarCapabilityModel;
import com.jcloisterzone.game.capability.BazaarItem;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.ForwardBackwardListener;
import com.jcloisterzone.ui.gtk.ThemedJLabel;
import com.jcloisterzone.ui.gtk.ThemedJPanel;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;
import com.jcloisterzone.wsio.message.BazaarBidMessage;
import com.jcloisterzone.wsio.message.BazaarBuyOrSellMessage;
import com.jcloisterzone.wsio.message.BazaarBuyOrSellMessage.BuyOrSellOption;
import com.jcloisterzone.wsio.message.PassMessage;

import io.vavr.collection.Queue;
import net.miginfocom.swing.MigLayout;


public class BazaarPanel extends ActionInteractionPanel<PlayerAction<?>> implements ForwardBackwardListener {

    private static Font FONT_HEADER = new Font(null, Font.BOLD, 18);
    private static Font FONT_BUTTON = new Font(null, Font.BOLD, 12);
    private static Font FONT_ACTION = new Font(null, Font.PLAIN, 12);

    private static final Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);

    public static enum BazaarPanelState { INACTIVE, SELECT_TILE, MAKE_BID, BUY_OR_SELL};

    private BazaarCapabilityModel model;

    private int selectedItem = -1;
    private BazaarPanelState panelState = BazaarPanelState.INACTIVE;

    private BazaarItemPanel itemPanels[];
    private boolean noAuction;

    private JLabel hint;

    private OverlayPanel overlay;


    public BazaarPanel(Client client, GameController gc) {
       super(client, gc);
    }

    private void initComponents(GameState state) {
        noAuction = state.getBooleanValue(Rule.BAZAAR_NO_AUCTION);

        setOpaque(true);
        setBackground(client.getTheme().getTransparentPanelBg());
        setLayout(new MigLayout("ins 0", "[grow]", ""));

        JLabel label;

        label = new ThemedJLabel(_tr("Bazaar supply"));
        label.setFont(FONT_HEADER);
        label.setForeground(client.getTheme().getHeaderFontColor());
        add(label, "wrap, gap 20 20 10 5");

        hint = new ThemedJLabel();
        hint.setFont(FONT_ACTION);
        add(hint, "wrap, gap 20 20 0 5");

        BazaarCapabilityModel model = state.getCapabilityModel(BazaarCapability.class);

        itemPanels = new BazaarItemPanel[model.getSupply().size()];
        int idx = 0;
        for (BazaarItem bi : model.getSupply()) {
            itemPanels[idx] = new BazaarItemPanel(idx);
            add(itemPanels[idx], "wrap, gap 0, growx, h 92");
            idx++;
        }

        overlay = new OverlayPanel();
    }

    @Override
    public void setGameState(GameState state) {
        super.setGameState(state);

        if (overlay == null) {
            initComponents(state);
        }

        this.model = state.getCapabilityModel(BazaarCapability.class);
        PlayerAction<?> action = getAction();

        if (!state.getActivePlayer().isLocalHuman()) {
            setPanelState(BazaarPanelState.INACTIVE);
            return;
        }

        if (action instanceof BazaarSelectTileAction) {
            Queue<BazaarItem> supply = model.getSupply();
            for (int i = 0; i < supply.size(); i++) {
                if (supply.get(i).getOwner() == null) {
                    setSelectedItem(i);
                    break;
                }
            }
            setPanelState(BazaarPanelState.SELECT_TILE);
        } else if (action instanceof BazaarBidAction) {
            // update index, although it is usually set from SELECT_TILE
            // game can be eg. loaded without rendering in SELECT_TILE state
            setSelectedItem(model.getAuctionedItemIndex());
            setPanelState(BazaarPanelState.MAKE_BID);
        } else if (action instanceof BazaarSelectBuyOrSellAction) {
            // update index, although it is usually set from SELECT_TILE
            // game can be eg. loaded without rendering in SELECT_TILE state
            setSelectedItem(model.getAuctionedItemIndex());
            setPanelState(BazaarPanelState.BUY_OR_SELL);
        }
    }

    class BazaarItemPanel extends ThemedJPanel {
        final int idx;

        public BazaarItemPanel(int idx) {
            this.idx = idx;
            setOpaque(false);
            setBackground(TRANSPARENT_COLOR);
            setLayout(new MigLayout("ins 0", "", ""));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (panelState == BazaarPanelState.SELECT_TILE) {
                        BazaarItem bi = model.getSupply().get(idx);
                        if (bi.getOwner() == null) {
                            setSelectedItem(idx);
                        }
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            BazaarItem bi = model.getSupply().get(idx);

            Image img =  client.getResourceManager().getTileImage(bi.getTile().getId(), Rotation.R0).getImage();

            if (selectedItem == idx) {
                g2.setColor(client.getTheme().getPlayerBoxBg());
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            g2.drawImage(img, 20, 0, 90, 90, null);

            if (bi.getCurrentBidder() == null && bi.getOwner() != null) {
                Image playerImage = client.getResourceManager().getLayeredImage(
                    new LayeredImageDescriptor(SmallFollower.class, bi.getOwner().getColors().getMeepleColor())
                );
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

                bidAmountLabel = new ThemedJLabel();
                add(bidAmountLabel);
            }

            leftButton = new JButton();
            leftButton.setFont(FONT_BUTTON);
            leftButton.setMargin(new Insets(1,1,1,1));
            leftButton.addActionListener(new ActionListener() {
                @SuppressWarnings("incomplete-switch")
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    switch (panelState) {
                    case SELECT_TILE:
                    case MAKE_BID:
                        gc.getConnection().send(
                            new BazaarBidMessage(
                                selectedItem,
                                bidAmountModel.getNumber().intValue()
                            )
                        );
                        break;
                    case BUY_OR_SELL:
                        gc.getConnection().send(
                            new BazaarBuyOrSellMessage(BuyOrSellOption.BUY)
                        );
                        break;
                    }

                }
            });
            add(leftButton);

            rightButton = new JButton();
            rightButton.setFont(FONT_BUTTON);
            rightButton.addActionListener(new ActionListener() {
                @SuppressWarnings("incomplete-switch")
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    switch (panelState) {
                    case SELECT_TILE:
                    case MAKE_BID:
                        gc.getConnection().send(new PassMessage());
                        break;
                    case BUY_OR_SELL:
                        gc.getConnection().send(
                            new BazaarBuyOrSellMessage(BuyOrSellOption.SELL)
                        );
                        break;
                    }
                }
            });
            rightButton.setMargin(new Insets(1,1,1,1));
            add(rightButton);
        }

        public void setPanelState(BazaarPanelState panelState) {
            MigLayout layout = (MigLayout)getLayout();
            switch (panelState) {
            case INACTIVE:
                hint.setText("");
                break;
            case SELECT_TILE:
                if (noAuction) {
                    hint.setText( _tr("Choose your tile."));
                } else {
                    hint.setText( _tr("<html>Choose tile for next auction<br>and make initial offer.</html>"));
                    updateBidRange();
                }
                break;
            case MAKE_BID:
                hint.setText( _tr("Raise bid or pass."));
                updateBidRange();
                break;
            case BUY_OR_SELL:
                hint.setText(_tr("Buy or sell tile from latest bidder."));
                break;
            }
            if (bidAmountLabel != null) {
                if (panelState == BazaarPanelState.BUY_OR_SELL) {
                    int points = model.getAuctionedItem().getCurrentPrice();
                    bidAmountLabel.setText(points + "  " + _tr("points"));
                    layout.setComponentConstraints(bidAmountLabel, "pos 20 20");
                } else {
                    bidAmountLabel.setText(_tr("points"));
                    layout.setComponentConstraints(bidAmountLabel, "pos 75 20");
                }
            }

            if (panelState == BazaarPanelState.SELECT_TILE) {
                layout.setComponentConstraints(leftButton, "pos 20 55 120 80");
            } else {
                layout.setComponentConstraints(leftButton, "pos 8 55 68 80");
                layout.setComponentConstraints(rightButton, "pos 72 55 132 80");
            }

            switch (panelState) {
            case BUY_OR_SELL:
                leftButton.setText(_tr("Buy"));
                rightButton.setText(_tr("Sell"));
                leftButton.setVisible(true);
                rightButton.setVisible(true);
                if (bidAmount != null) {
                    bidAmount.setVisible(false);
                    bidAmountLabel.setVisible(true);
                }
                break;
            case SELECT_TILE:
                leftButton.setText(_tr("Select"));
                leftButton.setVisible(true);
                rightButton.setVisible(false);
                if (bidAmount != null) {
                    bidAmount.setVisible(true);
                    bidAmountLabel.setVisible(true);
                }
                break;
            case MAKE_BID:
                leftButton.setText(_tr("Bid"));
                rightButton.setText(_tr("Pass"));
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
        return panelState;
    }

    public void setPanelState(BazaarPanelState panelState) {
        this.panelState = panelState;
        overlay.setPanelState(panelState);
        revalidate();
        //gc.getGameView().getGridPanel().repaint(); //must repaint whole panel to avoid ghost bg
    }


    private void updateBidRange() {
        overlay.bidAmountModel.setMaximum(999);

        if (model.getAuctionedItemIndex() == null) {
            overlay.bidAmountModel.setMinimum(0);
            overlay.bidAmountModel.setValue(0);
        } else {
            int min = model.getAuctionedItem().getCurrentPrice()+1;
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
        if (panelState == BazaarPanelState.SELECT_TILE) {
            int selected = selectedItem;
            Queue<BazaarItem> supply = model.getSupply();
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
        if (panelState == BazaarPanelState.SELECT_TILE) {
            int selected = selectedItem;
            Queue<BazaarItem> supply = model.getSupply();
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
