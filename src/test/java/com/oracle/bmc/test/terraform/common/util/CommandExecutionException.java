package com.oracle.bmc.test.terraform.common.util;


/**
 * Object encapsulating the exception/error/failure state after command execution.
 */

public class CommandExecutionException extends RuntimeException {
    private ExecutionResult result = null;


    public CommandExecutionException(String message, Throwable e) {

        this(message, new ExecutionResult(1, e.getMessage(), getCause(e)), e);
    }

    /**
     * Create a command exception from the message and execution result.
     *
     * @param message
     * @param result
     */
    public CommandExecutionException(String message, ExecutionResult result) {
        super(message);
        this.result = result;
    }

    /**
     * Create a command exception from the message, exception and execution result.
     *
     * @param message
     * @param result
     */
    public CommandExecutionException(String message, ExecutionResult result, Throwable e) {
        super(message + " Details:" + e.toString(), e);
        this.result = result;
    }

    private static String getCause(Throwable e) {
        return (e.getCause() == null) ? "Unknown exception cause" : e.getCause().toString();
    }

    /**
     * @return An execution result object.
     */
    public ExecutionResult getResult() {
        return result;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(UtilConstants.lineSep);
        buffer.append("Message:").append(UtilConstants.lineSep)
                .append(getMessage()).append(UtilConstants.lineSep)
                .append(result);
        return buffer.toString();
    }
}

