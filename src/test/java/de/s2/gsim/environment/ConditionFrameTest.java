package de.s2.gsim.environment;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

public class ConditionFrameTest {
    
    @Test
    public void verify_copy_constructor() throws Exception {
        ConditionFrame a = ConditionFrame.newConditionFrame("a", ">", "1");
        ConditionFrame b = ConditionFrame.copyAndWrap(a);
        assertThat("wrapped frame is equivalent to original", a, equalTo(b));
    }

}
