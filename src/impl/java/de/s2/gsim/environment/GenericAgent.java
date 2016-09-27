package de.s2.gsim.environment;

import java.util.List;

import de.s2.gsim.objects.attribute.Attribute;

/**
 * Provides a basic implementation of the Agent class. This agent type can be used for anything in a model, but some logic will have to be provided.
 */
public class GenericAgent extends Instance {

    public static final long serialVersionUID = 101191270000954720L;

    protected BehaviourDef behaviour;

    /**
     * Copy constructor.
     * 
     * @param inst
     */
    private GenericAgent(GenericAgent inst) {
    	super(inst);
        behaviour = new BehaviourDef((BehaviourDef) inst.getBehaviour().clone());    	
    }
    
    public static GenericAgent from(GenericAgent copy) {
    	return new GenericAgent(copy);
    }
    
    /**
     * Inheritance constructor.
     * 
     * @param name
     *            String
     * @param cls
     *            GenericAgentClass
     * @param id
     *            int
     */
    public GenericAgent(String name, GenericAgentClass cls) {
        super(name, cls);
        this.instanciate(cls);
        behaviour = new BehaviourDef(cls.getBehaviour());
    }

    @Override
    public GenericAgent clone() {
        return new GenericAgent(this);
    }

    public BehaviourDef getBehaviour() {
        return behaviour;
    }

    public List<Attribute> getProperties() {
        return getAttributes("properties");
    }

    public void setBehaviour(BehaviourDef b) {
        behaviour = b;
    }

    protected void deleteUnneccessaryFrames() {
        behaviour.setFrame(null);
    }


}
