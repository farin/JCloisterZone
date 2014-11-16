# JCloisterZone

JCloisterZone is a Java implementation of a popular board game Carcassonne.
It supports multiplayer game on a local computer or over a network.
Game can be also played against computer AI.

## Development guide

Helpers for convenient development and application debugging.

### VM arguments

use different configuration file, don't create error.log (console out is enough), change log level and enable assertions

    -Dconfig=debug.yaml -DerrorLog=false -Dorg.slf4j.simpleLogger.defaultLogLevel=info -DforceChat=true -ea


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
you can comment just preset key to disable whole autostart

    autostart:
      preset: default
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

### gettext

    xgettext -k_ -o po/keys.pot --from-code=utf-8 $(find . -name "*.java")
    msgmerge -N -U po/cs.po po/keys.pot
    msgmerge -N -U po/de.po po/keys.pot
    msgmerge -N -U po/el.po po/keys.pot
    msgmerge -N -U po/en.po po/keys.pot
    msgmerge -N -U po/es.po po/keys.pot
    msgmerge -N -U po/fi.po po/keys.pot
    msgmerge -N -U po/fr.po po/keys.pot
    msgmerge -N -U po/hu.po po/keys.pot
    msgmerge -N -U po/it.po po/keys.pot
    msgmerge -N -U po/pl.po po/keys.pot
    msgmerge -N -U po/ro.po po/keys.pot
    msgmerge -N -U po/ru.po po/keys.pot
    msgmerge -N -U po/sk.po po/keys.pot
    rm po/*~
    rm po/keys.pot