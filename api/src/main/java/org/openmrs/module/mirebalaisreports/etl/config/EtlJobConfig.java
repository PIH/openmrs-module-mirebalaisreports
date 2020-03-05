package org.openmrs.module.mirebalaisreports.etl.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Encapsulates a particular ETL job configuration
 */
public class EtlJobConfig {

    private String type;
    private JsonNode configuration;

    public EtlJobConfig(JsonNode config) {
        this.type = config.get("type").asText();
        this.configuration = config.get("configuration");
    }

    /**
     * @return the configuration setting at the nested level of configuration
     */
    public JsonNode get(String... keys) {
        if (configuration == null || keys == null || keys.length == 0) {
            return configuration;
        }
        JsonNode ret = configuration.get(keys[0]);
        for (int i=1; i<keys.length; i++) {
            if (ret != null) {
                ret = ret.get(keys[i]);
            }
        }
        return ret;
    }

    /**
     * Convenience to get the configuration of a given setting as a String
     */
    public String getString(String... keys) {
        JsonNode n = get(keys);
        if (n != null) {
            return n.asText();
        }
        return null;
    }

    /**
     * Convenience to get the configuration of a given setting as a String
     */
    public boolean getBoolean(String... keys) {
        JsonNode n = get(keys);
        if (n != null) {
            return n.asBoolean();
        }
        return false;
    }

    /**
     * Convenience to get the configuration of a given setting as a String
     */
    public List<String> getStringList(String... keys) {
        List ret = new ArrayList();
        JsonNode n = get(keys);
        if (n != null) {
            ArrayNode arrayNode = (ArrayNode)n;
            for (Iterator<JsonNode> i = arrayNode.iterator(); i.hasNext();) {
                JsonNode arrayMember = i.next();
                ret.add(arrayMember.asText());
            }
        }
        return ret;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
