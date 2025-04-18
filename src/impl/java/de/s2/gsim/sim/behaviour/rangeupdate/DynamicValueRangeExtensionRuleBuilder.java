package de.s2.gsim.sim.behaviour.rangeupdate;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.RLRule;
import de.s2.gsim.sim.behaviour.rulebuilder.ExpansionParameterReferences;
import de.s2.gsim.sim.behaviour.rulebuilder.TreeExpansionBuilder;
import jess.Context;
import jess.Fact;
import jess.JessException;

public abstract class DynamicValueRangeExtensionRuleBuilder {

	private static Logger logger = Logger.getLogger(DynamicValueRangeExtensionRuleBuilder.class);


	private DynamicValueRangeExtensionRuleBuilder() {
		// static class
	}

	public static String increaseIntervalRangeInExperimentalRule(RuntimeAgent agent, RLRule braRule,
	        String stateName, Fact statefactelem, double min, double max, Context context, boolean isUpperBoundInclusive)
	        throws JessException {

		ExpansionParameterReferences consts = new ExpansionParameterReferences();

		String s = statefactelem.getDeftemplate().getBaseName();
		String pm = statefactelem.getSlotValue("param-name").stringValue(context);

		if (s.equals("state-fact-element")) {
			double m = statefactelem.getSlotValue("from").floatValue(context);
			double x = statefactelem.getSlotValue("to").floatValue(context);
			if (min < m) {
				m = min;
			}
			if (max > x) {
				x = max;
			}

			consts.setIntervalAttributes(pm, m, x);

		} else {
			List<String> f = consts.getFillers(pm);
			consts.setSetAttributes(pm, f);
		}

		String n = "";
		try {
			n = TreeExpansionBuilder.buildExperimentationRule(agent, braRule, stateName, consts, isUpperBoundInclusive);
			Logger.getLogger(DynamicValueRangeExtensionRuleBuilder.class).debug("New rule after dynamic attribute add:\n" + n);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return n;

	}

	public static String addCategoryToExperimentalRule(RuntimeAgent a, RLRule r, String stateName,
	        Fact[] statefactelems, String domainAttr, String newFiller, Context context) throws JessException {

		ExpansionParameterReferences consts = new ExpansionParameterReferences();

		for (int i = 0; i < statefactelems.length; i++) {
			String s = statefactelems[i].getDeftemplate().getBaseName();
			String pm = statefactelems[i].getSlotValue("param-name").stringValue(context);

			if (s.equals("state-fact-element")) {
				double m = statefactelems[i].getSlotValue("from").floatValue(context);
				double x = statefactelems[i].getSlotValue("to").floatValue(context);
				consts.setIntervalAttributes(pm, m, x);

			} else {
				String c = statefactelems[i].getSlotValue("category").stringValue(context);

				List<String> f = consts.getFillers(pm);
				f = maybeAddFiller(f, c);
				if (pm.equals(domainAttr)) {
					f = maybeAddFiller(f, newFiller);
				}
				consts.setSetAttributes(pm, f);
			}
		}

		String n = "";
		try {
			n = TreeExpansionBuilder.buildExperimentationRule(a, r, stateName, consts, false);
			Logger.getLogger(DynamicValueRangeExtensionRuleBuilder.class).debug("New rule after dynamic attribute add:\n" + n);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return n;

	}



	// TODO condition fehlt!
	public static String createNewExperimentalRule(RLRule r, RuntimeAgent a, String stateName,
	        Fact[] remainingStatefactelems, Context context, String param, double from, double to, boolean isUpper) throws JessException {

		ExpansionParameterReferences consts = new ExpansionParameterReferences();
		consts.setIntervalAttributes(param, from, to);

		for (int i = 0; i < remainingStatefactelems.length; i++) {
			String s = remainingStatefactelems[i].getDeftemplate().getBaseName();
			String pm = remainingStatefactelems[i].getSlotValue("param-name").stringValue(context);
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
			n = TreeExpansionBuilder.buildExperimentationRule(a, r, stateName, consts, isUpper);
			// System.out.println(">>>>>>>>>>>>>>>>>>>>>>" + n);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return n;

	}

	public static String createNewExperimentalRuleCat(RuntimeAgent a, RLRule r, String stateName, String paramToExpand,
	        List<String> fillersOfExpand, Fact[] constants, Context context) throws JessException {

		ExpansionParameterReferences consts = new ExpansionParameterReferences();

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
			n = TreeExpansionBuilder.buildExperimentationRule(a, r, stateName, consts, false);
			// System.out.println(">>>>>>>>>>>>>>>>>>>>>>" + n);
		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.debug("new rule created: " + n);

		return n;

	}

	public static String createNewSelectionNodes(TreeExpansionBuilder b, RLRule r, RuntimeAgent a, String stateName, Fact elem,
	        Fact[] constants, Context context) throws JessException {

		String n = "";

		ExpansionParameterReferences consts = new ExpansionParameterReferences();

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

	public static String createNewSelectionNodesCat(TreeExpansionBuilder b, RLRule r, String stateName, String param, List<String> fillers,
	        Fact[] constants, int depth, Context context) throws JessException {

		String n = "";

		ExpansionParameterReferences consts = new ExpansionParameterReferences();

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

	private static List<String> maybeAddFiller(List<String> oldFillers, String newFiller) {
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