package net.gdface.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;

public class FaceUtilitsX {

	/**
	 * 通过 {@link ImageIO#getImageReaders(Object)}获取图像类型，返回文件格式名,如"jpeg","gif"<br>
	 * 用于保存图像文件时的后缀名
	 * @param img 图像数据
	 * @return
	 * @throws IllegalArgumentException {@code img}为null或空字符串""
	 * @throws IOException 
	 * @see ImageIO#getImageReaders(Object)
	 * @see ImageReader#getFormatName()
	 */
	public static String getFormatName(byte[] img) throws IllegalArgumentException, IOException{
		Assert.notEmpty(img, "img");
		
		Iterator<ImageReader> it = ImageIO.getImageReaders(new MemoryCacheImageInputStream(
				new ByteArrayInputStream(img)));
		if (!it.hasNext())
			throw new IOException("UNRECOGNIZED IMG FORMAT");
		ImageReader reader = it.next();
		try{
			return reader.getFormatName();
		}finally{
			reader.dispose();
		}
	
	}

	/**
	 * 当类被加载的时候,根据环境变量‘is_building’判断当前是运行状态还是代码编译构建状态<br>
	 * 如果没有定义环境变量，则默认为false<br>
	 * 
	 * @return
	 */
	public static final boolean isBuilding() {
		if (null == FaceUtilitsX.IS_BUILDING) {
			FaceUtilitsX.IS_BUILDING = Boolean.valueOf(FaceUtilitsX.getRuntimeProperty("is_building"));
		}
		return FaceUtilitsX.IS_BUILDING;
	}

	private static Boolean IS_BUILDING=null;

	/**
	 * 通过环境变量CATALINA_HOME,获取axis2/WEB-INF/conf的位置<br>
	 * 如果没有定义CATALINA_HOME或没有找到/webapps/axis2/WEB-INF/conf,则抛出{@link FileNotFoundException}
	 * @return 返回conf文件夹位置
	 * @throws FileNotFoundException
	 */
	public static File getAxis2Conf() throws FileNotFoundException{
		String tomcat=System.getenv("CATALINA_HOME");
		if(Judge.isEmpty(tomcat))
			throw new FileNotFoundException("NOT DEFINED environment variable CATALINA_HOME,can't locate configuration file");
		File conf = new File(tomcat+"/webapps/axis2/WEB-INF/conf");
		if(!conf.exists()||!conf.isDirectory())
			throw new FileNotFoundException(String.format("NOT FOUND %s for configuration file",conf));
		return conf;
	
	}

	/**
	 * 当类被加载的时候,根据环境变量‘notLoadCodeTable’判断是否要加载code表<br>
	 * 如果没有定义环境变量，则默认为false<br>
	 * @return
	 */
	public static final boolean notLoadCodeTable() {
		return Boolean.valueOf(FaceUtilitsX.getRuntimeProperty("notLoadCodeTable"));
	}

	public static final String getRuntimeProperty(String key){
		String value = System.getProperty(key);
		if (null == value)
			value = System.getenv(key);
		System.out.printf("environment variable(java property)  %s=%b\n", key, Boolean.valueOf(value));
		return value;
	}

}
