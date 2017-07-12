package com.oracle.bmc.test.terraform.network;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.oracle.bmc.core.model.Instance;
import com.oracle.bmc.test.terraform.common.json.TFJSon;
import com.oracle.bmc.test.terraform.common.util.*;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by mseelam on 7/4/2017.
 */
public class SSHInstancePublicIp {

    @ClassRule
    public static final TestSuiteWatcher testSuiteWatcher = new TestSuiteWatcher();
    private static Logger logger = Util.getJunitTestLogger();
    private static TFJSon json;
    private static SSHClient sshClient;

    @Rule
    public TestRule testCaseWatcher = new TestCaseWatcher(testSuiteWatcher);

    @BeforeClass
    public static void staticPrepare() throws Exception {
        logger.info("One-time setup...");
        json = new TFJSon();
        sshClient = new SSHClient(logger);
    }

    @Test
    public void sshTest() throws JSchException {
        logger.info("Verifying BMC instance using SSH.");
        List<Instance> tfInstances = json.getInstances();

        boolean found1 = false;
        for (Instance ins : tfInstances) {
            String ip = ins.getMetadata().get("public_ip");
            logger.info("session created for ip " + ip);
            Session session = sshClient.getSession(ip);
            session.connect(5000);
            session.disconnect();
            logger.info("SSH Connection completed successfully.for ip " + ip);
        }
    }
}

