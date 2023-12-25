package de.s2.gsim.sim.behaviour.util;

import de.s2.gsim.util.Utils;
import jess.Fact;
import jess.Rete;
import org.jgraph.JGraph;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StateActionTree extends JFrame {

    private static final long serialVersionUID = 1L;

    int y = 0;

    private String agentName = "";

	private Map<Object, Object> attributes = new HashMap<>();

    private int cellH = 30;

    private int cellW = 150;

    private JGraph graph;

    private GraphModel model;

    private Rete rete = null;

    public StateActionTree(String agentName, Rete rete) {

        super("State space of " + agentName);
        this.agentName = agentName;
        this.rete = rete;

        this.setSize(800, 700);
        model = new DefaultGraphModel();
        graph = new JGraph(model);
        graph.setLayout(new BorderLayout());
        graph.setSize(new Dimension(600, 500));
        setVisible(true);
    }

    public void buildDisplay(int time) {

        model = new DefaultGraphModel();

        attributes.clear();

        int width = getWidth();
        buildGraphRek(null, model, 0, new Offset(width / 2));

        // for (Fact f: this.getChildren(null)) {
        // this.addLinks(f);
        // }
        addLinks(null);

        try {

            setTitle("State-space of " + agentName + ", time=" + time);

            graph.setModel(model);
            graph.setGridEnabled(true);
            graph.setGridVisible(true);
            graph.setEditable(true);
            graph.setEnabled(true);

            JScrollPane p = new JScrollPane();
            p.getViewport().setView(graph);

            getContentPane().setLayout(new BorderLayout());
            getContentPane().removeAll();
            getContentPane().add(p);
            setVisible(true);

            Thread.sleep(1000);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("graph not updated, because of an error");
        }
    }

    private void addLink(DefaultGraphCell self, DefaultGraphCell child) {
        try {

            DefaultEdge edge = new DefaultEdge();

            // TODO this will probably not work
			Map<?, ?> edgeAttrib = GraphConstants.createAttributes(edge, "key2", "port");
            attributes.put(edge, edgeAttrib);

            int arrow = GraphConstants.ARROW_CLASSIC;
            GraphConstants.setLineEnd(edgeAttrib, arrow);
            GraphConstants.setEndFill(edgeAttrib, true);
            GraphConstants.setForeground(edgeAttrib, Color.red);

            DefaultPort p1 = new DefaultPort();
            self.add(p1);
            // f1.setUserObject(key);
            DefaultPort p2 = new DefaultPort();
            child.add(p2);
            // f2.setUserObject(key2);

            ConnectionSet cs = new ConnectionSet(edge, p1, p2);
            Object[] connections = new Object[] { edge, self, child };

            model.insert(connections, attributes, cs, null, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	@SuppressWarnings({ "rawtypes" })
    private void addLinks(Fact g) {
        try {

            for (Fact f : getChildren(g)) {

                String key = "root";
                if (g != null) {
                    key = g.getSlotValue("name").stringValue(rete.getGlobalContext());
                    if (key.length() > 20) {
                        key = key.substring(0, 20);
                    }
                    key += "-" + g.getSlotValue("port").atomValue(rete.getGlobalContext());
                    if (key.length() > 24) {
                        key = key.substring(0, 24);
                    }
                    key = key + "," + g.getSlotValue("last-activation").atomValue(rete.getGlobalContext());
                }

                String key2 = f.getSlotValue("name").stringValue(rete.getGlobalContext());
                if (key2.length() > 20) {
                    key2 = key2.substring(0, 20);
                }
                key2 += "-" + f.getSlotValue("port").atomValue(rete.getGlobalContext());
                if (key2.length() > 24) {
                    key2 = key2.substring(0, 24);
                }
                key2 = key2 + "," + f.getSlotValue("last-activation").atomValue(rete.getGlobalContext());

                DefaultGraphCell self = findCell(key);
                DefaultGraphCell child = findCell(key2);

                DefaultEdge edge = new DefaultEdge();
                // TODO this will probably not work
                Map edgeAttrib = GraphConstants.createAttributes(edge, "key2", "port");
                attributes.put(edge, edgeAttrib);

                int arrow = GraphConstants.ARROW_CLASSIC;
                GraphConstants.setLineEnd(edgeAttrib, arrow);
                GraphConstants.setEndFill(edgeAttrib, true);
                GraphConstants.setForeground(edgeAttrib, Color.red);

                DefaultPort p1 = new DefaultPort();
                self.add(p1);
                p1.setUserObject(key);
                DefaultPort p2 = new DefaultPort();
                child.add(p2);
                p2.setUserObject(key2);

                ConnectionSet cs = new ConnectionSet(edge, p1, p2);
                Object[] connections = new Object[] { edge, self, child };

                model.insert(connections, attributes, cs, null, null);

                addLinks(f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	@SuppressWarnings({ "rawtypes" })
    private void buildGraphRek(Fact fact, GraphModel model, int offsetTop, Offset offsetLeft) {

        try {
            String key = "";
            String s = "-1";
            String active = "-1";
            if (fact != null) {
                key = fact.getSlotValue("name").stringValue(rete.getGlobalContext());
                if (key.length() > 20) {
                    key = key.substring(0, 20);
                }
                key = key + "-" + fact.getSlotValue("port").atomValue(rete.getGlobalContext());
                if (key.length() > 24) {
                    key = key.substring(0, 24);
                }
                key = key + "," + fact.getSlotValue("last-activation").atomValue(rete.getGlobalContext());

                s = fact.getSlotValue("leaf").atomValue(rete.getGlobalContext());
                active = fact.getSlotValue("active").atomValue(rete.getGlobalContext());
            } else {
                key = "root";
            }

            Rectangle position = new Rectangle(offsetLeft.o, offsetTop, cellW, cellH);

            DefaultGraphCell cell = new DefaultGraphCell(key);
            // TODO this will probably not work
            Map cellAttrib = GraphConstants.createAttributes(cell, "key", "port");
            attributes.put(cell, cellAttrib);
            Rectangle cellBounds = position; // scale(wProp, hProp,
            // position);
            GraphConstants.setBounds(cellAttrib, cellBounds);

            Color c = Color.GREEN; // position.getColor();//new Color(135, 255,
            // 189);
            if (Utils.isNumerical(s)) {
                if (Double.parseDouble(s) == 4) {
                    c = Color.ORANGE;
                }
            }
            if (Double.parseDouble(active) == 1) {
                c = Color.CYAN;
            }

            GraphConstants.setBackground(cellAttrib, c);
            GraphConstants.setOpaque(cellAttrib, true);
            GraphConstants.setBorder(cellAttrib, BorderFactory.createRaisedBevelBorder());
            GraphConstants.setFont(cellAttrib, new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 9));
            Object[] cells = new Object[] { cell };
            model.insert(cells, attributes, null, null, null);

            ArrayList<Fact> children = getChildren(fact);
            int ot = offsetTop + cellH + 40;

            ArrayList<Fact> actions = fact != null ? getActions(fact.getSlotValue("name").stringValue(rete.getGlobalContext()))
                    : new ArrayList<Fact>();

            int ww = actions.size() > 0 ? getActions(fact.getSlotValue("name").stringValue(rete.getGlobalContext())).size() : 1;
            int xx = 1;
            if (ww > 0) {
                xx = (int) (ww * (cellW / 1d));
            }
            int potentialSiblingSpace = offsetLeft.h * cellW + 10 + offsetLeft.h * (xx / 1);
            Offset newOffset = new Offset(offsetLeft.o - ((cellW) * children.size() > 0 ? children.size() : 1) + potentialSiblingSpace);
            Offset newOffset2 = new Offset(offsetLeft.o - (cellW) + 10); // new
            // Offset(offsetLeft.o-(cellW/2)*actions.size()>0?actions.size():1);
            newOffset.o = newOffset.o - cellW + 10;

            for (Fact f : actions) {

                String key2 = f.getSlotValue("action-name").stringValue(rete.getGlobalContext());
                if (key2.contains(".")) {
                    String[] p = key2.split("\\.");
                    key2 = p[p.length - 1];
                }

                if (key2.length() > 10) {
                    key2 = key2.substring(0, 10);
                }
                key2 += "-" + f.getSlotValue("port").atomValue(rete.getGlobalContext());
                if (key2.length() > 15) {
                    key2 = key2.substring(0, 15);
                }
                key2 += "-" + f.getSlotValue("count").atomValue(rete.getGlobalContext());
                key2 += "-" + f.getSlotValue("time").atomValue(rete.getGlobalContext());

                DefaultGraphCell cell0 = new DefaultGraphCell(key2);
                // TODO this will probably not work
                Map cellAttrib0 = GraphConstants.createAttributes(cell, "key", "port");
                attributes.put(cell0, cellAttrib0);
                Rectangle position2 = new Rectangle(newOffset2.o, ot, cellW, cellH);
                newOffset2.o += cellW + 10;
                Rectangle cellBounds0 = position2; // scale(wProp, hProp,
                // position);
                GraphConstants.setBounds(cellAttrib0, cellBounds0);
                GraphConstants.setFont(cellAttrib0, new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));

                Color c0 = Color.RED; // position.getColor();//new Color(135,
                // 255, 189);
                GraphConstants.setBackground(cellAttrib0, c0);
                GraphConstants.setOpaque(cellAttrib0, true);
                GraphConstants.setBorder(cellAttrib0, BorderFactory.createRaisedBevelBorder());
                Object[] cells0 = new Object[] { cell0 };
                model.insert(cells0, attributes, null, null, null);
                addLink(cell, cell0);

            }
            ot = ot + cellH + 40;
            for (Fact f : children) {
                // String key2 =
                // f.getSlotValue("name").stringValue(rete.getGlobalContext());

                buildGraphRek(f, model, ot, newOffset);
                int w = getActions(f.getSlotValue("name").stringValue(rete.getGlobalContext())).size();
                int x = 1;
                if (w > 0) {
                    x = (int) (w * (cellW / 1d));
                }
                newOffset.o = newOffset.o + x + 10;
                newOffset.h = newOffset.h + 1;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DefaultGraphCell findCell(String key) {
        for (int i = 0; i < model.getRootCount(); i++) {
            DefaultGraphCell cell = (DefaultGraphCell) model.getRootAt(i);

            String name = cell.toString();
            if (name != null) {

                if (name.equals(key)) {
                    return cell;
                }

            }
        }

        return null;
    }

    private ArrayList<Fact> getActions(String sfn) {

        ArrayList<Fact> list = new java.util.ArrayList<Fact>();

        try {
			Iterator<?> iter = rete.listFacts();
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

    private ArrayList<Fact> getChildren(Fact parent) {

        ArrayList<Fact> list = new java.util.ArrayList<Fact>();

        try {
            if (parent != null) {
                String n = parent.getSlotValue("name").stringValue(rete.getGlobalContext());
				Iterator<?> iter = rete.listFacts();
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
				Iterator<?> iter = rete.listFacts();
                while (iter.hasNext()) {
                    Fact f = (Fact) iter.next();
                    if (f.getDeftemplate().getBaseName().equals("state-fact")) {
                        String s = f.getSlotValue("parent").stringValue(rete.getGlobalContext());
                        if (s.equals("nil")) {
                            list.add(f);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private class Offset {
        int h = 0;

        int o = 0;

        public Offset(int off) {
            o = off;
        }
    }

}
