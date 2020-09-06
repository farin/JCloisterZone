package com.jcloisterzone.game.state;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.game.capability.TunnelCapability.Tunnel;

import java.io.Serializable;
import java.util.Objects;

@Immutable
public class PlacedTunnelToken implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int playerIndex;
    private final Tunnel token;

    public PlacedTunnelToken(int playerIndex, Tunnel token) {
        assert playerIndex > -1;
        this.playerIndex = playerIndex;
        this.token = token;
    }

    public int getPlayerIndex() {
        return playerIndex;
    }

    public Tunnel getToken() {
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
