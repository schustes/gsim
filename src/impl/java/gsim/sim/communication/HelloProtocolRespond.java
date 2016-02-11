package gsim.sim.communication;

import org.apache.log4j.Logger;

import de.s2.gsim.sim.communication.CommunicationProtocolRespond;
import de.s2.gsim.sim.communication.Message;

public class HelloProtocolRespond extends CommunicationProtocolRespond {

    private static Logger logger = Logger.getLogger(HelloProtocolRespond.class);

    private static final long serialVersionUID = 1L;

    @Override
    public void onEnd() {
        System.out.println(super.agent.getName() + ": Hello-Conversation has ended");
    }

    @Override
    public Message respond(Message m) {
        logger.info(super.agent.getName() + ": Received message from " + m.getSender() + ", type=" + m.getType());
        if (m.getType().equals("hello")) {
            logger.info(super.agent.getName() + ": sending hello-response to " + m.getSender());
            Message response = new Message(m.getReceiver(), m.getSender(), "hello-response");
            return response;
        } else if (m.getType().equals("hello-response")) {
            logger.info(super.agent.getName() + ": saying 'leave me alone' to " + m.getSender());
            Message response = new Message(m.getReceiver(), m.getSender(), "don't bother me");
            return response;
        } else if (m.getType().equals("dont bother me")) {
            return null; // Ok, I don't bother you.
        }

        return null;// don't know what to say=end comm.
    }

}
