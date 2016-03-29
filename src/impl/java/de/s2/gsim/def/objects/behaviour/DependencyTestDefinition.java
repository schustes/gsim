package de.s2.gsim.def.objects.behaviour;

import de.s2.gsim.def.objects.Frame;
import de.s2.gsim.def.objects.Instance;

public class DependencyTestDefinition extends Instance {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DependencyTestDefinition(Frame f) {
        super(f.getTypeName(), f);
    }

    public DependencyTestDefinition(Instance f) {
        super(f);
    }

    public void addDependencyTest(DependencyTest f) {
        super.addChildInstance("list", f);
    }

    public DependencyTest[] getDependencyTests() {
        DependencyTest[] res = new DependencyTest[getChildInstances("list").length];
        int i = 0;
        for (Instance f : getChildInstances("list")) {
            DependencyTest frame = new DependencyTest(f);
            res[i] = frame;
            i++;
        }
        return res;
    }

}
