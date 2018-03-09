package net.gdface.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * classpath resource 工具
 * @author guyadong
 *
 */
public class ClassResourceUtils {

	/**
	 * 文件名过滤器接口
	 * @author guyadong
	 *
	 */
	public static interface FileFilter{
		/**
		 * Tests if a specified file should be included in a file list.
		 * @param filename
		 * @return
		 */
		boolean accept(String filename);
	}
	/**
	 * 参见 {@link #getResourceFileList(Class, String)},<br>
	 * {@link IOException}封装为{@link RuntimeException},
	 * @param clazz Class to use when getting the System classloader
	 * @param path
	 * @return
	 */
	public static List<String> getFilesUnchedked(Class<?> clazz, String path){
		try {
			return getResourceFileList(ClassResourceUtils.class,path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 
	 * @param clazz Class to use when getting the System classloader
	 * @param path
	 * @param filter A filename filter
	 * @return
	 * @see #getFilesUnchedked(Class, String)
	 */
	public static List<String> getFilesUnchecked(Class<?> clazz,String path, FileFilter filter){
		List<String> list = getFilesUnchedked(clazz, path);
		if(null != filter){
			for(Iterator<String> itor = list.iterator();itor.hasNext();){
				String file = itor.next();
				if(!filter.accept(file)){
					itor.remove();
				}
			}
		}
		return list;
	}
	/**
	 * Returns true if resource exist.
	 * @param clazz Class to use when getting the System classloader
	 * @param resource
	 * @return
	 */
	public static boolean resourceExist(Class<?> clazz, String resource){
		return getResource(clazz,resource)!=null;
	}

	/**
     * Return a context-relative path, beginning with a "/", that represents
     * the canonical version of the specified path after ".." and "." elements
     * are resolved out.  If the specified path attempts to go outside the
     * boundaries of the current context (i.e. too many ".." path elements
     * are present), return <code>null</code> instead.
     *
     * @param path Path to be normalized
     * @return String normalized path
     */
    public static String normalizePath(String path)
    {
        // Normalize the slashes and add leading slash if necessary
        String normalized = path;
        if (normalized.indexOf('\\') >= 0)
        {
            normalized = normalized.replace('\\', '/');
        }

        if (!normalized.startsWith("/"))
        {
            normalized = "/" + normalized;
        }

        // Resolve occurrences of "//" in the normalized path
        while (true)
        {
            int index = normalized.indexOf("//");
            if (index < 0){
                break;
            }
            normalized = normalized.substring(0, index) +
            normalized.substring(index + 1);
        }

        // Resolve occurrences of "%20" in the normalized path
        while (true)
        {
            int index = normalized.indexOf("%20");
            if (index < 0){
                break;
            }
            normalized =new StringBuilder()
            	.append(normalized.substring(0, index))
            	.append(" ")
            	.append(normalized.substring(index + 3)).toString();
        }

        // Resolve occurrences of "/./" in the normalized path
        while (true)
        {
            int index = normalized.indexOf("/./");
            if (index < 0){
                break;
            }
            normalized = normalized.substring(0, index) +
            normalized.substring(index + 2);
        }

        // Resolve occurrences of "/../" in the normalized path
        while (true)
        {
            int index = normalized.indexOf("/../");
            if (index < 0){
                break;
            }
            if (index == 0){
            	// Trying to go outside our context
                return (null);  
            }
            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized = normalized.substring(0, index2) +
            normalized.substring(index + 3);
        }

        // Return the normalized path that we have completed
        return (normalized);
    }
    /**
     * 
     * Return a normalized directory path, end with "/", but not start with one 
     * @param path Path to be normalized
     * @return Path to be normalized
     * @see #normalizePath(String)
     */
    public static String normalizeDirPath(String path){
    	path = normalizePath(path);
    	if (path.startsWith("/"))
        {
            path = path.substring(1);
        }
    	if(!path.endsWith("/")){
    		path += "/";
    	}
    	return path;
    }
    /**
     * Returns an input stream for reading the specified resource.
     * @param clazz Class to use when getting the System classloader (used if no Thread
     * Context classloader available or fails to get resource).
     * @param name name of the resource
     * @return InputStream for the resource.
     * @see #getResource(Class, String)
     */
    public static InputStream getResourceAsStream(Class<?> clazz, String name)
    {
        URL url = getResource(clazz,name);
        try {
            return url != null ? url.openStream() : null;
        } catch (IOException e) {
            return null;
        }
    }
    /**
     * Finds a resource with the given name.  Checks the Thread Context
     * classloader, then uses the System classloader.  Should replace all
     * calls to <code>Class.getResourceAsString</code> when the resource
     * might come from a different classloader.  (e.g. a webapp).
     * @param claz Class to use when getting the System classloader (used if no Thread
     * Context classloader available or fails to get resource).
     * @param path name of the resource
     * @return  A <tt>URL</tt> object for reading the resource, or
     *          <tt>null</tt> if the resource could not be found or the invoker
     *          doesn't have adequate  privileges to get the resource.
     */
    public static URL getResource(Class<?> claz, String path)
    {
        URL result = null;
        path = normalizePath(path);
        /**
         * remove leading slash so path will work with classes in a JAR file
         */
        path = path.substring(1);
        ClassLoader classLoader = Thread.currentThread()
                                    .getContextClassLoader();

        if (classLoader == null)
        {
            classLoader = claz.getClassLoader();
            result = classLoader.getResource( path );
        }
        else
        {
            result= classLoader.getResource( path );

            /**
            * for compatibility with texen / ant tasks, fall back to
            * old method when resource is not found.
            */

            if (result == null)
            {
                classLoader = claz.getClassLoader();
                if (classLoader != null){
                    result = classLoader.getResource( path );
                }
            }
        }
        return result;

    }
    private static final FilenameFilter FILE_FILTER = new FilenameFilter(){
		@Override
		public boolean accept(File dir, String name) {
			return new File(dir,name).isFile();
		}};
	private static final String PROTOCOL_FILE = "file";
	private static final String PROTOCOL_JAR = "jar";
    /**
     * List file names for a resource folder. Not recursive.
     * This is basically a brute-force implementation.
     * Works for regular files and also JARs.<br>
     * refer to: <a href="http://www.uofr.net/~greg/java/get-resource-listing.html">Java: Listing the contents of a resource directory</a>
     * @param clazz Any java class that lives in the same place as the resources you want.
     * @param dirPath 
     * @return Just the name of each member item, not the full paths.
     * @throws IOException 
     */
    public static List<String> getResourceFileList(Class<?> clazz, String dirPath) throws IOException {
    	if(null == dirPath || dirPath.isEmpty()){
    		throw new IllegalArgumentException("path must not be null or empty");
    	}
    	dirPath = normalizeDirPath(dirPath);
        URL dirURL = getResource(clazz,dirPath);
        if(null == dirURL){
        	throw new FileNotFoundException(dirPath);
        }
        
        if (PROTOCOL_FILE.equals(dirURL.getProtocol())) {
          /* A file path: easy enough */
          return new ArrayList<String>(Arrays.asList(new File(URI.create(dirURL.toString())).list(FILE_FILTER)));
        } 
   
        if (PROTOCOL_JAR.equals(dirURL.getProtocol())) {
        	/* A JAR path */
        	//strip out only the JAR file
        	String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); 
        	JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
        	LinkedList<String> result = new LinkedList<String>();
          try{
        	//gives ALL entries in jar
        	  Enumeration<JarEntry> entries = jar.entries(); 
        	  while(entries.hasMoreElements()) {
        		  JarEntry entry = entries.nextElement();
        		// if it is a subdirectory, skip
        		  if(!entry.isDirectory()){
        			  String name = entry.getName();
            		  if (name.startsWith(dirPath)) { 
            			  //filter according to the path
            			  String element = name.substring(dirPath.length());
            			  int checkSubdir = element.indexOf("/");
            			  if (checkSubdir < 0 ) {
            				  result.add(element);
            			  }
            		  }
        		  }        		  
        	  }
          }finally{
        	  jar.close();
          }
          return result;
        }           
        throw new UnsupportedOperationException("Cannot list files for URL "+dirURL);
    }
}
