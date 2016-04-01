package de.s2.gsim.api.objects.impl;


import de.s2.gsim.GSimException;
import de.s2.gsim.environment.ActionFrame;
import de.s2.gsim.environment.Unit;

public class ActionClass2 implements de.s2.gsim.objects.Action, UnitWrapper {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private BehaviourClass owner;

    private ActionFrame real;

    public ActionClass2(BehaviourClass owner, ActionFrame real) {
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
        real.clearObjectClassParams();
        owner.addOrSetAction(this);
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
        return real.getTypeName();
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
