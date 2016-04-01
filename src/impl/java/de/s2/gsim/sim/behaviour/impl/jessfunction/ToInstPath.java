package de.s2.gsim.sim.behaviour.impl.jessfunction;

import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.Instance;
import jess.Context;
import jess.JessException;
import jess.RU;
import jess.Userfunction;
import jess.Value;
import jess.ValueVector;

public class ToInstPath implements Userfunction, java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * call
     * 
     * @param valueVector
     *            ValueVector
     * @param context
     *            Context
     * @return Value
     * @throws JessException
     */
    @Override
    public Value call(ValueVector valueVector, Context context) throws JessException {
        try {

            String framePath = "/";
            String instName = "l";

            Value v = context.getEngine().fetch("AGENT");

            Value val = valueVector.get(1);
            framePath = val.resolveValue(context).toString();
            framePath = framePath.replace("\"", "");
            val = valueVector.get(2);
            instName = val.resolveValue(context).toString();
            instName = instName.replace("\\", "");
            instName = instName.replace("\"", "");

            Instance owner = (Instance) v.externalAddressValue(context);

            String[] p = framePath.split("/");
            Frame def = (Frame) owner.getDefinition().resolveName(p);
            if (def == null) {
                def = owner.getDefinition().getListType(p[0]);
            }
            int pos = -1;
            for (int i = 0; i < p.length; i++) {
                if (def.isSuccessor(p[i]) || p[i].equals(def.getTypeName())) {
                    pos = i - 1;
                }
            }
            String prefix = "";
            for (int i = 0; i <= pos; i++) {
                prefix += p[i];
                if (i < pos) {
                    prefix += "/";
                }
            }
            String postfix = "";
            for (int i = pos + 2; i < p.length; i++) {
                postfix += p[i];
                if (i < p.length - 1) {
                    postfix += "/";
                }
            }

            if (postfix.length() > 0) {
                return new Value(prefix + "/" + instName + "/" + postfix, RU.STRING);
            }
            return new Value(prefix + "/" + instName, RU.STRING);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * getName
     * 
     * @return String
     */
    @Override
    public String getName() {
        return "toInstPath";
    }
}
