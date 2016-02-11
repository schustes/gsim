package de.s2.gsim.sim.communication;

import java.util.Observable;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

/**
 *
 * Any communication in gsim is defined ASYNCHRONOSLY (even in the single-machin case for coherent semantics). A method invocation must there for
 * register an observer if using this convenience class, or code all logic within the respond-method of the protocol.
 *
 * @author s.schuster@surrey.ac.uk
 */
public class MethodInvocationProtocolSender extends CommunicationProtocol {

    private static final long serialVersionUID = 1L;

    private String methodName = null;

    private ProtocolObservable observable = new ProtocolObservable();

    private Object[] params = null;

    private Object result = null;

    private Semaphore sema = new Semaphore(1);

    public MethodInvocationProtocolSender(String ownName, String receiverName, String methodName, Object[] params) {
        super(ownName, receiverName, new MethodInvocationProtocolReceiver());
        this.methodName = methodName;
        this.params = params;
    }

    public Observable getObservable() {
        return observable;
    }

    /**
     * Blocks until protocol has finished.
     * 
     * @return the result value of the called method
     */
    public Object getResult() {
        try {

            if (result == null) {
                sema.acquire(1);
            }
            // can only be acquired if released by protocol finish message
            sema.acquire(1);

        } catch (Exception e) {
            Logger.getLogger(MethodInvocationProtocolSender.class).error("Method invocation protocol failed:", e);
        } finally {
            // then there should be now again the free permit
            sema.release();
        }

        return result;
    }

    @Override
    public Message getStartMessage() {
        Message m = new Message(getOwnName(), getPartnerName(), "MethodInvocation");
        m.addMessageContent("params", params);
        m.addMessageContent("method", methodName);
        return m;
    }

    @Override
    public Message respond(Message message) {

        result = message.getMessageContent("result");

        observable.setChanged();
        observable.notifyObservers(result);

        sema.release();

        return null;
    }

    private class ProtocolObservable extends Observable {

        @Override
        public void setChanged() {
            super.setChanged();
        }

    }

}
