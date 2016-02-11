package gsim.core.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;

import de.s2.gsim.core.BatchManager;
import de.s2.gsim.core.GSimCore;
import de.s2.gsim.core.DefinitionEnvironment;
import de.s2.gsim.core.GSimException;
import de.s2.gsim.core.ScenarioConnector;
import de.s2.gsim.core.ScenarioManager;
import gsim.def.Environment;
import gsim.sim.engine.local.SimulationInstanceContainerLocal;

public class SimCoreLocalImpl implements GSimCore {

    private String configDir = "";

    private String domainDir = "";

    private String loggingLevel = "";

    private String persistDir = "";

    private String rootDir = "";

    private String ruleHandler = "jess2";

    private String templatePath = "";

    public SimCoreLocalImpl() {
    }

    public SimCoreLocalImpl(Properties prop) {
        init(prop);
    }

    @Override
    public ScenarioConnector connectScenarioManager(String ns, HashMap props) throws GSimException {
        try {
            ScenarioConnector m = new ScenarioManagerLocal(ns);
            return m;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public DefinitionEnvironment create(String ns, HashMap props) {
        try {
            Environment env = Environment.newInstance(ns);
            EnvLocalImpl impl = new EnvLocalImpl(env);
            return impl;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    public DefinitionEnvironment create(String ns, InputStream setup, HashMap props) {
        try {

            Environment env = Environment.newInstance(ns, setup);

            Logger.getLogger(SimCoreLocalImpl.class).debug(env);

            EnvLocalImpl impl = new EnvLocalImpl(env);
            return impl;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public DefinitionEnvironment create(String ns, java.io.File setup, HashMap props) throws GSimException {
        try {
            InputStream setupStream = setup == null ? null : new java.io.FileInputStream(setup);
            return this.create(ns, setupStream, props);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public DefinitionEnvironment create(String ns, String setup, HashMap props) {
        try {
            InputStream setupStream = new java.io.ByteArrayInputStream(setup.getBytes());
            return this.create(ns, setupStream, props);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public BatchManager createBatchManager(de.s2.gsim.sim.engine.Executable e, HashMap props) throws GSimException {
        LocalBatchManager m = new LocalBatchManager(e);
        return m;
    }

    @Override
    public ScenarioManager createScenarioManager(DefinitionEnvironment env, HashMap props, int steps, int runs) throws GSimException {

        if (env instanceof EnvLocalImpl) {
            ScenarioManager m = new ScenarioManagerLocal(((EnvLocalImpl) env).getComplicatedInterface(), props, steps, runs);
            return m;
        } else {
            throw new GSimException(
                    "Tried to create a local implemenation of scenario-manager, but received a remote" + " implementation of the environment");
        }
    }

    public void endSession() throws GSimException {
        // TODO Auto-generated method stub

    }

    public String getConfigDir() {
        return configDir;
    }

    public String getDomainDir() {
        return domainDir;
    }

    public String getLoggingLevel() {
        return loggingLevel;
    }

    @Override
    public InputStream getModelDefinition(String path) {
        try {
            File file = new File(path);
            FileInputStream s = new FileInputStream(file);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getPersistenceDir() {
        return persistDir;
    }

    public String getRootDir() {
        return rootDir;
    }

    public String getRuleHandlerType() {
        return ruleHandler;
    }

    @Override
    public String[] getRunningSimulations(HashMap props) {
        return SimulationInstanceContainerLocal.listNameSpaces();
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public String[] listModelPaths() {
        try {
            ArrayList<String> list = new ArrayList<String>();
            File dir = new File("../repos");
            if (dir.exists()) {
                File[] f = dir.listFiles(new Filter());
                for (int i = 0; i < f.length; i++) {
                    if (!f[i].isDirectory()) {
                        list.add(f[i].getName());
                    }
                }
                String[] res = new String[list.size()];
                list.toArray(res);
                return res;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public DefinitionEnvironment open(String ns, InputStream setup, HashMap props) {
        try {
            Environment env = Environment.openInstance(ns, setup);
            EnvLocalImpl impl = new EnvLocalImpl(env);
            return impl;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public DefinitionEnvironment open(String ns, java.io.File setup, HashMap props) throws GSimException {
        try {
            return this.open(ns, new java.io.FileInputStream(setup), props);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public DefinitionEnvironment open(String ns, String setup, HashMap props) {
        try {
            InputStream setupStream = new java.io.ByteArrayInputStream(setup.getBytes());
            return this.open(ns, setupStream, props);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setTemplatePath(String path) {
        templatePath = path;
    }

    public void startSession() throws GSimException {
        // TODO Auto-generated method stub

    }

    public void startSession(int concurrencyLimit) throws GSimException {

    }

    private void init(Properties prop) {

        rootDir = prop.getProperty("root.dir", "c:/tmp");
        persistDir = prop.getProperty("persistence.dir", "c:/tmp");
        configDir = prop.getProperty("config.dir", "c:/tmp");
        domainDir = prop.getProperty("imatcher.domain.dir", "c:/tmp");
        templatePath = prop.getProperty("template", "c:/tmp/setup.xml");
        loggingLevel = prop.getProperty("logging.level");
        ruleHandler = prop.getProperty("rule.handler");

    }

    private class Filter implements FilenameFilter {
        @Override
        public boolean accept(File f, String s) {
            if (s.endsWith("model")) {
                return true;
            }

            return false;
        }
    }

}
