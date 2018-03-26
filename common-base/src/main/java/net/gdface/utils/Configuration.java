/**   
 * @Title: Configuratoin.java 
 * @Package net.gdface.utils 
 * @Description: 管理properties文件
 * @author guyadong   
 * @date 2014-10-14 下午3:20:09 
 * @version V1.0   
 */
package net.gdface.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Properties;
import java.util.Map.Entry;

/**读取配置文件(properties)中的参数
 * @author guyadong
 * 
 */
public class Configuration extends Properties{
	private static final long serialVersionUID = -3929851625670931787L;
	/**
	 * property 属性名前缀,在调用{@link #getProperty(String)}或其他getProperty方法时参数会自动加上该前缀,默认为"",
	 * 如prefix为 net.gdface.facedbsdk.FaceDbSDKLocal
	 * 调用getProperty("minThread")时，其实是执行getProperty("net.gdface.facedbsdk.FaceDbSDKLocal.minThread")
	 */
	private ThreadLocal<String> prefixs = new ThreadLocal<String>() {
		@Override
		protected String initialValue() {
			return "";//prefix初始值为""
		}
	};

	/**使用指定的ClassLoader加载properties文件propFile
	 * @param classLoader
	 * @param propFile
	 * @throws IOException
	 * @see #configure(URL)
	 */
	public Configuration(ClassLoader classLoader,String propFile) throws IOException {
		URL url= classLoader.getResource(propFile);
		if(null==url)
			throw new IOException(String.format("Cant found resource [%s] by %s",propFile,classLoader.getClass().getName()));
		configure(url);
	}
	/**
	 * 从{@link File}构造对象
	 * @param propFile
	 * @throws IOException
	 */
	public Configuration(File propFile) throws IOException {
		this(propFile.toURI().toURL());
	}	
	public Configuration() {
		super();
	}
	public Configuration(Properties defaults) {
		super(defaults);
	}
	/**
	 * @param url
	 * @throws IOException
	 */
	public Configuration(URL url) throws IOException {
		configure(url);
	}	
	/**
	 * 根据{@link URL}加载配置文件
	 * @param url
	 * @throws IOException
	 * @see #configure(InputStream)
	 */
	private void configure(URL url) throws IOException {
		if(null==url)
			throw new NullPointerException("parameter [url] is null");
		System.out.printf("Load properties from [%s]\n", url.toString());
		InputStream is = url.openStream();
		try {
			load(is);
		} finally {
			is.close();
		}
	}
	
	/**
	 * 读取指定的key，用静态valueOf方法(如果有)将之转换成clazz指定的类型<br>
	 * 适用于{@link Number}，{@link Boolean}类型,如果key不存在或为空或转换出错则返回null
	 * 
	 * @param key
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T> T _getPropertyBaseType(String key, Class<T> clazz) {
		if (null == key||0==key.length())
			throw new IllegalArgumentException("the argument 'key' must not be null or empty");
		if (null == clazz)
			throw new IllegalArgumentException("the argument 'clazz' must not be null");
		String value = getProperty(key);
		try {
			return null == value|| 0 == value.length() ? null : (T) clazz.getMethod("valueOf", String.class).invoke(null, value.trim());
		} catch (NoSuchMethodException e) {
			new RuntimeException(
					String.format("%s:not found static method 'valueOf' for %s", e.getClass().getSimpleName(),
							clazz.getName()), e);
		} catch (IllegalAccessException e) {
			new RuntimeException(
					String.format("%s:can't invoke method 'valueOf' for %s", e.getClass().getSimpleName(),
							clazz.getName()), e);
		} catch (InvocationTargetException e) {
			try {
				throw e.getCause();
			} catch (NumberFormatException ie) {
				System.out.printf("%s: [%s=%s] can't be convert to %s\n", e.getClass().getSimpleName(), key, value,
						clazz.getName());
			} catch (Throwable ie) {
				new RuntimeException(ie);
			}
		}
		return null;
	}
	
	/**
	 * 设置指定的key为value
	 * 
	 * @param key
	 *            为{@link null}时抛出 {@link IllegalArgumentException}<br>
	 *            适用于{@link Number}，{@link Boolean}类型
	 * @param value
	 *            为{@code null}时删除key并返回{@code null}
	 * @return 用静态valueOf方法返回key原来的值
	 */
	@SuppressWarnings("unchecked")
	private <T> T _setPropertyBaseType(String key, T value) {		
		if (null == key||0==key.length())
			throw new IllegalArgumentException("the argument 'key' must not be null or empty");
		// value为null时删除key,并返回null
		if(null==value){remove(key);return null;}
		try {
			String old = (String) setProperty(key,value.toString());
			return null == old|| 0 == old.length() ? null : (T) value.getClass().getMethod("valueOf", String.class).invoke(null, old.trim());
		} catch (NoSuchMethodException e) {
			new RuntimeException(
					String.format("%s:not found static method 'valueOf' for %s", e.getClass().getSimpleName(),
							value.getClass().getName()), e);
		} catch (IllegalAccessException e) {
			new RuntimeException(
					String.format("%s:can't invoke method 'valueOf' for %s", e.getClass().getSimpleName(),
							value.getClass().getName()), e);
		} catch (InvocationTargetException e) {
			try {
				throw e.getCause();
			} catch (NumberFormatException ie) {
				System.out.printf("%s: [%s=%s] can't be convert to %s\n", e.getClass().getSimpleName(), key, value.toString(),
						value.getClass().getName());
			} catch (Throwable ie) {
				new RuntimeException(ie);
			}
		}
		return null;
	}
	
	@Override
	public String getProperty(String key) {
		return super.getProperty(this.getPrefix()+key);
	}

