package de.s2.gsim.def.objects.behaviour;

import java.util.ArrayList;

import de.s2.gsim.def.objects.FrameOLD;

public class ActionCollectionFrame extends FrameOLD {

    public final static String CATEGORY = "action";

    public final static String INST_ACTION_LIST = "actions";

    static final long serialVersionUID = 5049004885070737414L;

    public ActionCollectionFrame(FrameOLD f) {
        super(f);
    }

    public ActionCollectionFrame(String name) {
        super(name, CATEGORY);
        FrameOLD f1 = new FrameOLD("{all actions}", "action");
        f1.setSystem(true);
        f1.setMutable(true);
        addChildFrame(INST_ACTION_LIST, f1);
    }

    public ActionCollectionFrame(String name, FrameOLD[] allowedActions) {
        super(name, CATEGORY);
        for (int i = 0; i < allowedActions.length; i++) {
            addChildFrame(INST_ACTION_LIST, allowedActions[i]);
        }
    }

    public void addAction(FrameOLD action) {
        super.addChildFrame(INST_ACTION_LIST, action);
    }

    @Override
    public Object clone() {
        ActionCollectionFrame f = new ActionCollectionFrame(this);
        return f;
    }

    public ActionFrame getAction(String name) {
        FrameOLD[] p = getChildFrames(INST_ACTION_LIST);
        for (int i = 0; i < p.length; i++) {
            if (p[i].getTypeName().equals(name)) {
                return new ActionFrame(p[i]);
            }
        }
        return null;

    }

    public ActionFrame[] getActions() {
        FrameOLD[] p = getChildFrames(INST_ACTION_LIST);
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
