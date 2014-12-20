package com.jcloisterzone.ui;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.bugreport.BugReportDialog;
import com.jcloisterzone.ui.dialog.HelpDialog;
import com.jcloisterzone.wsio.message.UndoMessage;

@SuppressWarnings("serial")
public class MenuBar extends JMenuBar {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Client client;
    private boolean isGameRunning = false;

    private JMenuItem create, connect, close, showDiscard, undo, save, load, farmHints;
    private JMenuItem zoomIn, zoomOut, screenShot;
    private JMenuItem history, reportBug;

    public MenuBar(Client _client) {
        this.client = _client;

        boolean isMac = Bootstrap.isMac();

        JMenu menu;
        JMenuItem menuItem;

        //menu.getAccessibleContext().setAccessibleDescription(
        //"The only menu in this program that has menu items");

        menu = new JMenu(_("Game"));
        menu.setMnemonic(KeyEvent.VK_G);
        create = new JMenuItem(_("New game"));
        create.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        create.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                client.createGame();
            }
        });
        menu.add(create);

        connect = new JMenuItem(_("Connect"));
        connect.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        connect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                client.showConnectGamePanel();
            }
        });
        menu.add(connect);

        close = new JMenuItem(_("Close game"));
        close.setEnabled(false);
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                client.closeGame();
            }
        });
        menu.add(close);


        menu.addSeparator();

        undo = new JMenuItem(_("Undo"));
        undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        undo.setEnabled(false);
        undo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                client.getConnection().send(new UndoMessage(client.getGame().getGameId()));
            }
        });
        menu.add(undo);

        menu.addSeparator();

        save = new JMenuItem(_("Save"));
        save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        save.setEnabled(false);
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                client.handleSave();
            }
        });
        menu.add(save);

        load = new JMenuItem(_("Load"));
        load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        load.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                client.handleLoad();
            }
        });
        menu.add(load);

        if (!isMac) {
            menu.addSeparator();

            menuItem = new JMenuItem(_("Quit"));
            menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    client.handleQuit();
                }
            });
            menu.add(menuItem);
        }

        this.add(menu);


        menu = new JMenu(_("Window"));
        zoomIn = new JMenuItem(_("Zoom in"));
        zoomIn.setAccelerator(KeyStroke.getKeyStroke('+')); //only show key code, handled by KeyController
        zoomIn.setEnabled(false);
        zoomIn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                client.getGridPanel().zoom(2.0);
            }
        });
        menu.add(zoomIn);
        zoomOut = new JMenuItem(_("Zoom out"));
        zoomOut.setAccelerator(KeyStroke.getKeyStroke('-')); //only show key code, handled by KeyController
        zoomOut.setEnabled(false);
        zoomOut.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                client.getGridPanel().zoom(-2.0);
            }
        });
        menu.add(zoomOut);
        
        screenShot = new JMenuItem(_("Screenshot"));
        screenShot.setAccelerator(KeyStroke.getKeyStroke('P'));
        screenShot.setEnabled(false);
        screenShot.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                client.takeScreenshot();
            }
        });
        menu.add(screenShot);
        

