package com.jcloisterzone.ui.theme;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jcloisterzone.XmlUtils;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.theme.SvgTransformationCollector.GeometryHandler;

class PointsParser {
    final URL resource;
    private Map<FeatureDescriptor, ImmutablePoint> points;

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public PointsParser(URL resource) {
        this.resource = resource;
    }

    private void processPointElement(Element pointNode) {
        int cx = Integer.parseInt(pointNode.getAttribute("cx"));
        int cy = Integer.parseInt(pointNode.getAttribute("cy"));
        final Point destPoint = new Point(), srcPoint = new Point(cx, cy);

        SvgTransformationCollector transformCollector = new SvgTransformationCollector(pointNode);
        transformCollector.collect(new GeometryHandler() {

            @Override
            public void processApply(Element node, FeatureDescriptor fd, AffineTransform transform) {
                assert !points.containsKey(fd) : fd + " already defined";
                transform.transform(srcPoint, destPoint);
                //TODO use 1000-pixel standard
                points.put(fd, new ImmutablePoint(destPoint.x/10, destPoint.y/10));
            }

            @Override
            public void processSubstract(Element node, String tileId, AffineTransform transform, boolean isFarm) {
                throw new UnsupportedOperationException("<substract> not allowed for points.xml");
            }

        });
    }

    public Map<FeatureDescriptor, ImmutablePoint> parse() {
        try {
            return doParse();
        } catch (IOException | SAXException | ParserConfigurationException e) {
            logger.error(e.getMessage(), e);
            return new HashMap<>();
        }
    }

    private Map<FeatureDescriptor, ImmutablePoint> doParse() throws IOException, SAXException, ParserConfigurationException {
        Element root = XmlUtils.parseDocument(resource).getDocumentElement();
        NodeList nl = root.getElementsByTagName("point");
        points = new HashMap<>();
        for (int i = 0; i < nl.getLength(); i++) {
            processPointElement((Element) nl.item(i));
        }
        return points;
    }
}