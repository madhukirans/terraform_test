package com.oracle.bmc.test.terraform.k8s_tests;

import com.oracle.bmc.core.model.Instance;
import com.oracle.bmc.test.terraform.common.json.TFJSon;
import com.oracle.bmc.test.terraform.common.util.*;
import org.junit.*;
import org.junit.rules.TestRule;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by mseelam on 7/4/2017.
 */
public class K8SVersionTest {
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

    @AfterClass
    public static void clean() throws Exception {
        CommandExecutor.closeAllSecureSessions();
    }

    @Test
    public void kubeletVersionTest() throws Exception {
        logger.info("Verifying kubelet verson.");
        List<Instance> tfInstances = json.getInstances();
        CommandBuilder command = new CommandBuilder();
        command.add("kubelet").add("--version");

        boolean found1 = false;
        for (Instance ins : tfInstances) {
            if (ins.getDisplayName().startsWith("k8s-master") || ins.getDisplayName().startsWith("k8s-minion")) {
                String ip = ins.getMetadata().get("public_ip");
                CommandExecutor exec = new CommandExecutor(ip, logger);
                ExecutionResult result = exec.execute(command);
                System.out.println(result.getOutputMessage());
                Assert.assertTrue("kubelet version expected Kubernetes v1.6.4 but found " + result.getOutputMessage(),
                        result.getOutputMessage().contains("Kubernetes v1.6.4"));
                logger.info("Verifying kubelet verson.");
            }
        }
    }

    @Test
    public void kubectlVersionTest() throws Exception {
        logger.info("Verifying kubectl verson.");
        List<Instance> tfInstances = json.getInstances();
        CommandBuilder command = new CommandBuilder();
        command.add("kubectl").add("version");

        boolean found1 = false;
        for (Instance ins : tfInstances) {
            if (ins.getDisplayName().startsWith("k8s-master") || ins.getDisplayName().startsWith("k8s-minion")) {
                String ip = ins.getMetadata().get("public_ip");
                CommandExecutor exec = new CommandExecutor(ip, logger);
                try {
                    ExecutionResult result = exec.execute(command);
                    System.out.println(result.getOutputMessage());

                    Assert.assertTrue("kubectl version expected [Major:\"1\", Minor:\"6\"] but found " + result.getOutputMessage(),
                            result.getOutputMessage().contains("Major:\"1\", Minor:\"6\""));
                } catch (CommandExecutionException e) {

                }
                logger.info("Verifying kubectl verson.");
            }
        }
    }
}
