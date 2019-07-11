package net.gdface.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class NetworkUtilTest {
	private static final Logger logger = LoggerFactory.getLogger(NetworkUtilTest.class) ;
	@Test
	public void testGetPhysicalNICs() {
		Set<InetAddress> sets = Sets.newHashSet();
		for(NetworkInterface nic:NetworkUtil.getPhysicalNICs()){
			for(Enumeration<InetAddress> enums = nic.getInetAddresses();enums.hasMoreElements();){
				InetAddress addr = enums.nextElement();
				if(addr instanceof Inet4Address){
					sets.add(addr);
					logger.info("nic:{}",addr.getHostAddress());
				}
			}
		}
	}

}
