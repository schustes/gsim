package de.s2.gsim.sim.agent;

import java.util.List;

import de.s2.gsim.objects.ObjectInstanceIF;
import de.s2.gsim.objects.attribute.Attribute;

public interface AgentState {

    Attribute getAgentAttribute(String attName);

    RtExecutionContext getExecutionContext(String name);

    String[] getExecutionContextNames();

    RtExecutionContext[] getExecutionContexts();

    String getLastAction();

    String getAgentName();

    String[] getAgentObjectListNames();

    List<ObjectInstanceIF> getAgentObjects(String list);

    String[] getAgentAttributesListNames();

    Attribute[] getAgentAttributes(String list);

}