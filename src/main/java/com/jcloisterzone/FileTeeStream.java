package com.jcloisterzone;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

public class FileTeeStream extends PrintStream {

    PrintStream outStream;
    File file;

    public FileTeeStream(PrintStream err, Path fileName) {
        super(err);
        this.file = fileName.toFile();
    }

    @Override
    public void write(byte buf[], int off, int len) {
        if (outStream == null && file != null) {
            try {
                outStream = new PrintStream(new FileOutputStream(file), true);
            } catch (FileNotFoundException e) {
                file = null;
                e.printStackTrace();
            }
        }
        super.write(buf, off, len);
        if (outStream != null) {
            outStream.write(buf, off, len);
            if (outStream.checkError()) {
                System.err.println("File stream write error.");
                outStream = null;
                file = null;
            }
        }
    }

    @Override
    public void flush() {
        super.flush();
        if (outStream != null) {
            outStream.flush();
        }
    }

    @Override
    public void close() {
        super.close();
        if (outStream != null) {
            outStream.close();
        }
    }

    public File getFile() {
        return file;
    }
}
