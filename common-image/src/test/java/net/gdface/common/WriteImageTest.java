package net.gdface.common;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.junit.Test;

import net.gdface.image.ImageUtil;

/**
 * @author guyadong
 *
 */
public class WriteImageTest {
	Logger logger = Logger.getLogger(WriteImageTest.class.getSimpleName());
	@Test
	public void test() {
		InputStream im = this.getClass().getResourceAsStream("/image/he049.jpg");
		try {
			BufferedImage bufferedImage = ImageIO.read(im);
			byte[] imageBytes = ImageUtil.wirteJPEGBytes(bufferedImage, 0.9F);
			logger.info(String.format("imageBytes = %d", imageBytes.length));
			byte[] bmpBytes = ImageUtil.wirteBMPytes(bufferedImage);
			logger.info(String.format("bmpBytes = %d", bmpBytes.length));
			byte[] pngBytes = ImageUtil.wirtePNGytes(bufferedImage);
			logger.info(String.format("pngBytes = %d", pngBytes.length));
			byte[] gifBytes = ImageUtil.wirteGIFytes(bufferedImage);
			logger.info(String.format("gifBytes = %d", gifBytes.length));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
