package com.jcloisterzone.bugreport;

import java.awt.Container;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.xml.transform.TransformerException;

import com.jcloisterzone.FileTeeStream;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.save.SavedGame;
import com.jcloisterzone.game.save.SavedGameParser;
import com.jcloisterzone.ui.JCloisterZone;

public class ReportingTool {

    private Game game;
    private Container container;

    public void createReport(FileOutputStream fos, String description) throws IOException, TransformerException {
        //createStringReport(System.out, description);
        createZipReport(fos, description);
    }

    public void createZipReport(FileOutputStream fos, String description) throws IOException, TransformerException  {
        ZipOutputStream zos = new ZipOutputStream(fos);
        ZipEntry ze;

        ze = new ZipEntry("description.txt");
        zos.putNextEntry(ze);
        zos.write(description.getBytes());
        zos.write("\r\n--- reported with -----\r\n".getBytes());
        zos.write((JCloisterZone.VERSION + " " + JCloisterZone.BUILD_DATE + " \r\n").getBytes());
        zos.write((System.getProperty("os.name")+" ").getBytes());
        zos.write((System.getProperty("os.arch")+" ").getBytes());
        zos.write((System.getProperty("os.version")+"\r\n").getBytes());
        zos.write((System.getProperty("java.vendor")+" ").getBytes());
        zos.write((System.getProperty("java.version")+"\r\n").getBytes());
        zos.closeEntry();

        ze = new ZipEntry("savegame.jcz");
        zos.putNextEntry(ze);
        SavedGame save = new SavedGame(game);
        SavedGameParser parser = new SavedGameParser(true);
        save.setAnnotations(game.getGameAnnotations());
        zos.write(parser.toJson(save).getBytes());
        zos.closeEntry();

        if (container != null) {
            ze = new ZipEntry("board.png");
            zos.putNextEntry(ze);
            BufferedImage im = new BufferedImage(container.getWidth(), container.getHeight(), BufferedImage.TYPE_INT_ARGB);
            container.paint(im.getGraphics());
            ImageIO.write(im, "PNG", zos);
            zos.closeEntry();
        }

        if (System.err instanceof FileTeeStream) {
            FileTeeStream errStream = (FileTeeStream) System.err;
            if (errStream.getFile().exists()) {
                ze = new ZipEntry("error.log");
                zos.putNextEntry(ze);
                zos.write(Files.readAllBytes(errStream.getFile().toPath()));
                zos.closeEntry();
            }
        }
        zos.close();
    }

    //dev purpose only, currently not used
    public void createStringReport(PrintStream out, String description) throws IOException, TransformerException {
        out.println("---------- description ------------");
        out.println(description);

        out.println("---------- save ------------");
        SavedGame save = new SavedGame(game);
        SavedGameParser parser = new SavedGameParser(true);
        save.setAnnotations(game.getGameAnnotations());
        parser.toJson(save, new OutputStreamWriter(out));

        out.println("---------- system env ------------");
        out.println(System.getProperty("java.version"));
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

}

