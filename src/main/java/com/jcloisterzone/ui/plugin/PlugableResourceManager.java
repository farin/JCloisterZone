package com.jcloisterzone.ui.plugin;

import java.awt.Image;
import java.awt.geom.Area;
import java.beans.IntrospectionException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.theme.TileTheme;

public class PlugableResourceManager implements ResourceManager {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Client client;
    private final List<Plugin> plugins;

    //LEGACY
    TileTheme tileTheme;

      @Deprecated
      public static void addURLToSystemClassLoader(URL url) throws IntrospectionException {
          URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
          Class<URLClassLoader> classLoaderClass = URLClassLoader.class;

          try {
              java.lang.reflect.Method method = classLoaderClass.getDeclaredMethod("addURL", new Class[] { URL.class });
              method.setAccessible(true);
              method.invoke(systemClassLoader, new Object[] { url });
          } catch (Throwable t) {
              t.printStackTrace();
              throw new IntrospectionException("Error when adding url to system ClassLoader ");
          }
      }

    public PlugableResourceManager(Client client, List<Plugin> plugins) {
        this.client = client;
        this.plugins = plugins;

        for (Plugin p: plugins) {
            try {
                addURLToSystemClassLoader(p.getUrl());
            } catch (IntrospectionException e) {
                logger.error(e.getMessage(), e);
            }
        }

        //LEGACY
        tileTheme = new TileTheme(client);
    }

    @Override
    public Image getTileImage(String tileId) {
        return tileTheme.getTileImage(tileId);
    }

    @Override
    public Area getFeatureArea(String tileId, Feature piece, Location loc) {
        //TODO
        throw new NotImplementedException();
    }

    @Override
    public ImmutablePoint getFigurePlacement(Tile tile, Meeple m) {
        return tileTheme.getFigurePlacement(tile, m);
    }

    @Override
    public ImmutablePoint getFigurePlacement(Tile tile,
            Class<? extends Feature> piece, Location loc) {
        return tileTheme.getFigurePlacement(tile, piece, loc);
    }

    @Override
    public Map<Location, Area> getBarnTileAreas(Tile tile, int size,
            Set<Location> corners) {
        return tileTheme.getBarnTileAreas(tile, size, corners);
    }

    @Override
    public Area getBridgeArea(int size, Location loc) {
        return tileTheme.getBridgeArea(size, loc);
    }

    @Override
    public Map<Location, Area> getBridgeAreas(int size, Set<Location> locations) {
        return tileTheme.getBridgeAreas(size, locations);
    }

    @Override
    public Area getMeepleTileArea(Tile tile, int size, Location d) {
        return tileTheme.getMeepleTileArea(tile, size, d);
    }

    @Override
    public Map<Location, Area> getMeepleTileAreas(Tile tile, int size,
            Set<Location> locations) {
        return tileTheme.getMeepleTileAreas(tile, size, locations);
    }


}
