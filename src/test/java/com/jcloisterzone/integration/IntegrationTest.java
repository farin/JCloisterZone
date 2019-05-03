package com.jcloisterzone.integration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.net.URL;

import com.google.gson.stream.JsonReader;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.game.GameSetup;
import com.jcloisterzone.game.GameStatePhaseReducer;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.PlayerSlot.SlotState;
import com.jcloisterzone.game.phase.Phase;
import com.jcloisterzone.game.save.SavedGame;
import com.jcloisterzone.game.save.SavedGame.SavedGamePlayerSlot;
import com.jcloisterzone.game.save.SavedGameParser;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.GameStateBuilder;
import com.jcloisterzone.wsio.message.WsReplayableMessage;
import com.jcloisterzone.wsio.message.WsSaltMessage;


public class IntegrationTest {

    private PlayerSlot[] createPlayerSlots(SavedGame savedGame) {
        PlayerSlot[] slots = new PlayerSlot[savedGame.getSlots().size()];
        int idx = 0;
        for (SavedGamePlayerSlot sgSlot : savedGame.getSlots()) {
            slots[idx] = new PlayerSlot(idx);
            slots[idx].setNickname(sgSlot.getNickname());
            slots[idx].setSerial(sgSlot.getSerial());
            slots[idx].setAiClassName(sgSlot.getAiClassName());
            slots[idx].setState(SlotState.OWN);
            idx++;
        }
        return slots;
    }

    protected GameState createGameState(String savedGameFile) {
        Config config = new Config();
        SavedGameParser parser = new SavedGameParser();
        JsonReader reader;
        try {
            URL resource = getClass().getClassLoader().getResource(savedGameFile);
            if (resource == null) {
                return null;
            }
            else {
                reader = new JsonReader(new FileReader(new File(resource.toURI())));
            }
        } catch (FileNotFoundException | URISyntaxException e1) {
            throw new RuntimeException(e1);
        }
        SavedGame sg = parser.fromJson(reader);

        GameSetup setup = sg.getSetup().asGameSetup();
        PlayerSlot[] slots = createPlayerSlots(sg);
        GameStatePhaseReducer phaseReducer = new GameStatePhaseReducer(setup, sg.getInitialSeed());
        GameStateBuilder builder = new GameStateBuilder(setup, slots, config);
        builder.setGameAnnotations(sg.getAnnotations());

        GameState state = builder.createInitialState();

        Phase firstPhase = phaseReducer.getFirstPhase();
        state = builder.createReadyState(state);
        state = state.setPhase(firstPhase.getClass());
        state = phaseReducer.applyStepResult(firstPhase.enter(state));
        for (WsReplayableMessage msg : sg.getReplay()) {
            if (msg instanceof WsSaltMessage) {
                phaseReducer.getRandom().setSalt(((WsSaltMessage) msg).getSalt());
            }
            state = phaseReducer.apply(state, msg);
        }

        return state;
    }

}
