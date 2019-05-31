package net.gdface.utils;

import static org.junit.Assert.*;

import java.net.UnknownHostException;

import org.junit.Test;

import jcifs.netbios.NbtAddress;

public class JcifsTest {

	@Test
	public void test() {
		try {
			NbtAddress nbtAddress = NbtAddress.getByName("10km-hasee",0x20,null);
			System.out.printf("host=%s,ip=%s", nbtAddress.getHostName(),nbtAddress.getInetAddress());
			nbtAddress.nextCalledName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	@Test
	public void test2() {
		try {
			NbtAddress[] addrs = NbtAddress.getAllByAddress("10km-hasee");
			for(NbtAddress nbtAddress : addrs){
				System.out.printf("host=%s,ip=%s", nbtAddress.getHostName(),nbtAddress.getInetAddress());
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

}
