package io.openshift.appdev.missioncontrol.service.openshift.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftCluster;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.SequenceNode;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class OpenShiftClusterConstructor extends Constructor {

    public OpenShiftClusterConstructor() {
        this.yamlClassConstructors.put(NodeId.sequence, new ConstructCluster());
    }

    private class ConstructCluster extends AbstractConstruct {

        @Override
        public List<OpenShiftCluster> construct(Node node) {
            List<OpenShiftCluster> clusters = new ArrayList<>();
            SequenceNode sequenceNode = (SequenceNode) node;
            for (Node n : sequenceNode.getValue()) {
                MappingNode mapNode = (MappingNode) n;
                Map<Object, Object> valueMap = constructMapping(mapNode);
                String id = (String) valueMap.get("id");
                String apiUrl = (String) valueMap.get("apiUrl");
                String consoleUrl = (String) valueMap.get("consoleUrl");
                clusters.add(new OpenShiftCluster(id, apiUrl, consoleUrl));
            }
            return clusters;
        }
    }

}
