package com.oracle.bmc.test.terraform.common.json;

import com.oracle.bmc.core.model.Instance;
import com.oracle.bmc.loadbalancer.model.LoadBalancer;

import java.util.ArrayList;

/**
 * Created by mseelam on 7/3/2017.
 */
public final class BMCResources {
    ArrayList<Instance> instanceList;
    ArrayList<LoadBalancer> loadbalcerList;

    public BMCResources() {
        instanceList = new ArrayList<>();
        loadbalcerList = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Instances:\n");
        for (Instance i : instanceList) {
            stringBuffer.append(i.getDisplayName());
            stringBuffer.append(i.getMetadata().get("public_ip"));
            stringBuffer.append("\n");
        }

        stringBuffer.append("Loadbalancers:\n");
        for (LoadBalancer i : loadbalcerList) {
            stringBuffer.append(i.getDisplayName());
            stringBuffer.append(i.getListeners());
            stringBuffer.append("\n");
        }
        return stringBuffer.toString();
    }

    public ArrayList<Instance> getInstances() {
        return instanceList;
    }
}
