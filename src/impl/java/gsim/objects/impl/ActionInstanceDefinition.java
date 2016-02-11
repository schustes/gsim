package gsim.objects.impl;

import de.s2.gsim.objects.ActionIF;
import de.s2.gsim.objects.BehaviourIF;
import de.s2.gsim.objects.GSimObjectException;
import gsim.def.objects.Unit;
import gsim.def.objects.behaviour.Action;

public class ActionInstanceDefinition implements ActionIF, UnitWrapper {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private BehaviourIF owner;

    private Action real;

    public ActionInstanceDefinition(BehaviourIF owner, Action real) {
        this.real = real;
        this.owner = owner;
    }

    @Override
    public void addObjectClassParam(String objectClassName) throws GSimObjectException {
        real.addObjectClassParam(objectClassName, null);
        owner.addOrSetAction(this);
    }

    @Override
    public void clearObjectClassParams() throws GSimObjectException {
        throw new GSimObjectException("Not implemented");
    }

    @Override
    public String getActionClassName() {
        return real.getClassName();
    }

    public String getFilterExpression(String objParam) {
        return real.getFilterExpression(objParam);
    }

    @Override
    public String getName() {
        return real.getName();
    }

    @Override
    public String[] getObjectClassParams() {
        return real.getObjectClassParams();
    }

    public double getSalience() {
        return real.getSalience();
    }

    @Override
    public boolean hasObjectParameter() {
        return real.hasObjectParameter();
    }

    @Override
    public void removeObjectClassParam(String name) throws GSimObjectException {
        real.removeObjectClassParam(name);
        owner.addOrSetAction(this);
    }

    @Override
    public void setActionClassName(String className) throws GSimObjectException {
        real.setClassName(className);
        owner.addOrSetAction(this);
    }

    public void setSalience(int s) throws GSimObjectException {
        real.setSalience(s);
        owner.addOrSetAction(this);
    }

    @Override
    public Unit toUnit() {
        return real;
    }

}
