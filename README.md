
# JCloisterZone

JCloisterZone is implementation of a popular board game [Carcassonne](http://www.boardgamegeek.com/boardgame/822/carcassonne).

Project website can be found under [https://jcloisterzone.com]

## JCloisterZone Platform

In 2020 (after 16 yeast of project existence) is time for essential change. New UI is going to be introduced, based
on completely different technology (Electron + Vue.JS).

This repository no longer contains full application, but only Java game engine.
Old whole Java application with client archived in [4.x branch](https://github.com/farin/JCloisterZone/tree/4.x).

JCloisterZone Client app can be found in [farin/JCloisterZone-Client](https://github.com/farin/JCloisterZone-Client) repository.

## Supported Expansions

List of supported expansions can found [here](https://github.com/farin/JCloisterZone/tree/master/src/main/resources/tile-definitions).

## Development recipes

Dump features map
`
    System.err.println("# features: " + state.getFeatureMap().mapValues(m -> m.toJavaMap()).toJavaMap());
`