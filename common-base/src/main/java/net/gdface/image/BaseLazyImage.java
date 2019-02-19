package net.gdface.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.ServiceLoader;

import net.gdface.utils.Assert;
import net.gdface.utils.BaseVolatile;
import net.gdface.utils.FaceUtilits;
import net.gdface.utils.ILazyInitVariable;
import net.gdface.utils.Judge;
/**
 * 图像数据处理对象<br>
 * @author guyadong
 *
 */
public abstract class BaseLazyImage implements ImageMatrix {
	private static final ILazyInitVariable<LazyImageFactory> lazyImageFactory = new BaseVolatile<LazyImageFactory>(){

		@Override
		protected LazyImageFactory doGet() {
			/* SPI(Service Provider Interface)机制加载 {@link LazyImageFactory}实例,没有找到则抛出异常 */
			ServiceLoader<LazyImageFactory> providers = ServiceLoader.load(LazyImageFactory.class);
			Iterator<LazyImageFactory> itor = providers.iterator();
			if(!itor.hasNext()){
				throw new NotFoundLazyImageFactoryException();
			}
			return itor.next();
		}};
	/**
	 * 图像原始数据(未解码)
	 */
	protected byte[] imgBytes = null;
	/**
	 * 图像数据本地存储文件
	 */
	protected File localFile = null;
	/**
	 * 图像数据的MD5校验码
	 */
	protected String md5 = null;
	/**
	 * 图像文件后缀
	 */
	protected String suffix = null;
	/**
	 * 是否在读取图像结束时自动执行 {@link #close()}释放资源<br>
	 * 默认为{@code true}
	 */
	protected boolean autoClose = true;
	protected int width;
	protected int height;
	/**
	 * RGB格式的图像矩阵数据(全图)
	 */
	protected byte[] matrixRGB = null;
	/**
	 * BGR格式的图像矩阵数据(全图)
	 */
	protected byte[] matrixBGR = null;
	/**
	 * 灰度图像矩阵数据(全图)
	 */
	protected byte[] matrixGray = null;
	protected FileInputStream fileInputStream;

	protected BaseLazyImage() {
	}
	/**
	 * @param imgBytes 图像数据,{@code imgBytes}为null或为空,则抛出 {@link IllegalArgumentException}
	 */
	public BaseLazyImage(byte[] imgBytes) {
		Assert.notEmpty(imgBytes, "imgBytes");
		this.imgBytes=imgBytes;
	}
	/**
	 * 用本地图像文件创建对象
	 * @param src
	 * @param md5 文件的MD5校验码，为{@code null}时自动计算
	 * @throws FileNotFoundException
	 */
	public BaseLazyImage(File src, String md5) throws FileNotFoundException {
		Assert.notNull(src, "src");
		this.localFile = src;
		if(!localFile.exists()||!localFile.isFile()||0==localFile.length()){
			throw new FileNotFoundException(String.format("NOT EXIST OR NOT FILE OR ZERO bytes%s",localFile.getAbsolutePath()));
		}
		String fileName = localFile.getName();
		this.suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
		this.md5 = md5;
	}
	/**
	 * 返回图像数据字节数组<br>
	 * 如果图像数据在本地文件中，则方法第一次被调用时将数据从文件中读取到内存
	 * @return the imgBytes,如果为无效图像，则返回null
	 * @throws IllegalArgumentException 参数错误
	 */
	public byte[] getImgBytes() {
	
		if(null==imgBytes){
			if(null!=localFile){
				try {
					imgBytes = FaceUtilits.getBytesNotEmpty(localFile);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}else
				throw new IllegalArgumentException("while isValidImage be true localFile & imgBytes can't be NULL all");
		}
		return imgBytes;
	}

	/**
	 * @return the md5
	 */
	public String getMd5() {
		if (null == md5)
			md5 = FaceUtilits.getMD5String(getImgBytes());
		return md5;
	}

	/**
	 * @return the suffix
	 */
	public String getSuffix() {
		return suffix;
	}

	/**
	 * @return localFile
	 */
	public File getLocalFile() {
		return localFile;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	/**
	 * 在执行 {@link #read()}或 {@link #open()}之前调用,才有效
	 * @param autoClose 要设置的 autoClose
	 * @return 
	 * @see #autoClose
	 */
	public BaseLazyImage setAutoClose(boolean autoClose) {
		this.autoClose = autoClose;
		return this;
	}
	/**
	 * 释放资源
	 * @throws IOException
	 */
	public void close() throws IOException{
		if(null!=fileInputStream){
			fileInputStream.close();
			fileInputStream=null;
		}
	}
	@Override
	public void finalize() throws Throwable {
		close();
		this.imgBytes=null;
		this.localFile=null;
	}
	/**
	 * 读取图像基本信息，检查图像数据有效性
	 * @return 
	 * @throws UnsupportedFormatException 
	 * @throws NotImageException 
	 */
	public abstract <T extends BaseLazyImage>T open() throws UnsupportedFormatException, NotImageException;
	/**
	 * 以 {@link #md5}为名字将文件保存在{@code folder}文件夹下<br>
	 * 如果同名文件存在，且长度不为0时不覆盖
	 * @param folder
	 * @return
	 * @throws IOException
	 * @see FaceUtilits#saveBytes(byte[], File, boolean)
	 */
	public File save(File folder) throws IOException {
		File file = new File(folder,getMd5()+(Judge.isEmpty(this.suffix)?"":"."+this.suffix));
		localFile= FaceUtilits.saveBytes(getImgBytes(), file, file.exists()&&file.isFile()&&0==file.length());		
		return localFile;
	}
	/**
	 * 返回当前 {@link LazyImageFactory}实例
	 * @return
	 * @throws NotFoundLazyImageFactoryException
	 */
	public static final LazyImageFactory getLazyImageFactory() throws NotFoundLazyImageFactoryException{
		return lazyImageFactory.get();
	}

}
