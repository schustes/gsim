package de.s2.gsim.api.sim.impl.local;

import de.s2.gsim.sim.Executable;

public class BatchExecutor {

    private Executable ex;

    public BatchExecutor(Executable ex) {
        this.ex = ex;
    }

    public void execute() {
        ex.execute();
    }
}
