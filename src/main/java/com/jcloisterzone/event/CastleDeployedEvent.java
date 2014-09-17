package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Castle;

public class CastleDeployedEvent extends PlayEvent {

    private final Castle part1, part2;

    public CastleDeployedEvent(Player triggeringPlayer, Castle part1, Castle part2) {
          super(triggeringPlayer, null);
          this.part1 = part1;
          this.part2 = part2;
    }

    public Castle getPart1() {
        return part1;
    }

    public Castle getPart2() {
        return part2;
    }
}
