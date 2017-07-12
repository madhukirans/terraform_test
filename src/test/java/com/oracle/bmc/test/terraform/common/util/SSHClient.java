package com.oracle.bmc.test.terraform.common.util;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by mseelam on 7/4/2017.
 */
public class SSHClient {
    private JSch jsch;
    private Session session;
    private Logger logger;

    public SSHClient(Logger logger) {
        this.logger = logger;
        try {
            jsch = new JSch();
            File f = new File(InitBMC.getConfig().get("ssh_key_file"));
            jsch.addIdentity(f.getAbsolutePath());
        } catch (JSchException e) {
            logger.log(Level.ALL, "In SSHClient:", e);
        }
    }

    public Session getSession(String ip) {
        try {
            Session session = jsch.getSession("ubuntu", ip);
            session.setConfig("StrictHostKeyChecking", "no");
            return session;
        } catch (JSchException e) {
            logger.log(Level.ALL, "In SSHClient:", e);
        }

        return null;
    }
}
