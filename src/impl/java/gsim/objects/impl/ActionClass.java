package gsim.objects.impl;

import de.s2.gsim.objects.ActionIF;
import de.s2.gsim.objects.GSimObjectException;
import gsim.def.objects.Unit;
import gsim.def.objects.behaviour.ActionFrame;

public class ActionClass implements ActionIF, UnitWrapper {

    private static final long serialVersionUID = 1L;

    private RuleClass owner;

    private ActionFrame real;

    public ActionClass(RuleClass owner, ActionFrame real) {
        this.real = real;
        this.owner = owner;
    }

    @Override
    public void addObjectClassParam(String objectClassName) throws GSimObjectException {
        real.addObjectClassParam(objectClassName, null);
        owner.addOrSetConsequent(this);
    }

    @Override
    public void clearObjectClassParams() throws GSimObjectException {
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
    public void removeObjectClassParam(String name) throws GSimObjectException {
        real.removeObjectClassParam(name);
        owner.addOrSetConsequent(this);
    }

    @Override
    public void setActionClassName(String className) throws GSimObjectException {
        real.setClassName(className);
        owner.addOrSetConsequent(this);
    }

    public void setSalience(int s) throws GSimObjectException {
        real.setSalience(s);
        owner.addOrSetConsequent(this);
    }

    @Override
    public Unit toUnit() {
        return real;
    }

}
