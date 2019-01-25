package net.gdface.image;

import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import net.gdface.utils.Assert;
import net.gdface.utils.FaceUtilits;
import net.gdface.utils.Judge;

/**
 * 图像数据处理对象<br>
 * {@link #open()}可以在不将图像全部解码加载到内存而获取图像的基本信息<br>
 * {@link #read()}使用内存做cache读取(不使用临时文件做cache)<br>
 * {@link #read(Rectangle, ImageTypeSpecifier)}可以对图像指定区域解码
 * @author guyadong
 *
 */
public class LazyImage implements ImageMatrix{
	/**
	 * 图像原始数据(未解码)
	 */
	private byte[] imgBytes = null;
	/**
	 * RGB格式的图像矩阵数据(全图)
	 */
	private byte[] matrixRGB  = null;
	/**
	 * BGR格式的图像矩阵数据(全图)
	 */
	private byte[] matrixBGR  = null;
	/**
	 * 灰度图像矩阵数据(全图)
	 */
	protected byte[] matrixGray = null;
 	/**
	 * 图像数据本地存储文件
	 */
	private File localFile = null;
	/**
	 * 图像数据的MD5校验码
	 */
	private String md5 = null;	
	/**
	 * 图像文件后缀
	 */
	private String suffix = null;
	private int width;
	private int height;
	private InputStream imageInputstream;
	private FileInputStream fileInputStream;
	/**
	 * 是否在 {@link #open()}和 {@link #read()}执行结束时自动执行 {@link #close()}释放资源<br>
	 * 默认为{@code true}
	 */
	private boolean autoClose=true;
	private Bitmap bitmap=null;
	/**
	 * 通过{@link ImageReader}来读取图像基本信息，检查图像数据有效性
	 * @return 
	 * @throws UnsupportedFormatException 
	 * @throws NotImageException 
	 */
	public LazyImage open() throws UnsupportedFormatException, NotImageException {
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			Bitmap tmp = BitmapFactory.decodeStream(getImageInputstream(),null, options);
			this.width = tmp.getWidth();
			this.height = tmp.getHeight();
			this.suffix = options.outMimeType;
			return this;
		} catch(Exception e){
			throw new NotImageException();
		}finally {
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
	 * @return
	 * @throws UnsupportedFormatException
	 * @see ImageReadParam
	 */
	protected Bitmap read() throws UnsupportedFormatException {
		try {
			if(null==this.bitmap){
				this.bitmap = BitmapFactory.decodeStream(getImageInputstream());
			}
			return this.bitmap;
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
	 * @param rect 解码区域,为null时全图解码
	 * @return 灰度图像矩阵数据
	 * @throws UnsupportedFormatException
	 */
	@Override
	public byte[] getMatrixGray() throws UnsupportedFormatException{		
		if(null==matrixGray){
			Bitmap image = read();
			if(image.getType()==BufferedImage.TYPE_BYTE_GRAY){
				matrixGray= (byte[]) image.getData().getDataElements(0, 0, width, height, null);
			}else{
				// 图像转灰
				BufferedImage grayImage = new BufferedImage(width, height,  
		                BufferedImage.TYPE_BYTE_GRAY);
				new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null).filter(image, grayImage);
			    matrixGray= (byte[]) grayImage.getData().getDataElements(0, 0, width, height, null);		
			}
		}
		return matrixGray;

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
	 * @param bitmap 已经解码的图像数据,为null或为空,则抛出 {@link IllegalArgumentException}
	 */
	public LazyImage(Bitmap bitmap)
	{
		Assert.notNull(bitmap, "bitmap");
		this.bitmap = bitmap;
	}
	/**
	 * @param imgBytes 图像数据,{@code imgBytes}为null或为空,则抛出 {@link IllegalArgumentException}
	 */
	public LazyImage(byte[] imgBytes) {
		Assert.notEmpty(imgBytes, "imgBytes");
		this.imgBytes=imgBytes;
	}

	/**
	 * 用本地图像文件创建对象
	 * @param src
	 * @param md5
	 * @throws FileNotFoundException
	 */
	public LazyImage(File src, String md5) throws FileNotFoundException {
		Assert.notNull(src, "src");
		this.localFile = src;
		if(!localFile.exists()||!localFile.isFile()||0==localFile.length())
			throw new FileNotFoundException(String.format("NOT EXIST OR NOT FILE OR ZERO bytes%s",localFile.getAbsolutePath()));
		String fileName = localFile.getName();
		this.suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
		this.md5 = md5;
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
	 * 返回{@link ImageInputStream}对象<br>
	 * 如果 {@link #imageInputstream} 为{@code null},则根据 {@link #imgBytes}或 {@link #localFile}创建
	 * @return imageInputstream
	 */
	private InputStream getImageInputstream() {
		if (null == imageInputstream) {
			if (null == imgBytes) {
				if (null == localFile)
					throw new IllegalArgumentException(
							"while isValidImage be true localFile & imgBytes can't be NULL all");
				try {
					this.fileInputStream=new FileInputStream(localFile);
					this.imageInputstream = fileInputStream;
				} catch (FileNotFoundException e) {
					throw new RuntimeException(e);
				}
			}else
				this.imageInputstream = new ByteArrayInputStream(imgBytes);
		}
		return imageInputstream;
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
	 * 释放资源
	 * @throws IOException
	 */
	public void close() throws IOException{
		if(null!=imageInputstream){
			imageInputstream.close();
			imageInputstream=null;
		}
		if(null!=fileInputStream){
			fileInputStream.close();
			fileInputStream=null;
		}
	}
	/**
	 * @return localFile
	 */
	public File getLocalFile() {
		return localFile;
	}
	
	@Override
	public void finalize() throws Throwable {
		close();
		this.imgBytes=null;
		this.bitmap=null;
		this.localFile=null;
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
	public LazyImage setAutoClose(boolean autoClose) {
		this.autoClose = autoClose;
		return this;
	}
}
