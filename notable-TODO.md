
* test Mayor scores 0 points on castle

* change resource manager interface - simple load all areas and start
and get it by one one instead of all per tile
+ support area for whole feature? (FarmHintsLayer)

* follower selection & zoom

* SelectPrisoner panel - inactive mode when displayed on remote client

* grid panel - verify left, right, top, bottom fields

* verify clock toggle during bazaar auction

* toggle clock / clearUndo can be derived automatically from state change

* test if builders works properly in extra abbey round (-> means buiders are ignored)

* if tile can be placed only with bridge, player can choose to discard tile

* add ZMG Castle variant - allow any (non-semicircular) cities as casle base
* do not eat meeples by Dragon on castles, do not capture by Tower

* TODO: draw bridge preview on tile preview icon if bridge is mandatory

* TODO <susbtract> -> <subtract>

* save game -> use history of messages
    undo -> send how many messages should be stripped ?

* there is still some bug probably related to bridges / or putting road piece inside ?
-> impplement save as list of messages (state transformations)

* do not challenge yaga hut with shrine - separate feature for yaga's hut? probably not
* + yaga scoring

* add debug options to limit tile pack size

* add function for applying reducer on state (reverse apply)
* rename updateXYZ to mapXYX, add it for everything and move it from mixins to state

* implement zoom by single affine transrom on grid layer? probably no - problems with area checking

* remove feature/meeple methods with state arg, eg isOccupied(state), put them rather on state

* Tower, place tile, use same circular ares as for selecting follower

* extract config to global singleton repository, eg Client or Application

* is GameController needed for phases? could be game sufficient? Or should be gc placed on Game?

* put capabilities and number of tile sets into saved game

* move markUndo from phase to Game and eventually remove game/gc from Phase
