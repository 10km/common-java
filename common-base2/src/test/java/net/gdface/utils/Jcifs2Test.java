package net.gdface.utils;

import org.junit.Test;

import jcifs.Address;
import jcifs.NameServiceClient;
import jcifs.context.SingletonContext;
import jcifs.netbios.NameServiceClientImpl;
import jcifs.netbios.NbtAddress;

public class Jcifs2Test {

	@Test
	public void test() {
		try {
			final NameServiceClientImpl nsc = new NameServiceClientImpl(SingletonContext.getInstance());

			NbtAddress[] addrs =	nsc.getNbtAllByAddress("192.168.3.6");
			for(NbtAddress address:addrs){
			System.out.printf("%s\n",address.getHostName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void test2() {
		try {
			final NameServiceClientImpl nsc = new NameServiceClientImpl(SingletonContext.getInstance());
			NbtAddress address = nsc.getNbtByName("guyadong-pc");
			System.out.printf("%s\n",address.getHostName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void test3() {
		try {
			final NameServiceClient nsc = new NameServiceClientImpl(SingletonContext.getInstance());
			{
				Address[] addrs = nsc.getAllByName("landtalkhost", true);
				for(Address address : addrs){
					System.out.printf("%s\n",address.toInetAddress());
				}
			}
			
			{
				System.out.println("============");
				Address address = nsc.getByName("landtalkhost");
				System.out.printf("%s %s\n",address,address.getHostAddress());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
