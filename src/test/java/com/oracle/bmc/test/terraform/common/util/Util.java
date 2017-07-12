package com.oracle.bmc.test.terraform.common.util;

import com.sun.jmx.snmp.ThreadContext;

import java.util.logging.*;

import static com.oracle.bmc.test.terraform.common.util.UtilConstants.*;

/**
 * Created by mseelam on 6/25/2017.
 */
public class Util {

    /**
     * Returns a JUnit Logger customized for logging JUnit 'core' operations.
     *
     * @returns JUnit Core Logger
     */
    public static Logger getJunitCoreLogger() {
        Logger logger = LogManager.getLogManager().getLogger("JUnitCoreLogger");
        if (logger == null) {
            logger = Logger.getLogger("JUnitCoreLogger");
            logger.setUseParentHandlers(false);
            ConsoleHandler customHandler = new ConsoleHandler();
            customHandler.setLevel(Level.FINE);
            customHandler.setFormatter(new JUnitCoreLogFormatter());
            logger.addHandler(customHandler);
            logger.setLevel(Level.FINE);
        }
        return logger;
    }

    /**
     * Returns a JUnit Logger customized for logging JUnit 'test' operations.
     *
     * @returns JUnit Test Logger
     */
    public static Logger getJunitTestLogger() {
        Logger logger = LogManager.getLogManager().getLogger("JunitTestLogger");
        if (logger == null) {
            logger = Logger.getLogger("JunitTestLogger");
            logger.setUseParentHandlers(false);
            ConsoleHandler customHandler = new ConsoleHandler();
            customHandler.setLevel(UtilConstants.JUNIT_TEST_LOGGER_LEVEL);
            customHandler.setFormatter(new JUnitTestLogFormatter());
            logger.addHandler(customHandler);
            logger.setLevel(UtilConstants.JUNIT_TEST_LOGGER_LEVEL);
        }
        return logger;
    }

    /**
     * Returns a string with the given message wrapped in the given ANSI color code.
     */
    public static String getANSIColorMessage(String message, ANSI_COLOR_CODE code) {
        StringBuffer sb = new StringBuffer();
        sb.append("\u001b[9;");
        if (code == ANSI_COLOR_CODE.RED) {
            sb.append("31m");
        } else if (code == ANSI_COLOR_CODE.GREEN) {
            sb.append("32m");
        } else if (code == ANSI_COLOR_CODE.YELLOW) {
            sb.append("33m");
        } else if (code == ANSI_COLOR_CODE.BLUE) {
            sb.append("34;1m"); // blue looks better in bold
        } else if (code == ANSI_COLOR_CODE.MAGENTA) {
            sb.append("35m");
        } else if (code == ANSI_COLOR_CODE.CYAN) {
            sb.append("36m");
        } else {
            sb.append("30m");
        }
        sb.append(message + "\u001b[m");
        return sb.toString();
    }

    /**
     * Log error message {@code mesg}, exception {@code e} and exit.
     *
     * @param mesg Error message
     * @param e    Exception
     */
    public static void die(String mesg, Throwable e) {
        if (mesg == null)
            mesg = "Error occured during test run";

        if (e == null)
            e = new Exception(mesg);

        System.out.println(mesg);
        e.printStackTrace();
        System.exit(1);
    }

    /**
     * Sleeps for number of seconds specified.
     *
     * @param sleepTime number of seconds to sleep.
     */
    public static void sleep(int sleepTime) {
        try {
            Thread.sleep(1000 * sleepTime);
        } catch (Exception e) {
            throw new IllegalStateException("Interuppted during sleep");
        }
    }

    /**
     * Custom log Formatter for use with JUnit 'core' operations.
     */
    private static class JUnitCoreLogFormatter extends Formatter {
        public JUnitCoreLogFormatter() {
            super();
        }

        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();
            sb.append(LS);
            sb.append("[").append(TIME_FORMAT.format(record.getMillis())).append("] ");

            sb.append(formatMessage(record));
            sb.append(LS);
            return sb.toString();
        }
    }

    /**
     * Custom log Formatter for use with JUnit 'test' operations.
     */
    private static class JUnitTestLogFormatter extends Formatter {

        @Override
        public String format(LogRecord record) {
            String className = record.getSourceClassName();
            StringBuffer sb = new StringBuffer();

            // For use with TestCaseWatcher - if we know the test case name being executed, use that, otherwise, use
            // a best guess: the method that logged this record.
            StringBuilder sb1 = new StringBuilder();
            Object threadContextKey = ThreadContext.get(UtilConstants.JUNIT_TEST_CONTEXT_KEY);
            String testName = threadContextKey != null ? threadContextKey.toString() :
                    className.substring(className.lastIndexOf(".") + 1) + "." + record.getSourceMethodName();
            sb1.append("[").append(testName).append("] ");
            sb.append(getANSIColorMessage(sb1.toString(), ANSI_COLOR_CODE.MAGENTA));
            sb.append(TIME_FORMAT.format(record.getMillis())).append(" ");

            sb.append(formatMessage(record));
            sb.append(LS);
            return sb.toString();
        }
    }
}
