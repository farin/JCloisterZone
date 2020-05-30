package com.jcloisterzone.plugin;

import static com.jcloisterzone.ui.resources.svg.ThemeGeometry.DEFAULT_GEOMETRY;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.ExpansionType;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.feature.Bridge;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.River;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.plugin.PluginMeta.ExpansionMeta;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.resources.FeatureArea;
import com.jcloisterzone.ui.resources.FeatureDescriptor;
import com.jcloisterzone.ui.resources.ImageLoader;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;
import com.jcloisterzone.ui.resources.ResourceManager;
import com.jcloisterzone.ui.resources.TileImage;
import com.jcloisterzone.ui.resources.svg.ThemeGeometry;

import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;

public class Plugin implements ResourceManager {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Config config;
    private final Path path;
    private final Path relPath;
    private final ImageLoader imageLoader;
    private final URLClassLoader loader;

    private PluginMeta meta;
    private Vector<String> includedClasses;

    private boolean loaded;
    private boolean enabled;

    private PluginAliases aliases;
    private ResourceManager parentResourceManager;

    private ThemeGeometry pluginGeometry;
    private Insets imageOffset =  new Insets(0, 0, 0, 0);
    private int imageRatioX = 1;
    private int imageRatioY = 1;

    private java.util.HashMap<Tuple2<Tile, Rotation>, Map<Location, FeatureArea>> areaCache = new java.util.HashMap<>();

    private Vector<Expansion> expansionsToRegister = Vector.empty();
    private java.util.Set<String> supportedExpansions = null; // expansion codes

    public static Plugin readPlugin(Config config, Path relPath, Path path) throws NotAPluginException, PluginLoadException {
        Plugin plugin = new Plugin(config, relPath, path);
        plugin.loadMetadata();
        return plugin;
    }

    public Plugin(Config config, Path relPath, Path path) throws PluginLoadException {
        this.config = config;
        this.relPath = relPath;
        this.path = path;
        URL url = getFixedURL();
        logger.debug("Creating plugin loader for {}", url);
        loader = new URLClassLoader(new URL[] { url });
        imageLoader = new ImageLoader(loader);
    }

    private URL getFixedURL() throws PluginLoadException {
        try {
            URL url = path.toUri().toURL();
            if (Files.isRegularFile(path) || url.toString().endsWith("/")) {
                return url;
            }
            return new URL(url.toString() + "/");
        } catch (MalformedURLException e) {
            throw new PluginLoadException(e);
        }
    }

