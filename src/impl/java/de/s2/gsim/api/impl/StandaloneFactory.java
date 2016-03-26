package de.s2.gsim.api.impl;

import de.s2.gsim.CoreFactory;
import de.s2.gsim.GSimCore;
import de.s2.gsim.GSimCoreFactory;

@CoreFactory(name = "Standalone")
public class StandaloneFactory extends GSimCoreFactory {

    @Override
    public GSimCore createCore() {
        // TODO replace with new messaging impl
        return new SimCoreLocalImpl();
    }

}
