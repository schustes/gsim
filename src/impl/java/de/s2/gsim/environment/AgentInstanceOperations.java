package de.s2.gsim.environment;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.s2.gsim.objects.attribute.Attribute;

public class AgentInstanceOperations {

    private EntitiesContainer container;

    private AgentClassOperations agentClassOperations;

    public AgentInstanceOperations(EntitiesContainer container) {
        this.container = container;
    }

    public GenericAgent addAgentRule(GenericAgent agent, UserRule f) {
        GenericAgent here = this.findGenericAgent(agent.getName());

        if (!(f instanceof RLRule)) {
            here.getBehaviour().addRule(f.clone());
        } else {
            here.getBehaviour().addRLRule((RLRule) f.clone());
        }

        return (GenericAgent) here.clone();
    }

    public GenericAgent addChildInstance(GenericAgent a, Path<TypedList<Instance>> path, Instance child) {
        GenericAgent here = findGenericAgent(a.getName());
        here.addChildInstance(path, child);
        return (GenericAgent) here.clone();
    }

    public GenericAgent changeAgentBehaviour(GenericAgent c, BehaviourDef b) {
        GenericAgent here = findGenericAgent(c.getName());
        here.setBehaviour(b);
        return (GenericAgent) here.clone();
    }

    public GenericAgent getAgent(String name) {
        GenericAgent a = findGenericAgent(name);
        if (a == null) {
            return null;
        }
        return (GenericAgent) a.clone();
    }

    public List<String> getAgentNames() {
        return container.getAgents().parallelStream().map(a -> a.getName()).collect(Collectors.toList());
    }

    public List<GenericAgent> getAgents(String parent, int offset, int count) {
        return this.container.getAgents().stream().skip(offset).limit(count).map(a -> a.clone()).collect(Collectors.toList());
    }

    public List<GenericAgent> getGenericAgents() {
        return this.container.getAgents().parallelStream().map(a -> a.clone()).collect(Collectors.toList());
    }

    public int getTotalAgentCount() {
        return this.container.getAgents().size();
    }

    public GenericAgent instanciateAgentWithNormalDistributedAttributes(GenericAgentClass cls, String name, double svar) {
        GenericAgent a = new GenericAgent(name, cls);
        a = Generator.randomiseAttributeValues(a, svar, Generator.Method.Uniform);
        container.getAgents().add(a);
        return (GenericAgent) a.clone();
    }

    public GenericAgent instanciateAgentWithUniformDistributedAttributes(GenericAgentClass cls, String name) {
        GenericAgent a = new GenericAgent(name, cls);
        a = Generator.randomiseAttributeValues(a, 0, Generator.Method.Normal);
        container.getAgents().add(a);
        return (GenericAgent) a.clone();
    }

    public List<GenericAgent> instanciateAgentsWithUniformDistributedAttributes(GenericAgentClass parent, Optional<String> prefix, int method,
            double svar,
            int count) {

        List<GenericAgent> result = instanciateAgents(parent, prefix, count,
                (name) -> instanciateAgentWithUniformDistributedAttributes(parent, name));

        Collections.shuffle(result);

        return result;

    }

    public List<GenericAgent> instanciateAgentsWithNormalDistributedAttributes(GenericAgentClass parent, Optional<String> prefix, int method,
            double svar,
            int count) {

        List<GenericAgent> result = instanciateAgents(parent, prefix, count,
                (name) -> instanciateAgentWithNormalDistributedAttributes(parent, name, svar));

        Collections.shuffle(result);

        return result;
    }

    /**
     * Creates an agent using a function for generating agents (e.g. using generator methods for specific attribute value distributions).
     * 
     * @param template the agent class to generate the instance from
     * @param prefix optional name prefix
     * @param count
     * @param func a function that generates a new agent with the passed name
     * @return
     */
    private List<GenericAgent> instanciateAgents(GenericAgentClass template, Optional<String> prefix, int count,
            Function<String, GenericAgent> func) {

        List<GenericAgent> result = new ArrayList<>();
        int counter = 0;
        while (counter < count) {
            counter++;
            String name;
            try {
                name = prefix.orElse("agent") + "-" + counter + "(" + java.net.InetAddress.getLocalHost().getCanonicalHostName() + ")";
            } catch (UnknownHostException e) {
                name = prefix.orElse("agent") + "-" + counter;
            }
            result.add(func.apply(name));
        }

        Collections.shuffle(result);

        return result;
    }

    public GenericAgent modifyAgentAttribute(GenericAgent inst, String[] path, Attribute att) {

        GenericAgent agent = findGenericAgent(inst.getName());

        if (agent.resolveName(path) == null) {
            agent.setAttribute(att);
        }

        UnitOperations.setChildAttribute(agent, path, att);
        agent.setDirty(true);
        agents.add(agent);
        return (GenericAgent) agent.clone();
    }


    public GenericAgent removeChildInstance(GenericAgent owner, String[] path, String child) {
        GenericAgent here = findGenericAgent(owner.getName());
        UnitOperations.removeChildInstance(here, path, child);
        agents.add(here);
        return (GenericAgent) here.clone();
    }

