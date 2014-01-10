package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Castle;

public class CastleDeployedEvent extends Event {

    private final Castle part1, part2;

    public CastleDeployedEvent(Player player, Castle part1, Castle part2) {
          super(player);
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
