package de.s2.gsim.api.objects.impl;


import de.s2.gsim.GSimException;
import de.s2.gsim.def.objects.UnitOLD;
import de.s2.gsim.def.objects.behaviour.ActionDef;
import de.s2.gsim.objects.Rule;

public class ActionInstance implements de.s2.gsim.objects.Action, UnitWrapper {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Rule owner;

    private ActionDef real;

    public ActionInstance(Rule owner, ActionDef real) {
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
    public UnitOLD toUnit() {
        return real;
    }

}
