package de.s2.gsim.api.objects.impl.behaviour;

import de.s2.gsim.GSimException;
import de.s2.gsim.api.objects.impl.UnitWrapper;
import de.s2.gsim.environment.ActionFrame;
import de.s2.gsim.environment.Unit;

public class ActionClass implements de.s2.gsim.objects.Action, UnitWrapper {

    private RuleClass owner;

    private ActionFrame real;

    public ActionClass(RuleClass owner, ActionFrame real) {
        this.real = real;
        this.owner = owner;
    }

    @Override
    public void addObjectClassParam(String objectClassName) throws GSimException {
        real.addObjectClassParam(objectClassName, null);
        owner.addOrSetConsequent(this);
    }

    @Override
    public void clearObjectClassParams() throws GSimException {
        real.clearObjectClassParams();
        owner.addOrSetConsequent(this);
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
        owner.addOrSetConsequent(this);
    }

    @Override
    public void setActionClassName(String className) throws GSimException {
        real.setClassName(className);
        owner.addOrSetConsequent(this);
    }

    public void setSalience(int s) throws GSimException {
        real.setSalience(s);
        owner.addOrSetConsequent(this);
    }

    @Override
    public Unit<?, ?> toUnit() {
        return real;
    }

}
