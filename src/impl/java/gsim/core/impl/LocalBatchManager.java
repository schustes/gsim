package gsim.core.impl;

import de.s2.gsim.core.BatchManager;
import de.s2.gsim.sim.engine.Executable;

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
