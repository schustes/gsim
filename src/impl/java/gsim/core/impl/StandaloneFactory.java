package gsim.core.impl;

import de.s2.gsim.core.CoreFactory;
import de.s2.gsim.core.GSimCore;
import de.s2.gsim.core.GSimCoreFactory;

@CoreFactory(name = "Standalone")
public class StandaloneFactory extends GSimCoreFactory {

    @Override
    public GSimCore createCore() {
        return new SimCoreLocalImpl();
    }

}
