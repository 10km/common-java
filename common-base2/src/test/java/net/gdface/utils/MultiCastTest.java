package net.gdface.utils;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;


import static net.gdface.utils.NetworkUtil.*;

public class MultiCastTest {
    private static final String hostAndPort = "224.42.64.11:26411";
	private static AtomicBoolean stop = new AtomicBoolean(false);
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		new Thread(){

			@Override
			public void run() {
				try {
					recevieMultiCastLoop(hostAndPort, 200,new Predicate<byte[]>() {

						@Override
						public boolean apply(byte[] input) {
							try {
								System.out.write(input);
								System.out.println();
							} catch (IOException e) {
								e.printStackTrace();
							}
							return true;
						}
					},
					Predicates.<Throwable>alwaysFalse(),
					stop);
				} catch (IOException e) {					
					e.printStackTrace();
				}
			}
			
		}.start();

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testSend() {

		try {
			for(int i=0;i<100;++i){
				sendMultiCast(hostAndPort, String.format("hello %s", i).getBytes());
				Thread.sleep(500);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		stop.set(true);
	}

}
