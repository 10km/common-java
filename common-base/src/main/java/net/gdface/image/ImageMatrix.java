package net.gdface.image;

/**
 * 图像矩阵读写接口
 * @author guyadong
 *
 */
public interface ImageMatrix {
	/**
	 * @return height
	 */
	int getHeight();
	/**
	 * @return width
	 */
	int getWidth();
	/**
	 *  对图像数据指定的区域解码返回灰度图像矩阵
	 * @return 灰度图像矩阵数据
	 * @throws UnsupportedFormatException
	 */
	byte[] getMatrixGray() throws UnsupportedFormatException;
	/**
	 * 对图像解码返回BGR格式矩阵数据
	 * @return
	 * @throws UnsupportedFormatException
	 */
	byte[] getMatrixBGR() throws UnsupportedFormatException;
	/**
	 * 对图像解码返回RGB格式矩阵数据
	 * @return 
	 * @throws UnsupportedFormatException
	 */
	byte[] getMatrixRGB() throws UnsupportedFormatException;
	/**
	 * 对图像解码返回RGBA格式矩阵数据
	 * @return
	 * @throws UnsupportedFormatException
	 */
	byte[] getMatrixRGBA() throws UnsupportedFormatException;

}
