package edu.mit.yingyin.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ObjectIO {
  public static void writeObject(Object obj, String filename)
      throws IOException {
    FileOutputStream fout = null;
    fout = new FileOutputStream(filename);
    OutputStream bout = new BufferedOutputStream(fout);
    ObjectOutputStream oout = new ObjectOutputStream(bout);
    oout.writeObject(obj);
    oout.flush();
    bout.flush();
    fout.flush();
    oout.close();
    fout.close();
  }

  public static void writeObjectCompressed(Object obj, String filename)
      throws IOException {
    FileOutputStream fout = null;
    fout = new FileOutputStream(filename);
    OutputStream bout = new BufferedOutputStream(fout);
    GZIPOutputStream gout = new GZIPOutputStream(bout);
    ObjectOutputStream oout = new ObjectOutputStream(gout);
    oout.writeObject(obj);
    oout.flush();
    gout.flush();
    bout.flush();
    fout.flush();

    oout.close();
    gout.close();
    bout.close();
    fout.close();
  }

  public static Object readStream(InputStream fin) throws IOException {
    Object obj = null;
    InputStream bin = new BufferedInputStream(fin);
    ObjectInputStream oin = new ObjectInputStream(bin);
    try {
      obj = oin.readObject();
    } catch (ClassNotFoundException c) {
      throw new IOException(c.getMessage());
    }
    oin.close();
    return obj;
  }

  public static Object readObject(String filename) throws IOException {
    FileInputStream fin = null;
    Object obj = null;
    fin = new FileInputStream(filename);
    InputStream bin = new BufferedInputStream(fin);
    ObjectInputStream oin = new ObjectInputStream(bin);
    try {
      obj = oin.readObject();
    } catch (ClassNotFoundException c) {
      throw new IOException(c.getMessage());
    } finally {
      oin.close();
    }
    fin.close();
    return obj;
  }

  public static Object readObjectCompressed(String filename) throws IOException {
    FileInputStream fin = null;
    Object obj = null;
    fin = new FileInputStream(filename);
    InputStream bin = new BufferedInputStream(fin);
    GZIPInputStream gin = new GZIPInputStream(bin);
    ObjectInputStream oin = new ObjectInputStream(gin);
    try {
      obj = oin.readObject();
    } catch (ClassNotFoundException c) {
      throw new IOException(c.getMessage());
    } finally {
      oin.close();
    }
    gin.close();
    fin.close();
    return obj;
  }
}
