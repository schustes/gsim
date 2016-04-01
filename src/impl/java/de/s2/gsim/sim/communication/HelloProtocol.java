package de.s2.gsim.sim.communication;

import org.apache.log4j.Logger;

public class HelloProtocol extends CommunicationProtocol {

    private static Logger logger = Logger.getLogger(HelloProtocol.class);

    private static final long serialVersionUID = 1L;

    public HelloProtocol(String ownName, String partnerName) {
        super(ownName, partnerName, new HelloProtocolRespond());
    }

    public String getName() {
        return "HelloProtocol";
    }

    @Override
    public Message getStartMessage() {
        Message helloMessage = new Message(super.getOwnName(), super.getPartnerName(), "hello");
        logger.info(getOwnName() + ": Sending a hello to " + getPartnerName());
        return helloMessage;
    }

    @Override
    public void onEnd() {
        logger.info(getOwnName() + ": Hello-Conversation has ended");
    }

    /**
     * onMessageReceive
     * 
     * @param m
     *            Message
     * @todo Implement this gsim.sim.engine.CommunicationProtocol method
     */
    @Override
    public Message respond(Message m) {
        logger.info(getOwnName() + ": Received message from " + m.getSender() + ", type=" + m.getType());
        if (m.getType().equals("hello")) {
            logger.info(getOwnName() + ": sending hello-response to " + m.getSender());
            Message response = new Message(m.getReceiver(), m.getSender(), "hello-response");
            return response;
        } else if (m.getType().equals("hello-response")) {
            logger.info(getOwnName() + ": saying 'leave me alone' to " + m.getSender());
            Message response = new Message(m.getReceiver(), m.getSender(), "don't bother me");
            return response;
        } else if (m.getType().equals("dont bother me")) {
            return null; // Ok, I don't bother you.
        }

        return null;// don't know what to say=end comm.
    }

}
