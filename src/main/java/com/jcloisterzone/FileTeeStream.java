package com.jcloisterzone;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class FileTeeStream extends PrintStream {

	PrintStream out, err;
    String fileName;

    public FileTeeStream(PrintStream err, String fileName) {
        super(err);
        this.fileName = fileName;
    }
    @Override
	public void write(byte buf[], int off, int len) {
    	if (out == null && fileName != null) {
    		try {
    			out = new PrintStream(new FileOutputStream(fileName),true);
			} catch (FileNotFoundException e) {
				fileName = null;
				e.printStackTrace(err);
			}
    	}
        super.write(buf, off, len);
        if (out != null) {
			out.write(buf, off, len);
			if (out.checkError()) {
				err.println("Log write error.");
				out = null;
				fileName = null;
			}
        }
    }
    @Override
	public void flush() {
        super.flush();
        if (out != null) {
			out.flush();
        }
    }
}
