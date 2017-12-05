package com.jcloisterzone.game.state;

import java.io.Serializable;
import java.util.Objects;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.game.Token;

@Immutable
public class PlacedTunnelToken implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int playerIndex;
    private final Token token;

    public PlacedTunnelToken(int playerIndex, Token token) {
        assert playerIndex > -1;
        assert token == Token.TUNNEL_A || token == Token.TUNNEL_B || token == Token.TUNNEL_C;
        this.playerIndex = playerIndex;
        this.token = token;
    }

    public int getPlayerIndex() {
        return playerIndex;
    }

    public Token getToken() {
        return token;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerIndex, token);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PlacedTunnelToken other = (PlacedTunnelToken) obj;
        if (playerIndex != other.playerIndex)
            return false;
        if (token != other.token)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return playerIndex + " " + token;
    }



}
