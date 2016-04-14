package de.s2.gsim;

import org.junit.Test;

import de.s2.gsim.environment.Frame;

public class FrameTest {

	@Test
	public void null_should_throw_exception() throws Exception {
		Frame f = Frame.newFrame(null);
	}

}
