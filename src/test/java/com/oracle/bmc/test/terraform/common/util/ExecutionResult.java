package com.oracle.bmc.test.terraform.common.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * This class encapsulates the result of command execution. Essentially, providing assessor
 * methods to exit status, output message and error message.
 */
public class ExecutionResult {
    private int exitStatus;

    private String mesgOutput = null;

    private String mesgError = null;

    /**
     * Create an execution result object.
     *
     * @param exitStatus Exit status of the command.
     * @param mesgOutput Message written to standard output post command execution.
     * @param mesgError  Message written to standard error post command execution.
     */
    public ExecutionResult(int exitStatus, String mesgOutput, String mesgError) {
        this.exitStatus = exitStatus;
        this.mesgOutput = (mesgOutput == null) ? "" : mesgOutput.trim();
        this.mesgError = (mesgError == null) ? "" : mesgError.trim();
    }

    /**
     * @return Exit Status of the command.
     */
    public int getExitStatus() {
        return exitStatus;
    }

    /**
     * @return Error message from the command.
     */
    public String getErrorMessage() {
        return mesgError;
    }

    /**
     * @return Output message from the command.
     */
    public String getOutputMessage() {
        return mesgOutput;
    }

    /**
     * Log details in a file with following format
     *
     * @{code <Invoker of ProcessControl function>.cmd.log}
     */
    public void log(String command) {
        StackTraceElement trace[] = new Throwable().getStackTrace();
        StackTraceElement invoker = null;

        String thisClassName = getClass().getName();
        for (StackTraceElement currTrace : trace) {
            if (!currTrace.getClassName().contains(thisClassName)) {
                invoker = currTrace;
                break;
            }
        }

        if (invoker == null)
            return;

        try {
            String fileName = invoker.getClassName().replaceAll("test.oracle.otd.", "") + ".cmd.log";
            PrintWriter out = new PrintWriter(new FileWriter(fileName, true));

            StringBuilder buffer = new StringBuilder();
            buffer.append("[" + new Date() + "] ")
                    .append("[" + invoker + "] ")
                    .append("[Thread=" + Thread.currentThread().getId() + "]");
            out.println();
            out.println("--------------------------------------------------------------------------");
            out.println(buffer);
            out.println("--------------------------------------------------------------------------");
            out.println();
            out.println("Command");
            out.println("-------");
            out.println(command);
            out.println();
            out.println("ExitStatus=" + exitStatus);
            out.println();
            out.println("Output Message");
            out.println("--------------");
            out.println(mesgOutput);
            out.println();
            out.println("Error Message");
            out.println("-------------");
            out.println(mesgError);
            out.flush();
            out.close();
        } catch (IOException ioe) {

        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(UtilConstants.lineSep);
        builder.append("Status:").append(UtilConstants.lineSep)
                .append("ExitCode=").append(exitStatus).append(UtilConstants.lineSep)
                .append(UtilConstants.lineSep)
                .append("Output:").append(UtilConstants.lineSep)
                .append(mesgOutput).append(UtilConstants.lineSep)
                .append(UtilConstants.lineSep)
                .append("Error:").append(UtilConstants.lineSep)
                .append(mesgError).append(UtilConstants.lineSep);

        return builder.toString();
    }
}   
