package gsim.core.impl;

import de.s2.gsim.core.BatchManager;
import de.s2.gsim.sim.engine.Executable;
import gsim.sim.engine.local.BatchExecutor;

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
