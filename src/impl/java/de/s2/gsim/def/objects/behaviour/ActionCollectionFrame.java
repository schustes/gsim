package de.s2.gsim.def.objects.behaviour;

import java.util.ArrayList;

import de.s2.gsim.def.objects.Frame;

public class ActionCollectionFrame extends Frame {

    public final static String CATEGORY = "action";

    public final static String INST_ACTION_LIST = "actions";

    static final long serialVersionUID = 5049004885070737414L;

    public ActionCollectionFrame(Frame f) {
        super(f);
    }

    public ActionCollectionFrame(String name) {
        super(name, CATEGORY);
        Frame f1 = new Frame("{all actions}", "action");
        f1.setSystem(true);
        f1.setMutable(true);
        addChildFrame(INST_ACTION_LIST, f1);
    }

    public ActionCollectionFrame(String name, Frame[] allowedActions) {
        super(name, CATEGORY);
        for (int i = 0; i < allowedActions.length; i++) {
            addChildFrame(INST_ACTION_LIST, allowedActions[i]);
        }
    }

    public void addAction(Frame action) {
        super.addChildFrame(INST_ACTION_LIST, action);
    }

    @Override
    public Object clone() {
        ActionCollectionFrame f = new ActionCollectionFrame(this);
        return f;
    }

    public ActionFrame getAction(String name) {
        Frame[] p = getChildFrames(INST_ACTION_LIST);
        for (int i = 0; i < p.length; i++) {
            if (p[i].getTypeName().equals(name)) {
                return new ActionFrame(p[i]);
            }
        }
        return null;

    }

    public ActionFrame[] getActions() {
        Frame[] p = getChildFrames(INST_ACTION_LIST);
        ArrayList list = new ArrayList();

        for (int i = 0; i < p.length; i++) {
            if (!p[i].getTypeName().startsWith("{")) {
                list.add(new ActionFrame(p[i]));
            }
        }
        ActionFrame[] a = new ActionFrame[list.size()];
        list.toArray(a);
        return a;
    }

    public void removeAction(String action) {
        super.removeChildFrame(INST_ACTION_LIST, action);
    }

}
