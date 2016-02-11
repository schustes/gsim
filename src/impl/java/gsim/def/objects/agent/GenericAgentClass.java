package gsim.def.objects.agent;

import java.util.ArrayList;
import java.util.Iterator;

import de.s2.gsim.objects.attribute.DomainAttribute;
import gsim.def.EntityConstants;
import gsim.def.objects.Frame;
import gsim.def.objects.behaviour.ActionFrame;
import gsim.def.objects.behaviour.RLRuleFrame;
import gsim.def.objects.behaviour.UserRuleFrame;

public class GenericAgentClass extends Frame {

    public static final String NAME = "Generic Agent";

    public static final String PROPERTIES_LIST = "properties";

    public final static long serialVersionUID = -6049153690484631715L;

    private BehaviourFrame behaviour = null;

    /**
     * Constructor for a top-level generic agent.
     * 
     * @param id
     *            int
     */
    public GenericAgentClass() {
        super(NAME, "generic");
        init();
    }

    /**
     * Copy constructor.
     * 
     * @param f
     *            Frame
     */
    public GenericAgentClass(Frame f) {
        super(f);
        behaviour = new BehaviourFrame(f.getTypeName() + "-" + behaviour, EntityConstants.TYPE_BEHAVIOUR);
    }

    /**
     * Copy constructor.
     * 
     * @param cloneFrom
     *            GenericAgentClass
     */
    public GenericAgentClass(GenericAgentClass cloneFrom) {
        super(cloneFrom);
        // this.behaviour = new BehaviourFrame(cloneFrom.getBehaviour());
        behaviour = (BehaviourFrame) cloneFrom.getBehaviour().clone();
    }

    public GenericAgentClass(GenericAgentClass top, GenericAgentClass otherRole, String name) {

        super(new Frame[] { otherRole }, name, EntityConstants.TYPE_GENERIC);

        Iterator iter = top.parents.values().iterator();
        while (iter.hasNext()) {
            Frame p = (Frame) iter.next();
            parents.put(p.getTypeName(), p);
        }
        iter = top.attributeLists.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            attributeLists.put(key, top.attributeLists.get(key));
        }
        iter = top.objectLists.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            objectLists.put(key, top.objectLists.get(key));
        }

        String behaviourName = otherRole.getTypeName() + " & " + top.getTypeName() + "-behaviour";
        BehaviourFrame old = top.getBehaviour();
        Frame[] oldParents = top.getBehaviour().getParentFrames();
        BehaviourFrame[] allParents = new BehaviourFrame[oldParents.length + 1];
        for (int i = 0; i < oldParents.length; i++) {
            allParents[i] = new BehaviourFrame(oldParents[i]);
        }
        allParents[allParents.length - 1] = otherRole.getBehaviour();

        behaviour = (BehaviourFrame) new BehaviourFrame(behaviourName, allParents, "behaviour").clone();

        behaviour.setDeleteUnusedAfter(old.getDeleteUnusedAfter());
        behaviour.setMaxDepth(old.getMaxDepth());
        behaviour.setMaxNodes(old.getMaxNodes());
        behaviour.setStateUpdateInterval(old.getStateUpdateInterval());
        behaviour.setTraversalMode(old.getTraversalMode());
        for (ActionFrame a : old.getDeclaredAvailableActions()) {
            behaviour.addAction(a);
        }
        for (RLRuleFrame r : old.getDeclaredRLRules()) {
            behaviour.addRLRule(r);
        }
        for (UserRuleFrame r : old.getDeclaredRules()) {
            behaviour.addRule(r);
        }

        // this.behaviour = new BehaviourFrame(behaviourName, new BehaviourFrame[] {
        // top.getBehaviour(), otherRole.getBehaviour() }, "behaviour");

    }

    /**
     * Inheritance constructor.
     * 
     * @param parent
     *            GenericAgentClass
     * @param name
     *            String
     * @param id
     *            int
     */
    public GenericAgentClass(GenericAgentClass parent, String name) {
        super(new Frame[] { parent }, name, "generic");
        // String name, BehaviourFrame[] parents, String category
        behaviour = (BehaviourFrame) new BehaviourFrame(getTypeName() + "-behaviour", new BehaviourFrame[] { parent.getBehaviour() },
                BehaviourFrame.CATEGORY).clone();
    }

    /**
     * Inheritance constructor.
     * 
     * @param parent
     *            GenericAgentClass
     * @param name
     *            String
     * @param id
     *            int
     */
    public GenericAgentClass(GenericAgentClass[] parents, String name) {
        super(parents, name, "generic");
        BehaviourFrame[] behaviours = new BehaviourFrame[parents.length];

        for (int i = 0; i < parents.length; i++) {
            behaviours[i] = parents[i].getBehaviour();
        }
        // String name, BehaviourFrame[] parents, String category
        behaviour = (BehaviourFrame) new BehaviourFrame(getTypeName() + "-behaviour", behaviours, "behaviour").clone();

    }

    /**
     * Constructor for a top-level generic agent.
     * 
     * @param id
     *            int
     */
    public GenericAgentClass(String name) {
        super(name, "generic");
        init();
    }

    @Override
    public Object clone() {
        Frame f = (Frame) super.clone();
        GenericAgentClass c = new GenericAgentClass(f);
        c.setBehaviour(getBehaviour());
        return c;
    }

    public BehaviourFrame getBehaviour() {
        return behaviour;
    }

    public DomainAttribute[] getProperties() {
        return getAttributes(PROPERTIES_LIST);
    }

    public void setBehaviour(BehaviourFrame f) {
        behaviour = f;
    }

    @Override
    public void setTypeName(String s) {
        super.typeName = s;
    }

    /**
     * Sets some initial values for convenience.
     */
    private void init() {
        attributeLists.put(PROPERTIES_LIST, new ArrayList());

        // DomainAttribute a = new DomainAttribute("maximum-condition-splits",
        // AttributeConstants.NUMERICAL);
        // a.setDefault("1");
        // addOrSetAttribute(PROPERTIES_LIST, a);

        behaviour = new BehaviourFrame(getTypeName() + "-behaviour", EntityConstants.TYPE_BEHAVIOUR);

    }

}
