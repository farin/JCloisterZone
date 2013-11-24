package com.jcloisterzone.ui.grid;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;

import com.jcloisterzone.ui.Client;

public class KeyController implements KeyEventDispatcher {

    private final Client client;

    boolean repeatLeft, repeatRight, repeatUp, repeatDown;
    boolean repeatZoomIn, repeatZoomOut;

    public KeyController(Client client) {
        this.client = client;
        (new Timer(true)).scheduleAtFixedRate(new KeyRepeater(), 0, 40);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        //System.out.println(e);
        if (!client.isActive()) return false; //AWT method on window (it not check if player is active)
        if (!isDispatchActive()) return false;
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            if (e.getKeyChar() == '`' || e.getKeyChar() == ';') {
                client.getGridPanel().getChatPanel().getInput().requestFocus();
                return true;
            }
            switch (e.getKeyCode()) {
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_ENTER:
                if (client.isClientActive()) {
                    client.getControlPanel().pass();
                    return true;
                }
                break;
            case KeyEvent.VK_TAB:
                if (e.getModifiers() == 0) {
                    client.getGridPanel().forward();
                } else if (e.getModifiers() == KeyEvent.SHIFT_MASK) {
                    client.getGridPanel().backward();
                }
                break;
            default:
                return dispatchReptable(e, true);
            }
        } else if (e.getID() == KeyEvent.KEY_RELEASED) {
            boolean result = dispatchReptable(e, false);
            if (result) e.consume();
            return result;
        } else if (e.getID() == KeyEvent.KEY_TYPED) {
            return dispatchKeyTyped(e);
        }
        return false;
    }

    private boolean isDispatchActive() {
        GridPanel gp = client.getGridPanel();
        if (gp != null) return !gp.getChatPanel().getInput().hasFocus();
        return true;
    }

    private boolean dispatchReptable(KeyEvent e, boolean pressed) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_LEFT:
        case KeyEvent.VK_A:
            repeatLeft = pressed;
            return true;
        case KeyEvent.VK_RIGHT:
        case KeyEvent.VK_D:
            repeatRight = pressed;
            return true;
        case KeyEvent.VK_DOWN:
        case KeyEvent.VK_S:
            repeatDown = pressed;
            return true;
        case KeyEvent.VK_UP:
        case KeyEvent.VK_W:
            repeatUp = pressed;
            return true;
        }
        if (e.getKeyChar() == '+') {
            repeatZoomIn = pressed;
            return true;
        }
        if (e.getKeyChar() == '-') {
            repeatZoomOut = pressed;
            return true;
        }
        return false;
    }

    private boolean dispatchKeyTyped(KeyEvent e) {
        if (e.getKeyChar() == '+' || e.getKeyChar() == '-') {
            e.consume();
            return true;
        }
        return false;
    }

    class KeyRepeater extends TimerTask {

        @Override
        public void run() {
            GridPanel gridPanel = client.getGridPanel();
            if (gridPanel == null) return;
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
            if (repeatZoomIn) {
                gridPanel.zoom(0.8);
            }
            if (repeatZoomOut) {
                gridPanel.zoom(-0.8);
            }
        }
    }

}
