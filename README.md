# JCloisterZone

JCloisterZone is a Java implementation of a popular board game Carcassonne.
It supports multiplayer game on a local computer or over a network.
Game can be also played against computer AI.

## Development guide

Helpers for more pleasant development and application debugging.

### VM arguments

use different configuration file, don't create error.log (console out is enough), change log level and enable assertions

    -Dconfig=debug.yaml -DerrorLog=false -Dorg.slf4j.simpleLogger.defaultLogLevel=info -ea


### debug.yaml

tweaked config.yaml

use unpacked plugins from source

    plugins:
      - plugins/classic
      - plugins/rgg_siege

for immediately AI play comment delay option

    # ai_place_tile_delay: 250

#### debug options

use debug options for quick and repeatable game setup with following possible options

    debug:
      # use some keys described below ...

don't compress saves, autosave before each AI play

    save_format: plain
    autosave: saves/_prerank.jcz

skip game config dialog, player is name or AI class
you can comment just profile key to disable whole autostart

    autostart:
      profile: default
      players:
        - Alice
        - Bob
        - com.jcloisterzone.ai.legacyplayer.LegacyAiPlayer
        - com.jcloisterzone.ai.DummyAiPlayer

developing expansion, don't bother with basic tiles. Override any expansion with own set definition.

    tile_definitions:
      BASIC: tile-definitions/basic-1card.xml

force drawn tiles

    draw:
      - BA.C
      - BA.Cccc+

and then force final scoring with dot item

    draw:
       # ... some tiles ...
       - .

experimental options

    off_capabilities: [ Dragon, Fairy ]
    area_highlight: figure