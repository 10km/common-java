package net.gdface.image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import gu.jimgutil.MatrixUtils;
import net.gdface.utils.Assert;

/**
 * 图像工具类
 * @author guyadong
 *
 */
public class ImageUtil {
	/**
	 * 对图像解码返回RGBA格式矩阵数据
	 * @param bitmap
	 * @return
	 */
	public static byte[] getMatrixRGBA(Bitmap bitmap){
		if(null==bitmap){
			throw new NullPointerException();
		}
		if(bitmap.getConfig() != Bitmap.Config.ARGB_8888){		
			bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
		}
		ByteBuffer buffer = ByteBuffer.allocate(bitmap.getByteCount());
		bitmap.copyPixelsToBuffer(buffer);
		return buffer.array();
	}
	/**
	 * 对图像解码返回BGR格式矩阵数据
	 * @param bitmap
	 * @return
	 */
	public static byte[] getMatrixBGR(Bitmap bitmap){
		if(null==bitmap){
			throw new NullPointerException();
		}
		if(bitmap.getConfig() != Bitmap.Config.ARGB_8888){		
			bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
		}
		ByteBuffer buffer = ByteBuffer.allocate(bitmap.getByteCount());
		bitmap.copyPixelsToBuffer(buffer);
		return MatrixUtils.RGBA2BGR(buffer.array(), bitmap.getWidth(), bitmap.getHeight(),bitmap.getRowBytes());
	}
	/**
	 * 对图像解码返回BGR格式矩阵数据
	 * @param bitmap
	 * @return
	 */
	public static byte[] getMatrixRGB(Bitmap bitmap){
		if(null==bitmap){
			throw new NullPointerException();
		}
		if(bitmap.getConfig() != Bitmap.Config.ARGB_8888){		
			bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
		}
		ByteBuffer buffer = ByteBuffer.allocate(bitmap.getByteCount());
		bitmap.copyPixelsToBuffer(buffer);
		return MatrixUtils.RGBA2RGB(buffer.array(), bitmap.getWidth(), bitmap.getHeight(),bitmap.getRowBytes());
	}
	/**
	 * 对图像解码返回BGR格式矩阵数据
	 * @param bitmap
	 * @return
	 */
	public static byte[] getMatrixGRAY(Bitmap bitmap){
		if(null==bitmap){
			throw new NullPointerException();
		}
		if(bitmap.getConfig() != Bitmap.Config.ARGB_8888){		
			bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
		}
		ByteBuffer buffer = ByteBuffer.allocate(bitmap.getByteCount());
		bitmap.copyPixelsToBuffer(buffer);
		return MatrixUtils.RGBA2GRAY(buffer.array(), bitmap.getWidth(), bitmap.getHeight(),bitmap.getRowBytes());
	}
	public static byte[] wirteJPEGBytes(Bitmap source){
		return wirteJPEGBytes(source,null);
	}
	public static byte[] wirteBMPBytes(Bitmap source){
		throw new UnsupportedOperationException();
	}
	public static byte[] wirtePNGBytes(Bitmap source){
		return wirteBytes(source,"PNG");
	}
	public static byte[] wirteGIFBytes(Bitmap source){
		throw new UnsupportedOperationException();
	}
	public static byte[] wirteWEBPBytes(Bitmap source){
		return wirteBytes(source,"WEBP");
	}
	/**
	 * 将原图压缩生成jpeg格式的数据
	 * @param source
	 * @return
	 * @see #wirteBytes(Bitmap, String)
	 */
	public static byte[] wirteJPEGBytes(Bitmap source,Float compressionQuality){
		return wirteBytes(source,"JPEG",compressionQuality);
	}
	public static byte[] wirteBytes(Bitmap source,String formatName){
		return wirteBytes(source,formatName,null);
	}
	
	/**
	 * 将{@link Bitmap}生成formatName指定格式的图像数据
	 * @param source
	 * @param formatName 图像格式名，图像格式名错误则抛出异常,可用的值 'PNG','JPEG','WEBP'
	 * @param compressionQuality 压缩质量(0.0~1.0),超过此范围抛出异常,为null使用默认值
	 * @return 图像字节数组
	 */
	public static byte[] wirteBytes(Bitmap source,String formatName,Float compressionQuality){
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			wirte(source, formatName, compressionQuality, output);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return output.toByteArray();		
	}
	/**
	 * 将{@link Bitmap}生成formatName指定格式的图像数据
	 * @param source
	 * @param formatName 图像格式名，图像格式名错误则抛出异常,可用的值 'PNG','JPEG','WEBP'
	 * @param compressionQuality 压缩质量(0.0~1.0),超过此范围抛出异常,为null使用默认值
	 * @param output 输出流
	 * @throws IOException
	 */
	public static void wirte(Bitmap source,String formatName,Float compressionQuality,OutputStream output) throws IOException{
		Assert.notNull(source, "source");
		Assert.notEmpty(formatName, "formatName");
		Assert.notNull(output, "output");
		if( ! (compressionQuality==null || (compressionQuality>0f && compressionQuality <= 1.0f)) ){
			throw new IllegalArgumentException("INVALID compressionQuality");
		}
		CompressFormat format = Bitmap.CompressFormat.valueOf(formatName);
		int quality = compressionQuality !=null ? (int)(compressionQuality*100):100;
		source.compress(format, quality, output);		
	}
}
