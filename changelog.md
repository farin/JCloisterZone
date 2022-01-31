# Version history

???
rules: spacial handling for fairy and acrobats pyramid. It is assigned to all meeples in acrobat pyramid.

## 5.10.1
* fix: unable to score Acrobats if player has no meeple in supply
* fix: Ringmaster - allow on top of tower, allows move to City of C., interact with Corn Circles

## 5.10.0
*2022-01-24*
* Under The Big Top support (uses "simplified" artwork as no C1 tiles exist)
* allow Shepherd / Band even Farmers rule is off
* fix Follower move from Quarter to Field at end of game
* fix Fairy on Tile variant
* Vodaynoy/Razboynik return can be performed from next turn (not immediately)
* fix mouse map dragging glitches - sometimes map dragging wasn't stopped when mouse button is released
* active player colored background in right sidebar works again
* fix possible crash by mouse over on some scoring events in history

## 5.9.1
*2021-12-12*
* fixes engine run when user profile path includes non-ascii characters
* engine support for Robber's son fan expansion (community addon)
* game setup tlle preview shows again C1/C2 tile variant (depending on Abbot figure disabled/enabled)
* fixes score overlay (feature highlight on score mouseover) for garden tiles
* addons can provide link type ('download' and 'wiki' are handled with apropriate icon now)
* dev: Theme inspector improvements - fixed header, remembers last setting on page reload / app restart


## 5.9.0
*2021-11-18*
* responsive final stats
* improved tunnel tokens positions, artwork addon can defines tunnel shape and position
* fix bazaar - all tiles in auction are now random and chosen independently
* fix field score in final game statistics
* fix tile distribution dialog for connected clients when "hide remaining tiles cheat sheet" is enabled
* fix: Count of Carcassonne expansion can be added only once to game
* fix no feedback from "Load Setup From File" button (now switch to Tiles tab same as load on favorite setup)
* fix: Watchtowers meeple bonus is not count from special meeples
* fix: in some cases prisoners can have wrong color in sidebar
* classic artwork, several glitches and wrong shapes corrected
* artworks - shape definition allows extended shape transformations and deriving shapes from others
* use webkit native websockets instead of ws library

## 5.8.0
*2021-10-04*
* "play online" - public play server - players can be behing NAT, keeps persistent games even if no player is online...
* saving game setup redesigned - added favorite setups section
* add basic final game statistics (to be improved in next versions)
* timer - make countdown more smooth and accurate
* make action panel more repsponsive - show smaller icons when window is small and too much actions are displayed
* fix: portal is now enabled by default for princess & dragon expansion
* engine fix: castle phase - do not offer castle actions improperly
* engine fix: coat of arms + siege and cathedral scoring
* engine fix: do not score disabled features from addons when addon is not active
* improved error dialog, doesn't block app window

## 5.7.4
*2021-08-23*
* game can be played with multiple meeples of same type
* fan expansions from addons can provide description and links (eg. point to rules, boardgamegeek.com page etc.)
* fix: deployment on labyrinth for advanced variant - deployment wans't allowed in all valid cases)
* fix: C1 wagon move was broken for several tiles

## 5.7.3
*2021-07-19*
* hot fix for broken Ferries and Flying Machine (embarrassing bug, unfortunately not covered by automated tests)

## 5.7.2
*2021-07-16*
* The Watchtowers expansion
* addons support - expansions / artworks are possible
* The City Gates and The Wells as referential addons (thx to Fancarpedia)
* new points breakdown ui
* show download progress when downloading classic artwork, show warning when it fails
* tiles dialog show also count of tiles hidden under hills
* tiles dialog add option to show available remaining tiles only
* fix: zoom with keyboard
* fix: tiles dialog crash for loaded games
* breaking changes: addons brings internal changes which implies backward saved games incompatibility

## 5.6.1
*2021-05-13*
* adjusted transparency for active player colored background
* field scoring breakdown enhancements (missing pig bonus for castle items and adjacent City of Carcassonne)
* fix: undo button confirms actions instead doing undo
* fix: scoring breakdown height on small screens

## 5.6.0
*2021-05-11*

* small screens layout enhancements (mainly player list)
* game setup overview (and dialog during game) shows changed rules
* tiles dialog shows always total count. (only remaining count can be hidden by game option)
* added undo button to action confirmation
* added option for player list rotation (active player can be on top)
* game setup can be saved
* active player is indicated by colored background
* both active player indicators (triangle, background) can be disabled in settings
* fix: auction bid must be higher then prev one
* ui fix: display missing gained points in history if first player auctioned tile
* ui fix: rule checkbox color for dark theme
* ui fix: awarded trade resources are displayed next each other in player list
* ui fix: fairy barely not visible under mage/witch when placed on same place
* internal: electron upgraded to version 12
* internal: config file location can be changed by env variable

## 5.5.4
*2021-04-17*