    protected void loadMetadata() throws NotAPluginException, PluginLoadException {
        Yaml yaml = new Yaml(new Constructor(PluginMeta.class));
        URL pluginYaml = loader.getResource("plugin.yaml");
        if (pluginYaml == null) {
            throw new NotAPluginException(String.format("%s is not a plugin", path));
        }
        try {
            meta = (PluginMeta) yaml.load(pluginYaml.openStream());
        } catch (IOException | YAMLException e) {
            throw new PluginLoadException(e);
        }

        includedClasses = Vector.empty();
        try {
            Files.walk(path)
                .filter(f -> f.toString().endsWith(".class"))
                .forEach(f -> {
                    String clsName = path.relativize(f).toString()
                        .replace(File.separator, ".")
                        .replaceAll("\\.class$", "");
                    includedClasses = includedClasses.append(clsName);
                });
        } catch (IOException e) {
            throw new PluginLoadException(e);
        }

        if (meta.getExpansions() != null) {
            for (ExpansionMeta expMeta : meta.getExpansions()) {
                ExpansionType type = ExpansionType.valueOf(expMeta.getType());
                java.util.List<Class<? extends Capability<?>>> capabilityClasses = new ArrayList<>();
                if (expMeta.getCapabilities() != null) {
                    for (String name : expMeta.getCapabilities()) {
                        Class<? extends Capability<?>> cls;
                        try {
                            cls = Capability.classForName(name, loader);
                            capabilityClasses.add(cls);
                        } catch (ClassNotFoundException e) {
                            throw new PluginLoadException(e);
                        }
                    }
                }
                @SuppressWarnings("unchecked")
                Class<? extends Capability<?>>[] _capabilityClasses = capabilityClasses.toArray(new Class[capabilityClasses.size()]);
                Expansion exp = new Expansion(expMeta.getName(), expMeta.getCode(),
                        getLocalizedString(expMeta.getLabel(), expMeta.getLabel_i18n()), _capabilityClasses, type);
                expansionsToRegister = expansionsToRegister.append(exp);
            }
        }


        if (!Files.isDirectory(path)) {
            //register tiles to make Paths.get working for packed plugins
            java.util.Map<String, String> env = new java.util.HashMap<>();
            env.put("create", "true");
            try {
                FileSystems.newFileSystem(getLoader().getResource("tiles").toURI(), env);
            } catch (IOException | URISyntaxException e) {
                throw new PluginLoadException(e);
            }
        }

        try {
            supportedExpansions = Files.list(Paths.get(getLoader().getResource("tiles").toURI()))
                .filter(Files::isDirectory)
                .map(d -> d.getFileName().toString().replaceAll("/$", ""))  // fix trailing / which appears when listing folders in jar
                .collect(Collectors.toSet());
        } catch (IOException | URISyntaxException e) {
            throw new PluginLoadException(e);
        }

        if (meta.getTile_images() != null) {
            String value = meta.getTile_images().getOffset();
            if (value != null) {
                String[] tokens = value.split(",");
                if (tokens.length != 4) {
                    throw new PluginLoadException("Invalid value for image-offset " + value);
                }
                imageOffset = new Insets(
                   Integer.parseInt(tokens[0]),
                   Integer.parseInt(tokens[1]),
                   Integer.parseInt(tokens[2]),
                   Integer.parseInt(tokens[3])
                );
            }
            Integer intValue = getMetadata().getTile_images().getRatio_x();
            if (intValue != null) {
                imageRatioX = intValue;
                if (imageRatioX == 0) imageRatioX = 1;
            }
            intValue = getMetadata().getTile_images().getRatio_y();
            if (intValue != null) {
                imageRatioY = intValue;
                if (imageRatioY == 0) imageRatioY = 1;
            }
        }
    }

    public String getLocalizedString(String defaultValue, java.util.Map<String, String> trans) {
        if (trans == null) {
            return defaultValue;
        }
        String lang = config.getLocaleObject().getLanguage();
        String value = trans.get(lang);
        return value == null ? defaultValue : value;
    }

    public Image getIcon() {
        return imageLoader.getImage("icon");
    }

    public Path getPath() {
        return path;
    }

    public Path getRelativePath() {
        return relPath;
    }

    public PluginMeta getMetadata() {
        return meta;
    }

    public String getTitle() {
        return getLocalizedString(meta.getTitle(), meta.getTitle_i18n());
    }

    public String getDescription() {
        return getLocalizedString(meta.getDescription(), meta.getDescription_i18n());
    }

