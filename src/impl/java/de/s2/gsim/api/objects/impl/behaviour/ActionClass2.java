package de.s2.gsim.api.objects.impl.behaviour;


import de.s2.gsim.GSimException;
import de.s2.gsim.api.objects.impl.UnitWrapper;
import de.s2.gsim.environment.ActionFrame;
import de.s2.gsim.environment.Unit;

public class ActionClass2 implements de.s2.gsim.objects.Action, UnitWrapper {

    public static final long serialVersionUID = 1L;

    private BehaviourClass owner;

    private ActionFrame real;

    public ActionClass2(BehaviourClass owner, ActionFrame real) {
        this.real = real;
        this.owner = owner;
    }

    private ActionFrame getReal() {
    	this.real = (ActionFrame)((UnitWrapper)owner.getAction(real.getName())).toUnit();
    	return this.real;
    }
    
    @Override
    public void addObjectClassParam(String objectClassName) throws GSimException {
    	getReal().addObjectClassParam(objectClassName, null);
        owner.addOrSetAction(this);
    }

    @Override
    public void clearObjectClassParams() throws GSimException {
    	getReal().clearObjectClassParams();
        owner.addOrSetAction(this);
    }

    @Override
    public String getActionClassName() {
        return getReal().getClassName();
    }

    public String getFilterExpression(String objParam) {
        return getReal().getFilterExpression(objParam);
    }

    @Override
    public String getName() {
        return getReal().getName();
    }

    @Override
    public String[] getObjectClassParams() {
        return getReal().getObjectClassParams();
    }

    public double getSalience() {
        return getReal().getSalience();
    }

    @Override
    public boolean hasObjectParameter() {
        return getReal().hasObjectParameter();
    }

    @Override
    public void removeObjectClassParam(String name) throws GSimException {
    	getReal().removeObjectClassParam(name);
        owner.addOrSetAction(this);
    }

    @Override
    public void setActionClassName(String className) throws GSimException {
    	getReal().setClassName(className);
        owner.addOrSetAction(this);
    }

    public void setSalience(int s) throws GSimException {
    	getReal().setSalience(s);
        owner.addOrSetAction(this);
    }

    @Override
    public Unit<?, ?> toUnit() {
        return real;
    }

}
