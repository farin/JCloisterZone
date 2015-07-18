# Upcoming changes

## Done in current dev

- preferences dialog to change settings from app
- improved TO, CC, GQ, AM images quality
- changed meeple selection (eg. princess undeploy, crop circles undeploy etc.) - instead selecting tile feature, meeple itself is highlighted directly
- rotate board fixes - don't rotate fairy and dragon (#145), gold counters and little building selection.
- rules update - Wagon can not choose which tile to move after completing a feature (#148)
- rules update - added and make default HiG fairy scoring option - fairy is placed next to follower instead of on tile (#137)
- slightly changed way how are mouseover handled for overlapping feature areas  - instead selecting none, a higher one is selected.
- fix: custom rule "Tiny city is scored only for 2 points" + unfinished two tile city with cathedral (gives 2pts at end instead of 0)
- fix #34, #155: relax legal river checking - now river is almost always legally finished, don't end game in rare case when lake can't fit   
- fix #135: Gold piece distribution is incorrect
- fix #139: abbey at game end
- fix #140: AI always use big follower
- fix #147: Missing farms on Abbey & Mayor tiles (small farms between crossed cities)
- fix #149: little building can be scored multiple times for one feature
- fix #150: Wagon can move to a tile occupied by dragon


## Roadmap

*  custom configuration - eg. tiles from Abbey&Mayor + wagon but  no Abbey and no Mayor.
* Hills ans Sheeps
* add AI support for more expansions
* .. and more expansions :)