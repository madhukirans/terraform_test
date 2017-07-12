package com.oracle.bmc.test.terraform.common.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.logging.Level;

/**
 * Created by mseelam on 6/25/2017.
 */
public class UtilConstants {
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SS");
    public static final String LS = System.getProperty("line.separator");
    public static final String JUNIT_TEST_CONTEXT_KEY = "junitTest";
    public static final Level JUNIT_TEST_LOGGER_LEVEL = Level.FINE;
    /**
     * Line separator
     */
    public static final String lineSep = System.getProperty("line.separator");
    /**
     * File separator
     */
    public static final String fileSep = File.separator;
    /**
     * Path separator
     */
    public static final String parthSep = File.pathSeparator;
    public static boolean DebugMode = true;
    public static String RemoteWork = "/home/opc/work";
    public static String LocalWork = "/home/opc/work";

    public static enum ANSI_COLOR_CODE {
        RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN
    }
}
