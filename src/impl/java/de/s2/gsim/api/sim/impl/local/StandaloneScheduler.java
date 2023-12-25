package de.s2.gsim.api.sim.impl.local;

import de.s2.gsim.sim.SimulationScheduler;
import de.s2.gsim.sim.Steppable;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class StandaloneScheduler implements SimulationScheduler {

    private static final int SCHEDULE_INTERVAL_MILLIS = 3;

    private static final int SCHEDULE_START_DELAY = 0;

    // Simulation Listeners
    private CopyOnWriteArrayList<SimulationInstanceListener> listeners = new CopyOnWriteArrayList<SimulationInstanceListener>();

    // What the scheduler schedules
    private Steppable model;

    // Model-Time set by caller when the execution has to stop
    private long stopTime = Long.MAX_VALUE;

    // Current discrete time step of the execution
    private int time = 0;

    ScheduledFuture<?> scheduledFuture;

    // The scheduler running the model at interval SCHEDULE_INTERVAL_MILLIS
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    enum State {INACTIVE, PAUSED, RUNNING, STOPPED};

    private State state = State.INACTIVE;

    public StandaloneScheduler(Steppable model) {
        this.model = model;
    }

    public void registerSimulationInstanceListener(SimulationInstanceListener l) {
        listeners.add(l);
    }

    @Override
    public long getCurrentTime() {
        return time;
    }

    @Override
    public Steppable getSteppable() {
        return model;
    }

    @Override
    public void setSteppable(Steppable svm) {
        model = svm;
    }

    @Override
    public void run(long requiredSteps) {

        stopTime = requiredSteps;

        scheduledFuture = this.executor.scheduleAtFixedRate(new SteppableExecutor()
                , SCHEDULE_START_DELAY
                , SCHEDULE_INTERVAL_MILLIS
                , TimeUnit.MILLISECONDS);

        this.state = State.RUNNING;

    }

    @Override
    public void pause() {
        this.state = State.PAUSED;
    }

    @Override
    public void resume() {
        this.state = State.RUNNING;
    }


    @Override
    public void shutdown() {
        this.state = State.STOPPED;
        scheduledFuture.cancel(false);
        executor.shutdown();
    }

    private class SteppableExecutor implements Runnable {

        @Override
        public void run() {

            if (state == State.RUNNING) {
                model.preStep(time);
                model.step();
                model.postStep();

                stepFinished();

                time++;
            }

            if (time == stopTime) {
                state = State.STOPPED;
                stepFinished();
            }


        }

    }

    private void stepFinished() {
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


}
