# JCloisterZone

JCloisterZone is a Java implementation of a popular board game Carcassonne.
It supports multiplayer game on a local computer or over a network.
Game can be also played against computer AI.

## Development guide

Helpers for more pleasant developement and application debugging.

### VM arguments

use different config.ini, don't create error.log (console out is enough), change log level and enable assertions

    -Dconfig=debug.ini -DerrorLog=false -Dorg.slf4j.simpleLogger.defaultLogLevel=info -ea


### debug.ini

tweaked config.ini

use unpacked pluging from source

    [plugins]
    plugin = plugins/classic
    plugin = plugins/rgg_siege

for immediately AI play comment delay option

    [players]
    ;ai_place_tile_delay = 200

#### [debug] section

use debug section for fast and repeatable game setup with following possible options

    [debug]

don't compress saves, autosave before each AI play

    save_format = plain
    save_before_ranking = saves/_prerank.jcz

skip game config dialog, player is name or AI class

    autostart = true
    autostart_player = Alice
    autostart_player = Bob
    autostart_player = com.jcloisterzone.ai.legacyplayer.LegacyAiPlayer

developing expansion, don't bother with basic tiles. Override any expansion with own set definition.

    tiles_BASIC = tile-definitions/basic-only-starting.xml

force drawn tiles

    draw=BA.C
    draw=BA.Rr
    draw=IC.CCCC

and then force final scoring

    draw=!

experimental options

    off_capabilities = Dragon, Fairy
    area_highlight = figure