//		menuItem = new JMenuItem(_("FullScreen"));
//		menuItem.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				//fullScreen();
//				throw new UnsupportedOperationException();
//			}
//		});
//		/*if (!GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().isFullScreenSupported()) {
//			menuItem.setEnabled(false);
//		}*/
//		menuItem.setEnabled(false);
//		menu.add(menuItem);

        menu.addSeparator();
        history = new JCheckBoxMenuItem(_("Show last placements"));
        history.setAccelerator(KeyStroke.getKeyStroke('x'));
        history.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JCheckBoxMenuItem ch = (JCheckBoxMenuItem) e.getSource();
                client.setShowHistory(ch.isSelected());

            }
        });
        menu.add(history);

        farmHints = new JCheckBoxMenuItem(_("Show farm hints"));
        farmHints.setAccelerator(KeyStroke.getKeyStroke('f'));
        farmHints.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JCheckBoxMenuItem ch = (JCheckBoxMenuItem) e.getSource();
                client.getMainPanel().setShowFarmHints(ch.isSelected());
            }
        });
        menu.add(farmHints);


        showDiscard = new JMenuItem(_("Show discarded tiles"));
        showDiscard.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        showDiscard.setEnabled(false);
        showDiscard.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                client.getDiscardedTilesDialog().setVisible(true);
            }
        });
        menu.add(showDiscard);


        this.add(menu);

        menu = new JMenu(_("Settings"));
        menuItem = new JCheckBoxMenuItem(_("Beep alert at player turn"));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean state = ((JCheckBoxMenuItem) e.getSource()).getState();
                client.getConfig().setBeep_alert(state);
                client.saveConfig();
            }
        });
        ((JCheckBoxMenuItem)menuItem).setSelected(client.getConfig().getBeep_alert());
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JCheckBoxMenuItem(_("Confirm placement on a farm"));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean state = ((JCheckBoxMenuItem) e.getSource()).getState();
                client.getConfig().getConfirm().setFarm_place(state);
                client.saveConfig();
            }
        });
        ((JCheckBoxMenuItem)menuItem).setSelected(client.getConfig().getConfirm().getFarm_place());
        menu.add(menuItem);

        menuItem = new JCheckBoxMenuItem(_("Confirm placement on a tower"));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean state = ((JCheckBoxMenuItem) e.getSource()).getState();
                client.getConfig().getConfirm().setTower_place(state);
                client.saveConfig();
            }
        });
        ((JCheckBoxMenuItem)menuItem).setSelected(client.getConfig().getConfirm().getTower_place());
        menu.add(menuItem);

        menuItem = new JCheckBoxMenuItem(_("Confirm ransom payment"));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean state = ((JCheckBoxMenuItem) e.getSource()).getState();
                client.getConfig().getConfirm().setRansom_payment(state);
                client.saveConfig();
            }
        });
        ((JCheckBoxMenuItem)menuItem).setSelected(client.getConfig().getConfirm().getRansom_payment());
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JCheckBoxMenuItem(_("Confirm game close"));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean state = ((JCheckBoxMenuItem) e.getSource()).getState();
                client.getConfig().getConfirm().setGame_close(state);
                client.saveConfig();
            }
        });
        ((JCheckBoxMenuItem)menuItem).setSelected(client.getConfig().getConfirm().getGame_close());
        menu.add(menuItem);

        this.add(menu);

        menu = new JMenu(_("Help"));

        if (!isMac) {

            menuItem = new JMenuItem(_("About"));
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    client.handleAbout();
                }
            });
            menu.add(menuItem);

        }

        menuItem = new JMenuItem(_("Controls"));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new HelpDialog();
            }
        });
        menu.add(menuItem);

        reportBug = new JMenuItem(_("Report bug"));
        reportBug.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new BugReportDialog(client.getReportingTool());
            }
        });
        reportBug.setEnabled(false);
        menu.add(reportBug);

        this.add(menu);

    }

    //legacy methods - TODO refactor

    public void setZoomInEnabled(boolean state) {
        zoomIn.setEnabled(state);
    }
    public void setZoomOutEnabled(boolean state) {
        zoomOut.setEnabled(state);
    }
    
    public void setScreenshotEnabled(boolean state)
    {
    	screenShot.setEnabled(state);
    }

    public void setIsGameRunning(boolean isGameRunning) {
        this.isGameRunning = isGameRunning;
        create.setEnabled(!isGameRunning);
        connect.setEnabled(!isGameRunning);
        close.setEnabled(isGameRunning);
        reportBug.setEnabled(isGameRunning);
        if (!isGameRunning) {
            showDiscard.setEnabled(false);
            undo.setEnabled(false);
        }
        save.setEnabled(isGameRunning);
    }

    public boolean isGameRunning() {
        return isGameRunning;
    }

    public void setShowDiscardedEnabled(boolean enabled) {
        showDiscard.setEnabled(enabled);
    }

    public JMenuItem getUndo() {
        return undo;
    }



}
