package com.jcloisterzone.ui.theme;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import org.w3c.dom.Element;

public class SvgToShapeConverter {

    private Shape shape;
    private String transformation;

    public SvgToShapeConverter(Element shapeSvgNode) {
        switch (shapeSvgNode.getNodeName()) {
        case "svg:polygon":
            shape = createPolygon(shapeSvgNode);
            break;
        case "svg:rect":
            shape = createRectangle(shapeSvgNode);
            break;
        case "svg:circle":
            shape = createCircle(shapeSvgNode);
            break;
        default:
            throw new IllegalArgumentException("Unable to convert "+shapeSvgNode);
        }
    }

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

    private Area transformArea(Area area, String svgTransformation) {
        if (svgTransformation == null) return area;
        AffineTransform at = null;
        switch (svgTransformation) {
        case "rotate(90 500 500)": at = AffineTransform.getRotateInstance(Math.PI * 0.5, 500, 500); break;
        case "rotate(180 500 500)": at = AffineTransform.getRotateInstance(Math.PI, 500, 500); break;
        case "rotate(270 500 500)": at = AffineTransform.getRotateInstance(Math.PI * 1.5, 500, 500); break;
        default: throw new IllegalArgumentException("Unsupported transform: "+transformation);
        }
        return area.createTransformedArea(at);
    }

    public Area convert() {
        return convert(null);
    }

    public Area convert(String svgTransformation) {
        Area area = new Area(shape);
        area = transformArea(area, transformation);
        area = transformArea(area, svgTransformation);
        return area;
    }


    public String getTransformation() {
        return transformation;
    }

    public void setTransformation(String transformation) {
        this.transformation = transformation;
    }
}
