/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package common;

import common.log.LoggerName;
import common.log.SimpleLogger;
import java.security.AccessControlException;

public abstract class Logger {
    private static Logger logger = null;

    public static final Logger getLogger(Class cl) {
        if (logger == null) {
            initializeLogger();
        }

        return logger.getLoggerImpl(cl);
    }

    private static synchronized void initializeLogger() {
        if (logger == null) {
            String loggerName = LoggerName.NAME;

            try {
                loggerName = System.getProperty("logger");
                if (loggerName == null) {
                    loggerName = LoggerName.NAME;
                }

                logger = (Logger)Class.forName(loggerName).newInstance();
            } catch (IllegalAccessException var2) {
                logger = new SimpleLogger();
                logger.warn("Could not instantiate logger " + loggerName + " using default");
            } catch (InstantiationException var3) {
                logger = new SimpleLogger();
                logger.warn("Could not instantiate logger " + loggerName + " using default");
            } catch (AccessControlException var4) {
                logger = new SimpleLogger();
                logger.warn("Could not instantiate logger " + loggerName + " using default");
            } catch (ClassNotFoundException var5) {
                logger = new SimpleLogger();
                logger.warn("Could not instantiate logger " + loggerName + " using default");
            }
        }
    }

    protected Logger() {
    }

    public abstract void debug(Object var1);

    public abstract void debug(Object var1, Throwable var2);

    public abstract void error(Object var1);

    public abstract void error(Object var1, Throwable var2);

    public abstract void fatal(Object var1);

    public abstract void fatal(Object var1, Throwable var2);

    public abstract void info(Object var1);

    public abstract void info(Object var1, Throwable var2);

    public abstract void warn(Object var1);

    public abstract void warn(Object var1, Throwable var2);

    protected abstract Logger getLoggerImpl(Class var1);

    public void setSuppressWarnings(boolean w) {
    }
}
