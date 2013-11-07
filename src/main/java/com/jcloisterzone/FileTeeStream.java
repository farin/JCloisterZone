package com.jcloisterzone;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class FileTeeStream extends PrintStream {

    PrintStream fileOut;
    String fileName;

    public FileTeeStream(PrintStream err, String fileName) {
        super(err);
        this.fileName = fileName;
    }

    @Override
    public void write(byte buf[], int off, int len) {
        if (fileOut == null && fileName != null) {
            try {
                fileOut = new PrintStream(new FileOutputStream(fileName), true);
            } catch (FileNotFoundException e) {
                fileName = null;
                e.printStackTrace();
            }
        }
        super.write(buf, off, len);
        if (fileOut != null) {
            fileOut.write(buf, off, len);
            if (fileOut.checkError()) {
                System.err.println("File stream write error.");
                fileOut = null;
                fileName = null;
            }
        }
    }

    @Override
    public void flush() {
        super.flush();
        if (fileOut != null) {
            fileOut.flush();
        }
    }

    @Override
    public void close() {
        super.close();
        if (fileOut != null) {
            fileOut.close();
        }
    }
}
