package com.jcloisterzone.ui;

import static com.jcloisterzone.ui.I18nUtils._tr;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.EnumMap;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.ui.view.ConnectP2PView;
import com.jcloisterzone.ui.view.ConnectPlayOnlineView;
@SuppressWarnings("serial")
public class MenuBar extends JMenuBar {

    public static enum MenuItem {
        //Session
        NEW_GAME(_tr("New game"), KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())),
        CONNECT_P2P(_tr("Connect"), KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())),
        PLAY_ONLINE(_tr("Play Online"), KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())),
        DISCONNECT(_tr("Disconnect")),
        LEAVE_GAME(_tr("Leave Game")),
        SAVE(_tr("Save Game"), KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())),
        LOAD(_tr("Load Game"), KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())),
        QUIT(_tr("Quit"), KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())),
        //Game
        UNDO(_tr("Undo"), KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())),
        ZOOM_IN(_tr("Zoom In")),
        ZOOM_OUT(_tr("Zoom Out")),
        ROTATE_BOARD(_tr("Rotate Board"), KeyStroke.getKeyStroke('/')),
        GAME_EVENTS(_tr("Show Game Events"), KeyStroke.getKeyStroke('e')),
        LAST_PLACEMENTS(_tr("Show Last Placements"), KeyStroke.getKeyStroke('x')),
        FARM_HINTS(_tr("Show Farm Hints"), KeyStroke.getKeyStroke('f')),
        PROJECTED_POINTS(_tr("Show Projected Points"), KeyStroke.getKeyStroke('p')),
        DISCARDED_TILES(_tr("Show Discarded Tiles"), KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())),
        GAME_SETUP(_tr("Show Game Setup")),
        TAKE_SCREENSHOT(_tr("Take Screenshot"), KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0)),
        //Settings
        BEEP_ALERT(_tr("Beep Alert at Player Turn")),
        CONFIRM_ANY_DEPLOYMENT(_tr("Confirm Every Meeple Deployment")),
        CONFIRM_FARM_DEPLOYMENT(_tr("Confirm Meeple Deployment on a Farm")),
        CONFIRM_TOWER_DEPLOYMENT(_tr("Confirm Meeple Deployment on a Tower")),
        CONFIRM_RANSOM(_tr("Confirm Ransom Payment")),
        PREFERENCES(_tr("Preferences"), JCloisterZone.isMac() ?
            KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) : null),
        //Help
        ABOUT(_tr("About")),
        TILE_DISTRIBUTION(_tr("Tile Distribution"), KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0)),
        CONTROLS(_tr("Controls")),
        REPORT_BUG(_tr("Report Bug"));

        String title;
        KeyStroke accelerator;

        MenuItem(String title) {
            this(title, null);
        };

        MenuItem(String title, KeyStroke accelerator) {
            this.title = title;
            this.accelerator = accelerator;
        }
    }

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Client client;

    private EnumMap<MenuItem, JMenuItem> items = new EnumMap<>(MenuItem.class);

    public MenuBar(Client _client) {
        this.client = _client;

        boolean isMac = JCloisterZone.isMac();

        JMenu menu;
        JCheckBoxMenuItem chbox;

        menu = new JMenu(_tr("Session"));
        menu.setMnemonic(KeyEvent.VK_P);

        menu.add(createMenuItem(MenuItem.NEW_GAME, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.createGame();
            }
        }));
        menu.add(createMenuItem(MenuItem.CONNECT_P2P, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.mountView(new ConnectP2PView(client));
            }
        }));
        menu.addSeparator();
        menu.add(createMenuItem(MenuItem.PLAY_ONLINE, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.mountView(new ConnectPlayOnlineView(client));
            }
        }));
        menu.add(createMenuItem(MenuItem.DISCONNECT, false));
        menu.addSeparator();
        menu.add(createMenuItem(MenuItem.LEAVE_GAME, false));
        menu.addSeparator();

        menu.add(createMenuItem(MenuItem.SAVE, false));
        menu.add(createMenuItem(MenuItem.LOAD, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.handleLoad();
            }
        }));

        if (!isMac) {
            menu.addSeparator();
            menu.add(createMenuItem(MenuItem.QUIT, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    client.handleQuit();
                }
            }));
        }
        this.add(menu);
        menu = new JMenu(_tr("Game"));
        menu.setMnemonic(KeyEvent.VK_G);
        menu.add(createMenuItem(MenuItem.UNDO, false));
        menu.addSeparator();
        menu.add(createMenuItem(MenuItem.ZOOM_IN, false));
        menu.add(createMenuItem(MenuItem.ZOOM_OUT, false));
        menu.add(createMenuItem(MenuItem.ROTATE_BOARD, false));
        menu.addSeparator();
        chbox = createCheckBoxMenuItem(MenuItem.GAME_EVENTS, false);
        chbox.setSelected(true);
        menu.add(chbox);
        menu.add(createCheckBoxMenuItem(MenuItem.LAST_PLACEMENTS, false));
        menu.add(createCheckBoxMenuItem(MenuItem.FARM_HINTS, false));
        menu.add(createCheckBoxMenuItem(MenuItem.PROJECTED_POINTS, false));
        menu.addSeparator();
        menu.add(createMenuItem(MenuItem.DISCARDED_TILES, false));
        menu.add(createMenuItem(MenuItem.GAME_SETUP, false));
        menu.addSeparator();
        menu.add(createMenuItem(MenuItem.TAKE_SCREENSHOT, false));
        this.add(menu);

        menu = new JMenu(_tr("Settings"));
        menu.add(chbox = createCheckBoxMenuItem(MenuItem.BEEP_ALERT, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBoxMenuItem ch = (JCheckBoxMenuItem) e.getSource();
                client.getConfig().setBeep_alert(ch.isSelected());
                client.saveConfig();
            }
        }));
        chbox.setSelected(client.getConfig().getBeep_alert());
        menu.addSeparator();
        menu.add(chbox = createCheckBoxMenuItem(MenuItem.CONFIRM_ANY_DEPLOYMENT, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBoxMenuItem ch = (JCheckBoxMenuItem) e.getSource();
                client.getConfig().getConfirm().setAny_deployment(ch.isSelected());
                client.saveConfig();
            }
        }));
        chbox.setSelected(client.getConfig().getConfirm().getAny_deployment());
        menu.add(chbox = createCheckBoxMenuItem(MenuItem.CONFIRM_FARM_DEPLOYMENT, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBoxMenuItem ch = (JCheckBoxMenuItem) e.getSource();
                client.getConfig().getConfirm().setFarm_deployment(ch.isSelected());
                client.saveConfig();
            }
        }));
        chbox.setSelected(client.getConfig().getConfirm().getFarm_deployment());
        menu.add(chbox = createCheckBoxMenuItem(MenuItem.CONFIRM_TOWER_DEPLOYMENT, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBoxMenuItem ch = (JCheckBoxMenuItem) e.getSource();
                client.getConfig().getConfirm().setOn_tower_deployment(ch.isSelected());
                client.saveConfig();
            }
        }));
        chbox.setSelected(client.getConfig().getConfirm().getOn_tower_deployment());
        menu.addSeparator();
        menu.add(chbox = createCheckBoxMenuItem(MenuItem.CONFIRM_RANSOM, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBoxMenuItem ch = (JCheckBoxMenuItem) e.getSource();
                client.getConfig().getConfirm().setRansom_payment(ch.isSelected());
                client.saveConfig();
            }
        }));
        chbox.setSelected(client.getConfig().getConfirm().getRansom_payment());
        menu.addSeparator();
        menu.add(createMenuItem(MenuItem.PREFERENCES, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.showPreferncesDialog();
            }
        }));


        this.add(menu);

        menu = new JMenu(_tr("Help"));

        if (!isMac) {
            menu.add(createMenuItem(MenuItem.ABOUT, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    client.showAboutDialog();
                }
            }));
        }

        menu.add(createMenuItem(MenuItem.TILE_DISTRIBUTION, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.showTileDistribution();
            }
        }));

        menu.add(createMenuItem(MenuItem.CONTROLS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.showHelpDialog();
            }
        }));
        menu.add(createMenuItem(MenuItem.REPORT_BUG, false));
        this.add(menu);
    }

    private JMenuItem createMenuItem(MenuItem def, boolean enabled) {
        return createMenuItem(def, null, enabled);
    }

    private JMenuItem createMenuItem(MenuItem def, ActionListener handler) {
        return createMenuItem(def, handler, true);
    }

    private JMenuItem createMenuItem(MenuItem def, ActionListener handler, boolean enabled) {
        JMenuItem instance = new JMenuItem(def.title);
        initMenuItem(instance, def, handler);
        instance.setEnabled(enabled);
        return instance;
    }

    private JCheckBoxMenuItem createCheckBoxMenuItem(MenuItem def, boolean enabled) {
        return createCheckBoxMenuItem(def, null, enabled);
    }

    private JCheckBoxMenuItem createCheckBoxMenuItem(MenuItem def, ActionListener handler) {
        return createCheckBoxMenuItem(def, handler, true);
    }

    private JCheckBoxMenuItem createCheckBoxMenuItem(MenuItem def, ActionListener handler, boolean enabled) {
        JCheckBoxMenuItem instance = new JCheckBoxMenuItem(def.title);
        initMenuItem(instance, def, handler);
        instance.setEnabled(enabled);
        return instance;
    }

    private void initMenuItem(JMenuItem instance, MenuItem def, ActionListener handler) {
        if (def.accelerator != null) {
            instance.setAccelerator(def.accelerator);
        }
        if (handler != null) {
            instance.addActionListener(handler);
        }
        items.put(def, instance);
    }

    public void setItemEnabled(MenuItem item, boolean state) {
        items.get(item).setEnabled(state);
    }

    public void setItemActionListener(MenuItem item, ActionListener handler) {
        JMenuItem instance = items.get(item);
        ActionListener[] listeners = instance.getActionListeners();
        for (ActionListener listener : listeners) {
            instance.removeActionListener(listener);
        }
        if (handler != null) {
            instance.addActionListener(handler);
        }
    }

    public boolean isSelected(MenuItem item) {
        JCheckBoxMenuItem instance = (JCheckBoxMenuItem) items.get(item);
        return instance.isSelected();
    }
}
