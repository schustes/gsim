package de.s2.gsim.def.objects.behaviour;

import de.s2.gsim.def.objects.FrameOLD;

public class DependencyTestDefinitionFrame extends FrameOLD {

    public final static DependencyTestDefinitionFrame DEFINITION = new DependencyTestDefinitionFrame("test-definition-definition");
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DependencyTestDefinitionFrame(FrameOLD f) {
        super(f);
    }

    public DependencyTestDefinitionFrame(String name) {
        super(name, "dependency-test");
        super.defineObjectList("list", new DependencyTestFrame("dummy-test"));
    }

    public void addDependencyTest(DependencyTestFrame f) {
        super.addChildFrame("list", f);
    }

    public DependencyTestFrame[] getDependencyTestFrames() {
        DependencyTestFrame[] res = new DependencyTestFrame[getChildFrames("list").length];
        int i = 0;
        for (FrameOLD f : getChildFrames("list")) {
            DependencyTestFrame frame = new DependencyTestFrame(f);
            res[i] = frame;
            i++;
        }
        return res;
    }

}
