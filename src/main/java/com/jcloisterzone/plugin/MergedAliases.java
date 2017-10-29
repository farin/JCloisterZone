package com.jcloisterzone.plugin;

import io.vavr.collection.Stream;
import io.vavr.collection.Vector;

public class MergedAliases implements Aliases {

    private final Vector<Aliases> aliases;

    public MergedAliases(Iterable<Plugin> plugins) {
        aliases = Stream.ofAll(plugins)
            .map(Plugin::getAliases)
            .toVector();
    }

    @Override
    public String getImageAlias(String tileId) {
        for (Aliases pluginAliases : aliases) {
            String res = pluginAliases.getImageAlias(tileId);
            if (res != null) {
                return res;
            }
        }
        return null;
    }

    @Override
    public String getGeometryAlias(String tileId) {
        for (Aliases pluginAliases : aliases) {
            String res = pluginAliases.getGeometryAlias(tileId);
            if (res != null) {
                return res;
            }
        }
        return null;
    }

}
