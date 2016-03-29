package de.s2.gsim.def.objects.behaviour;

import de.s2.gsim.def.objects.InstanceOLD;

public class ActionCollection extends InstanceOLD {

    static final long serialVersionUID = -9032868199755804359L;

    public ActionCollection(ActionCollectionFrame f) {
        super(f.getTypeName(), f);
    }

    public ActionCollection(InstanceOLD inst) {
        super(inst);
    }

    public ActionDef getAction(String name) {
        InstanceOLD inst = this.getChildInstance(ActionCollectionFrame.INST_ACTION_LIST, name);
        if (inst != null) {
            return new ActionDef(inst);
        }
        return null;
    }

    public ActionDef[] getActions() {
        InstanceOLD[] inst = getChildInstances(ActionCollectionFrame.INST_ACTION_LIST);
        if (inst == null) {
            return new ActionDef[0];
        }
        ActionDef[] a = new ActionDef[inst.length];
        for (int i = 0; i < inst.length; i++) {
            a[i] = new ActionDef(inst[i]);
        }
        return a;
    }

}
