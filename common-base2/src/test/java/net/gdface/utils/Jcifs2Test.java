package net.gdface.utils;

import static org.junit.Assert.*;

import java.net.UnknownHostException;

import org.junit.Test;

import jcifs.NetbiosAddress;
import jcifs.context.SingletonContext;
import jcifs.netbios.NameServiceClientImpl;
import jcifs.netbios.NbtAddress;
import jcifs.netbios.UniAddress;

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
			final NameServiceClientImpl nsc = new NameServiceClientImpl(SingletonContext.getInstance());
			{
				UniAddress[] addrs = nsc.getAllByName("guyadong-pc", false);
				for(UniAddress address : addrs){
					System.out.printf("%s\n",address.toInetAddress());
				}
			}
			
			{
				System.out.println("============");
				UniAddress address = nsc.getByName("guyadong-pc");
				System.out.printf("%s\n",address);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
