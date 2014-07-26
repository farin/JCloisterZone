package com.jcloisterzone.wsio;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.jcloisterzone.wsio.message.CreateGameMessage;
import com.jcloisterzone.wsio.message.GameMessage;
import com.jcloisterzone.wsio.message.HelloMessage;
import com.jcloisterzone.wsio.message.JoinGameMessage;
import com.jcloisterzone.wsio.message.WelcomeMessage;

public final class MessageParser {

    private Gson gson = new Gson();
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, Class<?>> types =  new ImmutableMap.Builder<String, Class<?>>()
            .put("ERR", String.class)
            .put("HELLO", HelloMessage.class)
            .put("WELCOME", WelcomeMessage.class)
            .put("CREATE_GAME", CreateGameMessage.class)
            .put("JOIN_GAME", JoinGameMessage.class)
            .put("GAME", GameMessage.class)

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

}
