
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

import com.cts.idashboard.services.metricservice.data.ALMRequirementDetails;
import com.cts.idashboard.services.metricservice.data.Requirement;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * RequirementDetailsDeserializer
 *
 * @author Cognizant
 */

public class RequirementDetailsDeserializer extends JsonDeserializer<ALMRequirementDetails> {
    @Override
    public ALMRequirementDetails deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
        ALMRequirementDetails requirementDetails = new ALMRequirementDetails();
        List<Requirement> requirements = new ArrayList<>();

        ArrayNode entities = (ArrayNode) jsonNode.get("entities");
        int totalResults = jsonNode.get("TotalResults").asInt();
        if (entities != null && entities.size() > 0) {
            entities.forEach(entityNode -> {
                ArrayNode fields = (ArrayNode) entityNode.get("Fields");
                if (fields != null && fields.size() > 0) {
                    Requirement requirement = getRequirement(fields);
                    requirements.add(requirement);
                }
            });
        }

        requirementDetails.setRequirements(requirements);
        requirementDetails.setTotalResults(totalResults);
        return requirementDetails;
    }

    private Requirement getRequirement(ArrayNode fields){
        Requirement requirement = new Requirement();
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
                            requirement.setRequirementId(value);
                            break;
                        case "name":
                            requirement.setRequirementName(value);
                            break;
                        case "description":
                            requirement.setDescription(value);
                            break;
                        case "status":
                            requirement.setStatus(value);
                            break;
                        case "owner":
                            requirement.setAuthor(value);
                            break;
                        case "parent-id":
                            requirement.setParentId(value);
                            break;
                        case "comments":
                            requirement.setComments(value);
                            break;
                        case "type-id":
                            requirement.setRequirementTypeId(value);
                            break;
                        case "req-product":
                            requirement.setProduct(value);
                            break;
                        case "target-rel":
                            requirement.setTargetReleaseId(value);
                            requirement.setTargetReleaseName(referenceValue);
                            break;
                        case "target-rcyc":
                            requirement.setTargetCycleId(value);
                            requirement.setTargetCycleName(referenceValue);
                            break;
                        case "father-name":
                            requirement.setFatherName(value);
                            break;
                        case "req-reviewed":
                            requirement.setReviewed(value);
                            break;
                        case "req-priority":
                            requirement.setPriority(value);
                            break;
                        case "has-linkage":
                            requirement.setHasLinkage(value);
                            break;
                        case "no-of-sons":
                            requirement.setNoOfSons(value);
                            break;
                        case "has-rich-content":
                            requirement.setHasRichContent(value);
                            break;
                        case "ver-stamp":
                            requirement.setVerStamp(value);
                            break;
                        case "istemplate":
                            requirement.setIsTemplate(value);
                            break;
                        case "hierarchical-path":
                            requirement.setHierarchicalPath(value);
                            break;
                        case "order-id":
                            requirement.setOrderId(value);
                            break;
                        case "creation-time":
                            requirement.setCreationDate(Util.getDateFromString(value));
                            break;
                        case "last-modified":
                            requirement.setLastModified(Util.getDateFromString(value));
                            break;
                        case "req-time":
                            requirement.setCreationTime(Util.getTimeFromString(value));
                            break;
                        case "vc-version-number":
                            requirement.setVersionNumber(value);
                            break;
                        default:
                            break;
                    }
                }
            }
        });
        return requirement;
    }
}
