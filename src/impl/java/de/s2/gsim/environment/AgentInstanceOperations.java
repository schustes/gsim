package de.s2.gsim.environment;

import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.DomainAttribute;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AgentInstanceOperations {

    private EntitiesContainer container;

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
        a = Generator.randomiseNormalDistributedAttributeValues(a, svar);
        container.addAgent(a);
        return (GenericAgent) a.clone();
    }

    public void randomiseNormalDistributedAttribute(GenericAgentClass cls, Path<DomainAttribute> attr, double mean, double svar) {
        Path<List<DomainAttribute>> attrList = Path.withoutLastAttributeOrObject(attr, Path.Type.ATTRIBUTE);
        String listName = attrList.getName();
        DomainAttribute da = cls.resolvePath(attr);
        for (GenericAgent genericAgent : container.getAllInstancesOfClass(cls, GenericAgent.class)) {
            Attribute att = genericAgent.resolvePath(Path.attributePath(attr.toStringArray()));
            this.replaceAgent((GenericAgent)Generator.randomiseAttributeNormallyDistributed(genericAgent, svar, listName, da, att));
        }

    }

    public GenericAgent instanciateAgentWithUniformDistributedAttributes(GenericAgentClass cls, String name) {
        GenericAgent a = new GenericAgent(name, cls);
        a = Generator.randomiseUniformAttributeValues(a);
        container.addAgent(a);
        return (GenericAgent) a.clone();
    }

    public List<GenericAgent> instanciateAgentsWithUniformDistributedAttributes(GenericAgentClass parent, Optional<String> prefix,
            int count) {

        List<GenericAgent> result = instanciateAgents(parent, prefix, count,
                (name) -> instanciateAgentWithUniformDistributedAttributes(parent, name));

        Collections.shuffle(result);

        return result;

    }

    public List<GenericAgent> instanciateAgentsWithNormalDistributedAttributes(GenericAgentClass parent, Optional<String> prefix,
            double svar,
            int count) {

        List<GenericAgent> result = instanciateAgents(parent, prefix, count,
                (name) -> instanciateAgentWithNormalDistributedAttributes(parent, name, svar));

        Collections.shuffle(result);

        return result;
    }

    /**
     * Creates an agent using a function for generating agents (e.g. using generator_gsim methods for specific attribute port distributions).
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

    public GenericAgent modifyAgentAttribute(GenericAgent inst, Path<List<Attribute>> attributeListPath, Attribute att) {

        GenericAgent agent = findGenericAgent(inst.getName());

        if (agent.resolvePath(attributeListPath) == null) {
            throw new GSimDefException(
                    "The attribute list path " + attributeListPath + " could not be resolved. Add path first in corresponding frame.");
        }

        agent.addChildAttribute(attributeListPath, att);

        return (GenericAgent) agent.clone();
    }


    public GenericAgent removeChildObject(GenericAgent owner, Path<Instance> instancePath) {
        GenericAgent here = findGenericAgent(owner.getName());
        here.removeChildInstance(instancePath);
        return (GenericAgent) here.clone();
    }

    public void removeGenericAgent(GenericAgent agent) {
        container.remove(agent);
    }

    public void replaceAgent(GenericAgent inst) {
        GenericAgent here = this.findGenericAgent(inst.getName());
        container.replaceAgent(here, inst);
    }

    /**
     * Throws NoSuchElementException if not existing.
     * 
     * @param extern the agent copy
     * @return the actual agent reference in the environment
     */
    protected GenericAgent findGenericAgent(String extern) {
        return container.getAgents().parallelStream().filter(a -> a.getName().equals(extern)).findAny().get();
    }

}
