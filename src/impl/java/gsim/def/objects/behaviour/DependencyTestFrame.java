package gsim.def.objects.behaviour;

import de.s2.gsim.objects.attribute.AttributeConstants;
import de.s2.gsim.objects.attribute.DomainAttribute;
import gsim.def.objects.Frame;

public class DependencyTestFrame extends Frame {

    public static DependencyTestFrame DEFINITION = new DependencyTestFrame("test-definition");
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DependencyTestFrame(Frame f) {
        super(f);
    }

    public DependencyTestFrame(String testName) {
        super(testName, "dependency-test");
    }

    public DependencyTestFrame(String testName, String actionType, String objType, String time, String testExpression, String forA, String forO,
            String forT) {

        this(testName);

        DomainAttribute a1 = new DomainAttribute("action-type", AttributeConstants.STRING);
        a1.setDefault(actionType);
        addOrSetAttribute("list", a1);

        DomainAttribute a2 = new DomainAttribute("object-type", AttributeConstants.STRING);
        a2.setDefault(objType);
        addOrSetAttribute("list", a2);

        DomainAttribute a3 = new DomainAttribute("time", AttributeConstants.STRING);
        a3.setDefault(time);
        addOrSetAttribute("list", a3);

        DomainAttribute a4 = new DomainAttribute("for-action-type", AttributeConstants.STRING);
        a4.setDefault(forA);
        addOrSetAttribute("list", a4);

        DomainAttribute a5 = new DomainAttribute("for-object-type", AttributeConstants.STRING);
        a5.setDefault(forO);
        addOrSetAttribute("list", a5);

        DomainAttribute a6 = new DomainAttribute("for-time", AttributeConstants.STRING);
        a6.setDefault(forT);
        addOrSetAttribute("list", a6);

        DomainAttribute a7 = new DomainAttribute("expression", AttributeConstants.STRING);
        a7.setDefault(testExpression);
        addOrSetAttribute("list", a7);

        DomainAttribute a8 = new DomainAttribute("wait", AttributeConstants.STRING);
        a8.setDefault("0");
        addOrSetAttribute("list", a8);

    }

    public String getActionType() {
        DomainAttribute a = this.getAttribute("list", "action-type");
        if (a != null) {
            return a.getDefaultValue();
        }
        return null;
    }

    public String getForActionType() {
        DomainAttribute a = this.getAttribute("list", "for-action-type");
        if (a != null) {
            return a.getDefaultValue();
        }
        return null;
    }

    public String getForObjectType() {
        DomainAttribute a = this.getAttribute("list", "for-object-type");
        if (a != null) {
            return a.getDefaultValue();
        }
        return null;
    }

    public String getForTime() {
        DomainAttribute a = this.getAttribute("list", "for-time");
        if (a != null) {
            return a.getDefaultValue();
        }
        return null;
    }

    public String getObjectType() {
        DomainAttribute a = this.getAttribute("list", "object-type");
        if (a != null) {
            return a.getDefaultValue();
        }
        return null;
    }

    public String getTestExpression() {
        DomainAttribute a = this.getAttribute("list", "expression");
        if (a != null) {
            return a.getDefaultValue();
        }
        return null;
    }

    public String getTime() {
        DomainAttribute a = this.getAttribute("list", "time");
        if (a != null) {
            return a.getDefaultValue();
        }
        return null;
    }

    public String getWait() {
        DomainAttribute a = this.getAttribute("list", "wait");
        if (a != null) {
            return a.getDefaultValue();
        }
        return null;
    }

}
