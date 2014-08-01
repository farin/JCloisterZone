package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("GET_RAND_SAMPLE")
public class GetRandSampleMessage implements WsMessage {

    private String gameId;
    private String name;
    private int population, k;

    public GetRandSampleMessage(String gameId, String name, int population, int k) {
        this.gameId = gameId;
        this.name = name;
        this.population = population;
        this.k = k;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }
}
