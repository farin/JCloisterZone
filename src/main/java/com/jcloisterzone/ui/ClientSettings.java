package com.jcloisterzone.ui;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;


//TODO possible with merge config

public class ClientSettings {

    private boolean playBeep;
    private boolean confirmFarmPlacement;
    private boolean confirmTowerPlacement;
    private boolean confirmGameClose;
    private boolean confirmRansomPayment;

    private boolean showHistory;

    public ClientSettings(Ini config) {
        Section uiConfig = config.get("settings");
        playBeep = uiConfig.get("beep_alert", boolean.class);
        confirmFarmPlacement = uiConfig.get("confirm_farm_place", boolean.class);
        confirmTowerPlacement = uiConfig.get("confirm_tower_place", boolean.class);
        confirmGameClose = uiConfig.get("confirm_game_close", boolean.class);
        confirmRansomPayment = uiConfig.get("confirm_ransom_payment", boolean.class);
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
