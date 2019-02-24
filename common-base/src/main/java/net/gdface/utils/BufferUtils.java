package net.gdface.utils;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

/**
 * NIO Buffer工具类
 * @author guyadong
 *
 */
public class BufferUtils {

	/**
	 * {@link DoubleBuffer} TO {@link ByteBuffer}
	 * @param input
	 * @return
	 */
	public static ByteBuffer asByteBuffer(DoubleBuffer input){
		if(null == input ){
			return null;
		}
		ByteBuffer buffer = ByteBuffer.allocate(input.capacity()* (Double.SIZE/8));
		while(input.hasRemaining()){
			buffer.putDouble(input.get());
		}
		return buffer;
	}
	/**
	 * double[] TO byte[]
	 * @param input
	 * @return
	 */
	public static byte[] asByteArray(double[] input){
		if(null == input ){
			return null;
		}
		return asByteBuffer(DoubleBuffer.wrap(input)).array();
	}
	/**
	 * {@link FloatBuffer} TO {@link ByteBuffer}
	 * @param input
	 * @return
	 */
	public static ByteBuffer asByteBuffer(FloatBuffer input){
		if(null == input ){
			return null;
		}
		ByteBuffer buffer = ByteBuffer.allocate(input.capacity()* (Float.SIZE/8));
		while(input.hasRemaining()){
			buffer.putFloat(input.get());
		}
		return buffer;
	}
	/**
	 * float[] TO byte[]
	 * @param input
	 * @return
	 */
	public static byte[] asByteArray(float[] input){
		if(null == input ){
			return null;
		}
		return asByteBuffer(FloatBuffer.wrap(input)).array();
	}
	/**
	 * {@link IntBuffer} TO {@link ByteBuffer}
	 * @param input
	 * @return
	 */
	public static ByteBuffer asByteBuffer(IntBuffer input){
		if(null == input ){
			return null;
		}
		ByteBuffer buffer = ByteBuffer.allocate(input.capacity()* (Integer.SIZE/8));
		while(input.hasRemaining()){
			buffer.putInt(input.get());
		}
		return buffer;
	}
	/**
	 * int[] TO byte[]
	 * @param input
	 * @return
	 */
	public static byte[] asByteArray(int[] input){
		if(null == input ){
			return null;
		}
		return asByteBuffer(IntBuffer.wrap(input)).array();
	}
	/**
	 * {@link LongBuffer} TO {@link ByteBuffer}
	 * @param input
	 * @return
	 */
	public static ByteBuffer asByteBuffer(LongBuffer input){
		if(null == input ){
			return null;
		}
		ByteBuffer buffer = ByteBuffer.allocate(input.capacity()* (Long.SIZE/8));
		while(input.hasRemaining()){
			buffer.putLong(input.get());
		}
		return buffer;
	}
	/**
	 * long[] TO byte[]
	 * @param input
	 * @return
	 */
	public static byte[] asByteArray(long[] input){
		if(null == input ){
			return null;
		}
		return asByteBuffer(LongBuffer.wrap(input)).array();
	}
	/**
	 * {@link ShortBuffer} TO {@link ByteBuffer}
	 * @param input
	 * @return
	 */
	public static ByteBuffer asByteBuffer(ShortBuffer input){
		if(null == input ){
			return null;
		}
		ByteBuffer buffer = ByteBuffer.allocate(input.capacity()* (Short.SIZE/8));
		while(input.hasRemaining()){
			buffer.putShort(input.get());
		}
		return buffer;
	}
	/**
	 * short[] TO byte[]
	 * @param input
	 * @return
	 */
	public static byte[] asByteArray(short[] input){
		if(null == input ){
			return null;
		}
		return asByteBuffer(ShortBuffer.wrap(input)).array();
	}
	/**
	 * byte[] TO double[]
	 * @param input
	 * @return
	 */
	public static double[] asDoubleArray(byte[] input){
		if(null == input ){
			return null;
		}
		DoubleBuffer buffer = ByteBuffer.wrap(input).asDoubleBuffer();
		double[] res = new double[buffer.remaining()];
		buffer.get(res);
		return res;
	}
	/**
	 * byte[] TO float[]
	 * @param input
	 * @return
	 */
	public static float[] asFloatArray(byte[] input){
		if(null == input ){
			return null;
		}
		FloatBuffer buffer = ByteBuffer.wrap(input).asFloatBuffer();
		float[] res = new float[buffer.remaining()];
		buffer.get(res);
		return res;
	}
	/**
	 * byte[] TO int[]
	 * @param input
	 * @return
	 */
	public static int[] asIntArray(byte[] input){
		if(null == input ){
			return null;
		}
		IntBuffer buffer = ByteBuffer.wrap(input).asIntBuffer();
		int[] res = new int[buffer.remaining()];
		buffer.get(res);
		return res;
	}
	/**
	 * byte[] TO long[]
	 * @param input
	 * @return
	 */
	public static long[] asLongArray(byte[] input){
		if(null == input ){
			return null;
		}
		LongBuffer buffer = ByteBuffer.wrap(input).asLongBuffer();
		long[] res = new long[buffer.remaining()];
		buffer.get(res);
		return res;
	}
	/**
	 * byte[] TO short[]
	 * @param input
	 * @return
	 */
	public static short[] asShortArray(byte[] input){
		if(null == input ){
			return null;
		}
		ShortBuffer buffer = ByteBuffer.wrap(input).asShortBuffer();
		short[] res = new short[buffer.remaining()];
		buffer.get(res);
		return res;
	}
}
