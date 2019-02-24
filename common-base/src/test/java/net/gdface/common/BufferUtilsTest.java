package net.gdface.common;

import static org.junit.Assert.*;

import org.junit.Test;

import net.gdface.utils.BufferUtils;
import net.gdface.utils.FaceUtilits;

public class BufferUtilsTest {

	@Test
	public void test() {
		double [] input = new double[]{0.1,3.14,0.618};
		byte[] output = BufferUtils.asByteArray(input);
		System.out.println(FaceUtilits.toHex(output));
		double[] output2 = BufferUtils.asDoubleArray(output);
		for(double e:output2)System.out.printf("%f,", e);
	}

}
