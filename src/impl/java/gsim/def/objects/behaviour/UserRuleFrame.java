package gsim.def.objects.behaviour;

import java.util.ArrayList;

import de.s2.gsim.objects.attribute.AttributeConstants;
import de.s2.gsim.objects.attribute.DomainAttribute;
import gsim.def.objects.Frame;

public class UserRuleFrame extends Frame {

    public static final String ATTR_LIST_ATTRS = "attributes";

    public static final String CATEGORY = "rule";

    public static UserRuleFrame DEFINITION = new UserRuleFrame("rule-definition");

    public static final String INST_LIST_COND = "conditions";

    public static final String INST_LIST_CONS = "consequences";

    static final long serialVersionUID = 4875468082081888178L;

    public UserRuleFrame(Frame f) {
        super(f);
        Frame f1 = new ConditionFrame("{all-conditions}", "=", "don't care");
        f1.setSystem(true);
        f1.setMutable(false);
        Frame f2 = new ActionFrame("{all-actions}", "action");
        f2.setSystem(false);
        f2.setMutable(false);
        addChildFrame("conditions", f1);
        addChildFrame("consequences", f2);

        if (this.getAttribute(ATTR_LIST_ATTRS, "activated") == null) {
            DomainAttribute a = new DomainAttribute("activated", AttributeConstants.STRING);
            a.setDefault("true");
            addOrSetAttribute(ATTR_LIST_ATTRS, a);
        }

    }

    /**
     * Inheritance constructor
     */
    public UserRuleFrame(Frame[] parents, String name, String category) {
        super(parents, name, category);
    }

    public UserRuleFrame(String name) {

        super(name, CATEGORY);

        Frame f2 = new Frame("{all-actions}", "action");
        f2.setSystem(false);
        f2.setMutable(false);
        addChildFrame("consequences", f2);

        DomainAttribute a = new DomainAttribute("activated", AttributeConstants.STRING);
        a.setDefault("true");
        addOrSetAttribute(ATTR_LIST_ATTRS, a);

        DomainAttribute c = new DomainAttribute("update-lag", AttributeConstants.STRING);
        c.setDefault("0");
        addOrSetAttribute(ATTR_LIST_ATTRS, c);

    }

    public void addCondition(ConditionFrame cond) {

        // resolve possible frame-references to $'s:

        super.addChildFrame(INST_LIST_COND, cond);

    }

    public void addConsequence(ActionFrame cons) {
        super.addChildFrame(INST_LIST_CONS, cons);
    }

    public ConditionFrame createCondition(String var, String op, String val) {
        ConditionFrame f = new ConditionFrame(var, op, val);
        return f;
    }

    public ConditionFrame[] getConditions() {
        Frame[] inst = getChildFrames(UserRuleFrame.INST_LIST_COND);
        ArrayList list = new ArrayList();
        for (int i = 0; i < inst.length; i++) {
            if (!inst[i].getTypeName().startsWith("{")) {
                list.add(new ConditionFrame(inst[i]));
            }
        }
        ConditionFrame[] cond = new ConditionFrame[list.size()];
        list.toArray(cond);
        return cond;
    }

    public ActionFrame[] getConsequences() {
        Frame[] inst = getChildFrames(UserRuleFrame.INST_LIST_CONS);
        java.util.ArrayList list = new java.util.ArrayList();
        for (int i = 0; i < inst.length; i++) {
            list.add(new ActionFrame(inst[i]));
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
        super.removeChildFrame(INST_LIST_COND, cond.getTypeName());
    }

    public void removeConsequence(ActionFrame cons) {
        super.removeChildFrame(INST_LIST_CONS, cons.getTypeName());
    }

    public void setActivated(boolean b) {
        DomainAttribute a = new DomainAttribute("activated", AttributeConstants.STRING);
        a.setDefault(String.valueOf(b));
        addOrSetAttribute(ATTR_LIST_ATTRS, a);
    }

    public void setConditionFrame(ConditionFrame cond) {
        super.addChildFrame(UserRuleFrame.INST_LIST_COND, cond);
    }

    public void setConsequence(ActionFrame cons) {
        super.addChildFrame(UserRuleFrame.INST_LIST_CONS, cons);
    }

    public void setUpdateLag(String s) {
        DomainAttribute a = new DomainAttribute("update-lag", AttributeConstants.STRING);
        a.setDefault(String.valueOf(s));
        addOrSetAttribute(ATTR_LIST_ATTRS, a);
    }

    public void setUpdateSpan(String s) {
        DomainAttribute a = new DomainAttribute("update-span", AttributeConstants.STRING);
        a.setDefault(String.valueOf(s));
        addOrSetAttribute(ATTR_LIST_ATTRS, a);
    }

}
