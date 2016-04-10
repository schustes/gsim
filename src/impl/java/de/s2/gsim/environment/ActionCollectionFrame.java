package de.s2.gsim.environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public class ActionCollectionFrame extends Frame {

    public final static String CATEGORY = "action";

    public final static String INST_ACTION_LIST = "actions";

    static final long serialVersionUID = 5049004885070737414L;

    public ActionCollectionFrame(Frame f) {
        super(f.getName(), Optional.of(f.getCategory()), f.isMutable(), f.isSystem());
        inherit(Arrays.asList(f), getName(), Optional.of(getCategory()));
    }

    public ActionCollectionFrame(String name) {
        super(name, Optional.of(CATEGORY), true, true);
        Frame f1 = Frame.newFrame("{all actions}", Optional.of("action"));
        addChildFrame(INST_ACTION_LIST, f1);
    }

    public ActionCollectionFrame(String name, Frame[] allowedActions) {
        super(name, Optional.of(CATEGORY), true, true);
        for (int i = 0; i < allowedActions.length; i++) {
            addChildFrame(INST_ACTION_LIST, allowedActions[i]);
        }
    }

    public void addAction(Frame action) {
        super.addChildFrame(INST_ACTION_LIST, action);
    }

    @Override
    public ActionCollectionFrame clone() {
        ActionCollectionFrame f = new ActionCollectionFrame(this);
        return f;
    }

    public ActionFrame getAction(String name) {
        List<Frame> p = getChildFrames(INST_ACTION_LIST);
        for (int i = 0; i < p.size(); i++) {
            if (p.get(i).getName().equals(name)) {
                return new ActionFrame(p.get(i));
            }
        }
        return null;

    }

    public ActionFrame[] getActions() {
        List<Frame> p = getChildFrames(INST_ACTION_LIST);
        List<ActionFrame> list = new ArrayList<>();

        for (int i = 0; i < p.size(); i++) {
            if (!p.get(i).getName().startsWith("{")) {
                list.add(new ActionFrame(p.get(i)));
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