* fix: everyone can buy each other imprisoned follower
* fix: move from the City of C. ignores follower type selected by a player
* fix: wagon shouldn't be allowed to move on Monastery with follower already placed as abbot
* fix: wagon moved on Monastery should be allowed to select deployment as monk or as abbot
* fix: don't allow phantom and magic portal placement on alredy occupied labyrinth
* fix: wagon should be able to move to an unoccupied labyrinth road branch

## 5.5.3
*2021-03-22*

* rules: player can pay ransom only if has at least 3 points
* fix: remaning tiles count - subtract also removed tiles (removed because no placement was available when drawn)
* ui enhancement: wagon hit was added, pointing to source feature when use can move wagon


## 5.5.2
*2021-03-05*

* fix: dragon shows remaining moves again
* fix: accidentally disabled corn circles (5.5.0 regression)
* ui fix: incomplete city with cathedral shows properly points from mage


## 5.5.1
*2021-03-02*

* fix: crash on Russian Promos trap catching Abbot, Wagon or Mayor


## 5.5.0
*2021-02-21*

* Russian Promos extended with tiles from 2016 (Vodyanoy and SoloveiRazboynik)
* player is now allowed to place tunnel token and meeple in any order (instead tunnel first only)
* fix: opponent's shepherd on closed field is score one turn later
* fix: "Show Game Setup" menu item should be enabled only when game is running
* fix: UI shouldn't allow to pay ransom twice despite builder double turn

## 5.4.2
*2021-01-30*

* fixed broken reconnect / join existing game afterd disconnect

## 5.4.1
*2021-01-18*

* hotfix: meeples on bridges are displayed on wrong position

## 5.4.0
*2021-01-17*

