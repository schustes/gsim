package de.s2.gsim.environment;

import javax.management.ObjectInstance;

import de.s2.gsim.api.objects.impl.UnitWrapper;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.objects.attribute.IntervalAttribute;
import de.s2.gsim.objects.attribute.NumericalAttribute;
import de.s2.gsim.objects.attribute.OrderedSetAttribute;
import de.s2.gsim.objects.attribute.SetAttribute;

public class Generator {

    public enum Method {
        Normal, Uniform
    }

    private Method method = Method.Normal;

    public Generator() {
        super();
    }

    /**
     * Randomises attribute values of agent itself and all contained objects, for attributes that are declared as 'mutable'.
     * 
     * @param a GenericAgent
     * @param svar double
     * @return GenericAgent
     */
    public GenericAgent randomiseAttributeValues(GenericAgent a, double svar, Method method) {
        GenericAgent agent = null;
        if (method.equals(Method.Normal)) {
            agent = (GenericAgent) this.randomiseAttributeValues(a, svar);
            String[] lists = a.getChildInstanceListNames();
            for (int i = 0; i < lists.length; i++) {
                Instance[] inst = agent.getChildInstances(lists[i]);
                for (int j = 0; j < inst.length; j++) {
                    Instance obj = this.randomiseAttributeValues(inst[j], svar);
                    agent.setChildInstance(lists[i], obj);
                }
            }
        } else {
            agent = (GenericAgent) this.randomiseAttributeValues(a);
            String[] lists = a.getChildInstanceListNames();
            for (int i = 0; i < lists.length; i++) {
                Instance[] inst = agent.getChildInstances(lists[i]);
                for (int j = 0; j < inst.length; j++) {
                    Instance obj = this.randomiseAttributeValues(inst[j]);
                    agent.setChildInstance(lists[i], obj);
                }
            }

        }
        return agent;
    }

    public ObjectInstance randomiseAttributeValues(ObjectInstance a, double svar, Method method) {
        if (method.equals(Method.Normal)) {
            Instance b = (Instance) ((UnitWrapper) a).toUnit();
            b = this.randomiseAttributeValues(b, svar);
            return a;
        } else {
            Instance b = (Instance) ((UnitWrapper) a).toUnit();
            b = this.randomiseAttributeValues(b);
            return a;
        }
    }

    private Instance randomiseAttributeValues(Instance obj) {

        String[] lists = obj.getAttributesListNames();

        for (int j = 0; j < lists.length; j++) {
            Attribute[] atts = obj.getAttributes(lists[j]);
            for (int k = 0; k < atts.length; k++) {
                DomainAttribute da = obj.getDefinition().getAttribute(lists[j], atts[k].getName());
                if (da.isMutable()) {
                    if (atts[k] instanceof SetAttribute) {
                        SetAttribute set = (SetAttribute) atts[k];
                        if (set.getFillers().length > 0) {
                            int v = cern.jet.random.Uniform.staticNextIntFromTo(0, da.getFillers().length - 1);
                            set.removeAllEntries();
                            set.addEntry(set.getFillers()[v]);
                        }
                    } else if (atts[k] instanceof IntervalAttribute) {
                        String value = da.getDefaultValue();
                        double x = 0;
                        double y = 0;
                        if (value.equals("")) {
                            value = "0";
                            x = (Double.valueOf(value)).doubleValue() - 1;
                            y = x + 2;
                        } else if (value.indexOf(" - ") > 0) {
                            String[] fromTo = value.split(" - ");
                            x = (Double.valueOf(fromTo[0])).doubleValue();
                            y = (Double.valueOf(fromTo[1])).doubleValue();
                        } else {
                            x = (Double.valueOf(value)).doubleValue() - 1;
                            y = x + 2;
                        }
                        double v = cern.jet.random.Uniform.staticNextDoubleFromTo(x, y);
                        IntervalAttribute att = (IntervalAttribute) atts[k];
                        att.setFrom(v);
                        att.setTo(v);
                    } else if (atts[k] instanceof NumericalAttribute) {
                        NumericalAttribute att = (NumericalAttribute) atts[k];
                        double mean = Double.parseDouble(da.getDefaultValue());
                        double v = cern.jet.random.Uniform.staticNextDoubleFromTo(mean - mean, mean * 2);
                        att.setValue(v);
                    }
                    obj.addOrSetAttribute(lists[j], atts[k]);
                }
            }
        }
        return obj;
    }

