package gsim.core.impl;

import de.s2.gsim.core.ModelDefinitionEnvironment;
import gsim.def.Environment;

public class EnvLocalImpl extends AbstractEnv implements ModelDefinitionEnvironment {

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
