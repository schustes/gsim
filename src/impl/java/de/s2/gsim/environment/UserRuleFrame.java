package de.s2.gsim.environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.objects.attribute.DomainAttribute;

public class UserRuleFrame extends Frame {

    public static final String ATTR_LIST_ATTRS = "attributes";

    public static final String CATEGORY = "rule";

    public static UserRuleFrame DEFINITION = UserRuleFrame.newUserRuleFrame("rule-definition");

    public static final String INST_LIST_COND = "conditions";

    public static final String INST_LIST_CONS = "consequences";

    static final long serialVersionUID = 4875468082081888178L;

    protected UserRuleFrame(String name, Frame... parents) {
        super(name, parents);
    }
    
    /**
     * Inheritance constructor.
     */
    public static UserRuleFrame inheritFromUserRuleFrames(List<UserRuleFrame> parents, String name, String category) {
    	Frame f = Frame.inherit(parents, name, Optional.of(category));
        return new UserRuleFrame(f.getName(), f);
    }


    /**
     * Wrap an existing frame.
     */
    public static UserRuleFrame wrap(Frame orig, String name) {
        UserRuleFrame ur = new UserRuleFrame(orig.getName(), orig.getParentFrames().toArray(new Frame[0]));
        Frame.copyInternal(orig, ur);
        return ur;
    }  
    /**
     * Wrap an existing frame.
     */
    public static UserRuleFrame wrap(Frame orig) {
    	return wrap(orig, orig.getName());
    }

    public static UserRuleFrame newUserRuleFrame(String name) {

    	UserRuleFrame f = new UserRuleFrame(name);// Frame.newFrame(name, Optional.of(CATEGORY));
        Frame f2 = Frame.newFrame("{all-actions}", Optional.of("action"));
        f.addOrSetChildFrame("consequences", f2);

        DomainAttribute a = new DomainAttribute("activated", AttributeType.STRING);
        a.setDefault("true");
        f.addOrSetAttribute(ATTR_LIST_ATTRS, a);

        return f;

    }

    public void addCondition(ConditionFrame cond) {

        // resolve possible frame-references to $'s:

        super.addOrSetChildFrame(INST_LIST_COND, cond);

    }

    public void addConsequence(ActionFrame cons) {
        super.addOrSetChildFrame(INST_LIST_CONS, cons);
    }

    public ConditionFrame createCondition(String var, String op, String val) {
        ConditionFrame f = ConditionFrame.newConditionFrame(var, op, val);
        return f;
    }

    public ConditionFrame[] getConditions() {
        ArrayList<ConditionFrame> list = new ArrayList<>();
        for (Frame f: getChildFrames(UserRuleFrame.INST_LIST_COND)) {
            if (!f.getName().startsWith("{")) {
                list.add(ConditionFrame.copyAndWrap(f));
            }
        }
        ConditionFrame[] cond = new ConditionFrame[list.size()];
        list.toArray(cond);
        return cond;
    }

    public ActionFrame[] getConsequents() {
        ArrayList<ActionFrame> list = new ArrayList<>();
        
        for (Frame f: getChildFrames(UserRuleFrame.INST_LIST_CONS)) {
            list.add(new ActionFrame(f));
        }
        ActionFrame[] cons = new ActionFrame[list.size()];
        list.toArray(cons);
        return cons;
    }

    public String getUpdateLag() {
        DomainAttribute a = this.getAttribute(ActionFrame.ATTR_LIST_ATTRS, "update-lag");
        String s = a.getDefaultValue();
        return s;
    }

    public String getUpdateSpan() {
        DomainAttribute a = this.getAttribute(ActionFrame.ATTR_LIST_ATTRS, "update-span");
        String s = a.getDefaultValue();
        return s;
    }

    public boolean isActivated() {
        DomainAttribute a = this.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "activated");
        Boolean b = Boolean.valueOf(a.getDefaultValue());
        return b.booleanValue();
    }

    public void removeCondition(ConditionFrame cond) {
        super.removeChildFrame(INST_LIST_COND, cond.getName());
    }

    public void removeConsequence(ActionFrame cons) {
        super.removeChildFrame(INST_LIST_CONS, cons.getName());
    }

    public void setActivated(boolean b) {
        DomainAttribute a = new DomainAttribute("activated", AttributeType.STRING);
        a.setDefault(String.valueOf(b));
        addOrSetAttribute(ATTR_LIST_ATTRS, a);
    }

    public void setConditionFrame(ConditionFrame cond) {
        super.addOrSetChildFrame(UserRuleFrame.INST_LIST_COND, cond);
    }

    public void setConsequence(ActionFrame cons) {
        super.addOrSetChildFrame(UserRuleFrame.INST_LIST_CONS, cons);
    }

    public void setUpdateLag(String s) {
        DomainAttribute a = new DomainAttribute("update-lag", AttributeType.STRING);
        a.setDefault(String.valueOf(s));
        addOrSetAttribute(ATTR_LIST_ATTRS, a);
    }

    public void setUpdateSpan(String s) {
        DomainAttribute a = new DomainAttribute("update-span", AttributeType.STRING);
        a.setDefault(String.valueOf(s));
        addOrSetAttribute(ATTR_LIST_ATTRS, a);
    }

}
