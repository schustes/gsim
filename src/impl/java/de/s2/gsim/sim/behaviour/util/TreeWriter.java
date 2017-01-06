package de.s2.gsim.sim.behaviour.util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import jess.Fact;
import jess.Rete;
import jess.Value;
import jess.ValueVector;

public class TreeWriter {

    // private String debugDir="/home/gsim/tmp";
    public void output(String agent, Rete rete, String debugDir) {
        try {
            Element root = new Element("root");

            ArrayList<Fact> level_1 = getChildren(rete, null);

            for (Fact f : level_1) {
                outputRek(rete, f, root);
            }

            XMLOutputter p = new XMLOutputter();

            File dir = new File(debugDir + "/trees");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(dir.getCanonicalPath() + "/" + agent + ".xml");
            p.output(root, out);
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	@SuppressWarnings("rawtypes")
	private ArrayList<Fact> getActions(Rete rete, String sfn) {

        ArrayList<Fact> list = new java.util.ArrayList<Fact>();

        try {
            Iterator iter = rete.listFacts();
            while (iter.hasNext()) {
                Fact f = (Fact) iter.next();
                if (f.getDeftemplate().getBaseName().equals("rl-action-node")) {
                    String s = f.getSlotValue("state-fact-name").stringValue(rete.getGlobalContext());
                    if (s.equals(sfn)) {
                        list.add(f);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

	@SuppressWarnings("rawtypes")
	private ArrayList<Fact> getChildren(Rete rete, Fact parent) {

        ArrayList<Fact> list = new java.util.ArrayList<Fact>();

        try {
            if (parent != null) {
                String n = parent.getSlotValue("name").stringValue(rete.getGlobalContext());
                Iterator iter = rete.listFacts();
                while (iter.hasNext()) {
                    Fact f = (Fact) iter.next();
                    if (f.getDeftemplate().getBaseName().equals("state-fact")) {
                        String s = f.getSlotValue("parent").stringValue(rete.getGlobalContext());
                        if (s.equals(n)) {
                            list.add(f);
                        }
                    }
                }
            } else {

                try {
                    Iterator iter = rete.listFacts();
                    while (iter.hasNext()) {
                        Fact f = (Fact) iter.next();
                        if (f.getDeftemplate().getBaseName().equals("state-fact")) {
                            String s = f.getSlotValue("parent").stringValue(rete.getGlobalContext());
                            if (s.equals("nil")) {
                                list.add(f);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

	@SuppressWarnings("rawtypes")
	private ArrayList<Fact> getStateElems(Rete rete, String sfn, String paramName) {

        ArrayList<Fact> list = new java.util.ArrayList<Fact>();

        try {
            Iterator iter = rete.listFacts();
            while (iter.hasNext()) {
                Fact f = (Fact) iter.next();
                if (f.getDeftemplate().getBaseName().equals("state-fact-element") || f.getDeftemplate().getBaseName().equals("state-fact-category")) {
                    String s = f.getSlotValue("state-fact-name").stringValue(rete.getGlobalContext());
                    String s1 = f.getSlotValue("param-name").stringValue(rete.getGlobalContext());
                    if (s.equals(sfn) && s1.equals(paramName)) {
                        list.add(f);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private void outputRek(Rete rete, Fact parent, Element elem) {
        try {

            Element next = new Element("state");
            StringBuffer buf = new StringBuffer();

            String name = parent.getSlotValue("name").stringValue(rete.getGlobalContext());
            next.setAttribute("name", name);

            double value = parent.getSlotValue("value").floatValue(rete.getGlobalContext());
            next.setAttribute("value", String.valueOf(value));
            int lact = (int) parent.getSlotValue("last-activation").floatValue(rete.getGlobalContext());
            next.setAttribute("last-activation", String.valueOf(lact));
            int count = (int) parent.getSlotValue("count").floatValue(rete.getGlobalContext());
            next.setAttribute("activation-count", String.valueOf(count));
            int cont = (int) parent.getSlotValue("leaf").floatValue(rete.getGlobalContext());
            next.setAttribute("contractions", String.valueOf(cont));
            int exp = (int) parent.getSlotValue("expansion-count").floatValue(rete.getGlobalContext());
            next.setAttribute("expansions", String.valueOf(exp));
            int active = (int) parent.getSlotValue("active").floatValue(rete.getGlobalContext());
            next.setAttribute("selected", String.valueOf(active));

            String sfn = parent.getSlotValue("name").stringValue(rete.getGlobalContext());
            ValueVector vv = parent.getSlotValue("expansion").listValue(rete.getGlobalContext());
            buf.append("Description: ");
            // logger.debug(name);
            for (int i = 0; i < vv.size(); i++) {
                if (i > 0) {
                    buf.append(" AND ");
                }
                Value v = vv.get(i);
                buf.append("(");
                String pname = v.stringValue(rete.getGlobalContext());
                ArrayList<Fact> se = getStateElems(rete, sfn, pname);
                // logger.debug(i+":"+sfn+":"+pname);
                int c = 0;
                for (Fact g : se) {
                    if (c > 0) {
                        buf.append(" OR ");
                    }
                    if (g.getDeftemplate().getBaseName().equals("state-fact-element")) {
                        buf.append(g.getSlotValue("from").floatValue(rete.getGlobalContext()));
                        buf.append("<=");
                        buf.append(pname);
                        buf.append("<");
                        buf.append(g.getSlotValue("to").floatValue(rete.getGlobalContext()));
                    }
                    if (g.getDeftemplate().getBaseName().equals("state-fact-category")) {
                        buf.append(pname);
                        buf.append(" = ");
                        buf.append(g.getSlotValue("category").stringValue(rete.getGlobalContext()));
                    }
                    c++;
                }
                buf.append(")");
            }

            next.setText(buf.toString());

            Element actions = new Element("actions");
            ArrayList<Fact> a = getActions(rete, sfn);
            for (Fact f : a) {
                Element aelem = new Element("action");
                String actionName = f.getSlotValue("action-name").stringValue(rete.getGlobalContext());
                try {
                    ValueVector v = f.getSlotValue("arg").listValue(rete.getGlobalContext());
                    if (v != null && v.size() > 0) {
                        actionName += v.get(0);
                    }
                } catch (Exception e) {
                    System.out.println("Ignore Problem with StateFile:" + e.getMessage());
                }
                aelem.setAttribute("name", actionName);
                aelem.setAttribute("value", String.valueOf(f.getSlotValue("value").floatValue(rete.getGlobalContext())));
                aelem.setAttribute("frequency", String.valueOf(f.getSlotValue("count").floatValue(rete.getGlobalContext())));
                actions.addContent(aelem);
            }
            next.addContent(actions);

            elem.addContent(next);

            ArrayList<Fact> children = getChildren(rete, parent);

            for (Fact f : children) {
                outputRek(rete, f, next);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
