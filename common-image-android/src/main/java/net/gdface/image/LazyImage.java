package net.gdface.image;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import net.gdface.utils.Assert;
import net.gdface.utils.FaceUtilits;

/**
 * 图像数据处理对象<br>
 * {@link #open()}可以在不将图像全部解码加载到内存而获取图像的基本信息<br>
 * @author guyadong
 *
 */
public class LazyImage extends BaseLazyImage implements ImageMatrix{
	private Bitmap bitmap=null;
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
			if(bitmap == null){
				BitmapFactory.Options options = new BitmapFactory.Options();
				// 只获取宽高基本信息，不对图像解码
				options.inJustDecodeBounds = true;
				byte[] data = getImgBytes();
				BitmapFactory.decodeByteArray(data,0,data.length, options);
				this.width = options.outWidth;
				this.height = options.outHeight;
				//  outMimeType 字段 '/'后面的部分作为后缀
				// Example: outMimeType=image/jpeg suffix = jpeg 
				int slash = options.outMimeType.lastIndexOf('/');
				if(slash<0){
					this.suffix = options.outMimeType;
				}else{
					this.suffix = options.outMimeType.substring(slash + 1);
				}
			}else{
				this.width = bitmap.getWidth();
				this.height = bitmap.getHeight();
			}
			return this;
		} catch(Exception e){
			throw new NotImageException(e);
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
	 * 对图像数据解码生成 {@link Bitmap}对象
	 * 
	 * @return
	 * @throws UnsupportedFormatException
	 * @see ImageReadParam
	 */
	public Bitmap read() throws UnsupportedFormatException {
		try {
			if(null == this.bitmap){
				byte[] data = getImgBytes();
				// 对图像数据解码，解码失败抛出异常
				this.bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
				if(null == bitmap){
					throw new UnsupportedFormatException("decode image error");
				}
			}
			return this.bitmap;
		} catch (UnsupportedFormatException e) {
			throw e;
		}catch (Exception e) {
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
	public byte[] getMatrixRGBA() throws UnsupportedFormatException {
		if (matrixRGBA==null){
			matrixRGBA=ImageUtil.getMatrixRGBA(read());
		}
		return matrixRGBA;	
	}

	@Override
	public byte[] getMatrixRGB() throws UnsupportedFormatException{
		if (matrixRGB==null){
			matrixRGB = ImageUtil.getMatrixRGB(read());
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
			ImageUtil.getMatrixGRAY(read());
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
	 * @param bitmap
	 * @return
	 * @see #LazyImage(Bitmap)
	 * @see #open()	 */
	public static LazyImage create(final Bitmap bitmap) {
		try {
			return new LazyImage(bitmap).open();
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

	@Override
	public void finalize() throws Throwable {
		this.bitmap=null;
		super.finalize();
	}
}
