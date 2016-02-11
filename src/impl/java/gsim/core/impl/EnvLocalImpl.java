package gsim.core.impl;

import de.s2.gsim.core.DefinitionEnvironment;
import gsim.def.Environment;

public class EnvLocalImpl extends AbstractEnv implements DefinitionEnvironment {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public EnvLocalImpl(Environment env) {
        super(env);
    }

    @Override
    public void destroy() {
        env = null;
    }

}
