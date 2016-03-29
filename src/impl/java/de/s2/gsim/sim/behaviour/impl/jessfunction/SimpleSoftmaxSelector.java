package de.s2.gsim.sim.behaviour.impl.jessfunction;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

import jess.Fact;
import jess.JessException;
import jess.RU;
import jess.Token;
import jess.Userfunction;
import jess.Value;
import jess.ValueVector;

public class SimpleSoftmaxSelector implements Userfunction, java.io.Serializable {

    private static Logger logger = Logger.getLogger(SimpleSoftmaxSelector.class);

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private double alpha = 1;

    private jess.Context ctx;

    private ArrayList evaluations = new ArrayList();

    private double maxReward = 1;

    private double sumOfPreferences = 0;

    private double sumReward = 0;

    public SimpleSoftmaxSelector() {
    }

    public SimpleSoftmaxSelector(double maxReward) {
        this.maxReward = maxReward;
    }

    @Override
    public Value call(ValueVector vv, jess.Context context) throws JessException {

        sumOfPreferences = 0;
        sumReward = 0;
        ctx = context;
        // this.maxReward = -Double.MAX_VALUE;
        evaluations.clear();

        Value val = vv.get(1);

        Iterator iter = (Iterator) val.externalAddressValue(context);

        // Iterator iter =
        // (Iterator)ctx.getEngine().fetch("query-result").externalAddressValue(ctx);

        if (!iter.hasNext()) {
            return new Value("NIL", RU.ATOM);
        }
        ;// quick fix: shortcut-rule is always exccuted, even if no actions were selected. If this happens, the iter is empty.

        while (iter.hasNext()) {
            Object o = iter.next();
            Fact f = null;
            if (o instanceof Token) {
                Token t = (Token) o;
                f = t.fact(1);
            } else if (o instanceof Fact) {
                f = (Fact) o;
            }
            String actionName = f.getSlotValue("action-name").stringValue(ctx) + " [" + f.getSlotValue("arg") + "] " + "["
                    + f.getSlotValue("state-fact-name") + "]";

            double currentReward = f.getSlotValue("value").numericValue(ctx);
            alpha = f.getSlotValue("alpha").numericValue(ctx);
            sumReward += Math.abs(currentReward);

            currentReward = currentReward / maxReward;
            evaluations.add(new Evaluation(actionName, currentReward, f));

            // if (this.maxReward == 0) this.maxReward = 1;
            // this.maxReward = 1;

        }

        calculatePreferences();

        Fact action = draw();
        evaluations.clear();
        if (action != null) {
            Value v = null;
            v = new Value(new Value(action));
            return v;
        }
        return new Value("NIL", RU.ATOM);
    }

    @Override
    public String getName() {
        return "simplesoftmax-action-selector";
    }

    private void calculatePreferences() {
        double max = getMaxPrefValue();

        if (max <= 0) {
            max = 1;
        }

        double factor = 1;// maxReward/max;
        Iterator<Evaluation> iter = evaluations.iterator();
        while (iter.hasNext()) {
            Evaluation e = iter.next();
            // logger.debug(e.pref+","+e.pref*factor+","+maxReward+","+max+","+alpha);
            e.pref = Math.exp((e.pref * factor) / alpha);
            // logger.debug(e.pref);
            // e.pref=Math.exp((e.pref/maxReward)/alpha);
            sumOfPreferences += e.pref;
        }
    }

    private Fact draw() {

        // Utils.shuffle(evaluations); //for same prob. of events with same p

        ArrayList intervals = new ArrayList();
        Iterator iter = evaluations.iterator();

        double sum = 0;// adds up to 1 in the end

        while (iter.hasNext()) {
            Evaluation v = (Evaluation) iter.next();
            double p = v.pref / sumOfPreferences;
            String stats = v.actionName + "," + v.reward + "," + v.pref + "," + p;

            try {
                de.s2.gsim.api.sim.agent.impl.RuntimeAgent a = (de.s2.gsim.api.sim.agent.impl.RuntimeAgent) ctx.getEngine().fetch("AGENT").externalAddressValue(ctx);
                String[] statVector0 = a.getCurrentStrategy();
                String[] statVector = new String[statVector0.length + 1];
                for (int i = 0; i < statVector0.length; i++) {
                    statVector[i] = statVector0[i];
                }
                statVector[statVector.length - 1] = stats;
                a.setCurrentStrategy(statVector);
            } catch (Exception e) {
                // ignore
            }
            // prefString+=v.actionName+":"+v.pref+"-->"+p+"\n";
            if (Double.isInfinite(p)) {
                p = 1;
            } else if (Double.isNaN(p)) {

                logger.debug("Nan-Error, but was not infinite");

                p = 1;
            }
            Interval in = new Interval(sum, sum + p, v);
            sum += p;
            intervals.add(in);
        }

        // logger.debug("action-prefs:"+prefString);
        // logger.debug("sum-pref:"+this.sumOfPreferences);
        // logger.debug("max:"+maxReward);

        // double z=new cern.jet.random.Uniform(new
        // RandomJava()).nextDoubleFromTo(0,1);
        double z = cern.jet.random.Uniform.staticNextDoubleFromTo(0, 1);
        iter = intervals.iterator();
        while (iter.hasNext()) {
            Interval in = (Interval) iter.next();
            if (in.from <= z && in.to >= z) {
                double p = in.e.pref / sumOfPreferences;
                // probString+=in.e.actionName+":"+p+"/";
                // logger.debug(aName+":::"+"p: " + p+", v.pref:"+in.e.pref +",
                // sumOfPreferences: " + sumOfPreferences+", name:" +in.e.actionName);
                logger.debug("p: " + p + ", v.pref:" + in.e.pref + ", sumOfPreferences: " + sumOfPreferences + ", name:" + in.e.actionName);
                return in.e.f;
            }
        }

        StringBuffer b = new StringBuffer();
        iter = intervals.iterator();
        while (iter.hasNext()) {
            Interval in = (Interval) iter.next();
            b.append(in.from);
            b.append("-");
            b.append(in.to);
            b.append("; ");
        }
        logger.info("Draw-Error. z=" + z + ",intervals:" + b.toString());
        return null;
    }

    private double getMaxPrefValue() {

        Iterator<Evaluation> iter = evaluations.iterator();
        double m = -1;
        while (iter.hasNext()) {
            Evaluation e = iter.next();
            if (e.pref > m) {
                m = e.pref;
            }
        }
        return m;
    }

    private class Evaluation {
        String actionName = "";

        Fact f = null;

        double pref = 0;

        String reward = "";

        public Evaluation(String actionName, double pref, Fact f) {
            this.pref = pref;
            reward = new String(String.valueOf(pref));
            this.actionName = actionName;
            this.f = f;
        }
    }

    private class Interval {
        Evaluation e;

        double from = 0;

        double to = 0;

        Interval(double f, double t, Evaluation e) {
            from = f;
            to = t;
            this.e = e;
        }
    }

    /*
     * private class Ball { public int number=0; public Object object=null; public Ball(int number, Object object) { this.number=number;
     * this.object=object; } }
     */
}
