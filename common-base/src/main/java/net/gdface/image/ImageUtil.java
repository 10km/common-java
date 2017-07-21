package net.gdface.image;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import net.gdface.utils.Assert;

/**
 * 图像缩放工具类
 * @author guyadong
 *
 */
public class ImageUtil {

	/**
	 * 对图像进行缩放
	 * @param source 原图
	 * @param targetWidth 缩放后图像宽度
	 * @param targetHeight 缩放后图像高度
	 * @param constrain 为true时等比例缩放，targetWidth,targetHeight为缩放图像的限制尺寸
	 * @return
	 */
	public static BufferedImage resize(BufferedImage source, int targetWidth, int targetHeight,boolean constrain) {
		if(constrain){
			double aspectRatio = (double)source.getWidth()/source.getHeight();
			double sx = (double) targetWidth / source.getWidth();
			double sy = (double) targetHeight / source.getHeight();
			if (sx > sy) {
				targetWidth = (int) Math.round(targetHeight*aspectRatio);
			} else {
				targetHeight = (int) Math.round(targetWidth/aspectRatio);
			}	
		}
		int type = source.getType();
		BufferedImage target = null;
		if (type == BufferedImage.TYPE_CUSTOM) {
			ColorModel cm = source.getColorModel();
			WritableRaster raster = cm.createCompatibleWritableRaster(targetWidth, targetHeight);
			target = new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null);
		} else {
			target = new BufferedImage(targetWidth, targetHeight, type);
		}
		Graphics2D g = target.createGraphics();
		try{
			g.drawImage(  
					source.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH),  
					0, 0, null);
		}finally{
			g.dispose();
		}
		return target;
	}
	
	/**
	 * 将原图压缩生成jpeg格式的数据
	 * @param source
	 * @return
	 * @see #wirteBytes(BufferedImage, String)
	 */
	public static byte[] wirteJPEGBytes(BufferedImage source){
		return wirteBytes(source,"JPEG");
	}
	/**
	 * 将{@link BufferedImage}生成formatName指定格式的图像数据
	 * @param source
	 * @param formatName 图像格式名，图像格式名错误则抛出异常
	 * @return
	 */
	public static byte[] wirteBytes(BufferedImage source,String formatName){
		Assert.notNull(source, "source");
		Assert.notEmpty(formatName, "formatName");
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Graphics2D g = null;
		try {
			// 对于某些格式的图像(如png)，直接调用ImageIO.write生成jpeg可能会失败
			// 所以先尝试直接调用ImageIO.write,如果失败则用Graphics生成新的BufferedImage再调用ImageIO.write
			for(BufferedImage s=source;!ImageIO.write(s, formatName, output);){
				if(null!=g)
					throw new IllegalArgumentException(String.format("not found writer for '%s'",formatName));
				s = new BufferedImage(source.getWidth(),
						source.getHeight(), BufferedImage.TYPE_INT_RGB);
				g = s.createGraphics();
				g.drawImage(source, 0, 0,null);				
			}				
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (null != g)
				g.dispose();
		}
		return output.toByteArray();		
	}
	/**
	 * 对原图创建缩略图对象<br>
	 * 如果原图尺寸小于指定的缩略图尺寸则直接返回原图对象的副本
	 * @param source 原图对象
	 * @param thumbnailWidth 缩略图宽度
	 * @param thumbnailHeight 缩略图高度
	 * @param ratioThreshold 最大宽高比阀值(宽高中较大的值/较小的值)，<此值时对原图等比例缩放，>=此值时从原图切出中间部分图像再等比例缩放
	 * @return
	 */
	public static BufferedImage createThumbnail(BufferedImage source,int thumbnailWidth,int thumbnailHeight,double ratioThreshold) {
		int w = source.getWidth();
		int h = source.getHeight();
		if (w < thumbnailWidth && h < thumbnailHeight) {
			// 返回原图的副本
			return source.getSubimage(0, 0, w, h);
		}
		double thumAspectRatio = (double) thumbnailWidth / thumbnailHeight;
		double wh_sca = w > h ? (double) w / h : (double) h / w;
		if (wh_sca >= ratioThreshold) {
			if (w > h) {
				int fw = (int) (thumAspectRatio * h);
				if (h <= thumbnailHeight) {
					return source.getSubimage((w - fw) / 2, 0, fw, h);
				} else {
					return resize(source.getSubimage( (w - fw) / 2, 0, fw, h), thumbnailWidth,
							thumbnailHeight,true);
				}
			} else {
				int fh = (int) (thumAspectRatio * w);
				if (w <= thumbnailWidth) {
					return source.getSubimage( 0, (h - fh) / 2, w, fh);
				} else {
					return resize(source.getSubimage( 0, (h - fh) / 2, w, fh), thumbnailWidth,
							thumbnailHeight,true);
				}
			}
		} else {
			return resize(source, thumbnailWidth, thumbnailHeight,true);
		}
	}
	/**
	 * 对原图创建JPEG格式的缩略图
	 * @param imageBytes 图像数据字节数组
	 * @param thumbnailWidth
	 * @param thumbnailHeight
	 * @param ratioThreshold
	 * @return 返回jpeg格式的图像数据字节数组
	 * @see #createThumbnail(BufferedImage, int, int, double)
	 * @see #wirteJPEGBytes(BufferedImage)
	 */
	public static byte[] createJPEGThumbnail(byte[] imageBytes,int thumbnailWidth,int thumbnailHeight,double ratioThreshold) {
		Assert.notEmpty(imageBytes, "imageBytes");
		try {
			BufferedImage source = ImageIO.read(new ByteArrayInputStream(imageBytes));
			if(null==source)
				throw new IllegalArgumentException("unsupported image format");
			BufferedImage thumbnail = createThumbnail(source, thumbnailWidth, thumbnailHeight, ratioThreshold);
			return wirteJPEGBytes(thumbnail);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}		
	}

	/**
	 * @param image
	 * @param bandOffset 用于判断通道顺序
	 * @return
	 */
	private static boolean equalBandOffsetWith3Byte(BufferedImage image,int[] bandOffset){
		if(image.getType()==BufferedImage.TYPE_3BYTE_BGR){
			if(image.getData().getSampleModel() instanceof ComponentSampleModel){
				ComponentSampleModel sampleModel = (ComponentSampleModel)image.getData().getSampleModel();
				if(Arrays.equals(sampleModel.getBandOffsets(), bandOffset)){
					return true;
				}
			}
		}
		return false;		
	}

	public static boolean isBGR3Byte(BufferedImage image){
		return equalBandOffsetWith3Byte(image,new int[]{0, 1, 2});
	}

	public static boolean isRGB3Byte(BufferedImage image){
		return equalBandOffsetWith3Byte(image,new int[]{2, 1, 0});
	}

	/**
	 * 对图像解码返回RGB格式矩阵数据
	 * @param image
	 * @return 
	 */
	public static byte[] getMatrixRGB(BufferedImage image) {
		if(null==image)
			throw new NullPointerException();
		byte[] matrixRGB;
		if(isRGB3Byte(image)){
			matrixRGB= (byte[]) image.getData().getDataElements(0, 0, image.getWidth(), image.getHeight(), null);
		}else{
			// 转RGB格式
			BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(),  
	                BufferedImage.TYPE_3BYTE_BGR);
			new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_sRGB), null).filter(image, rgbImage);
			matrixRGB= (byte[]) rgbImage.getData().getDataElements(0, 0, image.getWidth(), image.getHeight(), null);
		} 
		return matrixRGB;
	}

	/**
	 * 对图像解码返回BGR格式矩阵数据
	 * @param image
	 * @return
	 */
	public static byte[] getMatrixBGR(BufferedImage image){
		if(null==image)
			throw new NullPointerException();
		byte[] matrixBGR;
		if(isBGR3Byte(image)){
			matrixBGR= (byte[]) image.getData().getDataElements(0, 0, image.getWidth(), image.getHeight(), null);
		}else{			
			// ARGB格式图像数据
			int intrgb[]=image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
			matrixBGR=new byte[image.getWidth() * image.getHeight()*3];
			// ARGB转BGR格式
			for(int i=0,j=0;i<intrgb.length;++i,j+=3){
				matrixBGR[j]=(byte) (intrgb[i]&0xff);
				matrixBGR[j+1]=(byte) ((intrgb[i]>>8)&0xff);
				matrixBGR[j+2]=(byte) ((intrgb[i]>>16)&0xff);
			}
		} 
		return matrixBGR;
	}
	
	/**
	 * 从源{@link BufferedImage}对象创建一份拷贝
	 * @param src
	 * @param imageType 创建的{@link BufferedImage}目标对象类型
	 * @return
	 * @see {@link BufferedImage#BufferedImage(int, int, int)}
	 */
	public static  BufferedImage copy(Image src,int imageType){
		if(null==src)
			throw new NullPointerException("src must not be null");
		
		BufferedImage dst = new BufferedImage(src.getWidth(null), src.getHeight(null),  imageType);
		Graphics g = dst.getGraphics();
		try{
			g.drawImage(src, 0, 0, null);
			return dst;
		}finally{
			g.dispose();
		}
	}
	/**
	 * 创建{@link BufferedImage#TYPE_3BYTE_BGR}类型的拷贝
	 * @param src
	 * @return
	 * @see #copy(BufferedImage, int)
	 */
	public static  BufferedImage copy(BufferedImage src){
		return copy(src,BufferedImage.TYPE_3BYTE_BGR);
	}
	/**
	 * 对原图缩放，返回缩放后的新对象
	 * @param src
	 * @param scale
	 * @return
	 */
	public static BufferedImage scale(BufferedImage src,double scale){
		if(null==src)
			throw new NullPointerException("src must not be null");
		if(0>=scale)
			throw new IllegalArgumentException("scale must >0");

		int width = src.getWidth();        // 源图宽   
		int height = src.getHeight();        // 源图高 
        Image image = src.getScaledInstance((int)Math.round(width * scale), (int)Math.round(height * scale), Image.SCALE_SMOOTH);
        return copy(image,BufferedImage.TYPE_3BYTE_BGR);
	}
	/**
	 * 将{@link Image}图像上下左右扩充指定的尺寸
	 * @param src
	 * @param imageType
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @return
	 */
	public static BufferedImage growCanvas(Image src,int imageType,int left,int top,int right,int bottom){
		if(null==src)
			throw new NullPointerException("src must not be null");
		if(left<0||top<0||right<0||bottom<0)
			throw new IllegalArgumentException("left,top,right,bottom must >=0");
		BufferedImage dst = new BufferedImage(src.getWidth(null)+left+right, src.getHeight(null)+top+bottom,  imageType);
		Graphics g = dst.getGraphics();
		try{
			g.setColor(Color.BLACK);
			g.fillRect(0,0, dst.getWidth(), dst.getHeight());
			g.drawImage(src, left, top, null);
			return dst;
		}finally{
			g.dispose();
		}
	}
	/**
	 * @param src
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @return
	 * @see #growCanvas(Image, int, int, int, int, int)
	 */
	public static BufferedImage growCanvas(Image src,int left,int top,int right,int bottom){
		return growCanvas(src,BufferedImage.TYPE_3BYTE_BGR,left,top,right,bottom);
	}
	/**
	 * 将{@link Image}图像(向右下)扩充为正文形(尺寸长宽最大边)
	 * @param src
	 * @return
	 */
	public static BufferedImage growSquareCanvas(Image src){
		if(null==src)
			throw new NullPointerException("src must not be null");
		int width=src.getWidth(null);
		int height=src.getHeight(null);
		int size=Math.max(width, height);
		return growCanvas(src,BufferedImage.TYPE_3BYTE_BGR,0,0,size-width,size-height);
	}
}
