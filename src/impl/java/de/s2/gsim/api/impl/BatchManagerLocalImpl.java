package de.s2.gsim.api.impl;

import de.s2.gsim.api.sim.impl.local.BatchExecutor;
import de.s2.gsim.sim.BatchManager;
import de.s2.gsim.sim.Executable;

public class BatchManagerLocalImpl implements BatchManager {

    private Executable e;

    public BatchManagerLocalImpl(Executable e) {
        this.e = e;
    }

    @Override
    public void start() {
        new BatchExecutor(e).execute();
    }

}
