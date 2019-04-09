package net.gdface.image;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import net.gdface.utils.Assert;
import net.gdface.utils.FaceUtilits;

/**
 * 图像数据处理对象<br>
 * {@link #open()}可以在不将图像全部解码加载到内存而获取图像的基本信息<br>
 * {@link #read(ImageReadParam)}使用内存做cache读取(不使用临时文件做cache)<br>
 * {@link #read(Rectangle, ImageTypeSpecifier)}可以对图像指定区域解码
 * @author guyadong
 *
 */
public class LazyImage extends BaseLazyImage implements ImageMatrix{
	private Rectangle rectangle=null;
	private ImageReader imageReader;
	private MemoryCacheImageInputStream imageInputstream;
	private BufferedImage bufferedImage=null;
	/**
	 * 通过{@link ImageReader}来读取图像基本信息，检查图像数据有效性
	 * @return 
	 * @throws UnsupportedFormatException 
	 * @throws NotImageException 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public LazyImage open() throws UnsupportedFormatException, NotImageException {
		try {
			if(bufferedImage == null){
				Iterator<ImageReader> it = ImageIO.getImageReaders(getImageInputstream());
				if (it.hasNext())
					try {
						imageReader = it.next();
						imageReader.setInput(getImageInputstream(), true, true);
						this.suffix = imageReader.getFormatName().trim().toLowerCase();
						this.width = imageReader.getWidth(0);
						this.height = imageReader.getHeight(0);
					} catch (Exception e) {						
						throw new UnsupportedFormatException(e);
					} 
				else {
					// 没有找到对应的图像解码则招聘异常
					throw new NotImageException();
				}
			}else{
				this.width = bufferedImage.getWidth();
				this.height = bufferedImage.getHeight();
			}
			return this;
		} finally {
			if (autoClose)
				try {
					close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
		}
	}
	
	
	/**
	 * 对图像数据解码生成 {@link BufferedImage}对象
	 * 
	 * @param param
	 *            图像读取参数对象,为null使用默认参数<br>
	 *            参见 {@link ImageReader#getDefaultReadParam()}
	 * @return
	 * @throws UnsupportedFormatException
	 * @see ImageReadParam
	 */
	protected BufferedImage read(ImageReadParam param) throws UnsupportedFormatException {
		try {
			if(null==this.bufferedImage||null!=param){
				ImageReader imageReader=getImageReader();
				if(null==imageReader.getInput())
					imageReader.setInput(getImageInputstream(), true,true);				
				BufferedImage bi = imageReader.read(0, null==param?imageReader.getDefaultReadParam():param);
				if (null==param)
					this.bufferedImage = bi;
				else
					return bi;
			}
			return this.bufferedImage;
		} catch (Exception e) {
			throw new UnsupportedFormatException(e);
		} finally {
			if(autoClose)
				try {
					close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
		}
	}
	public BufferedImage read() throws UnsupportedFormatException{
		return read(null);
	}
	/**
	 * 对图像数据指定的区域解码
	 * 
	 * @param rect
	 *            解码区域对象, 默认({@code null})全图解码<br>
	 *            参见 {@link ImageReadParam#setSourceRegion(Rectangle)}
	 * @param destinationType
	 *            目标图像的所需图像类型,默认为null, <br>
	 *            例如用此参数可以在解码时指定输出的图像类型为RGB,<br>
	 *            如下代码 生成 destinationType参数对象：<br>
	 *            {@code 
	 *            		ImageTypeSpecifier destinationType=ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_3BYTE_BGR);
	 *            }<br>
	 *            用上面的ImageTypeSpecifier对象来调用此方法返回的BufferedImage对象中的raster成员(通过BufferedImage.getData()获取)
	 *            的getDataElements()方法返回的就是包含RGB数据byte[]类型的数组<br>
	 *            而直接用BufferedImage.getRGB()方式只能获取ARGB类型的int[]数组 参见
	 *            {@link ImageReadParam#setDestinationType(ImageTypeSpecifier) }
	 * @return
	 * @throws UnsupportedFormatException
	 * @see #read(ImageReadParam)
	 */
	public BufferedImage read(Rectangle rect, ImageTypeSpecifier destinationType) throws UnsupportedFormatException {
		ImageReadParam param = getImageReader().getDefaultReadParam();
		if (rect != null && !rect.equals(getRectangle()))
			param.setSourceRegion(rect);
		param.setDestinationType(destinationType);		
		if(null!=destinationType)
			param.setDestination(destinationType.createBufferedImage(width, height));
		return read(param);
	}
	@Override
	public byte[] getMatrixRGBA() throws UnsupportedFormatException{
		if (matrixRGBA==null){
			matrixRGBA=ImageUtil.getMatrixRGBA(read());
		}
		return matrixRGBA;
	}

	@Override
	public byte[] getMatrixRGB() throws UnsupportedFormatException{
		if (matrixRGB==null){
			matrixRGB=ImageUtil.getMatrixRGB(read());
		}
		return matrixRGB;
	}

	@Override
	public byte[] getMatrixBGR() throws UnsupportedFormatException{
		if (matrixBGR==null){	
			matrixBGR=ImageUtil.getMatrixBGR(read());
		}
		return matrixBGR;
	}
	/**
	 *  对图像数据指定的区域解码返回灰度图像数据
	 * @return 灰度图像矩阵数据
	 * @throws UnsupportedFormatException
	 */
	@Override
	public byte[] getMatrixGray() throws UnsupportedFormatException{		
		if(null==matrixGray){
			matrixGray = ImageUtil.getMatrixGRAY(read());
		}
		return matrixGray;

	}
	@Override
	public byte[] wirtePNGBytes(){
		try {
			if("PNG".equalsIgnoreCase(getSuffix())){
				if(getImgBytes() != null){
					return getImgBytes();
				}
			}
			return ImageUtil.wirtePNGBytes(read());
		} catch (UnsupportedFormatException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public byte[] wirteJPEGBytes(){
		try {
			if("JPEG".equalsIgnoreCase(getSuffix())){
				if(getImgBytes() != null){
					return getImgBytes();
				}
			}
			return ImageUtil.wirteJPEGBytes(read(),0.9f);
		} catch (UnsupportedFormatException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 创建并打开对象
	 * @param imgBytes
	 * @return
	 * @throws NotImageException
	 * @throws UnsupportedFormatException
	 * @see #LazyImage(byte[])
	 * @see #open()
	 */
	public static LazyImage create(final byte[] imgBytes) throws NotImageException, UnsupportedFormatException {
		return new LazyImage(imgBytes).open();
	}
	/**
	 * 创建对象
	 * @param bufferedImage
	 * @see #LazyImage(BufferedImage)
	 * @see #open()
	 * @return
	 */
	public static LazyImage create(final BufferedImage bufferedImage) {
		try {
			return new LazyImage(bufferedImage).open();
		} catch (ImageErrorException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 用本地图像文件创建对象
	 * @param file
	 * @param md5 {@code file}的MD5较验码，可以为null
	 * @return
	 * @throws NotImageException
	 * @throws UnsupportedFormatException
	 */
	public static LazyImage create(final File file, String md5) throws NotImageException, UnsupportedFormatException {
		try {
			return new LazyImage(file, md5).open();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 多源创建对象
	 * @param src
	 * @return
	 * @throws NotImageException
	 * @throws UnsupportedFormatException
	 * @see #LazyImage(Object)
	 * @see FaceUtilits#getBytesNotEmpty(Object)
	 */
	public static <T> LazyImage create(final T src) throws NotImageException, UnsupportedFormatException {
		try {
			return new LazyImage(src).open();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @param bufferedImage 已经解码的图像数据,为null或为空,则抛出 {@link IllegalArgumentException}
	 */
	public LazyImage(BufferedImage bufferedImage)
	{
		Assert.notNull(bufferedImage, "bufferedImage");
		this.bufferedImage = bufferedImage;
	}
	/**
	 * @param imgBytes 图像数据,{@code imgBytes}为null或为空,则抛出 {@link IllegalArgumentException}
	 */
	public LazyImage(byte[] imgBytes) {
		super(imgBytes);
	}

	/**
	 * 用本地图像文件创建对象
	 * @param src
	 * @param md5
	 * @throws FileNotFoundException
	 */
	public LazyImage(File src, String md5) throws FileNotFoundException {
		super(src, md5);
	}
	
	/**
	 * 多源创建对象
	 * @param src
	 * @throws IOException
	 * @see FaceUtilits#getBytesNotEmpty(Object)
	 */
	public <T>LazyImage(T src) throws IOException {
		this(FaceUtilits.getBytesNotEmpty(src));		
	}

	/**
	 * 返回{@link ImageInputStream}对象<br>
	 * 如果 {@link #imageInputstream} 为{@code null},则根据 {@link #imgBytes}或 {@link #localFile}创建
	 * @return imageInputstream
	 */
	private ImageInputStream getImageInputstream() {
		if (null == imageInputstream) {
			if (null == imgBytes) {
				if (null == localFile)
					throw new IllegalArgumentException(
							"while isValidImage be true localFile & imgBytes can't be NULL all");
				try {
					this.imageInputstream = new MemoryCacheImageInputStream(new FileInputStream(localFile));
				} catch (FileNotFoundException e) {
					throw new RuntimeException(e);
				}
			}else{
				this.imageInputstream = new MemoryCacheImageInputStream(new ByteArrayInputStream(imgBytes));
			}
		}
		return imageInputstream;
	}
	
	/**
	 * 返回{@link ImageReader}对象<br>
	 * 如果 {@link #imageReader}为{@code null},则根据 {@link #suffix}创建，失败抛出
	 * @return imageReader {@link ImageReader}对象
	 * @throws IllegalStateException 无法根据{@link #suffix}获取{@link ImageReader}
	 */
	private ImageReader getImageReader() throws IllegalStateException {
		if (null == imageReader) {
			Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName(suffix);
			if (it.hasNext())
				imageReader = it.next();
			else
				throw new IllegalStateException(String.format("invalid suffix %s", suffix));
		}
		return imageReader;
	}

	/**
	 * 释放资源
	 * @throws IOException
	 */
	public void close() throws IOException{
		if(null!=imageReader){
			imageReader.dispose();
			imageReader=null;
		}
		if(null!=imageInputstream){
			imageInputstream.close();
			imageInputstream=null;
		}
		super.close();
	}
	@Override
	public void finalize() throws Throwable {
		this.bufferedImage=null;
		this.rectangle=null;
		super.finalize();
	}


	/**
	 * 获取图像矩形对象
	 * @return rectangle
	 */
	public Rectangle getRectangle() {
		if(null==rectangle)
			rectangle=new Rectangle(0,0,width,height);
		return rectangle;
	}	
}
