package com.oracle.bmc.test.terraform.common.util;

import com.google.common.base.Supplier;
import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimplePrivateKeySupplier;
import com.oracle.bmc.core.ComputeClient;
import com.oracle.bmc.core.VirtualNetworkClient;
import com.oracle.bmc.core.model.Instance;
import com.oracle.bmc.core.requests.ListInstancesRequest;
import com.oracle.bmc.core.responses.ListInstancesResponse;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by mseelam on 7/4/2017.
 */
public class InitBMC {

    /**
     * Config file should contain the following properties
     * <p>
     * [DEFAULT]
     * user=ocid1.user.oc1.*
     * fingerprint=11:e7:7b:b5:52:20:97:92:65:8f:28:4a:ea:5b:fb:ea
     * key_file=./data/bmcs_api_key.pem   [BMC private key]
     * compartment=ocid1.compartment.oc1*
     * tenancy=ocid1.tenancy.oc1*
     * ssh_key_file=./data/inst_private_sshkey  [SSH private key for instances]
     * terraform_binary_loc=  [location of terraform binary]
     */

    private static ConfigFileReader.ConfigFile config;
    private static ComputeClient computeClient;

    static {
        try {
            //-ea -DConfigFile=D:\\newproject\\terraform-test\\data\\config -DTFJSonFile=D:\\temp\\terraform.tfstate.json
            String path = System.getProperty("ConfigFile");
            Path p = Paths.get(path);
            if (path != null && java.nio.file.Files.exists(p)) {
                System.out.println("Config File path:" + p.toAbsolutePath());
            } else {
                System.out.println("Config File path not found [" + p.toAbsolutePath() + "]");
                System.exit(-1);
            }
            config = ConfigFileReader.parse(path, "DEFAULT");
            Supplier<InputStream> privateKeySupplier = new SimplePrivateKeySupplier(config.get("key_file"));

            AuthenticationDetailsProvider provider = SimpleAuthenticationDetailsProvider.builder()
                    .tenantId(config.get("tenancy"))
                    .userId(config.get("user"))
                    .fingerprint(config.get("fingerprint"))
                    .privateKeySupplier(privateKeySupplier)
                    .build();

            computeClient = new ComputeClient(provider);
            VirtualNetworkClient vcnClient = new VirtualNetworkClient(provider);
            computeClient.setRegion(Region.US_PHOENIX_1);
            vcnClient.setRegion(Region.US_PHOENIX_1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public InitBMC() {

    }

//    public String getSSHPrivateKey() {
//        return Files.readLines(Path(config.get("ssh_key_file")), Charset.defaultCharset());
//    }

    public static ConfigFileReader.ConfigFile getConfig() {
        return config;
    }

    public static List<Instance> getInstances() {
        ListInstancesRequest request = ListInstancesRequest.builder().compartmentId(config.get("compartment")).build();
        // for the instance, list its vnic attachments
        ListInstancesResponse listInstanceResponse = computeClient.listInstances(request);
        return listInstanceResponse.getItems();
    }
}
