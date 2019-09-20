package net.gdface.utils;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static net.gdface.utils.NetworkUtil.*;

public class NetworkUtilTest {
	private static final Logger logger = LoggerFactory.getLogger(NetworkUtilTest.class) ;
	@Test
	public void testGetPhysicalNICs() {
		logger.info("nic:{}",NetworkUtil.ipv4AddressesOfPhysicalNICs());
		logger.info("nic:{}",NetworkUtil.addressesOfPhysicalNICs(FILTER_IPV4));
		logger.info("nic:{}",NetworkUtil.ipv4AddressesOfNoVirtualNICs());
		logger.info("nic:{}",NetworkUtil.addressesOfNoVirtualNICs(FILTER_IPV4));
	}

}
