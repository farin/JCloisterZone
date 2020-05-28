# Feel free to edit. Mind all your own comments or white-spaces may be stripped by configuration save peformed by application.

update: ${update}
port: ${port}

# keep empty for system language or fill to force locale
# supported locales are cs, de, el, en, es, fr, hu, it, ja, nl, pl, ro, ru, sk, zh
locale: ${locale}

score_display_duration: ${score_display_duration} # seconds
theme: ${theme}

tile_rotation: ${tile_rotation}
beep_alert: ${beep_alert}
confirm: ${confirm}

# machine identification for remote games
client_name: ${client_name}
client_id: ${client_id}
secret: ${secret}
play_online_host: ${play_online_host}

ai:
  place_tile_delay: ${ai_place_tile_delay} # miliseconds to wait before computer player place tile
  class_name: ${ai_class_name}

players:
  # Colors as Java awt.Color constant or in hex-value. (third-party themes can ignore these colors)
  colors: ${colors}

  # You can declare default player names
  names: ${player_names}
  ai_names: ${ai_names}

screenshots:
    # Specifies a folder for screenshots to be saved into. If leaved empty the JCloisterZone $workdir/screenshots will be used.
    folder: ${screenshot_folder}
    # Specifies the size of the tiles when a screenshot is taken;
    scale: ${screenshot_scale}

saved_games:
    # Specifies a default folder for saved games. If leaved empty the JCloisterZone $workdir/saves will be used.
    folder: ${saved_games_folder}
    # Specifies how JSON file is stored. Possible values: compact (no white spaces), pretty (human readable pretty print)
    format: ${saved_games_format}

plugins:
  lookup_folders: ${plugins_lookup_folders}
  enabled_plugins: ${plugins_enabled_plugins}

# possible expansions in profile definition:
#   WINTER, INNS_AND_CATHEDRALS, TRADERS_AND_BUILDERS, PRINCESS_AND_DRAGON,
#   TOWER, ABBEY_AND_MAYOR, BRIDGES_CASTLES_AND_BAZAARS, HILLS_AND_SHEEP,
#   KING_AND_ROBBER_BARON, RIVER, RIVER_II, CATHARS, BESIEGERS, COUNT,
#   GQ11, CULT, TUNNEL, CORN_CIRCLES, FESTIVAL, PHANTOM, WIND_ROSE,
#   GERMAN_MONASTERIES, FLIER, GOLDMINES, MAGE_AND_WITCH, CORN_CIRCLES_II, LITTLE_BUILDINGS
#   RUSSIAN_PROMOS, DARMSTADT_PROMO, LABYRINTH
#
# possible rules:
#   RANDOM_SEATING_ORDER,
#   USE_PIG_HERDS_INDEPENDENTLY, PRINCESS_MUST_REMOVE_KNIGHT, DRAGON_MOVE_AFTER_SCORING,
#   FAIRY_ON_TILE, ESCAPE_RGG, PIG_HERD_ON_GQ_FARM,
#   MULTI_BARN_ALLOWEDD, TUNNELIZE_ALL_EXPANSIONS, MORE_TUNNEL_TOKENS, BAZAAR_NO_AUCTION, KEEP_CLOISTERS,
#   BULDINGS_DIFFERENT_VALUE, FESTIVAL_FOLLOWER_ONLY, ON_HILL_NUMBER_TIEBREAKER, ADVANCED_LABYRINTH
#   TINY_CITY_2_POINTS
#   CLOCK_PLAYER_TIME


presets: ${presets}

connection_history: ${connection_history}



${if hasDebug}
debug:
  window_size: ${window_size}

  autosave: ${autosave}

  # comment preset to disable autostart
  # use player name for human player or class for ai
  # - com.jcloisterzone.ai.legacyplayer.LegacyAiPlayer
  # - com.jcloisterzone.ai.DummyAiPlayer
  autostart: ${autostart}

  tile_definitions: ${tile_definitions}

  game_annotation: ${game_annotation}

  # area or figure
  area_highlight: ${area_highlight}
${end}

