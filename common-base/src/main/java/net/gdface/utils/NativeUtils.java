/*
 * Class NativeUtils is published under the The MIT License:
 *
 * Copyright (c) 2012 Adam Heinrich <adam@adamh.cz>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.gdface.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * 从jar包中加载指定路径下的动态库<br>
 * 
 * @see <a href="http://adamheinrich.com/blog/2012/how-to-load-native-jni-library-from-jar">http://adamheinrich.com/blog/2012/how-to-load-native-jni-library-from-jar</a>
 * @see <a href="https://github.com/adamheinrich/native-utils">https://github.com/adamheinrich/native-utils</a>
 *
 */
public class NativeUtils {
    // buffer size used for reading and writing
    private static final int BUFFER_SIZE = 8192;
    public static final String NATIVE_FOLDER_PATH_PREFIX = "nativeutils";

    /**
     * Temporary directory which will contain the DLLs.
     */
    private static File temporaryDir;

    /**
     * Private constructor - this class will never be instanced
     */
    private NativeUtils() {
    }

    /**
     * Loads library from current JAR archive
     * @see #copyToTempFromJar(String, Class) 
     */
    public static synchronized void loadLibraryFromJar(String path, Class<?> loadClass) throws IOException {
    	File temp = copyToTempFromJar(path,loadClass);
        System.load(temp.getAbsolutePath());
    }
    /**
     * copy file from current JAR archive to system temporary directory
     * 
     * The file from JAR is copied into system temporary directory and then loaded. The temporary file is deleted after
     * exiting.
     * Method uses String as filename because the pathname is "abstract", not system-dependent.
     * 
     * @param path The path of file inside JAR as absolute path (beginning with '/'), e.g. /package/File.ext
     * @param loadClass class that provide {@link ClassLoader} to load library file by input stream,if null, current class instead.
     * @throws IOException If temporary file creation or read/write operation fails
     * @throws IllegalArgumentException If the path is not absolute or if the filename is shorter than three characters
     * (restriction of {@link File#createTempFile(java.lang.String, java.lang.String)}).
     * @throws FileNotFoundException If the file could not be found inside the JAR.
     */
    public static synchronized File copyToTempFromJar(String path, Class<?> loadClass) throws IOException {
    	 
        if (null == path || !path.startsWith("/")) {
            throw new IllegalArgumentException("The path has to be absolute (start with '/').");
        }
 
        // Prepare temporary file
        if (temporaryDir == null) {
            temporaryDir = createTempDirectory(NATIVE_FOLDER_PATH_PREFIX);
            temporaryDir.deleteOnExit();
        }

        File temp = new File(temporaryDir, path);
        Class<?> clazz = loadClass == null ? NativeUtils.class	: loadClass;
        InputStream is = clazz.getResourceAsStream(path);
        try{
            copy(is, temp);
            temp.deleteOnExit();
            return temp;
        } catch (IOException e) {
            temp.delete();
            throw e;
        } catch (NullPointerException e) {
            temp.delete();
            throw new FileNotFoundException("File " + path + " was not found inside JAR.");
        } finally {
			is.close();
		}       
    }
    /**
	 * @see {@link #copyToTempFromJar(String, Class)}
     */
    public static File copyToTempFromJar(String path) throws IOException {
    	return copyToTempFromJar(path, null);
    }
    /**
     * 从jar包中加载指定的动态库
     * @param path
     * @throws IOException
     * @see {@link #loadLibraryFromJar(String, Class)}
     */
    public static void loadLibraryFromJar(String path) throws IOException {
    	loadLibraryFromJar(path,null);
    }
    private static File createTempDirectory(String prefix) throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        File generatedDir = new File(tempDir, prefix + System.nanoTime());
        if (!generatedDir.mkdir())
            throw new IOException("Failed to create temp directory " + generatedDir.getName());
        
        return generatedDir;
    }
    /**
     * 从jar包中位置'/lib/os_prefix'加载指定名字的动态库<br>
     * os_prefix由{@link Platform#getNativeLibraryResourcePrefix()}计算
     * @param name 库名
     * @throws IOException
     * @see {@link #loadLibraryFromJar(String, Class)}
     */
    public static void loadFromJar(String name) throws IOException {
    	String prefix = Platform.getNativeLibraryResourcePrefix();
    	loadLibraryFromJar("/lib/" + prefix +"/" + System.mapLibraryName(name));
    }
    /**
     * 将动态库解析为内部资源路径
     * @param name
     * @return
     * @throws IOException
     */
    private static String resolveName(String name) throws IOException {
    	String prefix = Platform.getNativeLibraryResourcePrefix();
    	return "/lib/" + prefix +"/" + System.mapLibraryName(name);
    }
	public static File getTemporaryDir() {
		return temporaryDir;
	}
    private static long copy(InputStream in, File target) throws IOException  {
    	target.delete();
		File parent = target.getParentFile();
        if (parent != null){
        	parent.mkdirs();
        }
    	OutputStream out = new FileOutputStream(target);
    	// do the copy
    	try {
    		return copy(in, out);
    	}finally{
    		out.close();
    	}
    }
    /**
     * Reads all bytes from an input stream and writes them to an output stream.
     */
    private static long copy(InputStream source, OutputStream dest)
        throws IOException
    {
        long nread = 0L;
        byte[] buf = new byte[BUFFER_SIZE];
        int n;
        while ((n = source.read(buf)) > 0) {
            dest.write(buf, 0, n);
            nread += n;
        }
        return nread;
    }
	/**
	 * 从资源中加载动态库
	 * @param name 动态库名,参见 {@link System#loadLibrary(String)}
	 * @param loaderClass 
	 * @throws IOException
	 */
	public static void loadLibraryFromResource(String name, Class<?> loaderClass) throws IOException{
		if(Platform.isAndroid()){
			System.loadLibrary(name);
			return;
		}
		if(null == loaderClass){
			loaderClass = NativeUtils.class;
		}
		URL url = loaderClass.getResource(resolveName(name));
		Assert.notNull(url, "url","not found library");
		if(url.getProtocol().equals("file")){
			System.load(url.getPath());
			return ;
		}
		if(url.getProtocol().equals("jar")){
			loadFromJar(name);
			return;
		}
		throw new UnsupportedOperationException();
	}
}