package de.s2.gsim.sim.communication;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cern.jet.random.Uniform;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String messageType;

    private HashMap<String, Object> messageContents;

    private String messageId;

    private String receiver;

    private String sender;

    public Message(String sender, String receiver, String type) {
        messageType = type;
        this.receiver = receiver;
        this.sender = sender;
        messageContents = new HashMap<String, Object>();
        messageId = Message.class.getSimpleName() + "-" + Uniform.staticNextIntFromTo(0, 10000000);
    }

    protected Message(Message clone) {

        messageContents = new HashMap<String, Object>();
        messageType = clone.messageType;
        receiver = clone.receiver;
        sender = clone.sender;

        for (Map.Entry<String, Object> e : clone.messageContents.entrySet()) {
            messageContents.put(e.getKey(), e.getValue());
        }

    }

    public void addMessageContent(String key, Object o) {
        messageContents.put(key, o);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Message) {
            Message m = (Message) o;
            return m.messageId.equals(messageId);
        }
        return false;
    }

    public Object getMessageContent(String key) {
        return messageContents.get(key);
    }

    public String getReceiver() {
        return receiver;
    }

    public String getSender() {
        return sender;
    }

    public String getType() {
        return messageType;
    }

    @Override
    public int hashCode() {
        return 78126;
    }

    public void setReceiver(String name) {
        receiver = name;
    }

    @Override
    public String toString() {
        String s = new String("\nMessage-Begin\n");
        s += "id: " + String.valueOf(messageId) + "\n";
        s += "Sender: " + sender + "\n";
        s += "Receiver " + receiver + "\n";
        s += "type: " + String.valueOf(messageType) + "\n";
        Iterator<String> keys = messageContents.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            s += "Object-Type: " + key + ", port: " + messageContents.get(key) + "\n";
        }
        s += "\nMessage-End\n";
        return s;
    }

}
