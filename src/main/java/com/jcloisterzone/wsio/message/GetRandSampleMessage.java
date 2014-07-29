package com.jcloisterzone.wsio.message;

public class GetRandSampleMessage {

    private String gameId;
    private String name;
    int population, k;

    public GetRandSampleMessage(String gameId, String name, int population, int k) {
        super();
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