* rules: each player can place abbey after last tile is placed
* rules: when field with Shepherd is closed player can still choose expand/score
* fix: skip action by opponent is no longer possible
* fix: load game with randomized seating order
* fix: do not allow wagon move on garden (causing game  crash)
* fix: unable to skip move from City of Carcassonne discrict (impossible in player's turn)
* fix: when "fairy on tile" rule was selected, 1 point fairy bonus wasn't awarded
* added confirmation after: wagon move, corn circle actions, move from City of Carcassonne
* rotating tile skips invalid rotations on selected position (now works same as in legacy application)
* it's possible rotate tile / select action also from keyboard with Tab key
* added progress bar to app update (will we active for next update)
* saved games are not compatible with previous version

## 5.3.3
*2020-12-30*
* hide unintentionally displayed experimental section "play online" (not working yet)
* zoom follows mouse pointer. wheel sensitivity decreased
* fix colors in game setup overview

## 5.3.2
*2020-12-29*

* fix: only regular meeples can use magic portal
* added option to randomize seating order
* added tiles dialog - shows removed and remaining tiles
* purist mode - option to disable remaining tiles
* user can display dialog with current game's setup

## 5.3.1
*2020-12-18*

* rules: phantom can't be played immediatelly after princess
* fix: don't clone auctioned tiles (auctioned tiles remained in tile pack)
* fix: move from City of Carcassonne can be skipped now
* fix: follower capture is not mandatory after increasing tower
* auction tiles in history sidebar are wrapped to multiple rows
* config.json is watched and read after manual change


## New app architecture (5.x.x)

* new app architecture - split between game engine (Java) and client (standalone executable app, based on Electron)

Game engine changes:
* added Abbot expansion
* added Spiel Doch promo
* added custom rules for Count move
* support for C2 Wagon move rules
* Darmstatd church - majority for counting church bonus is evaluated before anything is score
    (in other words before any follower is returned to a supply)
* fix flier and magic portal regression - do not allow meeple placement on castle by flier and magic portal
* fix: do not score castle created this turn
* fix: castle can be created on besiged cities
* fix: disallow undo for dragon moves
* fix: trigger double turn even if builder was eaten by dragon
* fix: Shepherd can co-existent on field with barn
* fix: Add Little Buildings bonus also for German Monasterie

## 4.6.1
*2020-06-02*

* fixed monasteries scoring
    - fixed regression when monasteries are scored additionally like monastery at the end of game
    - Monasteries + Flier interaction - majority take count when scoring abbots on the monasteries
* fixed monasteries and castle interaction - monastery should not trigger castle scoring when only abbots are deployed there
    - see note http://wikicarpedia.com/index.php/Bridges,_Castles_and_Bazaars_(1st_edition)#cite_note-28
* fixed: wagon -> castle move (game crashed when castle was connected to finished feature with wagon)
* and hopefully final fix for annoying "Message PlaceTileMessage hasn't been handled by ActionPhase phase"
* display followers properly when both monastery abbot and monk are deployed on the monastery (via flier)
* user can't deselect default plugin (classic) It works as fallback for incomplete artworks. (regression)
* support loading of saved games created in app with version before 4.6.0

## 4.6.0
*2020-05-30*

* added Labyrinth promo (including advanced rules)
* added Darmstadt promo
* added option to disable farmers (no placement on fields, ignore market quarter and corn circle related to fields)
* put back legacy rule "score tiny city for 2 points"
* Flier update
    - if landed on german monastery user can choose monk/abbot (#318),
    - deployment on target tile is optional
* display paid ransom in game events panel (history)
* added experimental option to rotate tile using mouse move (go to settings to enable) - (contibution by marekventur)
* fixed #321: Castle may not be scored in turn when is created
* fixed #319: Game stats: time percentage is counted incorrectly (thx chrissy0 for contribution)
* fixed: time limit setup checkbox glitches

## 4.5.1
*2020-05-01*

* fixed #313: Shepherd placement on empty and closed field cause error

## 4.5.0
*2020-05-01*

* newart plugin extended by River/River II and Abbey & Mayor tiles
* rules: allow farmer placement on field with shepherd (#305)
* rules: Shepherd is automatically returned when field is "closed" (#281)
* fixed #306: H&S null pointer exception when trying to place tile next to weird CCCF tile
* fixed #304: Adjacent monastery and shrine should not challenge each if one of them is unoccupied
* fixed #283: Sheep/wolf token counting error (panel should display remaining tokes not sheep sum)
* fixed #295: fix NullPointerException at ClientMessageListener in error log when playing online
* fixed: Null pointer exception and crashed game when user clicks on king or robber icon in panel

## Play server "big update"
*2020-04-28*

* server migrated to a new framework
* fixed: undo corrupt game backup on server. When game was interrupted/leaved and reconnected and reloaded from server it didn't worked
  (causing "Message PlaceTileMessage hasn't been handled by ActionPhase phase" error, see #272)

## 4.4.2
*2020-04-07*

* OSX helpers removed because they seems to crash app on Mac with some new Java versions (UI layer will be migrate to modern Java FX in next release)

## 4.4.1
*2019-04-24*

* fixed "weird" city tile from Hills and Sheep issues
   - Sometimes city remains unfinished when placed and two edges of this tile was adjacent to same city.
   - Also city wasn't completed when Abbey was attached to "weird" edge

## 4.4.0
*2019-04-17*

* Hills and Sheep expansion
   - note about hills: a tile hidden under hill is selected from all tiles including dragon tiles even they
     are not in play yet (if hill is drawn before volcano).
     Because in reality no specific tile is internally selected at the time when hill is placed.
     With such implementation nobody can dig particular tiles from saved game because
     no hidden information exists.
* End game stats shows number of placed tiles and consumed time. (#227)
* fixed #263: game freeze when undo was used on meeple placement after using flying machine
* fixed #276: ferry move removes inn from roads
* fixed #262: error when wagon scores castle
* newart plugin extended with King and Robber baron, and Cult.

## 4.3.2
*2019-04-11*

* rebuild which fixes run on Java 8 (unable to connect issue)
* newart plugin was bundled in (Basic game, Inns and Catherdrals, Traders and Builders for now)

## 4.3.1
*2019-04-09*

* fixed Ferries + Tunnel: sometimes ferry can't be changed when road is extended through tunnel connection
* fixed often computer player crash when playing with Corn Circles (may fixes similar situation on other expansions)

## 4.3.0
*2018-02-11*

* Ferries (Mini #3) expansion
* The Count, final scoring changed according to "Complete annotated rules" notes.
  - followers can be moved from all quarters (not only from the Market to forms)
  - players take turns
    Question:
        How does follower placement during the final scoring work?
    Answer:
        In principle very similarly to the way it works during the game. The 'trigger' for the final scoring
        is the player who placed the last tile and so ended the game. Beginning with the player on the left
        of the 'trigger' player, each player redeploys one of his or her followers from
        Carcassonne to an appropriate feature [followers in castle can only be deployed
        to cities, and so on] on the board. Followers can also be redeployed to incomplete
        roads, cities, cloisters, or farms, since these will also be scored at the end of the game.
        This process continues until no player can redeploy any more players from Carcassonne.
        The Count still blocks the city quarter in which he is resident. Normally the player with
        the most followers in Carcassonne will be the one to redeploy the last figure.
* fix: road with bridge can remain not scored. This can happen when bridge was placed result of tile placement
  on adjacent tile (and only when tile bridge is rotated once). Such bridge can also cause error during final scoring.
* fix: gold was not awarded from monastery tile itself (but only from adjacent tiles)
* fix: missing bid buttons when game is saved and loaded during bazaar auction

## 4.2.1
*2018-02-05*

* display info message when game is created with debug options (forced draw order). This is important
  for remote party because this option is part of game setup (since 4.x) and they are used by all participants.
  And currently there was no change to recognize that game is regular.
* fixed #255: Abbey and scoring road/city touching abbey with more then one side. Now feature is correctly finished.
* fixed #257: Farm hints display correct color for farms with barns.
* fixed #258: Pig/Barn issue, when pig was deployed instead of barn.
* fix: Barn and Market quarter:
  followers for the City of C. can be deployed on field when barn is placed
  or when barn field is extended by another field with followers
  score properly followers moved on barn field just before final scoring
* fix: some legal wagon moves were missing
* fix: City of C. followers can be now redeployed also onto features closed by abbey.
* fix: when moves from multiple City of C. quarters are available (eg legal move on monastery and road at
  one time) there was possibility that follower was moved from wrong quarter (eg. from blacksmith to monastery)
* fix: builder double turn allowed wrongly add follower into City of C. after each turn part if opponent
  feature was scored during first part
* fix: restrict corn circles actions to followers only (instead of meeples)
* fix: the rules that restrict the placement of cloisters next to already placed shrines also restrict the placement of abbeys.
* fix: german monasteries and abbot related issues (portal allows placement next to abbot, crash when abbot in tower area)
* fix: broken undo for loaded games
* fix: don't offer AI unsupported expansion in create game panel after "Play again" is used
* fix: preserve figure order in player panels

## 4.1.1
*2018-01-19*

* underlying JavaWebsocket library reverted to version used in 3.x.
  Recent version seems to be unreliable and dropping messages
  sometimes. Which makes client out of sync and caused unpredictable bugs.

## 4.1.0
*2017-12-25*

* connection stability improvements - websocket connection revisited and fixed reconnect on lost connection issues
* handle properly when single message is sent to server twice
* fix: captured meeple color in game events panel
* fix: play again button

## 4.0.2
*2017-12-10*

* fixed trade goods final scoring

## 4.0.1
*2017-12-05*

* fixed Tunnel regression. Game crashes when playing with or without some colors
  (to be exact, when you not take player slots from beginning)

## 4.0.0
*2017-12-04*

* multiple undo steps are now supported (including eg. undoing paid ransom)
* saved game format changed to JSON (and instead of game snapshot contains just action history)
* event log added - game events are displayed on top of the window (use E key to hide/show)

* added The Count expansion
* added game event panel on top of window
* added Japanese translation (thx to Alexis Jeandeau)
* multiple plugin folders can be specified in config (as relative or absolute paths)
* plugins can contain java classes and register new expansions and capabilities
* added "Carrcassone for 2" plugin (thx to Tom Hill)
* catapult tiles moved to separate plugin, disabled by default
  (with many other expansion these extra tiles are much less important)
* issue #63: experimental change: multiple tile sets of same expansion are allowed.
  No UI is present but feature can be enabled by manual edit of preset in config file
  (or by save game edit)
* issue #223 - experimental change: capabilities and expansions are independent. Game can be
  created with different set of capabilities then standard derived from expansion.
  Eg. Princess and dragon tiles but without dragon figure. Or add big follower
  to basic tiles (without inns & cathedrals tiles)
  No UI for creation such game is present yet, but same as multiple sets
  it can be achieved by manual edit of preset in config.yaml
  (Such games can be normally played with remote players)
* fixed definition for FE.RC tile (Festival) - city wasn't bind to field (field may score one city less sometimes)


### Current expansions changes

#### Princess and Dragon

PRINCESS_MUST_REMOVE_KNIGHT rule implemented in proper way: If enabled and there is option to remove princess no other action is allowed.

#### Tower

If players has several prisoners belonging to the other player during prisoners exchange,
the owner may decide which prisoner should be returned.

#### Bridges, Castles, and Bazaars

* Bazaar auction is not triggered when tile is discarded.
* When tile can place only with bridge, player is allowed to pass and let the tile to be discarded.
* fix: When there is no legal placement for auctioned tile, tile is discarded and random tile is drawn instead.
* Display bridge preview if bridge placement is mandatory (and bridge must be placed as part of tile placement).

#### River

Rule change: Lakes (River 1 lake and River 2 volcano lake) are drawn by player as common tile
(changed according to New Carcassonne (C II) rules)

#### Tunnel

Added MORE_TUNNEL_TOKENS rule (each player has three token sets for 2 players game or two token sets for 3 player game)

#### Russion promos

Baba Yaga's hut is now not involved in shrine-monastery challenges (when played together with The Cult expansion)

### Technical Notes

* Grand rewrite in favor of functional programming and immutable data structure.
* vavr.io library is awesome!

## 3.4.3
*2017-05-08*

* MacOS: application title is again JCloisterZone
* fixed: crash caused by two "Undo"s triggered in short period
* fixed: Little Buildings (unable to place building)
* fixed: WindRose points can be repeated by undo tile placement infinitely
* fixed TO.CccC+ definition (shield on one Tower tile is properly counted)
* experimental support for "rectangular" tiles - possibility to display 3d tiles projected as rectangle - currently no theme

## 3.4.2
*2015-11-21*

* Russian Promos "expansion"
* dark theme (there is definitely place for many improvements, especially in create game view)
* fixed: Impossible to undo Goldmine tile placement
* fixed The Corn Circles II tile definitions - cities was linked to field (this bug can cause less points on field in some cases)
* fixed BB.CFR.b tile definition - another missing city-field link

## 3.4.1
*2015-10-31*

* it's possible to enable/disable plugins without app restart
* tile distribution window (in help menu) - show overview of tiles in expansions
* added OSX native full screen hook (thx khalidqasrawi)
* monasteries in netherlands and belgium theme plugin
* winter theme plugin
* plugin icons are displayed in preferences/plugins
* colored placement history (thx fatsu)
* #112 - warn aboul last chance to place abbey
* fixed few winter extra tiles definitions to match original winter tiles
* fixed #184 - opponent can accidentally skip follower placement on Abbey
* fixed #182 - Cloister/Shrine can't be placed next to monastery AND shrine

## 3.3.1
*2015-08-25*

* undo is possible also for bridge placements (and also for a tile which forced bridge placement) (#170)
* fix #174: undo placement of a tile with tower base crash game
* fix #177: apostrophes in translations are not shown

## 3.3.0
*2015-08-15*

* preferences dialog to change settings from app
* improved TO, CC, GQ, AM images quality
* changed meeple selection (eg. princess undeploy, crop circles undeploy etc.) - instead selecting tile feature, meeple itself is highlighted directly
* rotate board fixes - don't rotate fairy and dragon (#145), gold counters and little building selection.
* rules update - Wagon can not choose which tile to move after completing a feature (#148)
* rules update - added and make default HiG fairy scoring option - fairy is placed next to follower instead of on tile (#137)
* slightly changed way how are mouseover handled for overlapping feature areas  - instead selecting none, a higher one is selected.
* added Catalan localization (thx to Joan Josep)
* packed to 7z instead zip - see #160 - new Players execute the jar from within the zip
* fix: custom rule "Tiny city is scored only for 2 points" + unfinished two tile city with cathedral (gives 2pts at end instead of 0)
* fix #34, #155: relax legal river checking - now river is almost always legally finished, don't end game in rare case when lake can't fit
* fix #135: Gold piece distribution is incorrect
* fix #138: undo & magic portal issue
* fix #139: abbey at game end
* fix #140: AI always use big follower
* fix #147: Missing farms on Abbey & Mayor tiles (small farms between crossed cities)
* fix #149: little building can be scored multiple times for one feature
* fix #150: Wagon can move to a tile occupied by dragon
* fix #158: Only the player with majority gets fairy bonus when completing a feature.
* fix: play again button did't work for loaded games
* online play: play server supports also legacy clients (since 3.2.0)

## 3.2.0
*2015-04-26*

* game clock
* meeple deployment undo - confirmations has been redesigned. Deploy is done immediately with possibility to undo it.
* better broken connection indicator
* auto reconnect and join game when connection is lost
* expansion: The Goldmines
* expansion: Little buildings
* fix #58: wrong nickname in pre-game chat
* fix: chat panel now auto hides again
* fix #123: after selecting "play again", game dialog don't hide ai unsupported expansion
* fix #124: non-bidding bidding bazaars and tile stealing
* fix #134: allow abbot placement on tile already surrounded by 8 tiles
* online play: fixed "continue" and wrong player order and names when game created with RANDOM_SEATING_ORDER
* online play: chat is available after game ends

## 3.1.1
*2015-03-04*

* online play - password protected games support
* tile color adjustment (Jozsef T)
* updated es translation (outdated in 3.1.0)
* error message bar can be dismissed
* rotate board (/ key)
* right click on board with shift down behave as middle mouse click (center board to click point)
* fix: too often AI placement on top of tower
* fix: "2-tiles bug" - if playing with GQ11 & King and Scout - at game and counter still shows 2 tiles
* fix: can't place on tower if there isn't normal placement possibility
* fix: It is not possible to place the phantom as an abbot on a German monastery
* fix: checking show field hints option before game starts is broken


## 3.1.0
*2015-01-29*

* play online feature - play through public webserver
* Mage and Witch (Mini #5) expansion
* tile rotation behavior changed - tile is rotated to first valid position
* added play again button
* show tunnel tokens supply in control panel
* CornCicles ui - don't ask if there is not deployed any follower of that kind (if no player can perform any action)
* click on tile in control panel triggers rotation
* take board screenshot (pull request by Decar)
* game setup dialog (you can get included expansion/rules during game)
* improved image quality (Tunnel, Corn Circles, Festival, Flier)
* server can be run without ui - just run SimpleServer.main()
* fix: if game folder is not writeable, system APPDATA dir (win) or ~/.jcloisterzone (unix) is used
* fix: Cathars escape is not allowed for builder
* fix: beep sound playback in Linux
* fix: bazaar & builder & end of turn. (ArrayIndexOutOfBoundsException / unused auctioned tiles in hand when game over)

## 3.0.2
*2014-11-02*

* rules update: taking prisoner is now not mandatory
* rules update: The placement of a princess tile (Princess & Dragon) with removal of a knight
   from the city cannot be used as a first “follower move” and be followed by placement
   of the Phantom (e.g. into the now-vacated city). As per the rules for the princess,
    “if a knight is removed from the city, the player may not deploy or move any other figure.”
* fix: abbot tower capture
* fix: wrong nickname in pre-game chat

## 3.0.1
*2014-09-17*

* bug reporting feature (in help menu)
* fix: wagon issue (usually when playing against ai causing game freeze)
* fix: wrong color of chat nicknames

## 3.0
*2014-08-27*

* network protocol changed completely
* German Monasteries expansion
* list of connected clients is displayed during game setup (can see unassigned players)
* chat is available during game setup
* more intuitive display of followers on and "under" bridges - bridge with it's follower always above other followers
* rules update: wagon moves for Inns & Cathedrals special cases, according to Completely Annotated Rules
* rules update: wagon can't move through a fair
* rules update: flier & phantom interaction - can't used flier with both common follower and phantom in single turn
* rules update: wagon can move to/from abbey
* fix: nasty rare tower capture and wagon glitches - mainly causing occasionally ai freezing (i hope there is no more similar bugs left)
* fix: wagon can move from monastery inside city to city and vice-versa on bridge and bazaars tile
* fix: loading game resets point statistics
* fix: in some cases unable to start loaded game initially created with "randomize seating order"


## 2.7
*2014-06-26*

* Greek localization (el locale)
* undo feature (tile placement only)
* rules update: wagon cannot cross a bazaar to the other side
* AI: The Tower support
* fix: builder & bazaar interaction
* fix: possible game load crash
* workaround for GTK look&feel bug - menus without border and separators
* code refactoring: game events (EventBus), AI tasks for select actions

## 2.6
*2014-01-08*

* yaml configuration (ini file not sufficient for stuctured presets)
* game setup presets: selected expansions/rules can be stored as named preset
* persistent settings
* field hints (press F key to toggle)
* The Besiegers expansions (Die Belagerer)
* Cathars/Siege: custom rule for RGG's escape variant (monastery must be adjacent to any city tile instead of directly to besieged tile)
* random seating order option
* game setup panel: flash effect when  expansion/rule is changed remotely
* custom rule: dragon movement after scorinhg (RGG)
* volcano rule updated according to latest FAQs: no follower deployment except barn, fairy movement allowed, so removed CANNOT_PLACE_BUILDER_ON_VOLCANO custom rule
* FARM_CITY_SCORED_ONCE custom rule removed - 2nd edition scoring is obsolete and may leads to confusion together with some expansions
* builder can be placed on uncompleted feature only (makes no sense to places on just finshed feature and return immediatelly)
* default player color changed
* chat is hidden in single player game
* submit connect dialog with enter
* connect dialog: last remote host is remembered
* rules update: Abbey placements is permitted after last landscape tile is drawn and placed
* rules update: dragon and followers are permitted on features outsidethe City of Carcassonne (Count tiles) (accesible by portal or flier)
* rules update: City of Carcassonne (Count expansion) treat as city for field scoring and King purposes.
* rules update: pig herds not stack (bonus is be awarded only once)
* rules update: ransom can be paid only once during builder double turn
* AI: Phantom support
* AI: Festival expansion allowed (trivial support only, AI ignores it and never removes own follower)
* fix: River II tile with city bridge over river should not separate farms
* fix: flier should be allowed to place on occupied feature
* fix: shortcut for save (Ctrl+S) is consumed by scroll down (S)
* fix: connect - connection refused handling
* fix: ghost meeples, sometimes removed meeples is displayed on screen forever
* fix: phantom meeple ignores magic portal - (now portal can be used if not used in turn yet)
* fix: meeple on bridge displayed incorrectly
* fix: city with mayor only cannot be converted to castle
* fix: followers placed on castles are lost after game load
* fix: sometimes non-zero pack size after game end

## 2.5
*2013-12-08*

* in-game chat
* remote players actions (inactive) are visible. So you can see remote player tile/meeple choices before placement.
* Flier expansion updated:
    - follower is selected before dice roll
    - ui changed - follower is selected with click on fier symbol on tile. it triggers dire roll.
* fix: abbey should not trigger builder extra turn when builder is placed in neighbouring city
* fix: Corn Circles II definition - possible crash or impossible game load

## 2.4.1
*2013-10-10*

* fix: builder should trigger only single extra turn (introduced in 2.4)

## 2.4
*2013-10-10*

* Wind Rose expansion
* grid can be scrolled by mouse drag
* removed grid lines
* AI improvements
* fix: decreased sensitivity to mouse drag (very short drag was recognized as click) - it should prevent ignored "click" bug
* fix: Cathars - escape action is optional (so player can pass it)

## 2.3.1
*2013-08-21*

* fix: sometimes game freezes when playing princess and dragon against AI (introduced in 2.2, know workaround save&load)

## 2.3
*2013-08-20*

* Java 7 required
* add check for new version on startup (can be disabled in config.ini)
* biggest city size / longest road length hint for king and scout bonuses is back (displayed on mouse over king or robber icon)
* improved highlight shapes and meeples positioning for many tiles
* one catapult tile changed - fĂŞte divides field
* emphasize captured follower when ransom can be paid (click on it to pay 3 points)
* internal: meeple position on tile can be defined for each theme (points.xml in theme jar)
* fix: corn circles - only followers are considered for corn actions
* fix: synchronization issue causing sometimes useless output in error.log

## 2.2
*2013-05-09*

* 12 extra tiles from Winter-Edition
* Corn Circles (full support instead of tiles only)
* Corn Circles II
* tiles count displayed along expansion in game set up dialog
* plugin for RGG's Siege graphics (enable in config.ini)
* fix: double skip is no longer required for Phantom expansion
* fix: corn circles T-road - roads are separated
* note: saved games from previous versions are incompatible

## 2.1.2
*2012-08-27*

* ro localization
* config.ini - ai_place_tile_delay
* fix: multiplied barn points when two or more barn are placed on same field

## 2.1.1
*2012-06-12*

* fr localization update
* fix: sometimes bazaars stop triggering auctions
* fix: control help dialog height updated

## 2.1
*2012-06-09*

* Bridges, Castles and Bazaars expansion
* The Phantom small expansion
* The Festival (10th anniversary) small expansion
* UI redesign
* more controls options
    - cursors and A,S,W,D keys moves board
    - middle click centers board
* optional ransom payment confirmation
* Hungarian localization
* fix: River I + River II + GQ11 crash bug

## 2.0.4
*2012-02-11*

* recompiled 2.0.3 for Java 6 (instead of a mistaken 7)

## 2.0.3
*2012-02-09*

* abbey phase: skip button gains focus (so Enter key can skip abbey easily)
* rule clarification: farmers on 'barn field' are scored during score phase in same time as other followers
(http://carcassonnecentral.com/forum/index.php?topic=982.msg11991#msg11991)
* fix: invalid load of discarded tiles (when tiles is present in pack more than once)
* fix: abbey "re-close feature issue" - city resources from vicinity cities can be assigned again, more than correct point for knight&scout bonus can be assigned)

## 2.0.2
*2012-01-17*

* Italian translation (thx to Giorgio C.)
* River1 images replaced (images didn't work in Linux and they had a bad quality)
* fix: do not allow pig deployment on barn field
* fix: some legal wagon moves can be missing
* fix: game load for Princess and dragon when all dragon tiles were placed caused crash

## 2.0.1
*2012-01-06*

* improved predefined names cycling in create game dialog
* AI plays fairy
* guard for AI errors (freezing)
* fix: score for barns
* fix: dragon can't enter "The Count" initial tiles. Also magic gate to Count edge features are not possible.
* fix: saved game with King & Scout don't lost information about biggest city / longest road
* fix: fairy + builder's double turn - 1 point for fairy is added only once
* fix: princess & dragon + river II - dragon is placed on lake with volcano
* fix: barn cannot be eaten by dragon
* fix: seducing last follower from city/field must also remove builder/pig
* fix: multiple network stubs if 'Create new game' selected from menu more then once

## 2.0
*2011-12-30*

Many changes. Huge code rewrite.
Remarkable points:
* UI changes:
  * game dialogs
  * independent graphics theme
  * zoom on mouse wheel
* save & load
* players can choose player order
* game over point statistics
* small River and River II rules correction (U-turns rule, lake with city)
* Crop circles, Plague and The Count of C. expansion tiles (without rules yet)
* tile images in better quality
* zoom performance
* .ini instead of .xml for configuration
* better Mac OS X support
* open source (GitHub)

## 1.6.12
*2011-04-10*

* fix: viewport update after board size is extended
* fix: builder on volcano & AI crash

## 1.6.11
*2011-04-07*

* fix: catapult city tile definition
* fix: rare scoring bug

## 1.6.10
*2011-03-17*

* fix: 5/6 six players, colors exception
* backport from 2.0; mouse wheel scroll, scroll bar in control panel

## 1.6.9
*2011-03-06*

* Russian translation by Andrew Mitrofanov

## 1.6.8
*2011-01-09*

* Dutch translation by Steven Post

## 1.6.7
*2010-11-01*

* French translation by Aegir

## 1.6.6
*2010-08-31*

* Polish translation by Tomasz Skowronski

## 1.6.5
*2010-07-09*

* fixed: 5 and 6 players game broken in 1.6.4

## 1.6.4
*2010-06-13*

* project renamed to JCloisterZone
* Spanish translation by Luis Olcese
* config file renamed to config.xml
* fixed: number of tower pieces for 2 or 3 players game
* fixed: unlimited tunnel pieces

## 1.6.3
*2010-05-29*

* Chinese translation (zh_TW, zh_HK, zh_CN locales) by Kingman Leung

## 1.6.2
*2010-02-15*

* beep notification is played also for dragon move
* fixed: vagon move on cards from other expansions
* fixed: black decoration on bright player colors (e.g. yellow)
* fixed: tower placement icon, correct display on "just placed" tile
* fixed: proper meeple position on particular "strange" card from expansions.

## 1.6.1
*2010-01-30*

* fixed: non-default 'default settings' in carcassonne.xml config has effect and works correctly

## 1.6
*2010-01-25*

* The Tunnel expansion
* translation to Finnish (thanks to Jari H.)
* fixed: mayor is not displayed in remote client
* fixed: field can contain two pig herds
* fixed: preview icon remains rarely displayed after tile placement

## 1.5.3
*2009-11-28*

* Catapult cards
* quick fix: dialog size in Linux, OpenJDK
* empty initial page for EN locale

## 1.5.2
* fixed: crash if expansion contains river tile (eg. GQ) and game is without rivers
* fixed: placed barn is not displayed on remote client
* fixed: possible missing vagon move
* default client settings in carcassonne.xml

## 1.5.1
* bugfixes
* don't create empty error.log

## 1.5.0
* Abbey & Mayor Expansion
* new application icon
* internal change: tile ids renamed, using expansion namespaces

## 1.4.0
*2009-02-09*

* code refactoring -> no backward compatibility !
* The Cult expansion
* princess and dragon - AI time consumption fixed for 'magic gate' tiles
* fixed: final field scoring

## 1.3.2
* small rules change: follower can be placed on tower with minimal height 1 (it means no placement directly on tower base)
* fixed: follower correctly displayed when placed on field on two tiles from Traders & Builders (tileId: T0121_C, T0121_W)

## 1.3.1
* fixed: possible crash (NullPointerException) when playing Tower and Princess expansions
* fixed: builder cannot be captured
* fixed: correct tower height alignment

## 1.3
* Tower expansion
* default checkbox state for expansions can by set in config
* action selection can be done by click on appropriate icon on panel
* figures on farms marked (also on towers)
* optional placement confirmation during follower placement on tower or field
* show dragon remaining moves
* flash on last placed tile
* placement history (use 'x' key to activate)
* tooltip for King And Scout expansion bonuses - information about biggest city & longest road
* AI supports Princes And Dragon expansion (experimental version, need improvements and optimizations)
* history of remote addresses and player names -> removed default player name from config file

## 1.2.3
* fixed: starting card only once instead 4-times.

## 1.2.2
* fixed dumb AI
* removed AI config from carcassonne.xml

## 1.2.1
* fixed: builder ignored in play against AI (appears in 1.2)

## 1.2
* required Java 6.0
* fixed scoring double figures at end of the game
* information about game in "add player" dialog
* version compatibility check for remote connection
* splash screen

many internal changes in code
  - network protocol revisited --> maybe fixed occasionally game freezing
  - AI architecture revisited
  - code optimizations (code style, memory consumtion, speed)

## 1.1.0
* River II expansion
* Slovak translation
* default custom rules in config
* default player name in config
* text anti-aliasing (score, nickname)
* background for assigned score
* final scoring remains on board
* river end fix
* beep alert option
* info box displayed when tile is discarded (if doesn't fit to any place)

## 1.0.3
* custom rules
* network protocol changed - fixed bug with lost messages at end of game
* improved emphasized areas for figure placement on "strange" cards - area fit a real shape

## 1.0.2
* minor card definition fix

## 1.0.1
* small expansions (GQ, The Cathars)

## 1.0
* bug fixes

## 0.9.9
* graphics anti-aliasing
* princess and dragon expansion
* interface changed, especially for figure/action placement
* assigned points are shown on board
* network layer revised
* application core mainly revised

## 0.9.5
* replaced communication protocol, Serialization instead XML
* external libraries for XML processing removed --> much smaller application size
* "builder and pig hiding" bug fixed
* shows player resources in control panel in proper form
* yellow double figure has black "2" label
* shows selected extensions when connect to game

## 0.9.1
* Traders and builders expansion
* "invisible figure" bug repaired
* full screen
* emphasized cities nicer :)
* AI from 0.8.2 version available in basic game

## 0.9
* full support of king & scout
* first extension (inns & cathedrals, double figure, ...)
* GUI changed
* shortcuts for next turn (ENTER), zoom in (+), zoom out (-)

## 0.8.2
* Ant build script
* buttons disabling
* minor bug fixes

## 0.8.1
* various nicknames of more AI players in one game
* replaced river tile images with better ones
* AI placed figures are drawn same as player figures - f.e. centered on score object
* area for figure place emphasize rewritten
* king & scout card added (not rules yet)

## 0.8
* initial version
