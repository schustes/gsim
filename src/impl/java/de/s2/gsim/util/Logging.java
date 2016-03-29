package de.s2.gsim.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Horribly hacked class to provide some logging.
 *
 */
public class Logging {

    public static AgentLogger AgentLogger;

    public static int ALL_OUT = 3;

    public static int FILE_OUT = 1;

    public static GSimLogger ModelLogger;

    public static PerformanceLogger pl;

    public static int SYSTEM_OUT = 2;

    private static String dir = null;

    private static FileHandler fileHandler;

    private static Handler handler;

    private static Level logLevel = Level.ALL; // Level.INFO;

    private static String getAgentLoggingDir() {
        return "/home/gsim/tmp/facts";
    }

    private static Level getLoggingLevel() {
        String s = "FINEST";
        Level l = Level.parse(s);
        return l;
    }

    private static int getLoggingMode() {
        return 1;
    }

    static {

        int handlerType = 1;
        Level l = getLoggingLevel();

        try {
            ModelLogger = new GSimLogger("Model");
            AgentLogger = new AgentLogger("Agent");
            pl = new PerformanceLogger("Only for measuring");

            if (handlerType == SYSTEM_OUT) {
                handler = new ConsoleHandler();
                ModelLogger.addHandler(handler);
                AgentLogger.addHandler(handler);
                pl.addHandler(handler);
            } else if (handlerType == FILE_OUT) {
                handler = new FileHandler("/home/newties/temp/agentsrv-logging/everything_else.log", 1000000, 1);
                Handler handler3 = new FileHandler("/home/newties/temp/agentsrv-logging/performance.log", 1000000, 1);

                ModelLogger.addHandler(handler);
                pl.addHandler(handler3);
            } else if (handlerType == ALL_OUT) {
                Handler a = new FileHandler("/home/newties/temp/agentsrv-logging/everything_else.log", 1000000, 1);
                Handler b = new ConsoleHandler();
                ModelLogger.addHandler(a);
                ModelLogger.addHandler(b);
                pl.addHandler(a);
                pl.addHandler(b);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (l != null) {
            logLevel = l;
        }
        handler.setLevel(logLevel);
    }

    public static class AgentLogger extends GSimLogger {

        private HashMap agentLoggers = new HashMap();

        public AgentLogger(String subsystem) {
            super(subsystem);
        }

        public void fine(String agentName, String msg, int time) {
            Logger n = (Logger) agentLoggers.get(agentName);
            if (n != null) {
                n.fine(msg + " (step: " + time + ")");
            } else {
                n = new GSimLogger("Agent");
                try {
                    Handler a = new FileHandler(getAgentLoggingDir() + "/" + agentName + "_%g.log", 1000000, 1);
                    n.addHandler(a);
                } catch (Exception e) {
                    ModelLogger.severe("Could not initalise log file for " + agentName + "[" + e.getMessage() + "]");
                }
                n.fine(msg + " (step: " + time + ")");
            }
            agentLoggers.put(agentName, n);
        }

        public void finer(String agentName, String msg, int time) {
            Logger n = (Logger) agentLoggers.get(agentName);
            if (n != null) {
                n.finer(msg + " (step: " + time + ")");
            } else {
                n = new GSimLogger("Agent");
                try {
                    Handler a = new FileHandler(getAgentLoggingDir() + "/" + agentName + "_%g.log");
                    // a.setFormatter(new UserInterfaceFormatter());
                    n.addHandler(a);
                } catch (Exception e) {
                    ModelLogger.severe("Could not initalise log file for " + agentName + "[" + e.getMessage() + "]");
                }
                n.finer(msg + " (step: " + time + ")");
            }
            agentLoggers.put(agentName, n);
        }

        public void finest(String agentName, String msg, int time) {
            Logger n = (Logger) agentLoggers.get(agentName);
            if (n != null) {
                n.finest(msg + " (step: " + time + ")");
            } else {
                n = new GSimLogger("Agent");
                try {
                    Handler a = new FileHandler(getAgentLoggingDir() + "/" + agentName + "_%g.log", 1000000, 1);
                    n.addHandler(a);
                } catch (Exception e) {
                    ModelLogger.severe("Could not initalise log file for " + agentName + "[" + e.getMessage() + "]");
                }
                n.finest(msg + " (step: " + time + ")");
            }
            agentLoggers.put(agentName, n);
        }

        public void info(String agentName, String msg, int time) {

            Logger n = (Logger) agentLoggers.get(agentName);
            if (n != null) {
                n.info(msg + " (step: " + time + ")");
            } else {
                n = new GSimLogger("Agent");
                try {
                    Handler a = new FileHandler(getAgentLoggingDir() + "/" + agentName + ".log", 1000000, 1);
                    a.setFormatter(new UserInterfaceFormatter());
                    n.addHandler(a);
                } catch (Exception e) {
                    ModelLogger.severe("Could not initalise log file for " + agentName + "[" + e.getMessage() + "]");
                }
                n.info(msg + " (step: " + time + ")");
            }
            agentLoggers.put(agentName, n);
        }

        public void log(String agentName, String msg) {
            Logger n = (Logger) agentLoggers.get(agentName);
            if (n != null) {
            } else {
                n = new GSimLogger("Agent");
                try {
                    Handler a = new FileHandler(getAgentLoggingDir() + "/" + agentName + "_%g.log");
                    n.addHandler(a);
                } catch (Exception e) {
                    ModelLogger.severe("Could not initalise log file for " + agentName + "[" + e.getMessage() + "]");
                }

            }
            n.log(logLevel, msg);
            agentLoggers.put(agentName, n);
        }

        public void severe(String agentName, String msg, int time) {
            Logger n = (Logger) agentLoggers.get(agentName);
            if (n != null) {
                n.severe(msg + " (step: " + time + ")");
            } else {
                n = new GSimLogger("Agent");
                try {
                    Handler a = new FileHandler(getAgentLoggingDir() + "/" + agentName + "_%g.log");
                    n.addHandler(a);
                } catch (Exception e) {
                    ModelLogger.severe("Could not initalise log file for " + agentName + "[" + e.getMessage() + "]");
                }
                n.severe(msg + " (step: " + time + ")");
            }
            agentLoggers.put(agentName, n);
        }

        public void warning(String agentName, String msg, int time) {
            Logger n = (Logger) agentLoggers.get(agentName);
            if (n != null) {
                n.warning(msg + " (step: " + time + ")");
            } else {
                n = new GSimLogger("Agent");
                try {
                    Handler a = new FileHandler(getAgentLoggingDir() + "/" + agentName + ".log", 1000000, 1);
                    n.addHandler(a);
                } catch (Exception e) {
                    ModelLogger.severe("Could not initalise log file for " + agentName + "[" + e.getMessage() + "]");
                }
                n.warning(msg + " (step: " + time + ")");
            }
            agentLoggers.put(agentName, n);
        }

    }

    public static class GSimLogger extends Logger {
        public GSimLogger(String subsystem) {
            super(subsystem, null);
            setLevel(logLevel);
        }

        public void log(String msg) {
            super.log(logLevel, msg);
        }

        public void severe(Throwable e) {
            StringWriter s = new StringWriter();
            PrintWriter w = new PrintWriter(s);
            e.printStackTrace(w);
            super.severe(s.toString());
        }

    }

    public static class PerformanceLogger extends GSimLogger {

        private long start;

        public PerformanceLogger(String subsystem) {
            super(subsystem);
        }

        public void start() {
            start = System.currentTimeMillis();
        }

        public void start(String additionalInfo) {
            start = System.currentTimeMillis();
            super.finer("Measuring " + additionalInfo);
        }

        public void stop() {
            long stop = System.currentTimeMillis();
            this.finer("Measured: " + ((stop - start) / 1000d) + " seconds");
        }

    }

    static class UserInterfaceFormatter extends SimpleFormatter {
        @Override
        public String formatMessage(LogRecord log) {
            String m = log.getMessage();
            String source = log.getSourceClassName();
            return source + ": " + m;
        }
    }

}
