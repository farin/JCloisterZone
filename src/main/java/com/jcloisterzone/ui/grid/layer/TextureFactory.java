package com.jcloisterzone.ui.grid.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;

public class TextureFactory {

    private final int squareSize;
    private int seq;

    public TextureFactory(int squareSize) {
        this.squareSize = squareSize;
    }

    public TexturePaint create(Color c) {
        switch (seq++) {
        case 9: seq = 0;
        case 0: return createDiagonalUp(c);
        case 1: return createCheck(c);
        case 2: return createZigZagDown(c);
        case 3: return createHorizontal(c);
        case 4: return createTriangles(c);
        case 5: return createDiagonalCheck(c);
        case 6: return createDiagonalDown(c);
        case 7: return createZigZagUp(c);
        default: return createVertical(c);
        }
    }

    public TexturePaint createMultiColor(Color[] c) {
        int a = squareSize/12;
        BufferedImage bi = new BufferedImage(a, a*(c.length+1), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        for (int i = 0; i < c.length; i++) {
            g2.setColor(c[i]);
            g2.fillRect(0, i*a, a, a);
        }
        return new TexturePaint(bi, new Rectangle(0, 0, a, a*(c.length+1)));
    }

    private TexturePaint createDiagonalUp(Color c) {
        int a = squareSize/4;
        if (a % 2 == 1) a++;
        BufferedImage bi = new BufferedImage(a, a, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setColor(c);
        g2.fill(new Polygon(new int[] {a/2, a, a}, new int[] {a, a/2, a}, 3));
        g2.fill(new Polygon(new int[] {0, a/2, a, 0}, new int[] {a/2, 0, 0, a}, 4));
        return new TexturePaint(bi, new Rectangle(0, 0, a, a));
    }

    private TexturePaint createDiagonalDown(Color c) {
        int a = squareSize/4;
        if (a % 2 == 1) a++;
        BufferedImage bi = new BufferedImage(a, a, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setColor(c);
        g2.fill(new Polygon(new int[] {0, 0, a/2}, new int[] {a, a/2, a}, 3));
        g2.fill(new Polygon(new int[] {0, a/2, a, a}, new int[] {0, 0, a/2, a}, 4));
        return new TexturePaint(bi, new Rectangle(0, 0, a, a));
    }

    private TexturePaint createVertical(Color c) {
        int a = squareSize/6;
        if (a % 2 == 1) a++;
        BufferedImage bi = new BufferedImage(a, a, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setColor(c);
        g2.fillRect(0, 0, a/2, a);
        return new TexturePaint(bi, new Rectangle(0, 0, a, a));
    }

    private TexturePaint createHorizontal(Color c) {
        int a = squareSize/6;
        if (a % 2 == 1) a++;
        BufferedImage bi = new BufferedImage(a, a, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setColor(c);
        g2.fillRect(0, 0, a, a/2);
        return new TexturePaint(bi, new Rectangle(0, 0, a, a));
    }

    private TexturePaint createDiagonalCheck(Color c) {
        int a = squareSize/6;
        if (a % 2 == 1) a++;
        BufferedImage bi = new BufferedImage(a, a, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setColor(c);
        g2.fill(new Polygon(new int[] {a/2, a, a/2, 0}, new int[] {0, a/2, a, a/2}, 4));
        return new TexturePaint(bi, new Rectangle(0, 0, a, a));
    }

    private TexturePaint createCheck(Color c) {
        int a = squareSize/6;
        if (a % 2 == 1) a++;
        BufferedImage bi = new BufferedImage(a, a, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setColor(c);
        g2.fillRect(0, 0, a/2, a/2);
        g2.fillRect(a/2, a/2, a/2, a/2);
        return new TexturePaint(bi, new Rectangle(0, 0, a, a));
    }

    private TexturePaint createZigZagDown(Color c) {
        int a = squareSize/4;
        if (a % 4 != 0) a = ((a/4)+1)*4;
        BufferedImage bi = new BufferedImage(a, a, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setColor(c);
        g2.fillRect(0, 3*a/4, a/2, a/4);
        g2.fillRect(a/4, 0, a/4, a/4);
        g2.fillRect(a/4, a/4, 3*a/4, a/4);
        g2.fillRect(3*a/4, a/2, a/4, a/2);
        return new TexturePaint(bi, new Rectangle(0, 0, a, a));
    }

    private TexturePaint createZigZagUp(Color c) {
        int a = squareSize/4;
        if (a % 4 != 0) a = ((a/4)+1)*4;
        BufferedImage bi = new BufferedImage(a, a, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setColor(c);
        g2.fillRect(a/4, 0, 3*a/4, a/4);
        g2.fillRect(a/4, a/4, a/4, a/4);
        g2.fillRect(0, a/2, a/2, a/4);
        g2.fillRect(3*a/4, a/2, a/4, a/2);
        return new TexturePaint(bi, new Rectangle(0, 0, a, a));
    }

    private TexturePaint createTriangles(Color c) {
        int a = squareSize/6;
        if (a % 2 == 1) a++;
        int b = (int) (a * Math.sqrt(3.0));
        BufferedImage bi = new BufferedImage(a, b, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setColor(c);
        g2.fill(new Polygon(new int[] {0, a/2, a}, new int[] {0, b/2, 0}, 3));
        g2.fill(new Polygon(new int[] {0, a/2, 0}, new int[] {b/2, b/2, b}, 3));
        g2.fill(new Polygon(new int[] {a, a/2, a}, new int[] {b/2, b/2, b}, 3));
        return new TexturePaint(bi, new Rectangle(0, 0, a, b));
    }
}