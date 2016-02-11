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

    public Action getAction(String name) {
        Instance inst = this.getChildInstance(ActionCollectionFrame.INST_ACTION_LIST, name);
        if (inst != null) {
            return new Action(inst);
        }
        return null;
    }

    public Action[] getActions() {
        Instance[] inst = getChildInstances(ActionCollectionFrame.INST_ACTION_LIST);
        if (inst == null) {
            return new Action[0];
        }
        Action[] a = new Action[inst.length];
        for (int i = 0; i < inst.length; i++) {
            a[i] = new Action(inst[i]);
        }
        return a;
    }

}
