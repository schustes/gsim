package de.s2.gsim.def.objects.behaviour;

import de.s2.gsim.def.objects.FrameOLD;
import de.s2.gsim.def.objects.InstanceOLD;
import de.s2.gsim.objects.attribute.StringAttribute;

public class DependencyTest extends InstanceOLD {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DependencyTest(FrameOLD f) {
        super(f.getTypeName(), f);
    }

    public DependencyTest(InstanceOLD f) {
        super(f);
    }

    public String getActionType() {
        StringAttribute a = (StringAttribute) this.getAttribute("list", "action-type");
        if (a != null) {
            return a.getValue();
        }
        return null;
    }

    public String getForActionType() {
        StringAttribute a = (StringAttribute) this.getAttribute("list", "for-action-type");
        if (a != null) {
            return a.getValue();
        }
        return null;
    }

    public String getForObjectType() {
        StringAttribute a = (StringAttribute) this.getAttribute("list", "for-object-type");
        if (a != null) {
            return a.getValue();
        }
        return null;
    }

    public String getForTime() {
        StringAttribute a = (StringAttribute) this.getAttribute("list", "for-time");
        if (a != null) {
            return a.getValue();
        }
        return null;
    }

    public String getObjectType() {
        StringAttribute a = (StringAttribute) this.getAttribute("list", "object-type");
        if (a != null) {
            return a.getValue();
        }
        return null;
    }

    public String getTestExpression() {
        StringAttribute a = (StringAttribute) this.getAttribute("list", "expression");
        if (a != null) {
            return a.getValue();
        }
        return null;
    }

    public String getTime() {
        StringAttribute a = (StringAttribute) this.getAttribute("list", "time");
        if (a != null) {
            return a.getValue();
        }
        return null;
    }

}
