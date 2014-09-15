package com.jcloisterzone.bugreport;

import java.awt.Container;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.xml.transform.TransformerException;

import com.google.common.collect.EvictingQueue;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.Snapshot;

public class ReportingTool {

    private EvictingQueue<String> events = EvictingQueue.create(1000);
    private Game game;
    private Container container;


    synchronized
    public void report(String s) {
        events.add(System.currentTimeMillis() + " " + (game == null ? "null" : game.getPhase()) + " " + s);
    }

    public void setEvents(EvictingQueue<String> events) {
        this.events = events;
    }

    public void createReport(FileOutputStream fos, String description) throws IOException, TransformerException {
        //createStringReport(System.out, description,);
        createZipReport(fos, description);
    }

    public void createZipReport(FileOutputStream fos, String description) throws IOException, TransformerException  {
        ZipOutputStream zos = new ZipOutputStream(fos);
        ZipEntry ze;

        ze = new ZipEntry("description.txt");
        zos.putNextEntry(ze);
        zos.write(description.getBytes());
        zos.closeEntry();

        ze = new ZipEntry("events.txt");
        zos.putNextEntry(ze);
        for (String s : events) {
            zos.write(s.getBytes());
            zos.write("\r\n".getBytes());
        }
        zos.closeEntry();

        ze = new ZipEntry("savegame.jcz");
        zos.putNextEntry(ze);
        Snapshot snapshot = new Snapshot(game);
        snapshot.save(zos, false, false);
        zos.closeEntry();

        if (container != null) {
            ze = new ZipEntry("board.png");
            zos.putNextEntry(ze);
            BufferedImage im = new BufferedImage(container.getWidth(), container.getHeight(), BufferedImage.TYPE_INT_ARGB);
            container.paint(im.getGraphics());
            ImageIO.write(im, "PNG", zos);
            zos.closeEntry();
        }



        zos.close();
    }

    public void createStringReport(PrintStream out, String description) throws IOException, TransformerException {
        out.println("---------- description ------------");
        out.println(description);
        out.println("---------- events ------------");
        for (String s : events) {
            out.println(s);
        }

        out.println("---------- save ------------");
        Snapshot snapshot = new Snapshot(game);
        snapshot.setGzipOutput(false);
        out.println(snapshot.saveToString());

        out.println("---------- system env ------------");
        out.println(System.getenv());
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

}

