package net.gdface.image;

import java.nio.ByteBuffer;
import android.graphics.Bitmap;
import gu.jimgutil.MatrixUtils;

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
		return MatrixUtils.RGBA2BGR(buffer.array(), bitmap.getWidth(), bitmap.getHeight());
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
		return MatrixUtils.RGBA2RGB(buffer.array(), bitmap.getWidth(), bitmap.getHeight());
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
		return MatrixUtils.RGBA2GRAY(buffer.array(), bitmap.getWidth(), bitmap.getHeight());
	}
}
