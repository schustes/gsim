package de.s2.gsim.sim.behaviour.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

import de.s2.gsim.sim.engine.common.DatabaseManagerPostgres;
import jess.Fact;
import jess.Rete;
import jess.Value;
import jess.ValueVector;

public class IndividualTreeDBWriter {

    // private String host = "169.254.229.39";
    private String host = "socnt07.soc.surrey.ac.uk";

    public IndividualTreeDBWriter() {
        if (System.getProperty("treedbhost") != null) {
            host = System.getProperty("treedbhost");
        }
    }

    public void writeToDB(String model, String agentId, int time, Rete rete, String role) {

        ArrayList<Fact> level_1 = getChildren(rete, null);
        Connection con = null;
        try {
            con = DatabaseManagerPostgres.getInstance().getConnection(host, "gsim", "gsim", "gsim1");

            for (Fact f : level_1) {
                buildAgentStateTreeRek(con, model, time, agentId, rete, f);
            }
        } catch (Exception e) {
            System.out.println("Problem during tree-writing");
        } finally {
            try {
                if (con != null) {// con.close();
                    DatabaseManagerPostgres.getInstance().releaseConnection(host, "gsim", con);
                }
            } catch (Exception e2) {
                System.out.println("Problem closing SQL connection: " + e2.getMessage());
            }
        }

    }

    private void buildAgentStateTreeRek(Connection con, String model, int time, String id, Rete rete, Fact parent) {
        try {

            StringBuffer descrBuf = new StringBuffer();

            String stateDescriptor = parent.getSlotValue("name").stringValue(rete.getGlobalContext());
            String stateDescriptorBeautified = stateDescriptor.substring(0, stateDescriptor.lastIndexOf('_'));
            descrBuf.append(stateDescriptorBeautified);
            descrBuf.append("::");

            double value = parent.getSlotValue("value").floatValue(rete.getGlobalContext());
            int lastActivation = (int) parent.getSlotValue("last-activation").floatValue(rete.getGlobalContext());
            int activationCount = (int) parent.getSlotValue("count").floatValue(rete.getGlobalContext());
            int contractions = (int) parent.getSlotValue("leaf").floatValue(rete.getGlobalContext());
            int expansions = (int) parent.getSlotValue("expansion-count").floatValue(rete.getGlobalContext());

            ValueVector vv = parent.getSlotValue("expansion").listValue(rete.getGlobalContext());

            for (int i = 0; i < vv.size(); i++) {
                if (i > 0) {
                    descrBuf.append(" AND ");
                }
                Value v = vv.get(i);
                descrBuf.append("(");
                String pname = v.stringValue(rete.getGlobalContext());
                ArrayList<Fact> se = getStateElems(rete, stateDescriptor, pname);
                int c = 0;
                for (Fact g : se) {
                    if (c > 0) {
                        descrBuf.append(" OR ");
                    }
                    if (g.getDeftemplate().getBaseName().equals("state-fact-element")) {
                        descrBuf.append(g.getSlotValue("from").floatValue(rete.getGlobalContext()));
                        descrBuf.append("<=");
                        descrBuf.append(pname);
                        descrBuf.append("<");
                        descrBuf.append(g.getSlotValue("to").floatValue(rete.getGlobalContext()));
                    }
                    if (g.getDeftemplate().getBaseName().equals("state-fact-category")) {
                        descrBuf.append(pname);
                        descrBuf.append(" = ");
                        descrBuf.append(g.getSlotValue("category").stringValue(rete.getGlobalContext()));
                    }
                    c++;
                }
                descrBuf.append(")");
            }

            String description = descrBuf.toString();

            long key = insertDescriptor(con, model, id, time, description, lastActivation, activationCount, contractions, expansions, value);

            // Element actions = new Element("actions");
            ArrayList<Fact> a = getActions(rete, stateDescriptor);
            for (Fact f : a) {
                String actionName = f.getSlotValue("action-name").stringValue(rete.getGlobalContext());
                try {
                    ValueVector v = f.getSlotValue("arg").listValue(rete.getGlobalContext());
                    if (v != null && v.size() > 0) {
                        actionName += v.get(0);
                    }
                } catch (Exception e) {
                    System.out.println("Ignore Problem with StateFile:" + e.getMessage());
                }

                String actionCount = String.valueOf(f.getSlotValue("count").floatValue(rete.getGlobalContext()));
                String actionValue = String.valueOf(f.getSlotValue("value").floatValue(rete.getGlobalContext()));
                // String actionDescription = actionName + "(value="+actionValue + ", count="+actionCount+")";

                insertAction(con, key, actionName, Double.valueOf(actionValue), Double.valueOf(actionCount).intValue());

            }

            ArrayList<Fact> children = getChildren(rete, parent);

            for (Fact f : children) {
                buildAgentStateTreeRek(con, model, time, id, rete, f);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Fact> getActions(Rete rete, String sfn) {

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

    private ArrayList<Fact> getChildren(Rete rete, Fact parent) {

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

                try {
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private ArrayList<Fact> getStateElems(Rete rete, String sfn, String paramName) {

        ArrayList<Fact> list = new java.util.ArrayList<Fact>();

        try {
            Iterator<?> iter = rete.listFacts();
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

    private void insertAction(Connection con, long fk, String actionName, double actionValue, int activationCount) {
        try {
            String sql = "insert into agent_stateactions values (nextval('descriptor_sequence'), '" + actionName + "'," + actionValue + ","
                    + activationCount + "," + fk + ")";
            // System.out.println(sql);
            Statement stmt = con.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long insertDescriptor(Connection con, String model, String id, int time, String description, int lastActivation, int activationCount,
            int contractions, int expansions, double value) {
        long key = -1;
        try {
            Statement stmt = con.createStatement();

            String sql = "insert into agent_statedescription values " + " ( nextval('descriptor_sequence') ,'" + id + "', '" + description + "',"
                    + lastActivation + "," + activationCount + "," + contractions + "," + expansions + ", " + value + "," + time + ",'" + model
                    + "')";
            // System.out.println(sql);
            stmt.executeUpdate(sql);
            // ResultSet res = stmt.getGeneratedKeys();
            // res.next();
            // key = res.getLong(1);
            // System.out.println(key);

            ResultSet res = stmt.executeQuery("SELECT last_value FROM descriptor_sequence");
            res.next();
            key = res.getLong(1);

            stmt.close();
        } catch (Exception e) {
            System.out.println("---->NEW");
            e.printStackTrace();
        }
        return key;
    }

}
