package de.s2.gsim.api.sim.impl.local;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import de.s2.gsim.sim.GSimEngineException;
import de.s2.gsim.sim.communication.AgentType;
import de.s2.gsim.sim.communication.BroadcastProtocol;
import de.s2.gsim.sim.communication.BroadcastProtocolRespond;
import de.s2.gsim.sim.communication.CommunicationProtocol;
import de.s2.gsim.sim.communication.CommunicationProtocolRespond;
import de.s2.gsim.sim.communication.Communicator;
import de.s2.gsim.sim.communication.Conversation;
import de.s2.gsim.sim.communication.Message;
import de.s2.gsim.sim.communication.Messenger;

public class LocalMessenger implements Messenger, java.io.Serializable, Communicator {

    private static Logger logger = Logger.getLogger(LocalMessenger.class);

    private static final long serialVersionUID = 1L;

    private List<AgentType> handledAgents;

    private HashMap<Conversation, CommunicationProtocol> openCommunications1;

    private HashMap<Conversation, CommunicationProtocolRespond> openCommunications2;

    public LocalMessenger(List<AgentType> handledAgents) {
        this.handledAgents = handledAgents;

        openCommunications1 = new HashMap<Conversation, CommunicationProtocol>();
        openCommunications2 = new HashMap<Conversation, CommunicationProtocolRespond>();

        logger.debug("Local msg context " + toString() + "handles : ");
        try {
            for (AgentType r : handledAgents) {
                logger.debug(r.getName());
            }
        } catch (Exception e) {
            logger.error("Error:", e);
        }
    }

    @Override
    public void addAgentToHandle(AgentType r) {
        handledAgents.add(r);
    }

    @Override
    public void broadcast(BroadcastProtocol p) {
        Message m = p.broadcast();

        for (AgentType t : handledAgents) {
            for (BroadcastProtocolRespond listener : p.getListeners()) {
                logger.debug("broadcasting message of type " + m.getType() + " to " + t.getName());
                listener.onMessage(t, m);
            }
        }
    }

    @Override
    public Conversation createConversation(CommunicationProtocol initiator) throws GSimEngineException {
        double l = cern.jet.random.Uniform.staticNextDouble();

        Conversation c = new Conversation(initiator, this);
        initiator.setCommId(l);

        CommunicationProtocolRespond receiverProtocol = initiator.getReceiverProtocol();
        receiverProtocol.setCommId(l);
        receiverProtocol.setPartnerName(initiator.getOwnName());

        String partnername = initiator.getPartnerName();

        receiverProtocol.setAgent(getAgent(partnername));

        openCommunications1.put(c, initiator);
        openCommunications2.put(c, receiverProtocol);

        return c;
    }

    @Override
    public void destroy() {
        handledAgents.clear();
        openCommunications1.clear();
        openCommunications2.clear();
    }

    // it is unknown who ends the communication!
    public void endConversation(Conversation commId) {
        CommunicationProtocol p1 = openCommunications1.remove(commId);
        CommunicationProtocolRespond p2 = openCommunications2.remove(commId);
        p2.onEnd();
        p1.onEnd();
    }

    @Override
    public void endSession() {
        logger.debug("ignoring this method, because with this messegner there is message buffering");
    }

    @Override
    public Communicator getCommunicationInterface() {
        return this;
    }

    @Override
    public String getNamespace() {
        return null;
    }

    public int getPendingConversationCount(String agentName) {
        int count = 0;
        for (Conversation c : openCommunications1.keySet()) {
            if (c.getProtocol().getOwnName().equals(agentName)) {
                count++;
            }
        }
        return count;
    }

    public boolean hasPendingConversations(String agentName) {
        for (Conversation c : openCommunications1.keySet()) {
            if (c.getProtocol().getOwnName().equals(agentName)) {
                return true;
            }
        }
        return false;
    }

    public void processAllConversations() {
        logger.debug("ignoring this method, because with this messegner there is message buffering");
    }

    public void sendManagementMessage(Message m) {
        // logger.debug("In the local version everything is synchronous and
        // management messages are ignored");
    }

    @Override
    public void setHandledAgents(List<AgentType> list) {
        handledAgents = list;
    }

    @Override
    public void startConversation(Conversation commId) {

        logger.debug("Start conversation commid=" + commId);

        Message m = commId.getProtocol().getStartMessage();

        CommunicationProtocol p1 = openCommunications1.get(commId);
        CommunicationProtocolRespond p2 = openCommunications2.get(commId);

        // if (p1.getPartnerName().equals(m.getReceiver())) {
        Message resp = p2.respond(m);
        if (resp != null) {
            resp.setReceiver(p1.getOwnName());
            talk(commId, resp);
        } else {
            endConversation(commId);
        }
        // } else {
        // throw new RuntimeException("Altough there are protocols for " + commId
        // +", there is a mismatch in sender and receiver name");
        // }
    }

    @Override
    public void startSession() {
        logger.debug("ignoring this method, because with this messegner there is message buffering");
    }

    private AgentType getAgent(String name) {
        for (AgentType r : handledAgents) {
            try {
                if (r.getName().equals(name)) {
                    return r;
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
        return null;
    }

    private void talk(Conversation commId, Message m) {
        CommunicationProtocol p1 = openCommunications1.get(commId);
        CommunicationProtocolRespond p2 = openCommunications2.get(commId);

        logger.debug("m:" + m);

        if (p1.getPartnerName().equals(m.getReceiver())) {
            Message resp = p2.respond(m);

            logger.debug("resp:" + resp);

            if (resp != null) {
                talk(commId, resp);
            } else {
                endConversation(commId);
            }
        } else if (p2.getPartnerName().equals(m.getReceiver())) {
            Message resp = p1.respond(m);
            logger.debug("resp:" + resp);
            if (resp != null) {
                talk(commId, resp);
            } else {
                endConversation(commId);
            }
        } else {
            throw new RuntimeException("Altough there are protocols for " + commId + ", there is a mismatch in sender and receiver name");
        }
    }

}
