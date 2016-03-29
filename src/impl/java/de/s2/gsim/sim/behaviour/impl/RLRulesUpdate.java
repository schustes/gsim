package de.s2.gsim.sim.behaviour.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.def.objects.behaviour.ExpansionDef;
import de.s2.gsim.def.objects.behaviour.RLRule;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.sim.behaviour.impl.jessfunction.DynamicRuleBuilder;
import de.s2.gsim.sim.behaviour.impl.jessfunction.Expand0;
import de.s2.gsim.sim.behaviour.util.CollectiveTreeDBWriter;
import jess.Context;
import jess.Fact;
import jess.JessException;
import jess.Rete;

public class RLRulesUpdate {

    private RuntimeAgent agent = null;

    private String debugDir;

    public RLRulesUpdate(RuntimeAgent agent, String debugDir) {
        this.agent = agent;
        this.debugDir = debugDir;
    }

    // RIGHT is other, LEFT is expanded

    public void update(DomainAttribute domainAttr, String newFiller, Context ctx) {
        for (RLRule r : agent.getBehaviour().getRLRules()) {
            for (ExpansionDef ex : r.getExpansions()) {
                // ok, because only attribute, not filler must be named in Expansion
                // object

                Attribute specified = (Attribute) agent.resolveName(ex.getParameterName().split("/"));

                if (specified.getName().equals(domainAttr.getName())) {
                    this.update(r.getName(), domainAttr, newFiller, ex.getParameterName(), ctx);
                    return;
                }

            }
        }

    }

    public void update(String baseRuleName, DomainAttribute domain, String newFiller, String spec, Context context) {

        DynamicRuleBuilder builder = new DynamicRuleBuilder();
        TreeExpansionBuilder treeBuilder = new TreeExpansionBuilder(agent);

        try {

            ArrayList<Fact> allStates = getStateFactsForRootRule(baseRuleName, context);
            List<Fact> selectedStates = chooseStates(allStates, context);

            // this iterates over all state-facts and state-fact-elems in the
            // hierarchy
            for (Fact state : selectedStates) {

                String stateName = state.getSlotValue("name").stringValue(context);

                String expansionRuleName = "experimental_rule_" + baseRuleName + "@" + stateName + "@";
                RLRule baseRule = agent.getBehaviour().getRLRule(baseRuleName);

                // first find elems:
                Fact[] elems = getStateElemFacts(stateName, context);

                // clone the rule associated with the state, but only if the rule
                // belongs
                // to the set of rules to which the new category is added
                // (1 rule <-> 1 state)
                String newRule = builder.addCategoryToExperimentalRule(treeBuilder, agent, baseRule, stateName, elems, domain.getName(), newFiller,
                        context);

                // add the new element to this state (exactly 1 at a time)
                builder.addStateFactCategoryElem(state, newFiller, spec, context);

                Rete rete = context.getEngine();
                CollectiveTreeDBWriter f = new CollectiveTreeDBWriter();
                f.output("before_deepening", rete, debugDir);

                // delete rule on current level
                deleteRule(expansionRuleName, context);
                // don't forget to add the modified rules
                context.getEngine().executeCommand(newRule);

                // make new expansion one level deeper
                double depth = state.getSlotValue("depth").floatValue(context);
                if (depth > 0) {
                    expandOriginalNodeToDeeperLevel(stateName, newFiller, context);
                }
                f = new CollectiveTreeDBWriter();
                f.output("after_deepening", rete, debugDir);

            }

        } catch (JessException e) {
            e.printStackTrace();
        }
    }

    // select the state-fact-elems to which the new category is appended
    private ArrayList<Fact> chooseStates(ArrayList<Fact> states, Context context) {

        ArrayList<Fact> set = new ArrayList<Fact>();

        try {

            HashMap<Fact, Integer> map = new HashMap<Fact, Integer>();

            int depth = -1;

            for (Fact state : states) {

                double active = state.getSlotValue("active").floatValue(context);

                if (active > 0) {

                    String stateName = state.getSlotValue("name").stringValue(context);

                    Fact[] elems = getStateElemFacts(stateName, context);

                    int d = (int) Double.parseDouble(state.getSlotValue("depth").stringValue(context));

                    if (d > depth) {
                        // map with state-name:elemcount

                        if (map.containsKey(stateName)) {
                            if (map.get(state) < elems.length) {
                                map.put(state, elems.length);
                            }
                        } else {
                            map.put(state, elems.length);
                        }
                    }
                }
            }

            Fact selected = null;
            int random = cern.jet.random.Uniform.staticNextIntFromTo(0, map.size() - 1);
            ArrayList<Fact> list = new ArrayList<Fact>();
            list.addAll(map.keySet());
            selected = list.get(random);
            /*
             * for (Fact state : map.keySet()) { int len = map.get(state); if (len > maxLen) { maxLen = len; selected = state; } }
             */

            rekAdd(set, context, selected, states);

            set.add(selected);

        } catch (JessException e) {
            e.printStackTrace();
        }

        return set;
    }

    private void deleteRule(String ruleName, Context context) {
        try {
            context.getEngine().executeCommand("(undefrule " + ruleName + ")");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void expandOriginalNodeToDeeperLevel(String stateName, String newCategory, Context context) throws JessException {
        Expand0 impl = new Expand0();

        ArrayList<Fact> allStateFactElems = FactHandler.getInstance().getStateFactElems(stateName, context);

        Fact stateFact = FactHandler.getInstance().getStateFact(stateName, context);

        Fact elemToExpand = null;
        int depth = -1;
        for (Fact f : allStateFactElems) {
            String cName = f.getSlotValue("category").stringValue(context);
            Fact sf = FactHandler.getInstance().getStateFact(f.getSlotValue("state-fact-name").stringValue(context), context);

            double d = sf.getSlotValue("depth").floatValue(context);
            if (cName.equals(newCategory) && d > depth) {
                elemToExpand = f;
                depth = (int) d;
            }
        }

        try {
            impl.createNextStatesCat(agent, stateFact, elemToExpand, allStateFactElems, context, true);
        } catch (Exception e) {
            throw new JessException("", "", e);
        }

    }

    private Fact[] getStateElemFacts(String parentName, Context context) {

        ArrayList<Fact> ret = new ArrayList<Fact>();

        try {
            Iterator iter = context.getEngine().listFacts();
            while (iter.hasNext()) {
                Fact f = (Fact) iter.next();
                if (f.getDeftemplate().getBaseName().equals("state-fact-category")) {
                    String s1 = f.getSlotValue("state-fact-name").stringValue(context);
                    if (s1.equals(parentName)) {
                        ret.add(f);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret.toArray(new Fact[ret.size()]);
    }

    private ArrayList<Fact> getStateFactsForRootRule(String name, Context context) {
        ArrayList<Fact> ret = new ArrayList<Fact>();
        try {
            Iterator iter = context.getEngine().listFacts();
            while (iter.hasNext()) {
                Fact f = (Fact) iter.next();
                if (f.getDeftemplate().getBaseName().equals("state-fact")) {
                    String s1 = f.getSlotValue("name").stringValue(context);
                    if (s1.startsWith(name)) {
                        ret.add(f);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    private void rekAdd(List<Fact> result, Context ctx, Fact fact, List<Fact> allStates) {
        try {
            String parentName = fact.getSlotValue("parent").stringValue(ctx);
            for (Fact state : allStates) {
                String name = state.getSlotValue("name").stringValue(ctx);
                if (name.equals(parentName)) {
                    result.add(state);
                    rekAdd(result, ctx, state, allStates);
                }
            }
        } catch (JessException e) {
            e.printStackTrace();
        }
    }

}
