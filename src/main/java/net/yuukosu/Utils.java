package net.yuukosu;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class Utils {

    public static ObjectNode getTemplate(String type) {
        ObjectNode node = Game.getJsonMapper().createObjectNode();
        node.put("TYPE", type);

        return node;
    }

    public static ObjectNode getTemplate(String type, String uniqueId) {
        ObjectNode node = Game.getJsonMapper().createObjectNode();
        node.put("TYPE", type);
        node.put("UUID", uniqueId);

        return node;
    }
}
