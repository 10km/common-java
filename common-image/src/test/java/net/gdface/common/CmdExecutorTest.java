package net.gdface.common;

import static org.junit.Assert.*;

import org.junit.Test;

import net.gdface.utils.CmdExecutor;

/**
 * 
 * @author guyadong
 *
 */
public class CmdExecutorTest {

	@Test
	public void test() {
		try {
			String out = CmdExecutor.builder("hadoop")
			.errRedirect(false)
			.sudoCmd("date -s '2016-12-27 23:23:23'")
			.sudoCmd("clock -w")
			.sudoCmd("cat /etc/sysconfig/network")
			.cmd("date")
//			.sudoCmd("ntpdate -u time.windows.com")
			.exec();
			System.out.println(out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

