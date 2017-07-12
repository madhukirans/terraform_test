/* $Header: otd_test/src/test/oracle/otd/common/util/CommandExecutor.java /main/13 2016/09/02 06:34:03 mseelam Exp $ */

/* Copyright (c) 2011, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

    MODIFIED   (MM/DD/YY)
    rbseshad    11/22/11 - Remove CommandBreakup to debug mode
    rbseshad    08/08/11 - Add option for sending interactive input.
    sbhatne     08/02/11 - Use ProcessBuilder and read streams before wait.
    mseelam     07/28/11 - Added Log messages for debug mode.
    rbseshad    06/02/11 - Execute a remote/local command.
    rbseshad    06/02/11 - Creation.
 */

package com.oracle.bmc.test.terraform.common.util;

import com.jcraft.jsch.*;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CommandExecutor is an utility to execute a command on a given host machine. *
 * The command is built using {@link CommandBuilder}.
 * <p>
 * <p>
 * Created by mseelam on 6/25/2017.
 */
public class CommandExecutor {
    /**
     * Map of host names to corresponding secure session objects.
     */
    private static Hashtable<String, Session> mapHostSecureSession = new Hashtable<String, Session>();
    /**
     * Current host name.
     */
    private String hostName = null;
    /**
     * Current session
     */
    private Session session = null;
    /**
     * Logger object.
     */
    private Logger logger = null;


    /**
     * Create a command executor instance for executing commands in <b>hostName</b>.
     *
     * @param hostName Host name where the command has to be executed.
     * @param logger   Logger object to be used.
     */
    public CommandExecutor(String hostName, Logger logger) {
        this.hostName = hostName;
        this.logger = logger;
        initSecureSession();
    }

    public static void main(String arg[]) {
        //new CommandExecutor(host , logger);
    }

