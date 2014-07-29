package com.jcloisterzone.wsio.message;

public class RandSampleMessage {

    private String gameId;
    private String name;
    int population;
    int[] values;

    public RandSampleMessage(String gameId, String name, int population, int[] values) {
        super();
        this.gameId = gameId;
        this.name = name;
        this.population = population;
        this.values = values;
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

    public int[] getValues() {
        return values;
    }

    public void setValues(int[] values) {
        this.values = values;
    }
}
