package net.gdface.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * 配置文件读取工具类
 * @author guyadong
 *
 */
public class ConfigUtils {
	private static final String JAR_SUFFIX = ".jar";

	/**
	 * 顺序加载不同位置的properties文件,加载顺序为:<br>
	 * 1.调用{@link ClassLoader#getResource(String)}方法在{@code clazz}所在位置查找,如果失败则抛出异常<br>
	 * 2.如果class在jar包中,则尝试读取在jar所在位置../confFolder/propFile,tomcat下即为WEB-INF/confFolder/propFile<br>
	 * 3.如果环境变量envVar定义，则从envVar指定的目录下读取propFile<br>
	 * 4.user.dir下查找confFolder/propFile加载配置<br>
	 * 后面的配置变量会覆盖前面的定义<br>
	 * @param propFile 要加载的properties文件名,为{@code null}或空时抛出异常 {@link IllegalArgumentException}
	 * @param confFolder popFile所在文件夹,{@code null}时使用默认值'conf'
	 * @param envVar 环境变量名 用于定义propFile位置,可为{@code null}
	 * @param clazz 用于获取 {@link ClassLoader}的类,为null时使用本类的class
	 * @param showProp 加载后是否显示所有值
	 * @return 返回加载后的{@link Properties}对象
	 */
	public static Properties loadAllProperties(String propFile, String confFolder, String envVar, Class<?> clazz, boolean showProp) {
		if(null==propFile||propFile.isEmpty()){
			throw new IllegalArgumentException("the argument 'propFile' must not be null or empty");
		}
		if (null == confFolder){
			confFolder = "conf";
		}
		if (null == clazz){
			clazz = ConfigUtils.class;
		}
		final String fileSeparator = System.getProperty("file.separator");
		String propPath = confFolder.concat(System.getProperty("file.separator")).concat(propFile);
		Properties props = new Properties();
		Set<File> loadedFiles = new HashSet<File>();
		try {
			// 在jar包中查找默认配置文件
			URL url = clazz.getClassLoader().getResource(propPath.replace(fileSeparator, "/"));
			if(null==url){
				throw new ExceptionInInitializerError(String.format("not found default properties %s", propPath));
			}
			loadProperties(url, props);
		} catch (Exception e) {
			// 默认配置必须加载成功否则抛出异常
			throw new ExceptionInInitializerError(String.format("fail to load default properties(加载默认配置文件失败) %s cause by %s", propPath,
					e.getMessage()));
		}
		try {
			// 加载 jar包所在位置 ../conf/cassdk.properties
			URL classLocation = clazz.getProtectionDomain().getCodeSource().getLocation();
			if (classLocation.toString().endsWith(JAR_SUFFIX)) {
				// jar包所在目录的父目录,tomcat下即为WEB-INF
				File jarParent = new File(classLocation.getPath()).getParentFile().getParentFile();
				if (null != jarParent) {
					File confFile = new File(jarParent, propPath);
					if (confFile.isFile()) {
						loadProperties(confFile.toURI().toURL(), props);
						loadedFiles.add(confFile);
					}
				}
			}
		} catch (Exception e) {
		}
		try {
			// 通过环境变量查找properties文件
			if (envVar != null && !envVar.isEmpty()) {
				String cf = System.getProperty(envVar);
				if (null != cf&&!cf.isEmpty()) {
					File envFile = new File(cf, propFile);
					if (!loadedFiles.contains(envFile)) {
						loadProperties(envFile.toURI().toURL(), props);
						loadedFiles.add(envFile);
					}
				} else{
					log("not defined environment variable '%s'", envVar);
				}
			}
		} catch (Exception e) {
		}
		try {
			// 在当前路径下查找配置文件
			File propInUserDir = new File(System.getProperty("user.dir"), propPath);
			if (propInUserDir.isFile() && !loadedFiles.contains(propInUserDir)) {
				loadProperties(propInUserDir.toURI().toURL(), props);
				loadedFiles.add(propInUserDir);
			}
		} catch (Exception e) {
		}

		// 输出所有参数值
		if(showProp){
			props.list(System.out);
		}
		return props;
	}
	
	/**
	 * 基于user.home，加载相对路径propPath指定的properties文件
	 * @param propPath
	 * @return
	 */
	public static Properties loadPropertiesInUserHome(String propPath){
		Properties props = new Properties();
		try {
			// 在user.home路径下查找配置文件
			File propInUserHome = new File(System.getProperty("user.home"), propPath);
			if (propInUserHome.isFile() ) {
				loadProperties(propInUserHome.toURI().toURL(), props);
			}
		} catch (Exception e) {
		}
		return props;
	}
	/**
	 * 基于user.home，保存指定的{@link Properties}
	 * @param properties
	 * @param propertiesFile properties文件名
	 * @throws IOException
	 * @see {@link Properties#store(java.io.Writer, String)}
	 * @see {@link System#getProperties()}
	 */
	public static void storePropertiesInUserHome(Properties properties,String propertiesFile) throws IOException{
		if(null==properties){
			throw new NullPointerException();
		}
		if(null==propertiesFile||propertiesFile.isEmpty()){
			throw new IllegalArgumentException("propertiesFile must not be empty or null");
		}
		File propInUserHome = new File(System.getProperty("user.home"), propertiesFile);
		File parent=propInUserHome.getParentFile();
		if(!parent.exists()){
			parent.mkdirs();
		}
		properties.store(new FileWriter(propInUserHome), null);

	}
	/**
	 * configure with the parameters given in the given url
	 * 
	 * @param url
	 *            the resource filename to be used
	 * @param props
	 *            dest properties to add
	 * @throws IOException
	 */
	private static void loadProperties(URL url, Properties props) throws IOException {
		if (null != url) {
			InputStream is = null;
			try {
				props.load(new InputStreamReader(is = url.openStream(),"UTF-8"));
				log("Load properties from %s", url.toString());
			} finally {
				if (is != null){
					is.close();
				}
			}			
		}
	}
	private static void log(String format, Object ... args){
		System.out.printf("[%s:%d]%s\n", 
				ConfigUtils.class.getSimpleName(),
				Thread.currentThread() .getStackTrace()[2].getLineNumber(),
				String.format(format, args));
	}
}
