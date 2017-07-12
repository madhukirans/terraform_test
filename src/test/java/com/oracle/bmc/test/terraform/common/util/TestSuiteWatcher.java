package com.oracle.bmc.test.terraform.common.util;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.logging.Logger;

import static com.oracle.bmc.test.terraform.common.util.UtilConstants.TIME_FORMAT;

/**
 * Created by mseelam on 6/25/2017.
 */
public class TestSuiteWatcher extends TestWatcher {

    int totalCount;
    int successCount;
    int failCount;
    int skipCount;
    private long timeStart;
    private long timeEnd;
    private Logger logger;

    public TestSuiteWatcher() {
        super();
        this.logger = Util.getJunitCoreLogger();
    }

    @Override
    protected void starting(Description description) {
        timeStart = System.currentTimeMillis();
        totalCount = description.testCount();
        StringBuffer sb = new StringBuffer();
        sb.append("--------------------------------------------------------------------+");
        sb.append("\n| [").append(TIME_FORMAT.format(new Date())).append("] BEGIN class ")
                .append(description.getClassName());
        sb.append("\n| Total Test Count: ").append(totalCount);
        sb.append("\n--------------------------------------------------------------------+");
        logger.info(sb.toString());
    }

    @Override
    protected void finished(Description description) {
        timeEnd = System.currentTimeMillis();
        double seconds = (timeEnd - timeStart) / 1000.0;
        StringBuffer sb = new StringBuffer();
        sb.append("--------------------------------------------------------------------+");
        sb.append("\n| [").append(TIME_FORMAT.format(new Date())).append("] END class ")
                .append(description.getClassName());
        sb.append(" (").append(new DecimalFormat("0.000").format(seconds)).append(" sec").append(")");
        sb.append("\n| Total Test Count: ").append(totalCount);
        sb.append("\n| Total Pass Count: ").append(successCount);
        sb.append("\n| Total Fail Count: ").append(failCount);
        sb.append("\n| Total Skip Count: ").append(skipCount);
        if (totalCount > 0 && successCount == 0 && failCount == 0 && skipCount == 0) {
            sb.append("\n| " + Util.getANSIColorMessage("All tests skipped!!!", UtilConstants.ANSI_COLOR_CODE.RED));
        }
        sb.append("\n--------------------------------------------------------------------+");
        logger.info(sb.toString());
    }

    public boolean didTestsFail() {
        return failCount > 0 || skipCount > 0;
    }
};
