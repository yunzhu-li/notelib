//
//  This file is part of NoteLib.
//
//  Copyright (c) 2012-2015 Yunzhu Li.
//
//  NoteLib is free software: you can redistribute it
//  and/or modify it under the terms of the GNU General
//  Public License version 3 as published by the Free
//  Software Foundation.
//
//  NoteLib is distributed in the hope that it will be
//  useful, but WITHOUT ANY WARRANTY; without even the
//  implied warranty of MERCHANTABILITY or FITNESS FOR A
//  PARTICULAR PURPOSE. See the GNU General Public License
//  for more details.
//
//  You should have received a copy of the GNU General Public
//  License along with NoteLib.
//  If not, see http://www.gnu.org/licenses/.
//

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
    public static void CopyFile(File src, File dst) {
        try {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);
            copyStream(in, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyStream(InputStream in, OutputStream out) {
        try {
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
