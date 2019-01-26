package net.gdface.image;

import java.io.File;

public class LazyImageFactoryByBufferedImage implements LazyImageFactory {

	public LazyImageFactoryByBufferedImage() {
	}

	@Override
	public BaseLazyImage create(byte[] imgBytes) throws NotImageException, UnsupportedFormatException {
		return LazyImage.create(imgBytes);
	}

	@Override
	public BaseLazyImage create(File file, String md5) throws NotImageException, UnsupportedFormatException {
		return LazyImage.create(file, md5);
	}

	@Override
	public <T> BaseLazyImage create(T src) throws NotImageException, UnsupportedFormatException {
		return LazyImage.create(src);
	}
}
