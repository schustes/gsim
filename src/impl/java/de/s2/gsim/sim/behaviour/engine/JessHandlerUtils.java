package de.s2.gsim.sim.behaviour.engine;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.ActionDef;
import de.s2.gsim.environment.GSimDefException;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.IntervalAttribute;
import de.s2.gsim.sim.behaviour.util.BuildingUtils;
import jess.Deftemplate;
import jess.Fact;
import jess.JessException;
import jess.RU;
import jess.Rete;
import jess.Value;

import java.util.Optional;
import java.util.Set;

public class JessHandlerUtils {

	public static Set<String> assertCurrentStateIntoRulebase(Rete rete, RuntimeAgent owner, Set<String> uniqueConditions)
	        throws JessException {

		for (String s : uniqueConditions) {
			assertConstants(rete, owner, s);
		}

		assertReferencedObjectParameters(rete, owner);

		return uniqueConditions;
	}

	public static void assertParameter(Rete rete, String name, String val) {
		Fact f = null;
		try {
			Deftemplate p = rete.findDeftemplate("parameter");

			if (val == null) {
				val = "0";
			}
			f = new Fact(p);
			f.setSlotValue("name", new Value(name, RU.STRING));

			if (de.s2.gsim.util.Utils.isNumerical(val)) {
				f.setSlotValue("value", new Value(Double.valueOf(val).doubleValue(), RU.FLOAT));
			} else {
				f.setSlotValue("value", new Value(val, RU.STRING));
			}
			rete.assertFact(f);
		} catch (JessException e) {
			f = null;
			e.printStackTrace();
		}

	}

	private static void assertReferencedObjectParameters(Rete rete, RuntimeAgent owner) {
		for (ActionDef r : owner.getBehaviour().getAvailableActions()) {
			addUserParams0(rete, owner, r.getObjectClassParams());
		}
	}

	private static void addUserParams0(Rete rete, RuntimeAgent owner, String[] params) {
		if (params == null || params != null && params.length == 0) {
			return;
		}
		for (String s : params) {
			String list = s.split("/")[0].trim();
			for (Instance inst : owner.getChildInstances(list)) {
				assertObjectParam(rete, s, inst.getName());
			}
		}
	}

	private static void assertObjectParam(Rete rete, String pathToFrame, String instanceName) {
		try {
			Deftemplate p = rete.findDeftemplate("object-parameter");
			Fact f = new Fact(p);
			f.setSlotValue("object-class", new Value(pathToFrame, RU.STRING));
			f.setSlotValue("instance-name", new Value(instanceName, RU.STRING));
			rete.assertFact(f);
		} catch (JessException e) {
			e.printStackTrace();
		}
	}


	private static void assertConstants(Rete rete, Instance owner, String n) {

		if (!n.contains("/")) {
			return;
		}

		String list = n.split("/")[0];

		for (String s : owner.getChildInstanceListNames()) {
			if (s.equals(list)) {
				assertConstantsInstanceRef(rete, owner, n);
			}
		}
		assertConstantsAttRefIfReferencesInstance(rete, owner, n);
	}

	private static void assertConstantsAttRefIfReferencesInstance(Rete rete, Instance owner, String n) {

		Optional<Path<?>> childAttr = BuildingUtils.extractChildAttributePathWithoutParent(owner.getDefinition(), n);
		if (!childAttr.isPresent()) {
			// this is possible if n is not a path to an attribute, but only to an instance
			return;
		}

		String att = childAttr.get().toString();

		Path<Attribute> path = Path.attributePath(n.split("/"));

		if (n.contains("{")) {
			String attRef = n.substring(n.indexOf("{") + 1, n.lastIndexOf("}"));
			Attribute ref = owner.resolvePath(path);
			if (ref instanceof IntervalAttribute) {
				double val = ((IntervalAttribute) ref).getValue();
				assertParameter(rete, attRef, String.valueOf(val));
			} else {
				assertParameter(rete, attRef, ref.toValueString());
			}
		} else {
			try {
				Attribute ref = owner.resolvePath(path);
				if (ref instanceof IntervalAttribute) {
					double val = ((IntervalAttribute) ref).getValue();
					assertParameter(rete, att, String.valueOf(val));
				} else {
					assertParameter(rete, att, ref.toValueString());
				}
			} catch (GSimDefException notExisiting) {
			    //this can happen if the condition refers to a class pattern. Ignore in this case, because it is not a constant, but is resolved e.g. in a rule or expansion later 
			}
		}

	}

	private static void assertConstantsInstanceRef(Rete rete, Instance owner, String n) {

		String listName = BuildingUtils.resolveList(n);
		String objectWithList = BuildingUtils.resolveChildFrameWithList(owner.getDefinition(), n);

		for (Instance inst : owner.getChildInstances(listName)) {
			assertObjectParam(rete, objectWithList, inst.getName());

			if (n.length() <= objectWithList.length() + 1) {
				// no attribute in path, so nothing more to insert
				continue;
			}
			String attributePathInChild = n.substring(objectWithList.length() + 1);

			Attribute ref = inst.resolvePath(Path.attributeListPath(attributePathInChild.split("/")));

			String fullPath = listName + "/" + inst.getName() + "/" + attributePathInChild;

			if (ref instanceof IntervalAttribute) {
				double val = ((IntervalAttribute) ref).getValue();
				assertParameter(rete, fullPath, String.valueOf(val));
			} else {
				assertParameter(rete, fullPath, ref.toValueString());
			}

		}
	}

}
