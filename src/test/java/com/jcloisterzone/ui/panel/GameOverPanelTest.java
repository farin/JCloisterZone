package com.jcloisterzone.ui.panel;

import com.jcloisterzone.Player;
import com.jcloisterzone.PlayerClock;
import com.jcloisterzone.game.Game;
import io.vavr.collection.Array;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.jcloisterzone.ui.panel.GameOverPanel.formatPercentageString;
import static com.jcloisterzone.ui.panel.GameOverPanel.formatPlaytimeString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore  // depends on ui which is not avaiable in WSL
@RunWith(MockitoJUnitRunner.class)
public class GameOverPanelTest {

    @Test
    public void shouldCalculatePlaytimeCorrectly() {
        Game game = mock(Game.class);
        Player player = mock(Player.class);

        Array<PlayerClock> playerClocks = Array.of(new PlayerClock(20_052_123));
        when(game.getClocks()).thenReturn(playerClocks);
        when(player.getIndex()).thenReturn(0);

        Playtime playtime = new Playtime(game, player, 30_000);

        assertEquals(5, playtime.getHours());
        assertEquals(34, playtime.getMinutes());
        assertEquals(12, playtime.getSeconds());
        assertEquals(8, playtime.getTotalHours());
        assertEquals(20, playtime.getTotalMinutes());
        assertEquals(0, playtime.getTotalSeconds());
        assertEquals(66.84f, playtime.getPercentage(), 1e-2);
    }

    @Test
    public void shouldFormatPercentageStringCorrectly() {
        Playtime playtimeMock = mock(Playtime.class);
        when(playtimeMock.getPercentage()).thenReturn(66.84f);

        String percentageString = formatPercentageString(playtimeMock);

        assertEquals("67%", percentageString);
    }

    @Test
    public void shouldFormatPlaytimeStringCorrectly() {
        Playtime playtimeMock = mock(Playtime.class);
        when(playtimeMock.getHours()).thenReturn(5);
        when(playtimeMock.getMinutes()).thenReturn(34);
        when(playtimeMock.getSeconds()).thenReturn(12);
        when(playtimeMock.getTotalHours()).thenReturn(8);
        when(playtimeMock.getTotalMinutes()).thenReturn(20);
        when(playtimeMock.getTotalSeconds()).thenReturn(0);

        String playtimeString = formatPlaytimeString(playtimeMock);

        assertEquals("5:34:12/\n8:20:00", playtimeString);
    }
}
