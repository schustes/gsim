package de.s2.gsim.api.impl;

import de.s2.gsim.GSimCore;
import de.s2.gsim.GSimException;
import de.s2.gsim.api.sim.impl.local.SimulationInstanceContainerLocal;
import de.s2.gsim.def.ModelDefinitionEnvironment;
import de.s2.gsim.environment.Environment;
import de.s2.gsim.sim.SimulationController;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

public class SimCoreLocalImpl implements GSimCore {

    private String configDir = "";

    private String domainDir = "";

    private String loggingLevel = "";

    private String persistDir = "";

    private String rootDir = "";

    private String ruleHandler = "jess2";

    private String templatePath = "";

    private String ns;

    public SimCoreLocalImpl() {
    }

    public SimCoreLocalImpl(Properties prop) {
        init(prop);
    }

    @Override
    public ModelDefinitionEnvironment create(String ns, Map<String, Object> props) {
        try {
            this.ns = ns;
            return new EnvironmentWrapper(new Environment(ns));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    public ModelDefinitionEnvironment create(String ns, InputStream setupFile, Map<String, Object> props) {
        try {

            Environment env = new Environment(ns);
			// EnvironmentSetup setup = new EnvironmentSetup(env);
			// setup.setInputStream(setupFile);
			// setup.runSetup();
            EnvironmentWrapper impl = new EnvironmentWrapper(env);
            return impl;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ModelDefinitionEnvironment create(String ns, java.io.File setup, Map<String, Object> props) throws GSimException {
        try {
            InputStream setupStream = setup == null ? null : new java.io.FileInputStream(setup);
            return this.create(ns, setupStream, props);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ModelDefinitionEnvironment create(String ns, String setup, Map<String, Object> props) {
        try {
            InputStream setupStream = new java.io.ByteArrayInputStream(setup.getBytes());
            return this.create(ns, setupStream, props);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public SimulationController createScenarioManager(ModelDefinitionEnvironment env
            , Map<String, Object> props
            , int steps
            , int runs) throws GSimException {

        if (env instanceof EnvironmentWrapper) {
            SimulationController m = new SimulationInstanceContainerLocal(((EnvironmentWrapper) env).getComplicatedInterface()
                    , this.ns, props, steps, runs);
            return m;
        } else {
            throw new GSimException(
                    "Tried to create a local implemenation of simulator-manager, but received a remote" + " implementation of the environment");
        }
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
    public String[] getRunningSimulations(Map<String, Object> props) {
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
            if (s.endsWith("common")) {
                return true;
            }

            return false;
        }
    }

}
