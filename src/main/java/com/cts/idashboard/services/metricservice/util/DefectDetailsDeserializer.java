
/*
 *    © [2021] Cognizant. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http:www.apache.orglicensesLICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.cts.idashboard.services.metricservice.util;

import com.cts.idashboard.services.metricservice.data.ALMDefectDetails;
import com.cts.idashboard.services.metricservice.data.Defect;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * DefectDetailsDeserializer
 *
 * @author Cognizant
 */

public class DefectDetailsDeserializer extends JsonDeserializer<ALMDefectDetails> {
    @Override
    public ALMDefectDetails deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
        ALMDefectDetails defectDetails = new ALMDefectDetails();
        List<Defect> defects = new ArrayList<>();

        ArrayNode entities = (ArrayNode) jsonNode.get("entities");
        int totalResults = jsonNode.get("TotalResults").asInt();
        if (entities != null && entities.size() > 0) {
            entities.forEach(entityNode -> {
                ArrayNode fields = (ArrayNode) entityNode.get("Fields");
                if (fields != null && fields.size() > 0) {
                    Defect defect = getDefect(fields);
                    defects.add(defect);
                }
            });
        }

        defectDetails.setDefects(defects);
        defectDetails.setTotalResults(totalResults);
        return defectDetails;
    }

    private Defect getDefect(ArrayNode fields) {
        Defect defect = new Defect();
        fields.forEach(fieldNode -> {
            String name = fieldNode.get("Name").asText();
            ArrayNode values = (ArrayNode) fieldNode.get("values");
            if (values != null && values.size() > 0) {
                JsonNode valueNode = values.get(0);
                if (valueNode.has("value")) {
                    String value = valueNode.get("value").asText();
                    String referenceValue = "";
                    if (valueNode.has("ReferenceValue")) referenceValue = valueNode.get("ReferenceValue").asText();
                    switch (name) {
                        case "id":
                            defect.setDefectId(value);
                            break;
                        case "name":
                            defect.setDefectName(value);
                            break;
                        case "description":
                            defect.setDescription(value);
                            break;
                        case "severity":
                            defect.setSeverity(value);
                            break;
                        case "status":
                            defect.setStatus(value);
                            break;
                        case "subject":
                            defect.setSubject(value);
                            break;
                        case "project":
                            defect.setProject(value);
                            break;
                        case "attachment":
                            defect.setAttachment(value);
                            break;
                        case "reproducible":
                            defect.setReproducible(value);
                            break;
                        case "priority":
                            defect.setPriority(value);
                            break;
                        case "environment":
                            defect.setEnvironment(value);
                            break;
                        case "owner":
                            defect.setAssignedTo(value);
                            break;
                        case "detected-by":
                            defect.setDetectedBy(value);
                            break;
                        case "target-rel":
                            defect.setTargetReleaseId(value);
                            defect.setTargetReleaseName(referenceValue);
                            break;
                        case "target-rcyc":
                            defect.setTargetCycleId(value);
                            defect.setTargetCycleName(referenceValue);
                            break;
                        case "detected-in-rel":
                            defect.setDetectedReleaseId(value);
                            defect.setDetectedReleaseName(referenceValue);
                            break;
                        case "detected-in-rcyc":
                            defect.setDetectedCycleId(value);
                            defect.setDetectedCycleName(referenceValue);
                            break;
                        case "dev-comments":
                            defect.setDevComments(value);
                            break;
                        case "actual-fix-time":
                            defect.setActualFixTime(Long.parseLong(value));
                            break;
                        case "detection-version":
                            defect.setDetectionVersion(value);
                            break;
                        case "has-linkage":
                            defect.setHasLinkage(value);
                            break;
                        case "has-others-linkage":
                            defect.setHasOthersLinkage(value);
                            break;
                        case "cycle-id":
                            defect.setCycleId(value);
                            break;
                        case "request-type":
                            defect.setRequestType(value);
                            break;
                        case "request-note":
                            defect.setRequestNote(value);
                            break;
                        case "ver-stamp":
                            defect.setVerStamp(value);
                            break;
                        case "creation-time":
                            defect.setCreationDate(Util.getDateFromString(value));
                            break;
                        case "last-modified":
                            defect.setLastModified(Util.getDateFromString(value));
                            break;
                        case "closing-date":
                            defect.setClosingDate(Util.getDateFromString(value));
                            break;
                        default:
                            break;
                    }
                }
            }
        });
        return defect;
    }
}
