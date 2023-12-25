package de.s2.gsim.api.sim.impl.local;

import de.s2.gsim.sim.SimulationScheduler;
import de.s2.gsim.sim.Steppable;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class StandaloneSchedulerOld implements SimulationScheduler {

    private static Logger logger = Logger.getLogger(StandaloneSchedulerOld.class);

    // Listeners
    private CopyOnWriteArrayList<SimulationInstanceListener> listeners = new CopyOnWriteArrayList<SimulationInstanceListener>();

    // What the scheduler schedules
    private Steppable model;

    private boolean run = false;

    // Scheduler proper
    private ScheduleThread runner = null;

    private boolean step = false;

    private boolean stop = false;

    // Control variables for schedulingthread.
    private boolean stopThread = false;

    // Model-Time set by caller when the execution has to stop
    private long stopTime = Long.MAX_VALUE;

    // Current time of the common to be scheduled
    private int time = 0;

    public StandaloneSchedulerOld(Steppable model) {
        this.model = model;
    }

    @Override
    public long getCurrentTime() {
        return time;
    }

    @Override
    public void pause() {
        run = false;
        step = false;
    }

    public void registerSimulationInstanceListener(SimulationInstanceListener l) {
        listeners.add(l);
    }

    @Override
    public void resume() {
        run = true;
    }

    @Override
    public void run(long t) {

        stopTime = t + 1;

        if (runner == null) {
            runner = new ScheduleThread();
            runner.start();
        }

        run = true;
        step = false;

    }

    @Override
    public void setSteppable(Steppable svm) {
        model = svm;
    }

    @Override
    public void shutdown() {
        stop();
        exitSim();
    }

    private void exitSim() {
        if (runner != null) {
            stopThread = true;
            run = false;
            step = false;
            stop = true;
            runner = null;
        }
    }

    private void notifyStep() {
        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            SimulationInstanceListener l = (SimulationInstanceListener) iter.next();
            try {
            l.onStep(time);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void stop() {
        stop = true;
        run = false;
        step = false;
    }

    private class ScheduleThread extends Thread {

        long alive = System.currentTimeMillis();

        boolean commandFinished = false;

        public ScheduleThread() {
            super(String.valueOf(Math.random()));
        }

        @Override
        public void run() {

            try {

                while (!stopThread || (stopThread && !commandFinished)) {

                    if (run && time == stopTime) {
                        stop = true;
                        run = false;
                    } else if (!step && run) {

                        model.preStep(time);
                        model.step();
                        model.postStep();

                        notifyStep();

                        time++;
                    } else if (stop) {
                        stop = false;
                        run = false;
                        step = false;
                    } else {
                        commandFinished = true;
                        logger.debug("StandaloneController says: I have nothing to do right now!");
                    }
                    sleep(1);
                }

            } catch (Exception e) {
                logger.error(e);
                e.printStackTrace();
            }

        }

    }

    @Override
    public Steppable getSteppable() {
        return model;
    }

}
