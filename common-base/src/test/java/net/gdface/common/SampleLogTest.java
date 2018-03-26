package net.gdface.common;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

import net.gdface.utils.SampleLog;

public class SampleLogTest {

	@Test
	public void test() {
		SampleLog.log("wwwwww", "tom");
		SampleLog.log("name {},age:{}");
		SampleLog.log("name {},age:{} ww", "tom");
		SampleLog.log("name {},age:{},date:{},time:{}", "tom",23,new Date());
	}

}
