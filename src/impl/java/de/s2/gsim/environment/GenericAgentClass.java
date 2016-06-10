package de.s2.gsim.environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import de.s2.gsim.objects.attribute.DomainAttribute;

public class GenericAgentClass extends Frame {

    public static final String NAME = "Generic Agent";

    public static final String PROPERTIES_LIST = "properties";

    public final static long serialVersionUID = -6049153690484631715L;

    private BehaviourFrame behaviour = null;

    /**
     * Constructor for a top-level generic agent.
     */
    private GenericAgentClass(String name) {
    	super(name, Optional.of("generic"), true, false);
        getAttributeLists().put(PROPERTIES_LIST, new ArrayList<>());
        behaviour = BehaviourFrame.newBehaviour(getName() + "-behaviour");
    }

    /**
     * Constructor for a top-level generic agent.
     */
    public static GenericAgentClass baseGenericAgentClass() {
        GenericAgentClass cls = new GenericAgentClass(NAME);
        return cls;
    }

    /**
     * Constructor for a top-level generic agent.
     */
    public static GenericAgentClass baseGenericAgentClassWithName(String name) {
        GenericAgentClass cls = new GenericAgentClass(name);
        return cls;
    }
    
    
    /**
     * Inherit.
     */
    public static GenericAgentClass inherit(GenericAgentClass parent, String name) {
    	Frame f = Frame.inherit(Arrays.asList(parent), name, Optional.of("generic"));
    	GenericAgentClass cls = new GenericAgentClass(f);
        BehaviourFrame bf = BehaviourFrame.inherit(name + "-behaviour", Arrays.asList(parent.getBehaviour()));
    	cls.behaviour = bf.clone();
    	return cls;
    }

    /**
     * Inherit.
     */
    public static GenericAgentClass inherit(String name, GenericAgentClass... parents) {
    	Frame f = Frame.inherit(Arrays.asList(parents), name, Optional.of("generic"));
    	GenericAgentClass cls = new GenericAgentClass(f);
    	
    	List<BehaviourFrame> behaviours = Arrays.stream(parents).map(g->g.getBehaviour()).collect(Collectors.toList());
        BehaviourFrame bf = BehaviourFrame.inherit(name + "-behaviour", behaviours);
    	cls.behaviour = bf.clone();
    	
    	return cls;

    }

    

    public static GenericAgentClass copy(Frame f) {
    	Frame c = Frame.copy(f);
    	GenericAgentClass cls = new GenericAgentClass(c);
        cls.behaviour = BehaviourFrame.newBehaviour(f.getName() + "-behaviour");
        return cls;
    }

    /**
     * Copy constructor.
     */
    public static GenericAgentClass copy(GenericAgentClass cloneFrom) {
    	Frame c = Frame.copy(cloneFrom);
    	GenericAgentClass cls = new GenericAgentClass(c);
        cls.behaviour = (BehaviourFrame) cloneFrom.getBehaviour().clone();
        return cls;
    }

    private GenericAgentClass(Frame f) {
    	super(f);
    }

    /**
     * Creates an agent and extends it at the same time with the second. The difference to normal frame inheritance is that the new 
     * agent class contains both behaviours.
     * 
     * @param top base agent
     * @param otherRole agent with which to extend first one
     * @param name name of the new agent class
     * @return the agent class
     */
    public static GenericAgentClass copyFromAndExtendWith(GenericAgentClass top, GenericAgentClass otherRole, String name) {

    	Frame f = Frame.inherit(Arrays.asList(otherRole), name, Optional.of(EntityTypes.GENERIC.toString()));
    	GenericAgentClass cls = new GenericAgentClass(f);
        
    	for (Frame p: top.getParentFrames()) {
    		cls.parents.put(p.getName(), p);
    	}
    	for (Entry<String, List<DomainAttribute>> atts: top.getAttributeLists().entrySet()) {
    		cls.getAttributeLists().put(atts.getKey(), atts.getValue());
    	}
    	for (Entry<String, TypedList<Frame>> objs: top.getObjectLists().entrySet()) {
    		cls.getObjectLists().put(objs.getKey(), objs.getValue());
    	}
    	

        String behaviourName = otherRole.getName() + " & " + top.getName() + "-behaviour";
        BehaviourFrame old = top.getBehaviour();
        List<Frame> oldParents = top.getBehaviour().getParentFrames();
        List<BehaviourFrame> allParents = new ArrayList<BehaviourFrame>();
        for (Frame bf: oldParents) {
        	allParents.add(BehaviourFrame.copy(bf));
        }
        allParents.add(otherRole.getBehaviour());

        cls.behaviour = (BehaviourFrame) BehaviourFrame.inherit(behaviourName, allParents).clone();

        cls.behaviour.setDeleteUnusedAfter(old.getDeleteUnusedAfter());
        cls.behaviour.setMaxDepth(old.getMaxDepth());
        cls.behaviour.setMaxNodes(old.getMaxNodes());
        cls.behaviour.setStateUpdateInterval(old.getStateUpdateInterval());
        cls.behaviour.setTraversalMode(old.getTraversalMode());
        for (ActionFrame a : old.getDeclaredAvailableActions()) {
        	cls.behaviour.addAction(a);
        }
        for (RLRuleFrame r : old.getDeclaredRLRules()) {
        	cls.behaviour.addRLRule(r);
        }
        for (UserRuleFrame r : old.getDeclaredRules()) {
        	cls.behaviour.addRule(r);
        }
        
        return cls;

    }


    @Override
    public GenericAgentClass clone() {
        Frame f = (Frame) super.clone();
        GenericAgentClass c = new GenericAgentClass(f);
        c.setBehaviour(getBehaviour());
        return c;
    }

    public BehaviourFrame getBehaviour() {
        return behaviour;
    }

    public List<DomainAttribute> getProperties() {
        return getAttributes(PROPERTIES_LIST);
    }

    public void setBehaviour(BehaviourFrame f) {
        behaviour = f;
        this.setDirty(true);
    }


}