    public GenericAgentClass removeChildObjectList(GenericAgentClass cls, String listName) throws GSimDefException {
        GenericAgentClass here = this.findGenericAgentClass(cls);

        here.removeChildFrameList(listName);
        agentSubClasses.add(here);

        Iterator members = getInstancesOfClass(here).iterator();
        while (members.hasNext()) {
            GenericAgent c = (GenericAgent) members.next();
            c.setFrame(here);
            c.removeDeclaredChildInstanceList(listName);
            agents.add(c);
        }

        ListIterator<GenericAgentClass> iter = agentSubClasses.listIterator();

        while (iter.hasNext()) {
            GenericAgentClass c = iter.next();
            if (c.isSuccessor(here.getTypeName())) {
                c.replaceAncestor(here);
                c.removeChildFrameList(listName);
                iter.set(c);
                members = getInstancesOfClass(c).iterator();
                while (members.hasNext()) {
                    GenericAgent cc = (GenericAgent) members.next();
                    cc.setFrame(c);
                    cc.removeDeclaredChildInstanceList(listName);
                    agents.add(cc);
                }
            }
        }

        return (GenericAgentClass) here.clone();

    }

    public void removeGenericAgent(GenericAgent agent) {
        Iterator iter = agents.iterator();
        while (iter.hasNext()) {
            GenericAgent a = (GenericAgent) iter.next();
            if (agent.getName().equals(a.getName())) {
                iter.remove();
                removed.add(a);
            }
        }
    }



    public GenericAgentClass setActivatedStatus(GenericAgentClass cls, String ruleName, boolean status) {

        GenericAgentClass here = this.findGenericAgentClass(cls);
        BehaviourFrame pb1 = here.getBehaviour();
        UserRuleFrame ur1 = pb1.getRule(ruleName);
        if (ur1 == null) {
            ur1 = pb1.getRLRule(ruleName);
            ur1.setActivated(status);
            pb1.addRLRule((RLRuleFrame) ur1);
        } else {
            ur1.setActivated(status);
            pb1.addOrSetRule(ur1);
        }
        here.setBehaviour(pb1);
        here.setDirty(true);

        agentSubClasses.add(here);

        ListIterator<Instance> iter = getInstancesOfClass(here).listIterator();
        while (iter.hasNext()) {
            GenericAgent p = (GenericAgent) iter.next();
            p.setDirty(true);
            BehaviourDef pb = p.getBehaviour();
            UserRule ur = pb.getRule(ruleName);
            if (ur == null) {
                ur = pb.getRLRule(ruleName);
                ur.setActivated(status);
                pb.addRLRule((RLRule) ur);
            } else {
                ur.setActivated(status);
                pb.addRule(ur);
            }
            p.setBehaviour(pb);
            iter.set(p);
        }

        ListIterator<GenericAgentClass> iter2 = agentSubClasses.listIterator();
        while (iter.hasNext()) {

            GenericAgentClass p = iter2.next();
            p.setDirty(true);
            if (p.isSuccessor(here.getTypeName())) {
                BehaviourFrame pb = p.getBehaviour();
                UserRuleFrame ur = pb.getRule(ruleName);
                if (ur == null) {
                    ur = pb.getRLRule(ruleName);
                    ur.setActivated(status);
                    pb.addRLRule((RLRuleFrame) ur);
                } else {
                    ur.setActivated(status);
                    pb.addOrSetRule(ur);
                }
                p.setBehaviour(pb);
                iter2.set(p);
                ListIterator<Instance> successorMembers = getInstancesOfClass(p).listIterator();
                while (successorMembers.hasNext()) {
                    GenericAgent a = (GenericAgent) iter.next();
                    p.setDirty(true);
                    BehaviourDef beh = a.getBehaviour();
                    UserRule ur2 = beh.getRule(ruleName);
                    if (ur2 == null) {
                        ur2 = beh.getRLRule(ruleName);
                        ur2.setActivated(status);
                        beh.addRule(ur2);
                    } else {
                        ur2.setActivated(status);
                        beh.addRule(ur2);
                    }
                    a.setBehaviour(beh);
                    successorMembers.set(a);
                }
            }
        }
        return (GenericAgentClass) here.clone();
    }

    public void setAgent(GenericAgent inst) throws GSimDefException {

        ListIterator<GenericAgent> iter = agents.listIterator();
        while (iter.hasNext()) {
            GenericAgent agent = iter.next();
            if (inst.getName().equals(agent.getName())) {
                if (!agent.getDefinition().getTypeName().equals(inst.getDefinition().getTypeName())) {
                    throw new GSimDefException("Agent " + inst.getName() + " must be of type " + agent.getDefinition().getTypeName() + ".");
                }
                iter.set(inst);
            }

        }

    }

    protected void createBehaviourClasses() {

        BehaviourFrame x = new BehaviourFrame("Behaviour", agentClass.getBehaviour(), de.s2.gsim.def.EntityConstants.TYPE_BEHAVIOUR);
        behaviourClass = x;
        behaviourClasses.add(x);

        x.setMutable(true);
        x.setSystem(false);

        Iterator iter = agentSubClasses.iterator();
        while (iter.hasNext()) {
            GenericAgentClass c = (GenericAgentClass) iter.next();
            BehaviourFrame f = new BehaviourFrame(c.getBehaviour(), EntityConstants.TYPE_BEHAVIOUR);
            f.replaceAncestor(x);
            behaviourClasses.add(f);
        }

    }


    protected GenericAgent findGenericAgent(String extern) {
        return container.getAgents().parallelStream().filter(a -> a.getName().equals(extern)).findAny().get();
    }

}
