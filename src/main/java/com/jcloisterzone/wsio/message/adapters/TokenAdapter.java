package com.jcloisterzone.wsio.message.adapters;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.BridgeCapability.BrigeToken;
import com.jcloisterzone.game.capability.CastleCapability.CastleToken;
import com.jcloisterzone.game.capability.FerriesCapability.FerryToken;
import com.jcloisterzone.game.capability.GoldminesCapability.GoldToken;
import com.jcloisterzone.game.capability.LittleBuildingsCapability.LittleBuilding;
import com.jcloisterzone.game.capability.TowerCapability.TowerToken;
import com.jcloisterzone.game.capability.TunnelCapability.Tunnel;

public class TokenAdapter extends TypeAdapter<Token> {

    @Override
    public void write(JsonWriter out, Token value) throws IOException {
        out.beginArray();
        out.value(value.getClass().getName());
        out.value(value.name());
        out.endArray();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Token read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.STRING) {
            // backward compatibility: version < 4.4.0
            String token = in.nextString();
            switch (token) {
            case "TOWER_PIECE": return TowerToken.TOWER_PIECE;
            case "BRIDGE": return BrigeToken.BRIDGE;
            case "LB_SHED": return LittleBuilding.LB_SHED;
            case "LB_HOUSE": return LittleBuilding.LB_HOUSE;
            case "LB_TOWER": return LittleBuilding.LB_TOWER;
            case "CASTLE": return CastleToken.CASTLE;
            case "FERRY": return FerryToken.FERRY;
            case "GOLD": return GoldToken.GOLD;
            case "TUNNEL_A": return Tunnel.TUNNEL_A;
            case "TUNNEL_B": return Tunnel.TUNNEL_B;
            case "TUNNEL_C": return Tunnel.TUNNEL_C;
            }
            throw new IOException("Unknown token " + token);
        }
        in.beginArray();
        String clsName = in.nextString();
        String tokenName = in.nextString();
        in.endArray();
        try {
            Class<? extends Token> cls = (Class<? extends Token>) Class.forName(clsName, true, Token.class.getClassLoader());
            return (Token) cls.getMethod("valueOf", String.class).invoke(null, tokenName);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException | ClassNotFoundException e) {
            throw new IOException(e);
        }

    }

}
