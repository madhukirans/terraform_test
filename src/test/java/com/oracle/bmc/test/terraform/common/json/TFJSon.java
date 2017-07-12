package com.oracle.bmc.test.terraform.common.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oracle.bmc.core.model.Instance;
import com.oracle.bmc.loadbalancer.model.LoadBalancer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by mseelam on 6/25/2017.
 */
public class TFJSon {
    BMCResources resources = new BMCResources();


    public TFJSon() throws Exception {
        JsonFactory jsonFactory = new JsonFactory();

        //-ea -DConfigFile=D:\\newproject\\terraform-test\\data\\config -DTFJSonFile=D:\\temp\\terraform.tfstate.json
        String path = System.getProperty("TFJSonFile");
        Path p = Paths.get(path);
        if (path != null && Files.exists(p)) {
            System.out.println("Terraform JSon File path:" + p.toAbsolutePath());
        } else {
            System.out.println("Terraform JSon File path not found [" + p.toAbsolutePath() + "]");
            System.exit(-1);
        }

        JsonParser jp = jsonFactory.createJsonParser(new File(path));
        jp.setCodec(new ObjectMapper());
        JsonNode jsonNode = jp.readValueAsTree();
        JsonNode moduleNodes = jsonNode.get("modules");
        Iterator<JsonNode> ite = moduleNodes.iterator();
        while (ite.hasNext()) {
            JsonNode mNode = ite.next();
            initResources(mNode.get("resources"));
        }

        System.out.println(resources);
    }

    public static void main(String a[]) throws Exception {
        new TFJSon();
    }

    private void initResources(JsonNode node) {
        Iterator<JsonNode> ite = node.iterator();
        while (ite.hasNext()) {
            JsonNode rNode = ite.next();
            if (rNode.get("type").toString().equals(TFConstants.TF_BM_INSTANCE)) {
                initInstance(rNode);
            } else if (rNode.get("type").toString().equals(TFConstants.TF_BM_LOADBALANCER)) {
                initLB(rNode);
            }
        }
        //@TODO Need to implement further resource types
    }

    private void initLB(JsonNode instanceNode) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode attributes = instanceNode.get("primary").get("attributes");
            LoadBalancer lb = LoadBalancer.builder()
                    .id(instanceNode.get("primary").get("id").asText())
                    .compartmentId(attributes.get("compartment_id").asText())
                    .displayName(attributes.get("display_name").asText())
                    .build();
            //@TODO need to add some more propertes
            resources.loadbalcerList.add(lb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initInstance(JsonNode instanceNode) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode attributes = instanceNode.get("primary").get("attributes");
            Map<String, String> metaData = new HashMap();
            metaData.put("public_ip", attributes.get("public_ip").asText());
            metaData.put("metadata.ssh_authorized_keys", attributes.get("metadata.ssh_authorized_keys").asText());

            Instance instance = Instance.builder()
                    .id(instanceNode.get("primary").get("id").asText())
                    .availabilityDomain(attributes.get("availability_domain").asText())
                    .compartmentId(attributes.get("compartment_id").asText())
                    .displayName(attributes.get("display_name").asText())
                    .metadata(metaData)
                    .build();
            //@TODO need to add some more propertes
            resources.instanceList.add(instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Instance> getInstances() {
        return resources.getInstances();
    }
}
