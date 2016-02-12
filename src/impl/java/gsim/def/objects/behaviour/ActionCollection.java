package gsim.def.objects.behaviour;

import gsim.def.objects.Instance;

public class ActionCollection extends Instance {

    static final long serialVersionUID = -9032868199755804359L;

    public ActionCollection(ActionCollectionFrame f) {
        super(f.getTypeName(), f);
    }

    public ActionCollection(Instance inst) {
        super(inst);
    }

    public ActionDef getAction(String name) {
        Instance inst = this.getChildInstance(ActionCollectionFrame.INST_ACTION_LIST, name);
        if (inst != null) {
            return new ActionDef(inst);
        }
        return null;
    }

    public ActionDef[] getActions() {
        Instance[] inst = getChildInstances(ActionCollectionFrame.INST_ACTION_LIST);
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
