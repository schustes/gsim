package de.s2.gsim.sim.behaviour.impl.jessfunction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jess.Context;
import jess.Fact;
import jess.JessException;
import jess.RU;
import jess.Rete;
import jess.Value;
import jess.ValueVector;

import org.apache.log4j.Logger;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.RLRule;
import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.sim.behaviour.impl.Attribute2ValuesMap;
import de.s2.gsim.sim.behaviour.impl.FactHandler;
import de.s2.gsim.sim.behaviour.impl.TreeExpansionBuilder;

public class DynamicRuleBuilder {

	private static Logger logger = Logger.getLogger(DynamicRuleBuilder.class);

	public DynamicRuleBuilder() {
		super();
	}

	public String addCategoryToExperimentalRule(TreeExpansionBuilder b, RuntimeAgent a, RLRule r, String stateName, Fact[] statefactelems,
			String domainAttr, String newFiller, Context context) throws JessException {

		Attribute2ValuesMap consts = new Attribute2ValuesMap();

		for (int i = 0; i < statefactelems.length; i++) {
			String s = statefactelems[i].getDeftemplate().getBaseName();
			String pm = statefactelems[i].getSlotValue("param-name").stringValue(context);

			DomainAttribute attr = (DomainAttribute) a.getDefinition().resolvePath(Path.attributePath(pm.split("/")));
			String simpleAttrName = attr.getName();

			if (s.equals("state-fact-element")) {
				double m = statefactelems[i].getSlotValue("from").floatValue(context);
				double x = statefactelems[i].getSlotValue("to").floatValue(context);
				consts.setIntervalAttributes(pm, m, x);

			} else {
				String c = statefactelems[i].getSlotValue("category").stringValue(context);

				List<String> f = consts.getFillers(pm);
				f = maybeAddFiller(f, c);
				if (simpleAttrName.equals(domainAttr)) {
					f = maybeAddFiller(f, newFiller);
				}
				consts.setSetAttributes(pm, f);
			}
		}

		String n = "";
		try {
			n = b.buildExperimentationRule(r, stateName, consts);
			Logger.getLogger(DynamicRuleBuilder.class).debug("New rule after dynamic attribute add:\n" + n);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return n;

	}

	public Fact addStateFactCategoryElem(Fact state, String attributeName, String categoryValue, Context context) throws JessException {

		String stateName = state.getSlotValue("name").stringValue(context);
		// String param = state.getSlotValue("expansion").stringValue(context);
		String name = attributeName + "->" + String.valueOf(categoryValue);

		return FactHandler.getInstance().addStateFactCat(context.getEngine(), state, stateName, name, attributeName, categoryValue);

	}

	public Fact addStateFactIntervalElem(String stateName, Fact stateFactElem, double from, double to, Context context) throws JessException {

		//String stateName = stateFactElem.getSlotValue("name").stringValue(context);
		String param = stateFactElem.getSlotValue("param-name").stringValue(context);
		String name = param + "->" + String.valueOf(from) + ":" + String.valueOf(to);

		return FactHandler.getInstance().addStateFactElement(context.getEngine(), stateFactElem, stateName, name, param, from, to);

	}

	protected ArrayList<Fact> activateActionNodes(Context context, String stateFactName) throws JessException {
		ArrayList<Fact> connectedActionNodes = FactHandler.getInstance().getFacts("rl-action-node", stateFactName, context);
		for (Fact f : connectedActionNodes) {

			context.getEngine().retract(f);
			f.setSlotValue("active", new Value(0.0, RU.FLOAT));
			context.getEngine().assertFact(f);
		}
		return connectedActionNodes;
	}

	//TODO condition fehlt!
	protected String createNewExperimentalRule(TreeExpansionBuilder b, RLRule r, RuntimeAgent a, String stateName, Fact[] remainingStatefactelems,
			Context context, String param, double from, double to) throws JessException {

		Attribute2ValuesMap consts = new Attribute2ValuesMap();
		consts.setIntervalAttributes(param, from, to);

		for (int i = 0; i < remainingStatefactelems.length; i++) {
			String s = remainingStatefactelems[i].getDeftemplate().getBaseName();
			String pm = remainingStatefactelems[i].getSlotValue("name").stringValue(context);
			if (s.equals("state-fact-element")) {
				double m = remainingStatefactelems[i].getSlotValue("from").floatValue(context);
				double x = remainingStatefactelems[i].getSlotValue("to").floatValue(context);
				consts.setIntervalAttributes(pm, m, x);

			} else {
				String c = remainingStatefactelems[i].getSlotValue("category").stringValue(context);

				List<String> fillersNow = consts.getFillers(pm);// getFillers(consts, c);
				fillersNow = maybeAddFiller(fillersNow, c);// add filler to array

				consts.setSetAttributes(pm, fillersNow);
			}
		}

		String n = "";
		try {
			n = b.buildExperimentationRule(r, stateName, consts);
			//System.out.println(">>>>>>>>>>>>>>>>>>>>>>" + n);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return n;

	}

	protected String createNewExperimentalRuleCat(TreeExpansionBuilder b, RLRule r, String stateName, String paramToExpand, List<String> fillersOfExpand,
			Fact[] constants, Context context) throws JessException {

		Attribute2ValuesMap consts = new Attribute2ValuesMap();

		consts.setSetAttributes(paramToExpand, fillersOfExpand);

		for (int i = 0; i < constants.length; i++) {
			String s = constants[i].getDeftemplate().getBaseName();
			String pm = constants[i].getSlotValue("param-name").stringValue(context);
			if (s.equals("state-fact-element")) {
				double m = constants[i].getSlotValue("from").floatValue(context);
				double x = constants[i].getSlotValue("to").floatValue(context);
				consts.setIntervalAttributes(pm, m, x);
			} else {
				String c = constants[i].getSlotValue("category").stringValue(context);

				List<String> f = consts.getFillers(pm);// getFillers(consts, c);
				f = maybeAddFiller(f, c);

				consts.setSetAttributes(pm, f);
			}
		}

		String n = "";
		try {
			n = b.buildExperimentationRule(r, stateName, consts);
			//System.out.println(">>>>>>>>>>>>>>>>>>>>>>" + n);
		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.debug("new rule created: " + n);

		return n;

	}

	protected String createNewSelectionNodes(TreeExpansionBuilder b, RLRule r, RuntimeAgent a, String stateName, Fact elem, Fact[] constants,
			Context context) throws JessException {

		String sfn = elem.getSlotValue("state-fact-name").stringValue(context);

		String n = "";

		Attribute2ValuesMap consts = new Attribute2ValuesMap();

		for (int i = 0; i < constants.length; i++) {
			String s = constants[i].getDeftemplate().getBaseName();
			String pm = constants[i].getSlotValue("name").stringValue(context);
			if (s.equals("state-fact-element")) {
				double m = constants[i].getSlotValue("from").floatValue(context);
				double x = constants[i].getSlotValue("to").floatValue(context);
				consts.setIntervalAttributes(pm, m, x);
			} else {
				String c = constants[i].getSlotValue("category").stringValue(context);
				List<String> f = consts.getFillers(pm);
				f = maybeAddFiller(f, c);

				consts.setSetAttributes(pm, f);
			}
		}

		return n;

	}

	protected String createNewSelectionNodesCat(TreeExpansionBuilder b, RLRule r, String stateName, String param, List<String> fillers, Fact[] constants,
			int depth, Context context) throws JessException {

		String sfn = stateName;

		String n = "";

		Attribute2ValuesMap consts = new Attribute2ValuesMap();

		for (int i = 0; i < constants.length; i++) {
			String s = constants[i].getDeftemplate().getBaseName();
			String pm = constants[i].getSlotValue("name").stringValue(context);
			if (s.equals("state-fact-element")) {
				double m = constants[i].getSlotValue("from").floatValue(context);
				double x = constants[i].getSlotValue("to").floatValue(context);
				consts.setIntervalAttributes(pm, m, x);
			} else {
				String c = constants[i].getSlotValue("category").stringValue(context);
				List<String> fill = consts.getFillers(c);
				fill.add(c);
				consts.setSetAttributes(c, fill);
			}
		}

		return n;

	}

	@SuppressWarnings("rawtypes")
	protected int getTime(Rete rete) {
		try {
			Iterator iter = rete.listFacts();
			while (iter.hasNext()) {
				Fact f = (Fact) iter.next();
				if (f.getDeftemplate().getBaseName().equals("timer")) {
					return (int) f.getSlotValue("time").floatValue(rete.getGlobalContext());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	protected void insertNewActionNodes(Context context, String oldStatefactName, String newStatefactName) throws JessException {

		ArrayList<Fact> connectedActionNodesAll = FactHandler.getInstance().getFacts("rl-action-node", oldStatefactName, context);

		for (Fact f : connectedActionNodesAll) {
			context.getEngine().retract(f);
			f.setSlotValue("active", new Value(0.0, RU.FLOAT));
			context.getEngine().assertFact(f);
		}

		Fact[] newConnectedActions = createActionNodes(newStatefactName, connectedActionNodesAll, context);

		for (Fact a : newConnectedActions) {
			context.getEngine().assertFact(a);
		}
	}

	private Fact[] createActionNodes(String stateFactName, ArrayList<Fact> oldActionNodes, Context context) throws JessException {

		Fact[] ret = new Fact[oldActionNodes.size()];

		int i = 0;

		for (Fact o : oldActionNodes) {

			String name = o.getSlotValue("action-name").stringValue(context);
			// String ruleName = o.getSlotValue("rule").stringValue(context);
			String evalFunc = o.getSlotValue("function").stringValue(context);
			double alpha = o.getSlotValue("alpha").floatValue(context);
			ValueVector args = o.getSlotValue("arg").listValue(context);

			Fact newAction = FactHandler.getInstance().createActionFact(context.getEngine(), name, evalFunc, alpha, args, stateFactName);
			newAction.setSlotValue("active", new Value(1.0, RU.FLOAT));
			ret[i] = newAction;
			i++;
		}
		return ret;

	}

	private List<String> maybeAddFiller(List<String> oldFillers, String newFiller) {
		if (oldFillers == null) {
			List<String> mutableList = new ArrayList<>();
			mutableList.add(newFiller);
			return mutableList;
		}
		for (String s : oldFillers) {
			if (s.equals(newFiller)) {
				return oldFillers;
			}
		}

		List<String> newFillers = new ArrayList<>(oldFillers);
		newFillers.add(newFiller);
		return newFillers;

	}

}