    public boolean isDefault() {
        return path.getFileName().toString().matches("^classic\\b");
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public URLClassLoader getLoader() {
        return loader;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    @Override
    public String toString() {
        return meta.getTitle();
    }

    //TOOD better throws own Exception
    public final void load() throws PluginLoadException {
        if (!isLoaded()) {
           doLoad();
           loaded = true;
        }
    }

    @Override
    public void reload() {
        //empty
    }

    public final void unload() {
        if (isLoaded()) {
           doUnload();
           loaded = false;
        }
    }

    protected void doLoad() throws PluginLoadException {
        includedClasses.forEach(clsName -> {
            try {
            Class<?> c = loader.loadClass(clsName);
            logger.debug("External class {} has been loaded.", c.getName());
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
        });

        for (Expansion exp : expansionsToRegister) {
            Expansion.register(exp, this);
        }

        try {
            aliases = new PluginAliases(getLoader(), "tiles");
            pluginGeometry = new ThemeGeometry(getLoader(), "tiles", getImageSizeRatio());
        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new PluginLoadException(e);
        }
    }

    protected void doUnload() {
        for (Expansion exp : expansionsToRegister) {
            Expansion.unregister(exp);
        }
    }

    public boolean isExpansionSupported(Expansion exp) {
        return supportedExpansions.contains(exp.getCode());
    }

    public java.util.Set<String> getContainedExpansions() {
        return supportedExpansions;
    }

    public double getImageSizeRatio() {
        return imageRatioY/(double)imageRatioX;
    }

    private boolean isTileSupported(String tileId) {
        return supportedExpansions.contains(tileId.split("\\.", 2)[0]);
    }

    @Override
    public TileImage getTileImage(String tileId, Rotation rot) {
        if (!isEnabled()) {
            return null;
        }

        String aliasId = aliases.getImageAlias(tileId);
        if (aliasId != null) {
            if (!isTileSupported(aliasId)) {
                // cross plugin alias, resolve it from parent resource manager
                return parentResourceManager.getTileImage(aliasId, rot);
            }
            tileId = aliasId;
        } else {
            if (!isTileSupported(tileId)) {
                return null;
            }
        }

        String[] tokens = tileId.split("\\.", 2);
        String baseName = String.format("tiles/%s/%s", tokens[0], tokens[1]);

        String fileName;
        Image img;
        // first try to find rotation specific image
        fileName = baseName + "@" + rot.ordinal();
        img =  getImageLoader().getImage(fileName);
        if (img != null) {
            return new TileImage(img, imageOffset);
        }
        // if not found, load generic one and rotate manually
        fileName = baseName;
        img =  getImageLoader().getImage(fileName);
        if (img == null) return null;
        if (rot == Rotation.R0) {
            return new TileImage(img, imageOffset);
        }
        BufferedImage buf;
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        if (rot == Rotation.R180) {
            buf = UiUtils.newTransparentImage(w, h);
        } else {
            buf = UiUtils.newTransparentImage(h, w);
        }
        Graphics2D g = (Graphics2D) buf.getGraphics();
        g.drawImage(img, rot.getAffineTransform(w, h), null);
        return new TileImage(buf, imageOffset);
    }

    @Override
    public Image getImage(String path) {
        return getImageLoader().getImage(path);
    }

    @Override
    public Image getLayeredImage(LayeredImageDescriptor lid) {
        return getImageLoader().getLayeredImage(lid);
    }

    @Override
    public ImmutablePoint getMeeplePlacement(String effectiveTileId, Tile tile, Rotation rot, Location loc) {
        if (!isEnabled()) {
            return null;
        }

        String aliasId = aliases.getGeometryAlias(effectiveTileId);
        if (aliasId != null) {
            if (!isTileSupported(aliasId)) {
                // cross plugin alias, resolve it from parent resource manager
                return parentResourceManager.getMeeplePlacement(aliasId, tile, rot, loc);
            }
            effectiveTileId = aliasId;
        } else {
            if (!isTileSupported(effectiveTileId)) {
                return null;
            }
        }

        if (loc == Location.MONASTERY) loc = Location.CLOISTER;

        Location iniLoc = loc.rotateCCW(rot);
        Feature feature = tile.getInitialFeatures().get(iniLoc).get();
        Class<? extends Feature> featureClass = feature.getClass();

        ImmutablePoint point = pluginGeometry.getMeeplePlacement(effectiveTileId, featureClass, iniLoc);
        if (point == null) {
            point = DEFAULT_GEOMETRY.getMeeplePlacement(effectiveTileId, featureClass, iniLoc);
        }
        if (point == null) {
            logger.warn("No point defined for <" + (new FeatureDescriptor(tile.getId(), featureClass, loc)) + ">");
            point = ImmutablePoint.ZERO;
        }
        return point.rotate100(rot);
    }

    @Override
    public ImmutablePoint getBarnPlacement() {
        return null;
    }

//    private FeatureArea applyRotationScaling(Tile tile, ThemeGeometry geom, FeatureArea area) {
//        if (area == null) return null;
//        /* rectangular tiles can have noScale direction to keep one dimension unchanged by rotation */
//        AreaRotationScaling ars = area.getRotationScaling();
//        if (ars != AreaRotationScaling.NORMAL)  {
//            Rotation rot = tile.getRotation();
//            if (rot == Rotation.R90 || rot == Rotation.R270) {
//                AffineTransform t = new AffineTransform();
//                if (ars == AreaRotationScaling.NO_SCALE_HEIGHT) {
//                    ars.concatAffineTransform(t, geom.getImageSizeRatio());
//                } else {
//                    ars.concatAffineTransform(t, 1.0 / geom.getImageSizeRatio());
//                }
//                area = area.transform(t);
//            }
//        }
//        return area;
//    }

    private FeatureArea getFeatureArea(String tileId, Class<? extends Feature> featureClass, Location loc) {
        boolean monasteryShading = false;
        if (loc == Location.MONASTERY) {
            loc = Location.CLOISTER;
            monasteryShading = true;
        }
        if (Castle.class.equals(featureClass)) {
            featureClass = City.class;
        }
        ThemeGeometry source = null;
        FeatureArea area = pluginGeometry.getArea(tileId, featureClass, loc);
        if (area == null) {
            area = adaptDefaultGeometry(DEFAULT_GEOMETRY.getArea(tileId, featureClass, loc));
            if (area == null) {
                logger.error("No shape defined for <" + (new FeatureDescriptor(tileId, featureClass, loc)) + ">");
                return new FeatureArea(new Area(), 0);
            } else {
                source = DEFAULT_GEOMETRY;
            }
        } else {
            source = pluginGeometry;
        }

        if (monasteryShading) {
            area = area.setZIndex(area.getzIndex() - 1);
        }

//        area = applyRotationScaling(tile, source, area);
//        AffineTransform t = new AffineTransform();
//        t.concatenate(tile.getRotation().getAffineTransform(NORMALIZED_SIZE, (int) (NORMALIZED_SIZE * getImageSizeRatio())));
//        area = area.transform(t);
        return area;
    }

    private Area getSubtractionArea(String tileId, boolean farm) {
        Area d = DEFAULT_GEOMETRY.getSubtractionArea(tileId, farm),
             p = pluginGeometry.getSubtractionArea(tileId, farm),
             area = new Area();

        if (d != null) {
            area.add(adaptDefaultGeometry(d));
        }
        if (p != null) {
            //HACK always area rotation scale as not scale in both width and height
            //it's what is required for ROAD subtraction but it's possible in future it will be needed scale area too.
//            Rotation rot = tile.getRotation();
//            if (rot == Rotation.R90 || rot == Rotation.R270) {
//                AffineTransform t = new AffineTransform();
//                AreaRotationScaling.NO_SCALE_HEIGHT.concatAffineTransform(t, getImageSizeRatio());
//                AreaRotationScaling.NO_SCALE_WIDTH.concatAffineTransform(t, 1.0 / getImageSizeRatio());
//                p = p.createTransformedArea(t);
//            }

            area.add(p);
        }

//        AffineTransform t = new AffineTransform();
//        t.concatenate(tile.getRotation().getAffineTransform(NORMALIZED_SIZE, (int) (NORMALIZED_SIZE * getImageSizeRatio())));
//        area.transform(t);
        return area;
    }

    private boolean isFarmComplement(String tileId, Location loc) {
        Boolean value = pluginGeometry.isFarmComplement(tileId, loc);
        if (value != null) {
            return value;
        }
        value = DEFAULT_GEOMETRY.isFarmComplement(tileId, loc);
        if (value != null) {
            return value;
        }
        return false;
    }

    private FeatureArea adaptDefaultGeometry(FeatureArea fa) {
        if (fa == null) return null;
        return fa.transform(AffineTransform.getScaleInstance(1.0, getImageSizeRatio()));
    }

    private Area adaptDefaultGeometry(Area a) {
        if (a == null) return null;
        if (imageRatioX != imageRatioY) {
            return a.createTransformedArea(
                AffineTransform.getScaleInstance(1.0, getImageSizeRatio())
            );
        }
        return a;
    }

    @Override
    public FeatureArea getFeatureArea(String effectiveTileId, Tile tile, Rotation rot, Location loc) {
        if (!isEnabled()) {
            return null;
        }

        Tuple2<Tile, Rotation> key = new Tuple2<>(tile, rot);
        Map<Location, FeatureArea> areas = areaCache.get(key);
        if (areas == null) {
            String aliasId = aliases.getGeometryAlias(effectiveTileId);
            if (aliasId != null) {
                if (!isTileSupported(aliasId)) {
                    // cross plugin alias, resolve it from parent resource manager
                    return parentResourceManager.getFeatureArea(aliasId, tile, rot, loc);
                }
                effectiveTileId = aliasId;
            } else {
                if (!isTileSupported(tile.getId())) {
                    return null;
                }
            }

            areas = getTileFeatureAreas(tile, rot, effectiveTileId);
            areaCache.put(key, areas);
        }
        return areas.get(loc).getOrNull();
    }


    private Map<Location, FeatureArea> getTileFeatureAreas(Tile tile, Rotation rot, String effectiveTileId) {
        Map<Location, Feature> features = tile.getInitialFeatures();

        Location complementFarm = features
            .find(t -> t._2 instanceof Farm && isFarmComplement(effectiveTileId, t._1))
            .map(Tuple2::_1).getOrNull();
        Location bridgeLoc = features
            .find(t -> t._2 instanceof Bridge)
            .map(Tuple2::_1).getOrNull();

        AffineTransform txRot = rot.getAffineTransform(NORMALIZED_SIZE);

        Area onlyFarmSubtraction = getSubtractionArea(effectiveTileId, true);
        Area allSubtraction = getSubtractionArea(effectiveTileId, false);
        onlyFarmSubtraction.transform(txRot);
        allSubtraction.transform(txRot);

        // get base areas for all features
        Map<Location, FeatureArea> baseAreas = Stream.ofAll(features)
            .filter(t -> !(t._2 instanceof River)) // TODO nice to have, defined river shapes as feature class instead of subtract areas
            .filter(t -> t._1 != complementFarm)
            .flatMap(t -> {
                if (t._2 instanceof Cloister && ((Cloister)t._2).isMonastery()) {
                    return List.of(
                        t,
                        t.update1(Location.MONASTERY)
                    );
                } else {
                    return List.of(t);
                }
            })
            .toMap(t -> {
                Location loc = t._1;
                Feature feature = t._2;
                FeatureArea fa;
                if (bridgeLoc == loc) {
                    fa = getBridgeArea(loc.rotateCCW(rot));
                } else {
                    fa = getFeatureArea(effectiveTileId, feature.getClass(), loc);
                }
                if (!fa.isFixed()) {
                    fa = fa.transform(txRot);
                }
                return new Tuple2<>(loc, fa);
            });


        FeatureArea towerArea = baseAreas.get(Location.TOWER).getOrNull();

        // farms are defined in shapes.xml as bounding regions, other non-farm
        // features must be subtract for get clear shape, compute farm subtraction now
        Area nonFarmUnion = baseAreas.foldLeft(new Area(), (area, t) -> {
            if (!t._1.isFarmLocation()) {
                area.add(t._2.getTrackingArea()); // Area is mutable, returning same reference
            }
            return area;
        });

        // complement farm area is remaining uncovered area
        if (complementFarm != null) {
            Area union = baseAreas.foldLeft(new Area(), (area, t) -> {
                area.add(t._2.getTrackingArea()); // Area is mutable, returning same ref
                return area;
            });
            Area farmArea = new Area(getFullRectangle());
            farmArea.subtract(union);
            baseAreas = baseAreas.put(complementFarm, new FeatureArea(farmArea, FeatureArea.DEFAULT_FARM_ZINDEX));
        }

        Stream<Tuple2<Location, FeatureArea>> areas = Stream.ofAll(baseAreas)
            .map(t -> {
                // subtract other features from farms
                if (!t._1.isFarmLocation()) return t;
                return t.map2(fa -> fa.subtract(nonFarmUnion));
            })
            .map(t -> {
                // subtract "restricted" areas
                Location loc = t._1;
                if (loc == bridgeLoc) return t;
                if (loc.isFarmLocation()) t = t.map2(fa -> fa.subtract(onlyFarmSubtraction));
                return t.map2(fa -> fa.subtract(allSubtraction));
            });

        // subtract tower
        if (towerArea != null) {
            areas = areas.map(t -> {
                Location loc = t._1;
                if (loc == bridgeLoc || loc == Location.TOWER) return t;
                return t.map2(fa -> fa.subtract(towerArea));
            });
        }

        // if flier area is requested, add it to result and subtract it from others
        //TODO don't subtract if contained in locations but subtract when present on tile
//        if (locations.contains(Location.FLIER)) {
//            FeatureArea flierArea = getFeatureArea(tileDef, null, Location.FLIER);
//            baseAreas = baseAreas.mapValues(fa -> fa.subtract(flierArea));
//            baseAreas = baseAreas.put(Location.FLIER, flierArea);
//        }

        // bridge should be above all, make bridge active on intersection with common areas
        // -> subtract bridge from other areas
        if (bridgeLoc != null) {
            FeatureArea bridgeArea = baseAreas.get(bridgeLoc).get();
            areas = areas.map(t -> {
                if (t._1 == bridgeLoc || t._1.isFarmLocation()) return t;
                return t.map2(fa -> fa.subtract(bridgeArea));
            });
        }

        //AffineTransform tx = getAreaScaleTransform(rot, width, height);
        return areas.toMap(t -> t.map1(loc -> loc.rotateCW(rot)));
//            new Tuple2<>(t._1.rotateCW(rot), t._2.transform(tx))
//        );
    }

//    //TODO return back, just scale to NORMALIZED size but use ImageSizeRatio
//    private AffineTransform getAreaScaleTransform(Rotation rot, int width, int height) {
//        double ratioX;
//        double ratioY;
//        if (rot == Rotation.R90 || rot  == Rotation.R270) {
//            ratioX = (double) height / NORMALIZED_SIZE / getImageSizeRatio();
//            ratioY = (double) width / NORMALIZED_SIZE;
//        } else {
//            ratioX = (double) width / NORMALIZED_SIZE;
//            ratioY = (double) height / NORMALIZED_SIZE / getImageSizeRatio();
//        }
//        return AffineTransform.getScaleInstance(ratioX, ratioY);
//    }

    @Override
    public FeatureArea getBarnArea() {
        return null;
    }

    //TODO move to Default
    @Override
    public FeatureArea getBridgeArea(Location loc) {
        Area a = pluginGeometry.getBridgeArea(loc);
        return (new FeatureArea(a, FeatureArea.DEFAULT_BRIDGE_ZINDEX)).setFixed(true);
    }

    private Rectangle getFullRectangle() {
        return new Rectangle(0,0, NORMALIZED_SIZE-1, (int) (NORMALIZED_SIZE * getImageSizeRatio()));
    }

    public Aliases getAliases() {
        return aliases;
    }

    public ResourceManager getParentResourceManager() {
        return parentResourceManager;
    }

    public void setParentResourceManager(ResourceManager parentResourceManager) {
        this.parentResourceManager = parentResourceManager;
    }
}

