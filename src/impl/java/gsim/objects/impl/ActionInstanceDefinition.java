package gsim.objects.impl;

import de.s2.gsim.core.GSimException;
import de.s2.gsim.objects.Behaviour;
import gsim.def.objects.Unit;
import gsim.def.objects.behaviour.ActionDef;

public class ActionInstanceDefinition implements de.s2.gsim.objects.Action, UnitWrapper {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Behaviour owner;

    private ActionDef real;

    public ActionInstanceDefinition(Behaviour owner, ActionDef real) {
        this.real = real;
        this.owner = owner;
    }

    @Override
    public void addObjectClassParam(String objectClassName) throws GSimException {
        real.addObjectClassParam(objectClassName, null);
        owner.addOrSetAction(this);
    }

    @Override
    public void clearObjectClassParams() throws GSimException {
        throw new GSimException("Not implemented");
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
    public void removeObjectClassParam(String name) throws GSimException {
        real.removeObjectClassParam(name);
        owner.addOrSetAction(this);
    }

    @Override
    public void setActionClassName(String className) throws GSimException {
        real.setClassName(className);
        owner.addOrSetAction(this);
    }

    public void setSalience(int s) throws GSimException {
        real.setSalience(s);
        owner.addOrSetAction(this);
    }

    @Override
    public Unit toUnit() {
        return real;
    }

}
