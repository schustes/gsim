package de.s2.gsim.api.core.impl;

import de.s2.gsim.sim.BatchManager;
import de.s2.gsim.sim.Executable;

public class LocalBatchManager implements BatchManager {

    private Executable e;

    public LocalBatchManager(Executable e) {
        this.e = e;
    }

    @Override
    public void start() {
        e.execute();
    }

}