    /**
     * Randomises attribute values. For this, 'svar' is interpreted as the percentage of variation in the values of the default values of the domain
     * attributes, which are seen as mean values of the normal distribution from which the values are drawn. This means that svar is expected to be >0
     * and <1.
     * 
     * @param obj Instance
     * @param svar double
     * @return Instance
     */
    private Instance randomiseAttributeValues(Instance obj, double svar) {

        String[] lists = obj.getAttributesListNames();

        for (int j = 0; j < lists.length; j++) {
            Attribute[] atts = obj.getAttributes(lists[j]);
            for (int k = 0; k < atts.length; k++) {
                DomainAttribute da = obj.getDefinition().getAttribute(lists[j], atts[k].getName());
                if (da.isMutable()) {
                    if (atts[k] instanceof OrderedSetAttribute) {
                        OrderedSetAttribute set = (OrderedSetAttribute) atts[k];
                        double mean = set.getOrder(da.getDefaultValue());
                        double v = cern.jet.random.Normal.staticNextDouble(mean, svar);
                        if (set.getFillers().length <= v) {
                            v = set.getFillers().length - 1;
                        } else if (v < 0) {
                            v = 0;
                        }
                        String[] f = set.getFillers();
                        double minDiff = 1000;
                        double order = 0;
                        for (int i = 0; i < f.length; i++) {
                            double o = set.getOrder(f[i]);
                            if (Math.abs(o - v) < minDiff) {
                                order = o;
                                minDiff = Math.abs(o - v);
                            }
                        }
                        set.removeAllEntries();
                        set.addEntry(set.getFiller(order));
                    } else if (atts[k] instanceof SetAttribute) {
                        SetAttribute set = (SetAttribute) atts[k];
                        if (set.getFillers().length > 0) {
                            // int v = cern.jet.random.Uniform.staticNextIntFromTo(0,
                            // da.getFillers().length - 1);
                            // set.removeAllEntries();
                            // set.addEntry(set.getFillers()[v]);
                        }
                    } else if (atts[k] instanceof IntervalAttribute) {
                        String value = da.getDefaultValue();
                        double x = 0;
                        double y = 0;
                        if (value.equals("")) {
                            value = "0";
                            x = (Double.valueOf(value)).doubleValue() - 1;
                            y = x + 2;
                        } else if (value.indexOf(" - ") > 0) {
                            String[] fromTo = value.split(" - ");
                            x = (Double.valueOf(fromTo[0])).doubleValue();
                            y = (Double.valueOf(fromTo[1])).doubleValue();
                        } else {
                            x = (Double.valueOf(value)).doubleValue() - 1;
                            y = x + 2;
                        }
                        double interval = (y - x);
                        if (interval > 0) {
                            double mean = interval / 2;
                            double v = cern.jet.random.Normal.staticNextDouble(mean, svar);
                            if (mean > 0 && v < 0) {
                                v = 0;
                            }
                            IntervalAttribute att = (IntervalAttribute) atts[k];
                            att.setFrom(v);
                            att.setTo(v);
                        }
                    } else if (atts[k] instanceof NumericalAttribute) {
                        NumericalAttribute att = (NumericalAttribute) atts[k];
                        double mean = Double.parseDouble(da.getDefaultValue());
                        // double variance = mean*svar;
                        double v = cern.jet.random.Normal.staticNextDouble(mean, svar);
                        if (mean > 0 && v < 0) {
                            v = 0;
                        }
                        att.setValue(v);
                    }
                    obj.addOrSetAttribute(lists[j], atts[k]);
                }
            }
        }
        return obj;
    }

}
