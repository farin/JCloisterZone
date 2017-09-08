package com.jcloisterzone.ui.plugin;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.feature.Bridge;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.capability.CountCapability;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.resources.FeatureArea;
import com.jcloisterzone.ui.resources.FeatureDescriptor;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;
import com.jcloisterzone.ui.resources.ResourceManager;
import com.jcloisterzone.ui.resources.TileImage;
import com.jcloisterzone.ui.resources.svg.ThemeGeometry;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;


public class ResourcePlugin extends Plugin implements ResourceManager {

    private static ThemeGeometry defaultGeometry;
    private ThemeGeometry pluginGeometry;
    private Insets imageOffset =  new Insets(0, 0, 0, 0);
    private int imageRatioX = 1;
    private int imageRatioY = 1;

    private java.util.HashMap<Tuple2<TileDefinition, Rotation>, Map<Location, FeatureArea>> areaCache = new java.util.HashMap<>();

    private Set<String> supportedExpansions = HashSet.empty(); //expansion codes

    static {
        try {
            defaultGeometry = new ThemeGeometry(ResourcePlugin.class.getClassLoader(), "defaults/tiles", 1.0);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            LoggerFactory.getLogger(ThemeGeometry.class).error(e.getMessage(), e);
        }
    }

    public ResourcePlugin(URL url, String relativePath) throws Exception {
        super(url, relativePath);
    }

    @Override
    protected void doLoad() throws IOException, SAXException, ParserConfigurationException {
        pluginGeometry = new ThemeGeometry(getLoader(), "tiles", getImageSizeRatio());
    }

    @Override
    protected void parseMetadata(Element rootElement) throws Exception {
        super.parseMetadata(rootElement);
        NodeList nl = rootElement.getElementsByTagName("expansions");
        if (nl.getLength() == 0) throw new Exception("Supported expansions missing in plugin.xml for " + getId());
        Element expansion = (Element) nl.item(0);
        nl = expansion.getElementsByTagName("expansion");
        if (nl.getLength() == 0) throw new Exception("No expansion is supported by " + getId());
        for (int i = 0; i < nl.getLength(); i++) {
            String expName = nl.item(i).getFirstChild().getNodeValue().trim();
            Expansion exp = Expansion.valueOf(expName);
            supportedExpansions = supportedExpansions.add(exp.getCode());
        }

        Element tiles = XMLUtils.getElementByTagName(rootElement, "tiles");
        if (tiles != null) {
            String value = XMLUtils.childValue(tiles, "image-offset");
            if (value != null) {
                String[] tokens = value.split(",");
                if (tokens.length != 4) {
                    throw new Exception("Invalid value for image-offset " + value);
                }
                imageOffset = new Insets(
                   Integer.parseInt(tokens[0]),
                   Integer.parseInt(tokens[1]),
                   Integer.parseInt(tokens[2]),
                   Integer.parseInt(tokens[3])
                );
            }
            value = XMLUtils.childValue(tiles, "image-ratio-x");
            if (value != null) {
                imageRatioX = Integer.parseInt(value);
                if (imageRatioX == 0)
                    imageRatioX = 1;
            }
            value = XMLUtils.childValue(tiles, "image-ratio-y");
            if (value != null) {
                imageRatioY = Integer.parseInt(value);
                if (imageRatioY == 0) imageRatioY = 1;
            }
        }
    }


    protected boolean containsTile(String tileId) {
        if (!isEnabled()) return false;
        String expCode = tileId.substring(0, 2);
        return supportedExpansions.contains(expCode);
    }

    public boolean isExpansionSupported(Expansion exp) {
        return supportedExpansions.contains(exp.getCode());
    }

    public double getImageSizeRatio() {
        return imageRatioY/(double)imageRatioX;
    }

    @Override
    public TileImage getTileImage(TileDefinition tile, Rotation rot) {
        return getTileImage(tile.getId(), rot);
    }

    @Override
    public TileImage getAbbeyImage(Rotation rot) {
        return getTileImage(TileDefinition.ABBEY_TILE_ID, rot);
    }

