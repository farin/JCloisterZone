package com.jcloisterzone.ui.theme;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import org.w3c.dom.Element;

public class SvgToShapeConverter {

    public Area convert(Element shapeSvgNode) {
        Shape s;
        switch (shapeSvgNode.getNodeName()) {
        case "svg:polygon":
            s = createPolygon(shapeSvgNode);
            break;
        case "svg:rect":
            s = createRectangle(shapeSvgNode);
            break;
        case "svg:circle":
            s = createCircle(shapeSvgNode);
            break;
        default:
            throw new IllegalArgumentException("Unable to convert "+shapeSvgNode.getNodeName());
        }
        return new Area(s);
    }

//    public Area convert(Element shapeSvgNode, AffineTransform transform) {
//        Area area = convert(shapeSvgNode);
//        return area.createTransformedArea(transform);
//    }

    private Rectangle createRectangle(Element shapeSvgNode) {
        int x = Integer.valueOf(shapeSvgNode.getAttribute("x"));
        int y = Integer.valueOf(shapeSvgNode.getAttribute("y"));
        int width = Integer.valueOf(shapeSvgNode.getAttribute("width"));
        int heigh = Integer.valueOf(shapeSvgNode.getAttribute("height"));
        return new Rectangle(x, y, width, heigh);
    }

    private Ellipse2D.Double createCircle(Element shapeSvgNode) {
        int x, y, r;
        x = Integer.parseInt(shapeSvgNode.getAttribute("cx"));
        y = Integer.parseInt(shapeSvgNode.getAttribute("cy"));
        r = Integer.parseInt(shapeSvgNode.getAttribute("r"));
        return new Ellipse2D.Double(x-r,y-r,2*r,2*r);
    }

    private Polygon createPolygon(Element shapeSvgNode) {
        Polygon p = new Polygon();
        String[] points = shapeSvgNode.getAttribute("points").split(" ");
        for (int i = 0; i < points.length; i++) {
            String[] xy = points[i].split(",");
            p.addPoint(Integer.valueOf(xy[0]), Integer.valueOf(xy[1]));
        }
        return p;
    }
}
