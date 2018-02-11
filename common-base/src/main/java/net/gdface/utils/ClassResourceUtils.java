package net.gdface.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

/**
 * classpath resource 工具
 * @author guyadong
 *
 */
public class ClassResourceUtils {

	/**
	 * 返回{@code path}指定的文件夹下的所有资源名
	 * @param path
	 * @return 资源没有找到抛出{@link FileNotFoundException}异常
	 * @throws IOException
	 */
	public static List<String> getFiles(String path) throws IOException {
		if(null == path){
			throw new NullPointerException("path is null");
		}

		InputStream in = getResourceAsStream(path);
		if(null == in){
			throw new FileNotFoundException(path);
		}
		try{
			return IOUtils.readLines(in, StandardCharsets.UTF_8);
		}finally{
			in.close();
		}
		/*List<String> filenames = new LinkedList<String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		try {
			String resource;	
			while ((resource = br.readLine()) != null) {
				filenames.add(resource);
			}
			return filenames;

		}catch(IOException e){
			e.printStackTrace();
			throw e;
		}finally{				
			br.close();
		}*/	
	}
	/**
	 * 参见 {@link #getFiles(String)},<br>
	 * {@link IOException}封装为{@link RuntimeException},
	 * @param path
	 * @return
	 */
	public static List<String> getFilesUnchedked(String path){
		try {
			return getFiles(path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static interface FileFilter{
		boolean accept(String filename);
	}
	public static List<String> getFilesUnchecked(String path,FileFilter filter){
		List<String> list = getFilesUnchedked(path);
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
	public static final boolean resourceExist(String resource){
		return getResource(resource)!=null;
	}
	private static URL getResource(String resource) {
		String path = normalizePath(resource);
		final URL in = Thread.currentThread().getContextClassLoader().getResource(path);
		return in == null ? ClassResourceUtils.class.getResource(path) : in;
	}
	private static InputStream getResourceAsStream(String resource) {
		String path = normalizePath(resource);
		return getResourceAsStream(ClassResourceUtils.class,path);
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
    public static final String normalizePath(String path)
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
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) +
            normalized.substring(index + 1);
        }

        // Resolve occurrences of "%20" in the normalized path
        while (true)
        {
            int index = normalized.indexOf("%20");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) + " " +
            normalized.substring(index + 3);
        }

        // Resolve occurrences of "/./" in the normalized path
        while (true)
        {
            int index = normalized.indexOf("/./");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) +
            normalized.substring(index + 2);
        }

        // Resolve occurrences of "/../" in the normalized path
        while (true)
        {
            int index = normalized.indexOf("/../");
            if (index < 0)
                break;
            if (index == 0)
                return (null);  // Trying to go outside our context
            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized = normalized.substring(0, index2) +
            normalized.substring(index + 3);
        }

        // Return the normalized path that we have completed
        return (normalized);
    }
    /**
     * Finds a resource with the given name.  Checks the Thread Context
     * classloader, then uses the System classloader.  Should replace all
     * calls to <code>Class.getResourceAsString</code> when the resource
     * might come from a different classloader.  (e.g. a webapp).
     * @param claz Class to use when getting the System classloader (used if no Thread
     * Context classloader available or fails to get resource).
     * @param name name of the resource
     * @return InputStream for the resource.
     */
    public static InputStream getResourceAsStream(Class<?> claz, String name)
    {
        InputStream result = null;

        /**
         * remove leading slash so path will work with classes in a JAR file
         */
        while (name.startsWith("/"))
        {
            name = name.substring(1);
        }

        ClassLoader classLoader = Thread.currentThread()
                                    .getContextClassLoader();

        if (classLoader == null)
        {
            classLoader = claz.getClassLoader();
            result = classLoader.getResourceAsStream( name );
        }
        else
        {
            result= classLoader.getResourceAsStream( name );

            /**
            * for compatibility with texen / ant tasks, fall back to
            * old method when resource is not found.
            */

            if (result == null)
            {
                classLoader = claz.getClassLoader();
                if (classLoader != null)
                    result = classLoader.getResourceAsStream( name );
            }
        }

        return result;

    }
}
