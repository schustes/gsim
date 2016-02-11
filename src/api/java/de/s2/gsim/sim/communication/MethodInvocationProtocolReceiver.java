package de.s2.gsim.sim.communication;

import java.lang.reflect.Method;

public class MethodInvocationProtocolReceiver extends CommunicationProtocolRespond {

    private static final long serialVersionUID = 1L;

    @Override
    public Message respond(Message message) {

        Message ret = new Message(agent.getName(), message.getSender(), "MethodInvocation");

        try {
            Object[] params = (Object[]) message.getMessageContent("params");
            String method = (String) message.getMessageContent("method");

            Class<?>[] types = new Class[params.length];
            for (int i = 0; i < types.length; i++) {
                types[i] = params[i].getClass();
            }
            Method m = agent.getClass().getMethod(method, types);
            Object val = m.invoke(agent, params);

            ret.addMessageContent("result", val);
            return ret;

        } catch (Exception e) {
            ret.addMessageContent("result", e.getMessage());
            e.printStackTrace();
        }

        return ret;

    }
}
