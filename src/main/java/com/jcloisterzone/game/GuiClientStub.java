package com.jcloisterzone.game;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.jcloisterzone.game.phase.CreateGamePhase;
import com.jcloisterzone.rmi.mina.ClientStub;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.wsio.CmdHandler;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.message.GameMessage;

import static com.jcloisterzone.ui.I18nUtils._;


public class GuiClientStub extends ClientStub {

    private final Client client;
    private boolean autostartPerfomed;

    public GuiClientStub(Client client) {
        this.client = client;
    }

    @Override
    protected Game createGame(GameMessage message) {
        Game game = super.createGame(message);
        game.setConfig(client.getConfig());
        client.setGame(game);
        return game;
    }

    @Override
    @CmdHandler("GAME")
    public void handleGameMessage(Connection conn, final GameMessage msg) {
        super.handleGameMessage(conn, msg);
        final PlayerSlot[] slots = ((CreateGamePhase) game.getPhase()).getPlayerSlots();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                client.showCreateGamePanel(msg.getSnapshot() == null, slots);
            }
        });
    }

// AUTOSTART not convered yet

//    @Override
//    protected void controllMessageReceived(final ControllMessage msg) {
//        super.controllMessageReceived(msg);
//
//        DebugConfig debugConfig = client.getConfig().getDebug();
//        if (!autostartPerfomed && debugConfig.isAutostartEnabled()) {
//            autostartPerfomed = true; //apply autostart only once
//            AutostartConfig autostartConfig = debugConfig.getAutostart();
//            final PresetConfig presetCfg = client.getConfig().getPresets().get(autostartConfig.getPreset());
//            if (presetCfg == null) {
//                logger.warn("Autostart profile {} not found.", autostartConfig.getPreset());
//                return;
//            }
//
//            final List<String> players = autostartConfig.getPlayers() == null ? new ArrayList<String>() : autostartConfig.getPlayers();
//            if (players.isEmpty()) {
//                players.add("Player");
//            }
//            SwingUtilities.invokeLater(new Runnable() {
//                public void run() {
//                    int i = 0;
//                    for (String name: players) {
//                        PlayerSlot slot;
//                        try {
//                            Class<?> clazz = Class.forName(name);
//                            slot = new PlayerSlot(i, SlotType.AI, "AI-"+i+"-"+clazz.getSimpleName(), getClientId());
//                            slot.setAiClassName(clazz.getName());
//                        } catch (ClassNotFoundException e) {
//                            slot = new PlayerSlot(i, SlotType.PLAYER, name, getClientId());
//                        }
//                        client.getServer().updateSlot(slot, null);
//                        i++;
//                    }
//
//                    presetCfg.updateGameSetup(client.getServer());
//                    client.getServer().startGame();
//                }
//            });
//        }
//    }

    protected void versionMismatch(int version) {
        super.versionMismatch(version);
        JOptionPane.showMessageDialog(client,
                _("Remote JCloisterZone is not compatible with local application. Please upgrade both applications to same version."),
                _("Incompatible versions"),
                JOptionPane.ERROR_MESSAGE
        );
    }

//    @Override
//    protected void onDisconnect() {
//        client.getGridPanel().setErrorMessage("Connection lost. Reconnecting...");
//    }
//
//    @Override
//    protected void onReconnect() {
//        client.getGridPanel().setErrorMessage(null);
//    }
}
