package net.gdface.image;

import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
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
import net.gdface.utils.Judge;

/**
 * 图像数据处理对象<br>
 * {@link #open()}可以在不将图像全部解码加载到内存而获取图像的基本信息<br>
 * {@link #read(ImageReadParam)}使用内存做cache读取(不使用临时文件做cache)<br>
 * {@link #read(Rectangle, ImageTypeSpecifier)}可以对图像指定区域解码
 * @author guyadong
 *
 */
public class LazyImage {
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
	private Rectangle rectangle=null;
	private ImageReader imageReader;
	private MemoryCacheImageInputStream imageInputstream;
	private FileInputStream fileInputStream;
	/**
	 * 是否在 {@link #open()}和 {@link #read(ImageReadParam)}执行结束时自动执行 {@link #close()}释放资源<br>
	 * 默认为{@code true}
	 */
	private boolean autoClose=true;
	private BufferedImage bufferedImage=null;
	/**
	 * 通过{@link ImageReader}来读取图像基本信息，检查图像数据有效性
	 * @return 
	 * @throws UnsupportedFormatException 
	 * @throws NotImageException 
	 */
	public LazyImage open() throws UnsupportedFormatException, NotImageException {
		try {
			Iterator<ImageReader> it = ImageIO.getImageReaders(getImageInputstream());
			if (it.hasNext())
				try {
					imageReader = it.next();
					imageReader.setInput(getImageInputstream(), true, true);
					this.suffix = imageReader.getFormatName().trim().toLowerCase();
					this.width = imageReader.getWidth(0);
					this.height = imageReader.getHeight(0);
					return this;
				} catch (Exception e) {
					throw new UnsupportedFormatException(e);
				} 
			else {
				throw new NotImageException();
			}
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
	/**
	 * 根据指定的参数创建一个RGB格式的BufferedImage
	 * @param matrixRGB 图像矩阵数据,为null则创建一个指定尺寸的空图像
	 * @param width
	 * @param height
	 * @return
	 */
	public static BufferedImage createRGBImage(byte[] matrixRGB,int width,int height){
		Assert.isTrue(null==matrixRGB||(null!=matrixRGB&&matrixRGB.length==width*height*3),"invalid image description");
	    DataBufferByte dataBuffer = null==matrixRGB?null:new DataBufferByte(matrixRGB, matrixRGB.length);
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        int[] nBits = {8, 8, 8};
        int[] bOffs = {2, 1, 0};
        ComponentColorModel colorModel = new ComponentColorModel(cs, nBits, false, false,
                                             Transparency.OPAQUE,
                                             DataBuffer.TYPE_BYTE);		   
        WritableRaster raster = null!=dataBuffer
        		? Raster.createInterleavedRaster(dataBuffer, width, height, width*3, 3, bOffs, null)
        		: Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height,width*3, 3, bOffs, null);;
        BufferedImage img = new BufferedImage(colorModel,raster,false,null);
        /*try {
			ImageIO.write(img, "bmp", new File(System.getProperty("user.dir"),"test.bmp"));
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	    return  img;	    
	}
	private static final void assertContains(final Rectangle parent, String argParent, final Rectangle sub, final String argSub)
			throws IllegalArgumentException {
		if(!parent.contains(sub))
			throw new IllegalArgumentException(String.format(
				"the %s(X%d,Y%d,W%d,H%d) not contained by %s(X%d,Y%d,W%d,H%d)",
				argSub,sub.x, sub.y,sub.width, sub.height, argParent,parent.x,parent.y,parent.width, parent.height));
	}
	
	/**
	 * 对图像解码返回RGB格式矩阵数据
	 * @return 
	 * @throws UnsupportedFormatException
	 */
	public byte[] getMatrixRGB() throws UnsupportedFormatException{
		if (matrixRGB==null){
			matrixRGB=ImageUtil.getMatrixRGB(read(null));
		}
		return matrixRGB;
	}
	/**
	 * 对图像解码返回BGR格式矩阵数据
	 * @return
	 * @throws UnsupportedFormatException
	 */
	public byte[] getMatrixBGR() throws UnsupportedFormatException{
		if (matrixBGR==null){	
			matrixBGR=ImageUtil.getMatrixBGR(read(null));
		}
		return matrixBGR;
	}
	/**
	 * 对图像数据指定的区域解码返回RGB格式数据
	 * @param rect 解码区域,为null时全图解码
	 * @return 解码的RGB图像矩阵数据
	 * @throws UnsupportedFormatException
	 */
	public byte[] getMatrixRGB(Rectangle rect) throws UnsupportedFormatException{
		return cutMatrix( getMatrixRGB(),getRectangle(),rect);
	}
	/**
	 * 对图像数据指定的区域解码返回BGR格式数据
	 * @param rect 解码区域,为null时全图解码
	 * @return 解码的RGB图像矩阵数据
	 * @throws UnsupportedFormatException
	 */
	public byte[] getMatrixBGR(Rectangle rect) throws UnsupportedFormatException{
		return cutMatrix( getMatrixBGR(),getRectangle(),rect);
	}
	/**
	 * 从matrix矩阵中截取rect指定区域的子矩阵
	 * @param matrix 3byte(RGB/BGR) 图像矩阵
	 * @param matrixRect 矩阵尺寸
	 * @param rect 截取区域
	 * @return 
	 */
	public static byte[] cutMatrix(byte[] matrix,Rectangle matrixRect,Rectangle rect) {
		// 解码区域,为null或与图像尺寸相等时直接返回 matrix
		if((rect == null || rect.equals(matrixRect)))
			return matrix;
		else{
			// 如果指定的区域超出图像尺寸，则抛出异常
			assertContains(matrixRect, "srcRect", rect ,"rect");
			byte[] dstArray=new byte[rect.width*rect.height*3];	
			// 从 matrix 中复制指定区域的图像数据返回
			for(int dstIndex=0,srcIndex=(rect.y*matrixRect.width+rect.x)*3,y=0;
					y<rect.height;
					++y,srcIndex+=matrixRect.width*3,dstIndex+=rect.width*3){
				// 调用 System.arrayCopy每次复制一行数据
				System.arraycopy(matrix, srcIndex, dstArray, dstIndex, rect.width*3);
			}
			return dstArray;
		}
	}
	
	/**
	 *  对图像数据指定的区域解码返回灰度图像数据
	 * @param rect 解码区域,为null时全图解码
	 * @return 灰度图像矩阵数据
	 * @throws UnsupportedFormatException
	 */
	public byte[] getMatrixGray(Rectangle rect) throws UnsupportedFormatException{		
		if(null==matrixGray){
			BufferedImage image = read(null);
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
		Rectangle srcRect = getRectangle();
		if(null==rect||srcRect.equals(rect)){
			return matrixGray;	
		}else{
			// 如果指定的区域超出图像尺寸，则抛出异常
			assertContains(srcRect, "srcRect", rect ,"rect");
			byte[] dstArray=new byte[rect.width*rect.height];	
			// 从 matrixGray中复制指定区域的图像数据返回
			for(int dstIndex=0,srcIndex=rect.y*srcRect.width+rect.x,y=0;
					y<rect.height;
					++y,srcIndex+=srcRect.width,dstIndex+=rect.width){
				// 调用 System.arrayCopy每次复制一行数据
				System.arraycopy(matrixGray, srcIndex, dstArray, dstIndex, rect.width);
			}
			return dstArray;			
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
	private ImageInputStream getImageInputstream() {
		if (null == imageInputstream) {
			if (null == imgBytes) {
				if (null == localFile)
					throw new IllegalArgumentException(
							"while isValidImage be true localFile & imgBytes can't be NULL all");
				try {
					this.fileInputStream=new FileInputStream(localFile);
					this.imageInputstream = new MemoryCacheImageInputStream(this.fileInputStream);
				} catch (FileNotFoundException e) {
					throw new RuntimeException(e);
				}
			}else
				this.imageInputstream = new MemoryCacheImageInputStream(new ByteArrayInputStream(imgBytes));
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
		if(null!=imageReader){
			imageReader.dispose();
			imageReader=null;
		}
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
		this.bufferedImage=null;
		this.localFile=null;
		this.rectangle=null;
	}

	/**
	 * @return width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return height
	 */
	public int getHeight() {
		return height;
	}
	/**
	 * 在执行 {@link #read(ImageReadParam)}或 {@link #open()}之前调用,才有效
	 * @param autoClose 要设置的 autoClose
	 * @return 
	 * @see #autoClose
	 */
	public LazyImage setAutoClose(boolean autoClose) {
		this.autoClose = autoClose;
		return this;
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
	
	public static void main(String args[]) throws FileNotFoundException, IOException, NotImageException, UnsupportedFormatException {
		byte[] imgBytes = FaceUtilits.getBytesNotEmpty(new File("D:\\tmp\\guyadong-1.jpg"));
		// LazyImage img = createInstance(imgBytes);
		//LazyImage img = new LazyImage(new File("D:\\tmp\\guyadong-1.jpg"), null);
		LazyImage img = new LazyImage(imgBytes);
		img.setAutoClose(true);
		img.open();
		//img.getMatrixGray(null);
		img.getMatrixRGB(new Rectangle(500,500,1024,1024));
		/*for (int i = 0; i < 100000; i++) {
			BufferedImage bi = img.read();
			System.out.printf("%dX%d\n", bi.getWidth(), bi.getHeight());
		}*/
	}	
}
