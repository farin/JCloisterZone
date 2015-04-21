package com.jcloisterzone.feature.score;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;

public interface ScoringStrategy {

	void addPoints(Player player, int points, PointCategory category);

}