    /**
     * @param hostName Host name to be tested for remoteness.
     * @return True, if <b>hostName<b> is not the same as host running the regress.
     */
    public static boolean isRemote(String hostName) {
        try {
            String localhost = InetAddress.getLocalHost().getHostName();
            return (localhost.equals(hostName)) ? false : true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Initialize a secure session.
     *
     * If the host is NOT remote then there is no session initialization.
     * If the host is remote and not present in {@link #mapHostSecureSession},
     * initialize and add session to {@link #mapHostSecureSession}.
     */

    /**
     * Write contents in <b>input</b> to output stream <b>outputStream</b>
     *
     * @param outputStream An output stream.
     * @param input        Array of stings.
     */
    public static void writeContent(OutputStream outputStream, String input[]) throws IOException {
        PrintWriter out = new PrintWriter(new OutputStreamWriter(outputStream));
        for (String currInput : input)
            out.println(currInput);
        out.close();
    }

    /**
     * Read content from a given input stream and return read content.
     *
     * @param inputStream Stream to be read input from
     * @return Content read from @{code inputStream}
     */
    public static String readContent(InputStream inputStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder bufferOutput = new StringBuilder();

        String currLine = null;
        while ((currLine = in.readLine()) != null)
            bufferOutput.append(currLine).append(UtilConstants.lineSep);

        return bufferOutput.toString();
    }

    /**
     * Close the SSH session
     * <b><font color="red">Note:</b>
     * This will be called by the driver test suite. So do NOT include or call in your test case code.
     * </font>
     */
    public static void closeAllSecureSessions() {
        for (Session currSession : mapHostSecureSession.values())
            currSession.disconnect();
    }

    private void initSecureSession() {
        if (!isRemote(hostName))
            return;

        if (mapHostSecureSession.containsKey(hostName)) {
            session = mapHostSecureSession.get(hostName);
            return;
        }

        try {
            //JSch jsch = new JSch();
            //Properties config = new Properties();
            // config.put("StrictHostKeyChecking", "no");
            //session = jsch.getSession(NISUserName, hostName, 22);
            //session.setConfig(config);
            // session.setPassword(TestProperties.NISPassword);
            session = new SSHClient(logger).getSession(hostName);
            session.connect();
        } catch (JSchException e) {
            StringBuilder buffer = new StringBuilder();
            buffer.append("Error establishing secure session for remote command execution.")
                    .append("   - Verify if NISUserName/NISPassword properties are set in Test.properties.")
                    .append("   - Verify if values for NISUserName/NISPassword are accurate.");
            exitFatal(buffer.toString(), e, CommandExecutor.class);
        }
        mapHostSecureSession.put(hostName, session);
    }

    /**
     * Invoke exitFatal (message, e, testClass);
     */
    public void exitFatal(String message, Exception e, Class<?> testClass) {
        exitFatal(message, e, testClass, null);
    }

    /**
     * Exit all tests with fatal {@code message}. Create a .dif file with error {@code message} and stack trace.
     * Append {@code difPrefix} to the name of the .dif file. Exception {@code e} should have the stack trace.
     */
    public void exitFatal(String message, Throwable e, Class<?> testClass, String prefix) {
        prefix = (prefix == null) ? "" : ("." + prefix);
        String fileName = testClass.getName() + prefix + ".fatal.dif";
        try {
            message = "[FATAL] " + message;
            PrintWriter out = new PrintWriter(new FileWriter(fileName));
            e.printStackTrace(out);
            out.close();
            logger.log(Level.SEVERE, message, e);
        } catch (IOException ie) {
            logger.log(Level.SEVERE, "Error writing fatal dif '" + fileName + "'");
        } finally {
            Util.die(message, e);
        }
    }

    /**
     * Invoke execute (builder, false)
     * <br> See {@link #execute(CommandBuilder, boolean)}
     */
    public ExecutionResult execute(CommandBuilder builder) {
        ExecutionResult result = execute(builder, false);
        result.log(builder.toString());
        return result;
    }

    /**
     * Invoke execute (builder, null, ignoreError);
     * <br> See {@link #execute(CommandBuilder, String[])}
     */
    public ExecutionResult execute(CommandBuilder builder, boolean ignoreError) {
        return execute(builder, null, ignoreError);
    }

    /**
     * Invoke execute (builder, input, false);
     * <br> See {@link #execute(CommandBuilder, String[], boolean)}
     */
    public ExecutionResult execute(CommandBuilder builder, String input[]) {
        return execute(builder, input, false);
    }

    /**
     * Execute the command encapsulated by <b>builder</b>, providing/redirecting the following <b>input</b>.
     * <p>
     * If <b>ignoreError</b> is true, ignore any error that arises after executing the command, otherwise
     * throw CommandExecutionExcetpion.
     *
     * @param builder     Command builder encapsulating the command.
     * @param input       Strings having input redirection (if any).
     * @param ignoreError If true, an error due to command execute is ignored.
     *                    CommandExecutionExcetpion is thrown otherwise.
     * @return An object of type {@link ExecutionResult}, encapsulating the result of command execution.
     */
    public ExecutionResult execute(CommandBuilder builder, String input[], boolean ignoreError) {
        String command = builder.toString();

        StringBuilder strBuilder = new StringBuilder();

        if (UtilConstants.DebugMode) {
            strBuilder.append("_________________________ Start _________________________" + UtilConstants.lineSep);
            strBuilder.append("Command         : " + command + UtilConstants.lineSep);
            strBuilder.append("Command Breakup : " + builder.getArgList() + UtilConstants.lineSep);
        } else {
            strBuilder.append("Command: " + command + UtilConstants.lineSep);
        }
        logger.info(Util.getANSIColorMessage(strBuilder.toString(), UtilConstants.ANSI_COLOR_CODE.CYAN));

        ExecutionResult result = (isRemote(hostName)) ? executeRemote(builder, input) : executeLocal(builder, input);

        strBuilder = new StringBuilder();
        strBuilder.append("ExitStatus: " + result.getExitStatus() + UtilConstants.lineSep);
        if (UtilConstants.DebugMode) {
            strBuilder.append("Error   :" + result.getErrorMessage() + UtilConstants.lineSep);
            strBuilder.append("OutPut  :" + result.getOutputMessage() + UtilConstants.lineSep);
            strBuilder.append("_________________________  End  _________________________" + UtilConstants.lineSep);
        }

        logger.info(strBuilder.toString());
        logger.info(Util.getANSIColorMessage(strBuilder.toString(), UtilConstants.ANSI_COLOR_CODE.CYAN));

        if (!ignoreError && result.getExitStatus() != 0)
            throw new CommandExecutionException("Error executing command '" + command + "'", result);

        return result;
    }

    /**
     * Execute the command on local host.
     *
     * @param builder Command builder encapsulating the command.
     * @param input   Strings having input redirection (if any).
     * @return An object of type {@link ExecutionResult}, encapsulating the result of command execution.
     */
    private ExecutionResult executeLocal(CommandBuilder builder, String input[]) {
        ExecutionResult result = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(builder.getArgList());
            Process process = processBuilder.start();

            if (input != null)
                writeContent(process.getOutputStream(), input);
            String mesgOutput = readContent(process.getInputStream());
            String mesgError = readContent(process.getErrorStream());

            process.waitFor(30, TimeUnit.MINUTES);

            int exitStatus = process.exitValue();
            result = new ExecutionResult(exitStatus, mesgOutput, mesgError);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing local command", e);
        }

        return result;
    }

    public synchronized void forkLocalChildProcess(CommandBuilder builder, String message) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(builder.getArgList());
        processBuilder.directory(new File(UtilConstants.RemoteWork));
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        File logFile = new File("CommandExecutorChild.log");
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        PrintWriter out = new PrintWriter(new FileWriter(logFile), true);

        try {
            final int MAX_ATTEMPTS = 120;
            final int SLEEP_PER_ATTEMPT = 3;
            boolean success = false;

            for (int i = 0; i < MAX_ATTEMPTS; ++i) {
                String logLine = in.readLine();
                out.println(logLine);
                out.flush();
                System.out.println(logLine);

                if (logLine == null)
                    throw new IllegalStateException("Error reading logs from " + logFile);

                if (logLine.contains(message)) {
                    success = true;
                    break;
                }
                Util.sleep(SLEEP_PER_ATTEMPT);
            }

            if (!success)
                throw new IllegalStateException("Message '" + message
                        + "' not logged by child process after wating for "
                        + (MAX_ATTEMPTS * SLEEP_PER_ATTEMPT) + " seconds.");
        } finally {
            in.close();
            out.close();
        }
    }

    /**
     * Execute the command on remote host.
     *
     * @param builder Command builder encapsulating the command.
     * @return An object of type {@link ExecutionResult}, encapsulating the result of command execution.
     */
    public ExecutionResult executeRemote(CommandBuilder builder) {
        return executeRemote(builder, null);
    }

    /**
     * Execute the command on remote host.
     *
     * @param builder Command builder encapsulating the command.
     * @param input   Strings having input redirection (if any).
     * @return An object of type {@link ExecutionResult}, encapsulating the result of command execution.
     */
    private ExecutionResult executeRemote(CommandBuilder builder, String input[]) {
        ExecutionResult result = null;

        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setErrStream(System.err);

            InputStream in = channel.getInputStream();
            InputStream err = channel.getExtInputStream();

            channel.setCommand(builder.toString());
            channel.connect();

            String mesgOutput = "", mesgError = "";
            while (!channel.isClosed()) {
                if (input != null)
                    writeContent(channel.getOutputStream(), input);

                mesgOutput = mesgOutput + readContent(in);
                mesgError = mesgError + readContent(err);
            }
            result = new ExecutionResult(channel.getExitStatus(), mesgOutput, mesgError);
            in.close();
            err.close();
            channel.disconnect();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing remote command", e);
        }
        return result;
    }

    /**
     * Set file as read only
     *
     * @param path Path to file.
     */
    public void setFileAsReadOnly(String path) {
        setFilePermission(path, true, false, false);
    }

    /**
     * Set file permission
     *
     * @param path    Path to file
     * @param read    If true, read permission for user is enabled.
     * @param write   If true, write permission for user is enabled.
     * @param execute If true, execute permission for user is enabled.
     */
    public void setFilePermission(String path, boolean read, boolean write, boolean execute) {
        try {
            if (!isRemote(hostName)) {
                File file = new File(path);
                file.setReadable(read);
                file.setWritable(write);
                file.setExecutable(execute);
                return;
            }

            int permValue[] = new int[]{4, 2, 1};
            boolean permInput[] = new boolean[]{read, write, execute};

            int permResultForUser = 0;
            for (int i = 0; i < permInput.length; ++i)
                permResultForUser += (permInput[i]) ? permValue[i] : 0;
            int permOctalToDecimal = Integer.valueOf("0" + permResultForUser + "44", 8);

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            sftpChannel.chmod(permOctalToDecimal, path);
            channel.disconnect();
        } catch (Exception e) {
            throw new IllegalStateException("Error setting permission for  " + path + " to rwx=" + read + "," + write + "," + execute, e);
        }
    }

    /**
     * Copy the file from local <b>pathSource</b> to <b>pathDestination</b> on remote host.
     *
     * @param pathSource      Path to local source file.
     * @param pathDestination Path to remote destination file.
     */
    public void putFile(String pathSource, String pathDestination) {
        try {
            if (!isRemote(hostName)) {
                System.out.print("Local Copy initiated.");
                Files.copy(Paths.get(pathSource), Paths.get(pathDestination), StandardCopyOption.REPLACE_EXISTING);
                new File(pathDestination).setWritable(true);
                return;
            }

            //if (pathSource.startsWith("/"))
            //{
            //  getFile (pathSource);
            //  pathSource = Paths.get(pathSource).getFileName().toString();
            //}

            System.out.print("Remote copy initiated.");
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            sftpChannel.put(pathSource, pathDestination, ChannelSftp.OVERWRITE);
            channel.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Error copying " + pathSource + " to " + pathDestination, e);
        }
    }

    /**
     * Get the file present at <b>pathSource</b> on remote host and place it in work directory.
     *
     * @param pathSource Path to remote source file.
     */
    public void getFile(String pathSource) {
        String fileName = new File(pathSource).getName();
        String pathDestination = new File(UtilConstants.LocalWork, fileName).getAbsolutePath();

        try {
            if (!isRemote(hostName)) {
                Files.copy(Paths.get(pathSource), Paths.get(pathDestination), StandardCopyOption.REPLACE_EXISTING);
                return;
            }

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            sftpChannel.get(pathSource, pathDestination);
            channel.disconnect();
        } catch (Exception e) {
            throw new IllegalStateException("Error copying " + pathSource + " to " + pathDestination, e);
        }
    }

    @SuppressWarnings("rawtypes")
    public boolean existsFile(String path) {
        try {
            if (!isRemote(hostName))
                return new File(path).exists();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            Vector result = sftpChannel.ls(path);
            channel.disconnect();

            return (result.size() == 0) ? false : true;
        } catch (Exception e) {
            throw new IllegalStateException("Error listing " + path, e);
        }
    }

    /**
     * Move files from directory <b>pathSrc</b> to directory <b>pathDest</b> matching
     * pattern <b>wildCardPattern</b>
     */
    public void moveFileToDirectory(String pathSrc, String pathDest, final String patternLocal, final String patternRemote) {
        try {
            if (!isRemote(hostName)) {
                File dirSrc = new File(pathSrc);
                File fileSelect[] = dirSrc.listFiles
                        (
                                new FilenameFilter() {
                                    public boolean accept(File dir, String name) {
                                        return name.matches(patternLocal);
                                    }
                                }
                        );

                for (int i = 0; i < fileSelect.length; i++)
                    Files.move(fileSelect[i].toPath(), new File(pathDest, fileSelect[i].getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);

                return;
            }

            CommandBuilder builder = new CommandBuilder()
                    .add("mv")
                    .add(pathSrc + "/" + patternRemote)
                    .add(pathDest);
            execute(builder);
        } catch (Exception e) {
            throw new IllegalStateException("Error moving " + pathSrc + "/" + patternLocal + " to " + pathDest, e);
        }
    }

    /**
     * Create directory <b>pathDir</b>
     *
     * @param pathDir
     */
    public void mkdir(String pathDir) {
        try {
            if (!isRemote(hostName)) {
                new File(pathDir).mkdirs();
                return;
            }
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            sftpChannel.mkdir(pathDir);
            channel.disconnect();
        } catch (Exception e) {
            throw new IllegalStateException("Error creating directory " + pathDir, e);
        }
    }


}
