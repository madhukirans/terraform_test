package com.oracle.bmc.test.terraform.common.util;

import com.sun.jmx.snmp.ThreadContext;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import static com.oracle.bmc.test.terraform.common.util.UtilConstants.TIME_FORMAT;

public class TestCaseWatcher extends TestWatcher {

    private TestSuiteWatcher testSuiteWatcher;
    private String status;
    private long timeStart;
    private long timeEnd;
    private Logger logger;

    public TestCaseWatcher(TestSuiteWatcher testSuiteWatcher) {
        super();
        this.testSuiteWatcher = testSuiteWatcher;
        this.logger = Util.getJunitCoreLogger();
    }

    @Override
    protected void starting(Description description) {
        ThreadContext.push(UtilConstants.JUNIT_TEST_CONTEXT_KEY,
                description.getTestClass().getSimpleName() + "." + description.getMethodName());
        timeStart = System.currentTimeMillis();
        StringBuffer sb = new StringBuffer();
        sb.append("--------------------------------------------------------------------+");
        sb.append("\n| [").append(TIME_FORMAT.format(new Date())).append("] BEGIN ")
                .append(description.getMethodName());
        sb.append("\n--------------------------------------------------------------------+");
        logger.info(sb.toString());
    }

    @Override
    protected void finished(Description description) {
        ThreadContext.push(UtilConstants.JUNIT_TEST_CONTEXT_KEY, null);
        timeEnd = System.currentTimeMillis();
        int currentCount = testSuiteWatcher.successCount + testSuiteWatcher.failCount + testSuiteWatcher.skipCount;
        double seconds = (timeEnd - timeStart) / 1000.0;
        StringBuffer sb = new StringBuffer();
        sb.append("--------------------------------------------------------------------+");
        sb.append("\n| ").append(new SimpleDateFormat("[HH:mm:ss z] ").format(new Date()));
        if (status.equalsIgnoreCase(STATUS.SUCCESS.toString())) {
            sb.append(Util.getANSIColorMessage(status, UtilConstants.ANSI_COLOR_CODE.GREEN));
        } else {
            sb.append(Util.getANSIColorMessage(status, UtilConstants.ANSI_COLOR_CODE.RED));
        }
        sb.append(" ").append(description.getMethodName());
        sb.append(" (").append(new DecimalFormat("0.000").format(seconds)).append(" sec").append(")");
        sb.append("\n| (").append(currentCount).append(" of ").append(testSuiteWatcher.totalCount).append(")");
        if (testSuiteWatcher.failCount > 0 || testSuiteWatcher.skipCount > 0) {
            sb.append(Util.getANSIColorMessage(" Not clean :-( ", UtilConstants.ANSI_COLOR_CODE.RED));
        } else {
            sb.append(Util.getANSIColorMessage(" Clean run :-) ", UtilConstants.ANSI_COLOR_CODE.GREEN));
        }
        sb.append(" Pass: ").append(testSuiteWatcher.successCount);
        sb.append(" Fail: ").append(testSuiteWatcher.failCount);
        sb.append(" Skip: ").append(testSuiteWatcher.skipCount);
        sb.append("\n--------------------------------------------------------------------+");
        logger.info(sb.toString());
    }

    @Override
    protected void succeeded(Description description) {
        testSuiteWatcher.successCount++;
        status = STATUS.SUCCESS.toString();
    }

    @Override
    protected void failed(Throwable e, Description description) {
        testSuiteWatcher.failCount++;
        StringWriter msg = new StringWriter();
        e.printStackTrace(new PrintWriter(msg));
        logger.severe(((e.getMessage() != null) ?
                Util.getANSIColorMessage(e.getMessage(), UtilConstants.ANSI_COLOR_CODE.RED) : "") + msg.toString());
        status = STATUS.FAIL.toString();
    }

    @Override
    protected void skipped(AssumptionViolatedException e, Description description) {
        testSuiteWatcher.skipCount++;
        status = STATUS.SKIP.toString();
    }

    private enum STATUS {SUCCESS, FAIL, SKIP}
};