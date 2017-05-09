package com.jcloisterzone.game.state;

import java.io.Serializable;
import java.util.function.Predicate;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.PlayerScore;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.Token;

import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Option;

@Immutable
public class PlayersState implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Array<Player> players;
    private final Array<PlayerScore> score;
    private final Array<Map<Token, Integer>> tokens;
    private final int turnPlayerIndex;

    private final Array<Seq<Follower>> followers;
    private final Array<Seq<Special>> specialMeeples;

    public static PlayersState createInitial(Array<Player> players, int turnPlayerIndex) {
        return new PlayersState(
            players,
            players.map(p -> new PlayerScore()),
            players.map(p -> HashMap.empty()),
            turnPlayerIndex,
            null,
            null
        );
    }

    public PlayersState(
            Array<Player> players,
            Array<PlayerScore> score,
            Array<Map<Token, Integer>> tokens,
            int turnPlayerIndex,
            Array<Seq<Follower>> followers,
            Array<Seq<Special>> specialMeeples
    ) {
        this.players = players;
        this.score = score;
        this.tokens = tokens;
        this.turnPlayerIndex = turnPlayerIndex;
        this.followers = followers;
        this.specialMeeples = specialMeeples;
    }

    public PlayersState setScore(Array<PlayerScore> score) {
        if (this.score == score) return this;
        return new PlayersState(
            players, score, tokens, turnPlayerIndex,
            followers, specialMeeples
        );
    }

    public PlayersState setTokens(Array<Map<Token, Integer>> tokens) {
        if (this.tokens == tokens) return this;
        return new PlayersState(
            players, score, tokens, turnPlayerIndex,
            followers, specialMeeples
        );
    }

    public PlayersState setTokenCount(int index, Token token, int count) {
        if (count < 0) {
            throw new IllegalArgumentException(String.format("Token %s count can't be %s", token, count));
        }
        Map<Token, Integer> playerTokens = tokens.get(index);
        if (playerTokens.get(token).getOrElse(0) == count) {
            return this;
        }
        if (count == 0) {
            return setTokens(tokens.update(index, playerTokens.remove(token)));
        } else {
            return setTokens(tokens.update(index, playerTokens.put(token, count)));
        }
    }

    public PlayersState setTokenCountForAllPlayers(Token token, int count) {
        PlayersState ps = this;
        for (Player p : getPlayers()) {
           ps = ps.setTokenCount(p.getIndex(), token, 1);
        }
        return ps;
    }

    public PlayersState addTokenCount(int index, Token token, int count) {
        if (count == 0) return this;
        int newValue = getPlayerTokenCount(index, token) + count;
        return setTokenCount(index, token, newValue);
    }

    public PlayersState setTurnPlayerIndex(int turnPlayerIndex) {
        if (this.turnPlayerIndex == turnPlayerIndex) return this;
        return new PlayersState(
            players, score, tokens, turnPlayerIndex,
            followers, specialMeeples
        );
    }

    public PlayersState setFollowers(Array<Seq<Follower>> followers) {
        if (this.followers == followers) return this;
        return new PlayersState(
            players, score, tokens, turnPlayerIndex,
            followers, specialMeeples
        );
    }

    public PlayersState setSpecialMeeples(Array<Seq<Special>> specialMeeples) {
        if (this.specialMeeples == specialMeeples) return this;
        return new PlayersState(
            players, score, tokens, turnPlayerIndex,
            followers, specialMeeples
        );
    }

    public Array<Player> getPlayers() {
        return players;
    }

    public Array<Player> getPlayersBeginWith(Player p) {
        return players.slice(p.getIndex(), players.length())
            .appendAll(players.slice(0, p.getIndex()));
    }

    public Player getPlayer(int idx) {
        return players.get(idx);
    }

    public int length() {
        return players.length();
    }

    public Array<PlayerScore> getScore() {
        return score;
    }

    public Array<Map<Token, Integer>> getTokens() {
        return tokens;
    }

    public int getPlayerTokenCount(int index, Token token) {
        Map<Token, Integer> playerTokens = tokens.get(index);
        return playerTokens.get(token).getOrElse(0);
    }

    public int getTurnPlayerIndex() {
        return turnPlayerIndex;
    }

    public Array<Seq<Follower>> getFollowers() {
        return followers;
    }

    public Array<Seq<Special>> getSpecialMeeples() {
        return specialMeeples;
    }

    public Option<Follower> findFollower(String meepleId) {
        Predicate<Follower> pred = f -> f.getId().equals(meepleId);
        for (Seq<Follower> l : followers) {
            Option<Follower> res = l.find(pred);
            if (!res.isEmpty()) {
                return res;
            }
        }
        return Option.none();
    }

    public Player getTurnPlayer() {
        return getPlayer(turnPlayerIndex);
    }

    @Override
    public String toString() {
        return String.join(",", players.map(p -> p.getNick()));
    }
}
