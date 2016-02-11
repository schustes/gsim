package gsim.def.objects.agent;

import de.s2.gsim.objects.attribute.Attribute;
import gsim.def.objects.Instance;

/**
 * Provides a basic implementation of the Agent class. This agent type can be used for anything in a model, but some logic will have to be provided.
 */
public class GenericAgent extends Instance {

    public static final long serialVersionUID = 101191270000954720L;

    protected Behaviour behaviour;

    /**
     * Copy constructor.
     * 
     * @param in
     *            GenericAgent
     */
    public GenericAgent(GenericAgent in) {
        super(in);
        behaviour = new Behaviour((Behaviour) in.getBehaviour().clone());
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
        behaviour = new Behaviour(cls.getBehaviour());
    }

    protected GenericAgent() {
        super();
    }

    @Override
    public Object clone() {
        GenericAgent a = new GenericAgent(this);
        return a;
    }

    public Behaviour getBehaviour() {
        return behaviour;
    }

    public Attribute[] getProperties() {
        return getAttributes("properties");
    }

    public void setBehaviour(Behaviour b) {
        behaviour = b;
    }

    protected void deleteUnneccessaryFrames() {
        behaviour.setFrame(null);
    }

}
