# Building [JCloisterZone](http://jcloisterzone.com/) from source


Don't hesitate to ask if there are any problems with the explanations below!



## Dependencies

- [Java SE 8](http://www.oracle.com/technetwork/java/javase/downloads/) (JDK 1.8) as your `$JAVA_HOME`.
- [Maven](https://maven.apache.org/) in your `$PATH`.
- [gettext](https://www.gnu.org/software/gettext/) in your `$PATH`.
- (optional) [Eclipse](https://eclipse.org/)



## Package the latest version of the `master` branch.


```bash
cd your-development-folder    # Your own preference.
                              # Get farin's fork of WebSockets.
git clone https://github.com/farin/Java-WebSocket.git
cd Java-WebSocket
mvn install                   # Install the WebSockets fork version.
cd - >/dev/null

                              # Get JCloisterZone.
git clone https://github.com/farin/JCloisterZone.git
cd JCloisterZone
mvn package                   # Create the main and plugin jar files.

open build/JCloisterZone.jar  # Execute the game.
```


## Running in Eclipse

1. Clone the code from https://github.com/farin/JCloisterZone.git
1. "Import..." the "JCloisterZone" project as an "Existing Maven project" from disk.
1. Right click the project and select "Update project..." from the Maven menu.
1. Run the project as a java application; select `JCloisterZone` (`com.jcloisterzone.ui.JCloisterZone`) as the main class.


## Troubleshooting


### Can't find `$JAVA_HOME` or java compiler version errors.

Maybe `$JAVA_HOME` was not set correctly. Try running ``export JAVA_HOME="`/usr/libexec/java_home -v 1.7`"`` before running `mvn` in the same terminal window.


### Can't find `msgfmt` so there are warnings for each language during `mvn package`.

Maybe [gettext](https://www.gnu.org/software/gettext/) wasn't in your `$PATH`. Try installing/finding it on your harddrive, then add it to your path variable.



## Development guide

Helpers for more pleasant development and application debugging.

### VM arguments and system properties

JCloisterZone supports various system properties to help development

* `config=foo.yaml` - use custom yaml configuration file
* `errorLog=false` - don't save error log to file
* `closeGameConfirm=false` - close game withiu showing confirmation dialog
* `nick=Bob` - force default nickname for play online
* `forceChat=true` - enable chat window for local games
* `allowAiOnlyOnlineGame=true` - allows online game without human players (must be allowed also by server)
* `allowHotSeatOnlineGame=true` - allows online game with multiple human players from one client
* `org.slf4j.simpleLogger.defaultLogLevel=info` - set log level
* `transparentScreenshots=true` - don't fill screenshot background
* `windowSize=L` - overrides config valus (eg 1024x768, or special values L or R)

Command line example:

```bash
java -jar JCloisterZone.jar -Dconfig=debug.yaml -DerrorLog=false -Dorg.slf4j.simpleLogger.defaultLogLevel=info -DforceChat=true -DcloseGameConfirm=false -ea
```

### `debug.yaml`

Tweaked `config.yaml`

Use unpacked plugins from source.

```yaml
plugins:
  - plugins/classic
  - plugins/rgg_siege
```

For immediately AI play comment delay option.

```yaml
# ai_place_tile_delay: 250
```

#### Debug options

Use debug options for quick and repeatable game setup with following possible options

```yaml
debug:
  # use some keys described below ...
```

Autosave before each AI play.

```yaml
autosave: saves/_prerank.jcz
```

Skip game config dialog, player is name or AI class.
You can comment just preset key to disable whole autostart.

```yaml
autostart:
  preset: default
  players:
    - Alice
    - Bob
    - com.jcloisterzone.ai.legacyplayer.LegacyAiPlayer
    - com.jcloisterzone.ai.DummyAiPlayer
```

Developing expansion, don't bother with basic tiles. Override any expansion with own set definition.

```yaml
tile_definitions:
  BASIC: tile-definitions/basic-1card.xml
```

Force drawn tiles.

```yaml
game_annotation:
  tilePack:
    className: "com.jcloisterzone.debug.ForcedDrawTilePack"
    params:
      drawOrder: ["BA.C", "BA.C", "BA.C"]
```

Final scoring can be forced by `#END` at end of the params list.

```yaml
draw:
# ... some tiles ...
- .
```

Experimental options

```yaml
off_capabilities: [ Dragon, Fairy ]
area_highlight: figure
```

### `gettext`

```bash
xgettext -k_tr -o po/keys.pot --from-code=utf-8 $(find . -name "*.java")
msgmerge -N -U po/ca.po po/keys.pot
msgmerge -N -U po/cs.po po/keys.pot
msgmerge -N -U po/de.po po/keys.pot
msgmerge -N -U po/el.po po/keys.pot
msgmerge -N -U po/en.po po/keys.pot
msgmerge -N -U po/es.po po/keys.pot
msgmerge -N -U po/fi.po po/keys.pot
msgmerge -N -U po/fr.po po/keys.pot
msgmerge -N -U po/hu.po po/keys.pot
msgmerge -N -U po/nl.po po/keys.pot
msgmerge -N -U po/it.po po/keys.pot
msgmerge -N -U po/ja.po po/keys.pot
msgmerge -N -U po/pl.po po/keys.pot
msgmerge -N -U po/ro.po po/keys.pot
msgmerge -N -U po/ru.po po/keys.pot
msgmerge -N -U po/sk.po po/keys.pot
msgmerge -N -U po/zh.po po/keys.pot
rm po/*~
rm po/keys.pot
```

### package

```
mvn package

cd build
chmod a+x JCloisterZone.jar
mkdir JCloisterZone
mv JCloisterZone.jar plugins JCloisterZone

JCZVER=3.4.3
tar cvzf JCloisterZone-$JCZVER.tgz JCloisterZone
7z a JCloisterZone-$JCZVER.7z JCloisterZone
zip -r -9 JCloisterZone-$JCZVER.zip JCloisterZone
```