	@Override
	public String getProperty(String key, String defaultValue) {
		return super.getProperty(key, defaultValue);
	}

	@Override
	public synchronized Object setProperty(String key, String value) {
		return super.setProperty(this.getPrefix()+key, value);
	}
	/**读取指定的key，将之转换成与defaultValue类型相同的对象，如果key不存在或转换出错则返回defaultValue
	 * @param key
	 * @param defaultValue 缺省值
	 * @return
	 */
	public <T> T getPropertyBaseType(String key, T defaultValue) {
		if (null == defaultValue)
			throw new IllegalArgumentException("the argument 'defaultValue' must not be null");
		@SuppressWarnings("unchecked")
		T res = (T) _getPropertyBaseType(key,defaultValue.getClass());
		return  ((null==res)?defaultValue:res);
	}

	public Boolean getPropertyBoolean(String key) {
		return _getPropertyBaseType(key, Boolean.class);
	}

	public Boolean getPropertyBoolean(String key, Boolean defaultValue) {
		return getPropertyBaseType(key, defaultValue);
	}
	
	public Byte getPropertyByte(String key) {
		return _getPropertyBaseType(key, Byte.class);
	}
	public Byte getPropertyByte(String key,Byte defaultValue) {
		return getPropertyBaseType(key, defaultValue);
	}
	public Double getPropertyDouble(String key) {
		return _getPropertyBaseType(key, Double.class);
	}

	public Double getPropertyDouble(String key,Double defaultValue) {
		return getPropertyBaseType(key, defaultValue);
	}

	public Float getPropertyFloat(String key) {
		return _getPropertyBaseType(key, Float.class);
	}

	public Float getPropertyFloat(String key,Float defaultValue) {
		return getPropertyBaseType(key, defaultValue);
	}

	public Integer getPropertyInteger(String key) {
		return _getPropertyBaseType(key, Integer.class);
	}

	public Integer getPropertyInteger(String key,Integer defaultValue) {
		return getPropertyBaseType(key, defaultValue);
	}

	public Long getPropertyLong(String key) {
		return _getPropertyBaseType(key, Long.class);
	}
	public Long getPropertyLong(String key,Long defaultValue) {
		return getPropertyBaseType(key, defaultValue);
	}
	public Short getPropertyShort(String key) {
		return _getPropertyBaseType(key, Short.class);
	}
	public Short getPropertyShort(String key,Short defaultValue) {
		return getPropertyBaseType(key, defaultValue);
	}
	//////////////

	public Boolean setPropertyBoolean(String key, Boolean value) {
		return _setPropertyBaseType(key, value);
	}
	
	public Byte setPropertyByte(String key,Byte value) {
		return _setPropertyBaseType(key, value);
	}

	public Double setPropertyDouble(String key,Double value) {
		return _setPropertyBaseType(key, value);
	}

	public Float setPropertyFloat(String key,Float value) {
		return _setPropertyBaseType(key, value);
	}

	public Integer setPropertyInteger(String key,Integer value) {
		return _setPropertyBaseType(key, value);
	}

	public Long setPropertyLong(String key,Long value) {
		return _setPropertyBaseType(key, value);
	}
	public Short setPropertyShort(String key,Short value) {
		return _setPropertyBaseType(key, value);
	}
	
	public void setPropertyBooleanIfAbsent(String key, Boolean value) {
		if(!containsKey(key)){
			setPropertyBoolean(key, value);
		}
	}
	
	public void setPropertyIfAbsent(String key, String value) {
		if(!containsKey(key)){
			setProperty(key, value);
		}
	}
	
	public void setPropertyByteIfAbsent(String key,Byte value) {
		if(!containsKey(key)){
			setPropertyByte(key, value);
		}
	}

	public void setPropertyDoubleIfAbsent(String key,Double value) {
		if(!containsKey(key)){
			setPropertyDouble(key, value);
		}
	}

	public void setPropertyFloatIfAbsent(String key,Float value) {
		if(!containsKey(key)){
			setPropertyFloat(key, value);
		}
	}

	public void setPropertyIntegerIfAbsent(String key,Integer value) {
		if(!containsKey(key)){
			setPropertyInteger(key, value);
		}
	}

	public void setPropertyLongIfAbsent(String key,Long value) {
		if(!containsKey(key)){
			setPropertyLong(key, value);
		}
	}
	
	public void setPropertyShortIfAbsent(String key,Short value) {
		if(!containsKey(key)){
			setPropertyShort(key, value);
		}
	}	
	/**如果参数为null，则prefix设置为""
	 * @param prefix 要设置的 prefix
	 * @return 
	 */
	public Configuration setPrefix(String prefix) {
		prefixs.set(null==prefix?"":prefix);
		return this;
	}
	/**
	 * 设置 {@link #prefixs}空
	 * @return
	 */
	public Configuration resetPrefix() {
		return setPrefix(null);
	}
	/**
	 * @return prefix
	 */
	public String getPrefix() {
		return prefixs.get();
	}
	/**
	 * 返回如果指定了 {@link java.util.Properties#defaults},则返回所有相对增加或修改的值<br>
	 * 否则返回所有值
	 * @param out 输出对象，可为null
	 * @return
	 */
	public Properties changedProperties(Properties out){
		if(null==out)out=new Properties();
		if(null==defaults)
			out.putAll(this);
		else
			for(Entry<Object, Object> entry:entrySet()){
				String key=(String) entry.getKey();
				String value=(String)entry.getValue();
				if(!defaults.containsKey(key)||!value.equals(defaults.getProperty(key))){
					out.setProperty(key, value);
				}
			}			
		return out;
	}
}
