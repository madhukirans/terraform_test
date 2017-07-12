/**
 * Created by mseelam on 6/25/2017.
 */
package com.oracle.bmc.test.terraform.acceptance;

import com.oracle.bmc.core.model.Instance;
import com.oracle.bmc.test.terraform.common.json.TFJSon;
import com.oracle.bmc.test.terraform.common.util.InitBMC;
import com.oracle.bmc.test.terraform.common.util.TestCaseWatcher;
import com.oracle.bmc.test.terraform.common.util.TestSuiteWatcher;
import com.oracle.bmc.test.terraform.common.util.Util;
import org.junit.*;
import org.junit.rules.TestRule;

import javax.validation.constraints.AssertFalse;
import java.util.List;
import java.util.logging.Logger;

public class BasicInstaceTest {

    @ClassRule
    public static final TestSuiteWatcher testSuiteWatcher = new TestSuiteWatcher();
    private static Logger logger = Util.getJunitTestLogger();
    private static TFJSon json;

    @Rule
    public TestRule testCaseWatcher = new TestCaseWatcher(testSuiteWatcher);

    @BeforeClass
    public static void staticPrepare() throws Exception {
        logger.info("One-time setup...");
        json = new TFJSon();
    }

    @Test
    public void compareInstnaces() throws Exception {
        logger.info("Verifying BMC instance.");
        List<Instance> bmcInstances = InitBMC.getInstances();
        List<Instance> tfInstances = json.getInstances();

        System.out.println("bmcInstances:" + bmcInstances.size());
        System.out.println("tfInstances:" + tfInstances.size());

        boolean found1 = false;
        for (Instance ins : bmcInstances) {
            System.out.println("bmc instance" + ins.getDisplayName());
            boolean found2 = false;
            for (Instance tfIns : tfInstances) {
                System.out.println("tf instance" + tfIns.getDisplayName());
                if (tfIns.getId().equals(ins.getId())) {
                    found2 = true;
                    logger.info("Instance found.");
                    break;
                }
            }

            if (found2 == false) {
                Assert.fail("Instance :" + ins.getDisplayName() + " not found.");
            }
        }
    }
}