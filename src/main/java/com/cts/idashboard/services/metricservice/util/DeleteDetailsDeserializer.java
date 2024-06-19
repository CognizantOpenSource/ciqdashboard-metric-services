//package com.cts.idashboard.services.metricservice.util;
//
//import com.cts.idashboard.services.metricservice.data.Delete;
//import com.fasterxml.jackson.core.JsonParser;
//import com.fasterxml.jackson.databind.DeserializationContext;
//import com.fasterxml.jackson.databind.JsonDeserializer;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.node.ArrayNode;
//import com.cognizant.collector.alm.beans.audit.ALMDeleteDetails;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//
//public class DeleteDetailsDeserializer<ALMDeleteDetails> extends JsonDeserializer<ALMDeleteDetails> {
//
//    @Override
//    public ALMDeleteDetails deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
//        JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
//        ALMDeleteDetails almDeleteDetails = new ALMDeleteDetails();
//        List<Delete> deleteList = new ArrayList<>();
//
//        JsonNode audits = jsonNode.get("Audits");
//
//        int totalResults = audits.get("TotalResults").asInt();
//
//        ArrayNode audit = (ArrayNode) audits.get("Audit");
//
//        if(!(audit.isNull()) && audit.size() > 0) {
//            audit.forEach(auditNode -> {
//                Delete delete = getDelete(auditNode);
//                deleteList.add(delete);
//            });
//        }
//
//        almDeleteDetails.setDeleteComponents(deleteList);
//        almDeleteDetails.setTotalResults(totalResults);
//        return almDeleteDetails;
//    }
//
//
//    private Delete getDelete(JsonNode auditNode) {
//
//        Delete delete = new Delete();
//
//        delete.setParentId(auditNode.get("ParentId").asText());
//        delete.setParentType(auditNode.get("ParentType").asText());
//
//        return delete;
//    }
//}
//
