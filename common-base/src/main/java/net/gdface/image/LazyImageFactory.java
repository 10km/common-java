package net.gdface.image;

import java.io.File;

/**
 * 创建 lazy image的工厂类接口
 * @author guyadong
 *
 */
public interface LazyImageFactory {
	/**
	 * 创建并打开对象
	 * @param imgBytes
	 * @return
	 * @throws NotImageException
	 * @throws UnsupportedFormatException
	 */
	public BaseLazyImage create(final byte[] imgBytes) throws NotImageException, UnsupportedFormatException;
	/**
	 * 用本地图像文件创建对象
	 * @param file
	 * @param md5 {@code file}的MD5较验码，可以为null
	 * @return
	 * @throws NotImageException
	 * @throws UnsupportedFormatException
	 */
	public BaseLazyImage create(final File file, String md5) throws NotImageException, UnsupportedFormatException;
	/**
	 * 多源创建对象
	 * @param src
	 * @return
	 * @throws NotImageException
	 * @throws UnsupportedFormatException
	 */
	public <T> BaseLazyImage create(final T src) throws NotImageException, UnsupportedFormatException;
}
