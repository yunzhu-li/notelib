package org.fatlyz.notelib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NAFileUtils {

	// Copies src file to dst file.
    // If the dst file does not exist, it is created
    public static void CopyFile(File src, File dst){
    	try{
    		InputStream in = new FileInputStream(src);
        	OutputStream out = new FileOutputStream(dst);
        	copyStream(in, out);
    	} catch (IOException e) {e.printStackTrace();}
    }
    
    public static void copyStream(InputStream in, OutputStream out)
    {
    	try{
	        // Transfer bytes from in to out
	        byte[] buf = new byte[1024];
	        int len;
	        while ((len = in.read(buf)) > 0) {
	            out.write(buf, 0, len);
	        }
	        in.close();
	        out.close();
        
		} catch (IOException e) {e.printStackTrace();}
    }
}
