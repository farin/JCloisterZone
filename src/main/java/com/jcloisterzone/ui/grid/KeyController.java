package com.jcloisterzone.ui.grid;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;

import com.jcloisterzone.ui.Client;

public class KeyController implements KeyEventDispatcher {

    private final Client client;
    private final GridPanel gridPanel;

    boolean repeatLeft, repeatRight, repeatUp, repeatDown;

    public KeyController(Client client) {
        this.client = client;
        gridPanel = client.getGridPanel();
        (new Timer(true)).scheduleAtFixedRate(new KeyRepeater(), 0, 40);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getModifiers() != 0) return false;
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            switch (e.getKeyCode()) {
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_ENTER:
                if (client.isClientActive()) {
                    client.getControlPanel().pass();
                    return true;
                }
                break;
            case KeyEvent.VK_TAB:
                if (client.isClientActive()) {
                    client.getControlPanel().getActionPanel().switchAction();
                    return true;
                }
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                repeatLeft = true;
                return true;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                repeatRight = true;
                return true;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                repeatDown = true;
                return true;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                repeatUp = true;
                return true;
            }
        }
        if (e.getID() == KeyEvent.KEY_RELEASED) {
            switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                repeatLeft = false;
                return true;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                repeatRight = false;
                return true;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                repeatDown = false;
                return true;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                repeatUp = false;
                return true;
            }
        }

        return false;
    }

    class KeyRepeater extends TimerTask {

        @Override
        public void run() {
            if (repeatLeft) {
                gridPanel.moveCenter(-1, 0);
            }
            if (repeatRight) {
                gridPanel.moveCenter(1, 0);
            }
            if (repeatUp) {
                gridPanel.moveCenter(0, -1);
            }
            if (repeatDown) {
                gridPanel.moveCenter(0, 1);
            }
        }
    }

}