    private TileImage getTileImage(String tileId, Rotation rot) {
        if (!containsTile(tileId)) return null;
        String baseName = "tiles/"+tileId.substring(0, 2) + "/" + tileId.substring(3);
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
    public ImmutablePoint getMeeplePlacement(TileDefinition tile, Rotation rot, Location loc) {
        if (!containsTile(tile.getId())) return null;
        if (loc == Location.MONASTERY) loc = Location.CLOISTER;

        Location iniLoc = loc.rotateCCW(rot);
        Feature feature = tile.getInitialFeatures().get(iniLoc).get();

        ImmutablePoint point = pluginGeometry.getMeeplePlacement(tile, feature.getClass(), iniLoc);
        if (point == null) {
            point = defaultGeometry.getMeeplePlacement(tile, feature.getClass(), iniLoc);
        }
        if (point == null) {
            logger.warn("No point defined for <" + (new FeatureDescriptor(tile.getId(), feature.getClass(), loc)) + ">");
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

    private FeatureArea getFeatureArea(TileDefinition tile, Class<? extends Feature> featureClass, Location loc) {
        boolean monasteryShading = false;
        if (loc == Location.MONASTERY) {
            loc = Location.CLOISTER;
            monasteryShading = true;
        }
        if (Castle.class.equals(featureClass)) {
            featureClass = City.class;
        }
        ThemeGeometry source = null;
        FeatureArea area = pluginGeometry.getArea(tile, featureClass, loc);
        if (area == null) {
            area = adaptDefaultGeometry(defaultGeometry.getArea(tile, featureClass, loc));
            if (area == null) {
                logger.error("No shape defined for <" + (new FeatureDescriptor(tile.getId(), featureClass, loc)) + ">");
                return new FeatureArea(new Area(), 0);
            } else {
                source = defaultGeometry;
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

    private Area getSubtractionArea(TileDefinition tile, boolean farm) {
        Area d = defaultGeometry.getSubtractionArea(tile, farm),
             p = pluginGeometry.getSubtractionArea(tile, farm),
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

    private boolean isFarmComplement(TileDefinition tile, Location loc) {
        if (pluginGeometry.isFarmComplement(tile, loc)) return true;
        if (defaultGeometry.isFarmComplement(tile, loc)) return true;
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
    public FeatureArea getFeatureArea(TileDefinition tile, Rotation rot, Location loc) {
        Tuple2<TileDefinition, Rotation> key = new Tuple2<>(tile, rot);
        Map<Location, FeatureArea> areas = areaCache.get(key);
        if (areas == null) {
            areas = getTileFeatureAreas(tile, rot);
            areaCache.put(key, areas);
        }
        return areas.get(loc).getOrNull();
    }


    private Map<Location, FeatureArea> getTileFeatureAreas(TileDefinition tile, Rotation rot) {
        if (!containsTile(tile.getId())) return HashMap.empty();

        Map<Location, Feature> features = tile.getInitialFeatures();

        Location complementFarm = features
            .find(t -> t._2 instanceof Farm && isFarmComplement(tile, t._1))
            .map(Tuple2::_1).getOrNull();
        Location bridgeLoc = features
            .find(t -> t._2 instanceof Bridge)
            .map(Tuple2::_1).getOrNull();

        AffineTransform txRot = rot.getAffineTransform(NORMALIZED_SIZE);

        Area onlyFarmSubtraction = getSubtractionArea(tile, true);
        Area allSubtraction = getSubtractionArea(tile, false);
        onlyFarmSubtraction.transform(txRot);
        allSubtraction.transform(txRot);

        // get base areas for all features
        Map<Location, FeatureArea> baseAreas = Stream.ofAll(features)
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
                    fa = getFeatureArea(tile, feature.getClass(), loc);
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

    //TODO return back, just scale to NORMALIZED size but use ImageSizeRatio
    private AffineTransform getAreaScaleTransform(Rotation rot, int width, int height) {
        double ratioX;
        double ratioY;
        if (rot == Rotation.R90 || rot  == Rotation.R270) {
            ratioX = (double) height / NORMALIZED_SIZE / getImageSizeRatio();
            ratioY = (double) width / NORMALIZED_SIZE;
        } else {
            ratioX = (double) width / NORMALIZED_SIZE;
            ratioY = (double) height / NORMALIZED_SIZE / getImageSizeRatio();
        }
        return AffineTransform.getScaleInstance(ratioX, ratioY);
    }

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
}
