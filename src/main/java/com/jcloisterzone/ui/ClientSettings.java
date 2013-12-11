package com.jcloisterzone.ui;

import com.jcloisterzone.Config;


//TODO possible with merge config

public class ClientSettings {

    private boolean playBeep;
    private boolean confirmFarmPlacement;
    private boolean confirmTowerPlacement;
    private boolean confirmGameClose;
    private boolean confirmRansomPayment;

    private boolean showHistory;

    public ClientSettings(Config config) {
        playBeep = config.getBeep_alert();
        confirmFarmPlacement = config.getConfirm().getFarm_place();
        confirmTowerPlacement = config.getConfirm().getTower_place();
        confirmGameClose = config.getConfirm().getGame_close();
        confirmRansomPayment = config.getConfirm().getRansom_payment();
    }

    public boolean isPlayBeep() {
        return playBeep;
    }
    public void setPlayBeep(boolean playBeep) {
        this.playBeep = playBeep;
    }
    public boolean isConfirmFarmPlacement() {
        return confirmFarmPlacement;
    }
    public void setConfirmFarmPlacement(boolean confirmFarmPlacement) {
        this.confirmFarmPlacement = confirmFarmPlacement;
    }
    public boolean isConfirmTowerPlacement() {
        return confirmTowerPlacement;
    }
    public void setConfirmTowerPlacement(boolean confirmTowerPlacement) {
        this.confirmTowerPlacement = confirmTowerPlacement;
    }

    public boolean isConfirmGameClose() {
        return confirmGameClose;
    }

    public void setConfirmGameClose(boolean confirmGameClose) {
        this.confirmGameClose = confirmGameClose;
    }

    public boolean isConfirmRansomPayment() {
        return confirmRansomPayment;
    }

    public void setConfirmRansomPayment(boolean confirmRansomPayment) {
        this.confirmRansomPayment = confirmRansomPayment;
    }

    public boolean isShowHistory() {
        return showHistory;
    }

    public void setShowHistory(boolean showHistory) {
        this.showHistory = showHistory;
    }


}
