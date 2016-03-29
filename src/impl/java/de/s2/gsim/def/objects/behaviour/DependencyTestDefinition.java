package de.s2.gsim.def.objects.behaviour;

import de.s2.gsim.def.objects.FrameOLD;
import de.s2.gsim.def.objects.InstanceOLD;

public class DependencyTestDefinition extends InstanceOLD {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DependencyTestDefinition(FrameOLD f) {
        super(f.getTypeName(), f);
    }

    public DependencyTestDefinition(InstanceOLD f) {
        super(f);
    }

    public void addDependencyTest(DependencyTest f) {
        super.addChildInstance("list", f);
    }

    public DependencyTest[] getDependencyTests() {
        DependencyTest[] res = new DependencyTest[getChildInstances("list").length];
        int i = 0;
        for (InstanceOLD f : getChildInstances("list")) {
            DependencyTest frame = new DependencyTest(f);
            res[i] = frame;
            i++;
        }
        return res;
    }

}
