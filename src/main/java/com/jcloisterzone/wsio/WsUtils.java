package com.jcloisterzone.wsio;

import java.lang.reflect.Method;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.jcloisterzone.wsio.message.CreateGameMessage;
import com.jcloisterzone.wsio.message.FlierDiceMessage;
import com.jcloisterzone.wsio.message.RandSampleMessage;
import com.jcloisterzone.wsio.message.GameMessage;
import com.jcloisterzone.wsio.message.GameSetupMessage;
import com.jcloisterzone.wsio.message.HelloMessage;
import com.jcloisterzone.wsio.message.JoinGameMessage;
import com.jcloisterzone.wsio.message.LeaveSlotMessage;
import com.jcloisterzone.wsio.message.RmiMessage;
import com.jcloisterzone.wsio.message.GetRandSampleMessage;
import com.jcloisterzone.wsio.message.RollFlierDiceMessage;
import com.jcloisterzone.wsio.message.SetExpansionMessage;
import com.jcloisterzone.wsio.message.SetRuleMessage;
import com.jcloisterzone.wsio.message.SlotMessage;
import com.jcloisterzone.wsio.message.StartGameMessage;
import com.jcloisterzone.wsio.message.TakeSlotMessage;
import com.jcloisterzone.wsio.message.WelcomeMessage;

public final class WsUtils {

    private Gson gson = new Gson();
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, Class<?>> types =  new ImmutableMap.Builder<String, Class<?>>()
            .put("ERR", String.class)
            .put("HELLO", HelloMessage.class)
            .put("WELCOME", WelcomeMessage.class)
            .put("CREATE_GAME", CreateGameMessage.class)
            .put("JOIN_GAME", JoinGameMessage.class)
            .put("GAME", GameMessage.class)
            .put("GAME_SETUP", GameSetupMessage.class)
            .put("TAKE_SLOT", TakeSlotMessage.class)
            .put("LEAVE_SLOT", LeaveSlotMessage.class)
            .put("SLOT", SlotMessage.class)
            .put("SET_EXPANSION", SetExpansionMessage.class)
            .put("SET_RULE", SetRuleMessage.class)
            .put("START_GAME", StartGameMessage.class)
            .put("GET_RAND_SAMPLE", GetRandSampleMessage.class)
            .put("RAND_SAMPLE", RandSampleMessage.class)
            .put("ROLL_FLIER_DICE", RollFlierDiceMessage.class)
            .put("FLIER_DICE", FlierDiceMessage.class)
            .put("RMI", RmiMessage.class)
            .build();

    public static class Command {
        public String command;
        public Object arg;

        public Command(String command, Object arg) {
            this.command = command;
            this.arg = arg;
        }
    }

    public Command fromJson(String message) {
        String s[] = message.split(" ", 2); //command, arg
        Class<?> type = types.get(s[0]);
        if (type == null) {
            throw new IllegalArgumentException("Mapping type is not declared for "+s[0]);
        }
        Object arg = "null".equals(s[1]) ? null : gson.fromJson(s[1], type);
        return new Command(s[0], arg);
    }

    public String toJson(String command, Object arg) {
        return command + " " + gson.toJson(arg);
    }

    //TODO what about cache targets
    public void delegate(Object target, Object subject, Command cmd) {
        Class<?> type = target.getClass();
        while (true) {
            if (delegateScanClass(target, type, subject, cmd)) break;
            type = type.getSuperclass();
            if (Object.class.equals(type)) break;
        }
    }

    private boolean delegateScanClass(Object target, Class<?> type, Object subject, Command cmd) {
        for (Method m : type.getDeclaredMethods()) {
            CmdHandler handler = m.getAnnotation(CmdHandler.class);
            if (handler != null && handler.value().equals(cmd.command)) {
                try {
                    m.invoke(target, subject, cmd.arg);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                return true;
            }
        }
        return false;
    }

}
