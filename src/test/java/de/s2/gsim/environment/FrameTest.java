package de.s2.gsim.environment;

import org.junit.Test;

public class FrameTest {

	@Test
	public void null_should_throw_exception() throws Exception {
		Frame f = Frame.newFrame(null);
	}

}
