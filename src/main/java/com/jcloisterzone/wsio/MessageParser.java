package com.jcloisterzone.wsio;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.wsio.message.AbandonGameMessage;
import com.jcloisterzone.wsio.message.ChannelMessage;
import com.jcloisterzone.wsio.message.ChatMessage;
import com.jcloisterzone.wsio.message.ClientUpdateMessage;
import com.jcloisterzone.wsio.message.ClockMessage;
import com.jcloisterzone.wsio.message.CreateGameMessage;
import com.jcloisterzone.wsio.message.DeployFlierMessage;
import com.jcloisterzone.wsio.message.EndTurnMessage;
import com.jcloisterzone.wsio.message.ErrorMessage;
import com.jcloisterzone.wsio.message.GameMessage;
import com.jcloisterzone.wsio.message.GameOverMessage;
import com.jcloisterzone.wsio.message.GameSetupMessage;
import com.jcloisterzone.wsio.message.GameUpdateMessage;
import com.jcloisterzone.wsio.message.HelloMessage;
import com.jcloisterzone.wsio.message.JoinGameMessage;
import com.jcloisterzone.wsio.message.LeaveGameMessage;
import com.jcloisterzone.wsio.message.LeaveSlotMessage;
import com.jcloisterzone.wsio.message.PingMessage;
import com.jcloisterzone.wsio.message.PongMessage;
import com.jcloisterzone.wsio.message.PostChatMessage;
import com.jcloisterzone.wsio.message.RmiMessage;
import com.jcloisterzone.wsio.message.SetExpansionMessage;
import com.jcloisterzone.wsio.message.SetRuleMessage;
import com.jcloisterzone.wsio.message.SlotMessage;
import com.jcloisterzone.wsio.message.StartGameMessage;
import com.jcloisterzone.wsio.message.TakeSlotMessage;
import com.jcloisterzone.wsio.message.ToggleClockMessage;
import com.jcloisterzone.wsio.message.UndoMessage;
import com.jcloisterzone.wsio.message.WelcomeMessage;
import com.jcloisterzone.wsio.message.WsMessage;

public final class MessageParser {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Gson gson;
    private final Map<String, Class<? extends WsMessage>> types = new HashMap<>();

    public static class CustomRulesMapAdapter extends TypeAdapter<Map<CustomRule, Object>> {

        @Override
        public void write(JsonWriter out, Map<CustomRule, Object> value) throws IOException {
            out.beginObject();
            for (Entry<CustomRule, Object> entry : value.entrySet()) {
                out.name(entry.getKey().name());
                if (entry.getValue() instanceof Boolean) {
                	out.value((Boolean)entry.getValue());
                } else if (entry.getValue() instanceof Integer) {
                	out.value((Integer)entry.getValue());
                } else {
                	out.value(entry.getValue().toString());
                }
            }
            out.endObject();
        }

        @Override
        public Map<CustomRule, Object> read(JsonReader in) throws IOException {
            Map<CustomRule, Object> result = new HashMap<>();
            in.beginObject();
            while (in.hasNext()) {
                CustomRule rule = CustomRule.valueOf(in.nextName());
                JsonToken p = in.peek();
                if (p == JsonToken.BOOLEAN) {
                	result.put(rule, in.nextBoolean());
                } else if (p == JsonToken.NUMBER) {
                	result.put(rule, in.nextInt());
                } else {
                	result.put(rule, rule.unpackValue(in.nextString()));
                }
            }
            in.endObject();
            return result;
        }
    }

    public MessageParser() {
        GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();

        builder.registerTypeAdapter(SetRuleMessage.class, new JsonDeserializer<SetRuleMessage>() {
            @Override
            public SetRuleMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject obj = json.getAsJsonObject();
                CustomRule rule = CustomRule.valueOf(obj.get("rule").getAsString());
                return new SetRuleMessage(
                        obj.get("gameId").getAsString(), rule,
                        obj.get("value") == null ? null : rule.unpackValue(obj.get("value").getAsString())
                );
            }
        });
        gson = builder.create();

        registerMsgType(ErrorMessage.class);
        registerMsgType(HelloMessage.class);
        registerMsgType(WelcomeMessage.class);
        registerMsgType(CreateGameMessage.class);
        registerMsgType(JoinGameMessage.class);
        registerMsgType(LeaveGameMessage.class);
        registerMsgType(AbandonGameMessage.class);
        registerMsgType(GameMessage.class);
        registerMsgType(GameSetupMessage.class);
        registerMsgType(TakeSlotMessage.class);
        registerMsgType(LeaveSlotMessage.class);
        registerMsgType(SlotMessage.class);
        registerMsgType(SetExpansionMessage.class);
        registerMsgType(SetRuleMessage.class);
        registerMsgType(StartGameMessage.class);
        registerMsgType(DeployFlierMessage.class);
        registerMsgType(RmiMessage.class);
        registerMsgType(UndoMessage.class);
        registerMsgType(ClientUpdateMessage.class);
        registerMsgType(GameUpdateMessage.class);
        registerMsgType(PostChatMessage.class);
        registerMsgType(ChatMessage.class);
        registerMsgType(ChannelMessage.class);
        registerMsgType(GameOverMessage.class);
        registerMsgType(PingMessage.class);
        registerMsgType(PongMessage.class);
        registerMsgType(ToggleClockMessage.class);
        registerMsgType(ClockMessage.class);
        registerMsgType(EndTurnMessage.class);
    }

    protected String getCmdName(Class<? extends WsMessage> msgType) {
        return msgType.getAnnotation(WsMessageCommand.class).value();
    }

    private void registerMsgType(Class<? extends WsMessage> type) {
        types.put(getCmdName(type), type);
    }

    public WsMessage fromJson(String payload) {
        String s[] = payload.split(" ", 2); //command, arg
        Class<? extends WsMessage> type = types.get(s[0]);
        if (type == null) {
            throw new IllegalArgumentException("Mapping type is not declared for "+s[0]);
        }
        return gson.fromJson(s[1], type);
    }

    public String toJson(WsMessage arg) {
        return getCmdName(arg.getClass()) + " " + gson.toJson(arg);
    }
}
