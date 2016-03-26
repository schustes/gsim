package de.s2.gsim.api.core.impl;

import de.s2.gsim.CoreFactory;
import de.s2.gsim.GSimCore;
import de.s2.gsim.GSimCoreFactory;

@CoreFactory(name = "Legacy")
public class LegacyStandaloneFactory extends GSimCoreFactory {

    @Override
    public GSimCore createCore() {
        return new SimCoreLocalImpl();
    }

}
