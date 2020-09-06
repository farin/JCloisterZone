package com.jcloisterzone.game.state;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.Token;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Option;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Represents the state of all players in a game.
 */
@Immutable
public class PlayersState implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Array<Player> players;
    private final Array<Integer> score;
    private final Array<Map<Token, Integer>> tokens;
    private final Integer turnPlayerIndex;

    private final Array<Seq<Follower>> followers;
    private final Array<Seq<Special>> specialMeeples;

    /**
     * Create the initial players state.
     *
     * @param players         the players
     * @param turnPlayerIndex the turn player index
     * @return the players state
     */
    public static PlayersState createInitial(Array<Player> players, Integer turnPlayerIndex) {
        return new PlayersState(
            players,
            players.map(p -> 0),
            players.map(p -> HashMap.empty()),
            turnPlayerIndex,
            null,
            null
        );
    }

    /**
     * Instantiates a new {@code PlayersState}.
     *
     * @param players         the players
     * @param score           the score
     * @param tokens          the tokens
     * @param turnPlayerIndex the turn player index
     * @param followers       the followers
     * @param specialMeeples  the special meeples
     */
    public PlayersState(
            Array<Player> players,
            Array<Integer> score,
            Array<Map<Token, Integer>> tokens,
            Integer turnPlayerIndex,
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

    /**
     * Sets the scores of all players.
     *
     * @param score the scores
     * @return a new instance with the scores updated
     */
    public PlayersState setScore(Array<Integer> score) {
        if (this.score == score) return this;
        return new PlayersState(
            players, score, tokens, turnPlayerIndex,
            followers, specialMeeples
        );
    }

    /**
     * Sets the tokens of all players (i.e., the pieces in their supply, such as bridges, castles, abbey tiles etc.).
     *
     * @param tokens the tokens
     * @return a new instance with the tokens updated
     */
    public PlayersState setTokens(Array<Map<Token, Integer>> tokens) {
        if (this.tokens == tokens) return this;
        return new PlayersState(
            players, score, tokens, turnPlayerIndex,
            followers, specialMeeples
        );
    }

    /**
     * Sets the token count for a specified player.
     *
     * @param index the index of the player
     * @param token the token type to update
     * @param count the new amount
     * @return a new instance with the token count updated
     */
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

    /**
     * Sets the count of a specific token for all players.
     *
     * @param token the token type of interest
     * @param count the amount
     * @return a new instance with the token counts updated
     */
    public PlayersState setTokenCountForAllPlayers(Token token, int count) {
        PlayersState ps = this;
        for (Player p : getPlayers()) {
           ps = ps.setTokenCount(p.getIndex(), token, count);
        }
        return ps;
    }

    /**
     * Adds tokens of a specified type to a given player.
     *
     * @param index the index of the player
     * @param token the token type to update
     * @param count the amount to merge
     * @return a new instance with the token count updated
     */
    public PlayersState addTokenCount(int index, Token token, int count) {
        if (count == 0) return this;
        int newValue = getPlayerTokenCount(index, token) + count;
        return setTokenCount(index, token, newValue);
    }

    /**
     * Sets the turn player index.
     *
     * @param turnPlayerIndex the turn player index
     * @return a new instance with the turn player index updated
     */
    public PlayersState setTurnPlayerIndex(Integer turnPlayerIndex) {
        if (Objects.equals(this.turnPlayerIndex, turnPlayerIndex)) return this;
        return new PlayersState(
            players, score, tokens, turnPlayerIndex,
            followers, specialMeeples
        );
    }

    /**
     * Sets followers for all players.
     *
     * @param followers the followers for all players
     * @return a new instance with the followers updated
     */
    public PlayersState setFollowers(Array<Seq<Follower>> followers) {
        if (this.followers == followers) return this;
        return new PlayersState(
            players, score, tokens, turnPlayerIndex,
            followers, specialMeeples
        );
    }

    /**
     * Sets special meeples for all players.
     *
     * @param specialMeeples the special meeples
     * @return a new instance with the special meeples updated
     */
    public PlayersState setSpecialMeeples(Array<Seq<Special>> specialMeeples) {
        if (this.specialMeeples == specialMeeples) return this;
        return new PlayersState(
            players, score, tokens, turnPlayerIndex,
            followers, specialMeeples
        );
    }

    /**
     * Gets the players data.
     *
     * @return the players data
     */
    public Array<Player> getPlayers() {
        return players;
    }

    /**
     * Gets players data in sequence starting by player {@code p}.
     *
     * @param p the player to start with
     * @return the players data
     */
    public Array<Player> getPlayersBeginWith(Player p) {
        return players.slice(p.getIndex(), players.length())
            .appendAll(players.slice(0, p.getIndex()));
    }

    /**
     * Gets a player data by its index.
     *
     * @param idx the index
     * @return the player data
     */
    public Player getPlayer(int idx) {
        return players.get(idx);
    }

    /**
     * Gets the number of players.
     *
     * @return the number of players
     */
    public int length() {
        return players.length();
    }

    /**
     * Gets the scores of all players.
     *
     * @return the scores of all players
     */
    public Array<Integer> getScore() {
        return score;
    }

    /**
     * Gets the tokens data of all players.
     *
     * @return the tokens data
     */
    public Array<Map<Token, Integer>> getTokens() {
        return tokens;
    }

    /**
     * Gets the token count of a specific type of token for a given player.
     *
     * @param index the index of the player
     * @param token the token type of interest
     * @return the token count
     */
    public int getPlayerTokenCount(int index, Token token) {
        Map<Token, Integer> playerTokens = tokens.get(index);
        return playerTokens.get(token).getOrElse(0);
    }

    public Player getPlayerWithToken(Token token) {
        int playersCount = players.length();
        for (int i = 0; i < playersCount; i++) {
            if (tokens.get(i).get(token).getOrElse(0) > 0) {
                return players.get(i);
            }
        }
        return null;
    }

    /**
     * Gets the turn player index.
     *
     * @return the turn player index
     */
    public Integer getTurnPlayerIndex() {
        return turnPlayerIndex;
    }

    /**
     * Gets the followers of all players.
     *
     * @return the followers
     */
    public Array<Seq<Follower>> getFollowers() {
        return followers;
    }

    /**
     * Gets the special meeples of all players.
     *
     * @return the special meeples
     */
    public Array<Seq<Special>> getSpecialMeeples() {
        return specialMeeples;
    }

    /**
     * Finds a follower by id.
     *
     * @param meepleId the meeple id
     * @return {@code Some} if the follower is found, {@code None} otherwise
     */
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

    /**
     * Gets the turn player.
     *
     * @return the turn player
     */
    public Player getTurnPlayer() {
        return turnPlayerIndex == null ? null : getPlayer(turnPlayerIndex);
    }
}
