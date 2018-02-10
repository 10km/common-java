package net.gdface.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
		List<String> filenames = new LinkedList<String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		try {
			String resource;	
			while ((resource = br.readLine()) != null) {
				filenames.add(resource);
			}
			return filenames;

		}finally{				
			br.close();
		}	
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
		final URL in = Thread.currentThread().getContextClassLoader().getResource(resource);
		return in == null ? ClassResourceUtils.class.getResource(resource) : in;
	}
	private static InputStream getResourceAsStream(String resource) {
		final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
		return in == null ? ClassResourceUtils.class.getResourceAsStream(resource) : in;
	}

}
