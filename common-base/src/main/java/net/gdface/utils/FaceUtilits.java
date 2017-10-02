/**   
* @Title: FaceUtilits.java 
* @Package net.gdface.utils 
* @Description: guyadong 
* @author guyadong   
* @date 2014-10-21 上午10:51:32 
* @version V1.0   
*/
package net.gdface.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * @author guyadong
 *
 */
public class FaceUtilits {
	private static final Pattern MD5_PATTERN = Pattern.compile("[0-9a-f]{32}",
			Pattern.CASE_INSENSITIVE);
	/**
	 * 生成MD5校验码
	 * 
	 * @param source
	 * @return
	 */
	static public byte[] getMD5(byte[] source) {
		if (Judge.isNull(source))
			return null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			return md.digest(source);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将16位byte[] 转换为32位的HEX格式的字符串String
	 * 
	 * @param buffer
	 * @return
	 */
	static public String toHex(byte buffer[]) {
		if (Judge.isNull(buffer))
			return null;
		StringBuffer sb = new StringBuffer(buffer.length * 2);
		for (int i = 0; i < buffer.length; i++) {
			sb.append(Character.forDigit((buffer[i] & 240) >> 4, 16));
			sb.append(Character.forDigit(buffer[i] & 15, 16));
		}
		return sb.toString();
	}

	
    public static byte[] hex2Bytes(String src){  
        byte[] res = new byte[src.length()/2];  
        char[] chs = src.toCharArray();  
        int[] b = new int[2];  
  
        for(int i=0,c=0; i<chs.length; i+=2,c++){              
            for(int j=0; j<2; j++){  
                if(chs[i+j]>='0' && chs[i+j]<='9'){  
                    b[j] = (chs[i+j]-'0');  
                }else if(chs[i+j]>='A' && chs[i+j]<='F'){  
                    b[j] = (chs[i+j]-'A'+10);  
                }else if(chs[i+j]>='a' && chs[i+j]<='f'){  
                    b[j] = (chs[i+j]-'a'+10);  
                }  
            }   
              
            b[0] = (b[0]&0x0f)<<4;  
            b[1] = (b[1]&0x0f);  
            res[c] = (byte) (b[0] | b[1]);  
        }  
          
        return res;  
    }  
	/**
	 * 生成MD5校验码字符串
	 * 
	 * @param source
	 * @return
	 * @see #getMD5(byte[])
	 * @see #toHex(byte[])
	 */
	static public String getMD5String(byte[] source) {
		return toHex(getMD5(source));
	}
	/**
	 * 生成MD5校验码字符串
	 * 
	 * @param source
	 * @return
	 * @see #getMD5(byte[])
	 * @see #toHex(byte[])
	 */
	static public String getMD5String(ByteBuffer source) {
		return null ==source || (!source.hasArray())? null:toHex(getMD5(source.array()));
	}
	/**
	 * 判断是否为有效的MD5字符串
	 * @return
	 */
	public static final boolean validMd5(String md5){
		return null!=md5&&MD5_PATTERN.matcher(md5).matches();
	}
	/**
	 * 从{@link InputStream}读取字节数组<br>
	 * 当{@code in}为{@link FileInputStream}时，调用{@link #readBytes(FileInputStream)}(NIO方式)读取<br>
	 *  结束时会关闭{@link InputStream}
	 * @param in
	 * @return
	 * @throws IOException
	 * @throws IllegalArgumentException {@code in}为{@code null}
	 */
	public static byte[] readBytes(InputStream in) throws IOException, IllegalArgumentException {
		Assert.notNull(in, "in");
		if(in instanceof FileInputStream)
			return readBytes((FileInputStream)in);
		try {
			int buffSize = Math.max(in.available(), 1024*8);
			byte[] temp = new byte[buffSize];
			ByteArrayOutputStream out = new ByteArrayOutputStream(buffSize);
			int size = 0;
			while ((size = in.read(temp)) != -1) {
				out.write(temp, 0, size);
			}
			return  out.toByteArray();
		} finally {
			in.close();
		}
	}
	
	/**
	 * NIO方式从{@link FileInputStream}读取字节数组<br>
	 *  结束时会关闭{@link InputStream}
	 * @param fin {@link FileInputStream}
	 * @return 返回读取的字节数 当{@code fin}为null时返回null;
	 * @throws IOException
	 */
	public static byte[] readBytes(FileInputStream fin) throws IOException {
		Assert.notNull(fin, "fin");
		FileChannel fc = fin.getChannel();
		try {
			ByteBuffer bb = ByteBuffer.allocate((int) fc.size());
			fc.read(bb);
			bb.flip();
			return bb.array();
		} finally {
			if (null != fc)
				fc.close();
			fin.close();
		}
	}
	/**
	 * 将对象转换为InputStream<br>
	 * 类型可以是byte[],{@link InputStream},{@link String}(base64编码),{@link File},{@link URL},{@link URI},否则抛出RuntimeException<br>
	 * 
	 * @param src
	 *            获取InputStream的源对象
	 * @return 返回获取的InputStream对象,src为null失败返回null或抛出异常
	 * @throws IOException
	 * @throws IllegalArgumentException 无法从{@code src}获取{@link InputStream}
	 */
	public static <T> InputStream getInputStream(T src) throws IOException, IllegalArgumentException {
		Assert.notNull(src, "src");
		if (src instanceof InputStream)
			return (InputStream) src;
		else if (src instanceof String) {
			return new ByteArrayInputStream(Base64Utils.decode(((String) src)));
		} else if (src instanceof byte[]) {
			return new ByteArrayInputStream((byte[]) src);
		} else if (src instanceof File) {
			return new FileInputStream((File) src);
		} else if (src instanceof URL) {
			return ((URL) src).openStream();
		} else if (src instanceof URI) {
			return ((URI) src).toURL().openStream();
		} else
			throw new IllegalArgumentException(String.format("Can't get inputstream from [%s]", src.getClass()
					.getCanonicalName()));
	}

	/**
	 * 将数据对象{@code src}转换为字节数组(byte[])<br>
	 * {@code src}的数据类型可以是byte[],{@link InputStream},{@link String}(base64编码),{@link File},{@link URL},{@link URI}
	 * 否则抛出{@link IllegalArgumentException}<br>
	 * 对象转换为InputStream或byte[]时,可能会抛出{@link IOException}
	 * 
	 * 当{@code src}为{@link File}或{@link FileInputStream}时，使用NIO方式({@link #readBytes(FileInputStream)})读取
	 * 
	 * @param src
	 *            获取byte[]的源对象
	 * @return 返回字节数组,参数为{@code null}或类型不对则抛出异常
	 * @throws IOException
	 * @throws IllegalArgumentException {@code src}为{@code null}或无法从{@code src}获取{@link InputStream}
	 * @see #readBytes(InputStream)
	 * @see #readBytes(FileInputStream)
	 * @see #getInputStream(Object)
	 * @see Base64Utils#decode(String)
	 */
	static public final <T> byte[] getBytes(T src) throws IOException, IllegalArgumentException {
		Assert.notNull(src, "src");		
		if (src instanceof byte[]) {
			return (byte[]) src;
		} else if (src instanceof String) {
			return Base64Utils.decode(((String) src));
		} else if (src instanceof FileInputStream){
			return readBytes((FileInputStream)src);
		}else if (src instanceof File){
			return readBytes(new FileInputStream((File)src));
		}else {
			return readBytes(getInputStream(src));
		}
	}
	
	/**
	 * 调用 {@link #getBytes(Object)}返回非空字节数据<br>
	 * 如果返回{@code null}或空字节数组，则抛出{@link IOException}<br>
	 * @param src
	 * @return
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @see #getBytes(Object)
	 */
	static public final <T> byte[] getBytesNotEmpty(T src) throws IOException, IllegalArgumentException {
		byte[] imgData = getBytes(src);
		if (Judge.isEmpty(imgData))
			throw new IOException(String.format("return null or zero length from %s", src.getClass()
					.getSimpleName()));
		return imgData;
	}
	/**
	 * 将图片数据保存在folder指定的文件夹下,文件名用图片的md5校验码命名,自动判断文件后缀<br>
	 * 
	 * @param img
	 *            图像数据
	 * @param folder
	 *            文件保存的位置
	 * @return 返回保存的文件名,如果{@code img}中无法获取格式名，则视为无效数据，不保存，返回null;
	 * @throws IOException
	 *             调用{@link FaceUtilitsX#getFormatName(byte[])}获取图像格式名称出错或其他IO异常
	 * @throws IllegalArgumentException
	 *             {@code data}为null或空时
	 * @see #saveBytes(byte[], File, boolean)
	 */
	public static File saveImageAutoName(byte[] img, File folder) throws IOException, IllegalArgumentException {
		Assert.notEmpty(img, "img");
		File file = new File(folder, getMD5String(img) + "."+FaceUtilitsX.getFormatName(img).toLowerCase());
		return saveBytes(img,file,file.exists()&&file.isFile()&&0==file.length());		
	}

	/**
	 * @param img
	 * @param folder
	 *            文件保存位置
	 * @throws IOException
	 *             数据非可识别的图像格式或其他IO异常
	 * @throws IllegalArgumentException
	 *             {@code img}为null
	 * @see #saveImageAutoName(byte[], File)
	 * @return 返回保存的文件,如果从{@code img}中读取的数据为空返回null
	 */
	public static File saveImage(InputStream img, File folder) throws IOException, IllegalArgumentException {
		Assert.notNull(img, "img");
		byte[] imgData = readBytes(img);
		return Judge.isEmpty(imgData)?null:saveImageAutoName(imgData,folder);
	}
	
	/**
	 * 将{@code URL}字符串转换为{@code URI}对象<br>
	 * 在转换过程中会将自动对不符合URI规范的字符进行编码,<br>
	 * 在转换过程中先从字符串生成{@code URL}对象,如果{@code String}不能转换成URL对象，则抛出异常
	 * @param urlStr
	 * @return
	 * @throws MalformedURLException
	 */
	public static URI createURI(String urlStr) throws MalformedURLException{
		try {
			return new URI(urlStr);
		} catch (URISyntaxException e) {
			try {
				URL url=new URL(urlStr);
				return new URI(url.getProtocol(),url.getUserInfo(),url.getHost(),url.getPort(),url.getPath(),url.getQuery(),url.getRef());
			} catch (URISyntaxException e1) {
				throw new RuntimeException(e1);
			}
		}
	}

	/**
	 * NIO方式将{@code data}数据保存在{@code file}指定的文件中<br>
	 * 如果{@code file}所在文件夹不存在，则会自动创建所有的文件夹<br>
	 * @param data
	 * @param file 文件保存的位置
	 * @param overwrite 同名文件存在时是否覆盖
	 * @return 返回保存的文件名
	 * @throws IOException {@code file}存在但不是文件或其他IO异常
	 * @throws IllegalArgumentException {@code data}为null时
	 */
	public static File saveBytes(byte[] data, File file, boolean overwrite) throws IOException,
			IllegalArgumentException {
		Assert.notNull(data, "data");
		FileOutputStream out = null;
		FileChannel fc = null;
		try {
			File folder = file.getParentFile();
			if (!folder.exists())
				folder.mkdirs();
			long free = folder.getFreeSpace()>>20;//可用磁盘空间(MB)
			if(free<10)
				throw new IOException(String.format("DISK ALMOST FULL(磁盘空间不足) FREE %dMB,%s",free,folder.getAbsolutePath()));
			if (!file.exists() || !file.isFile() || overwrite) {
				out = new FileOutputStream(file);
				fc = out.getChannel();
				ByteBuffer bb = ByteBuffer.wrap(data);
				fc.write(bb);
			}
			return file;
		} finally {
			if (null != fc)
				fc.close();
			if (null != out)
				out.close();
		}
	}
	
	/**
	 * 如果无法获取泛型参数对象，返回null
	 * @param clazz
	 * @return
	 * @see #getParameterizedType(Class)
	 */
	public static  Class<?>[] getParameterizedTypeNoThrow(Class<?>clazz) {
		try{
		Class<?>[] types=getParameterizedType(clazz);
		return types;
		}catch(Exception e){
			return null;
		}
	}
	/**
	 * 返回{@code clazz}泛型超类的参数对象<br>
	 * 如果超类不是泛型对象，则抛出{@link IllegalArgumentException}<br>
	 * @param clazz
	 * @return
	 * @throws MalformedParameterizedTypeException 超类不是泛型类
	 * @throws IllegalArgumentException 无法获取实际泛型参数对象类型
	 */
	public static  Class<?>[] getParameterizedType(Class<?>clazz) throws MalformedParameterizedTypeException, IllegalArgumentException{
		Type partype = clazz.getGenericSuperclass();		
		if(!(partype instanceof ParameterizedType))//超类不是泛型
			throw new IllegalArgumentException(String.format("superclass of %s  not ParameterizedType(超类不是泛型类)",clazz.getName()));
		Type[] types = ((ParameterizedType) partype).getActualTypeArguments();
		if(!(types[0] instanceof Class<?>)){
			System.err.print("cant'not get class for ParameterizedType (无法获取实际泛型参数对象类型(Class))");
			throw new MalformedParameterizedTypeException();
		} 
		Class<?>[] paramClass=new Class<?>[types.length];		
		for(int i=0;i<paramClass.length;i++)
			paramClass[i]=(Class<?>) types[i];
		return paramClass;		
	}

	public final static Throwable getCause(Throwable e) {
		return e==null?null:(e.getCause()==null?e:e.getCause());
	}

	/**
	 * 以递归方式返回被{@code shellClass}多层封装的异常<br>
	 * @param e
	 * @param shellClass 封装异常的类
	 * @return
	 */
	public static final Throwable stripThrowableShell(Throwable e, Class<? extends Throwable> shellClass){
		if(!Judge.hasNull(e,e.getCause())&&e.getClass()==shellClass){
			return stripThrowableShell(e.getCause(), shellClass);
		}
		return e;
	}

	/**
	 * 对{@link Map}中元素以key排序后，每行以{key}={value}形式输出到{@link Writer}<br>
	 * map为空或null时则不向writer写入任何内容
	 * @param map
	 * @param writer 为null抛出{@link IllegalArgumentException}
	 * @param lineSeparator 换行符,为null则使用系统默认的换行符(windows \n linux \r\n)
	 * @throws IOException
	 */
	public static  void storeSortedMap(Map<String,String> map,Writer writer, String lineSeparator)  throws IOException {
		Assert.notNull(writer, "writer");
		TreeMap<String, String> sortedMap = new TreeMap<String,String>();
		if(null!=map)
			sortedMap.putAll(map);
		BufferedWriter bw=(writer instanceof BufferedWriter)?(BufferedWriter)writer
				: new BufferedWriter(writer);
		for (Entry<String,String> e:sortedMap.entrySet()) {
			bw.write(e.getKey() + "=" + e.getValue());
			if(null==lineSeparator)
				bw.newLine();
			else
				bw.write("\n");
		}
		bw.flush();
	}

	/**
	 * 对 {@link Collection}中元素排序后(去除重复)，元素分行输出到{@link Writer}<br>
	 * collection为空或null时则不向writer写入任何内容
	 * @param collection
	 * @param writer 为null抛出{@link IllegalArgumentException}
	 * @param lineSeparator 换行符,为null则使用系统默认的换行符(windows \n linux \r\n)
	 * @throws IOException
	 */
	public static  void storeSortedSet(Collection<String> collection,Writer writer, String lineSeparator)  throws IOException {
		Assert.notNull(writer, "writer");
		TreeSet<String> sortedSet = new TreeSet<String>();
		if(null!=collection)
			sortedSet.addAll(collection);
		BufferedWriter bw=(writer instanceof BufferedWriter)?(BufferedWriter)writer
				: new BufferedWriter(writer);
		for (String e:sortedSet) {			
			bw.write(e);
			if(null==lineSeparator)
				bw.newLine();
			else
				bw.write("\n");
		}
		bw.flush();
	}
	
	/**
	 * 比较两个Map是否相等
	 * @param m1
	 * @param m2
	 * @return
	 */
	public static <K,V>boolean equals(Map<K,V> m1,Map<K,V> m2){
		if(m1==m2)return true;
		if(null ==m1 || null ==m2)return false;
		if(m1.size() != m2.size())return false;
		for(Entry<K, V> entry:m1.entrySet()){
			K key = entry.getKey();
			if(!m2.containsKey(key))return false;
			V v1 = entry.getValue();
			V v2 = m2.get(key);
			if(v1 ==v2 ) continue;
			if(null ==v1 || null ==v2)return false;
			if(!v1.equals(v2))
				return false;
		}
		return true;
	}
}
