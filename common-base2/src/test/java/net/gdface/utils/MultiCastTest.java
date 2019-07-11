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

		// 启动组播数据接收线程
		new Thread(new MultiCastDispatcher(hostAndPort, 200,new Predicate<byte[]>() {

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
		Predicates.<Throwable>alwaysFalse()).init()).start();

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testSend() {
		
		// 发送组播数据
		try {
			for(int i=0;i<100;++i){
				sendMulticast(hostAndPort, String.format("hello %s", i).getBytes());
				Thread.sleep(500);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// 结束组播接收线程
		stop.set(true);
	}

}
