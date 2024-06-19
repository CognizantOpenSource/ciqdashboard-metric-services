
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

import com.cts.idashboard.services.metricservice.data.ALMRequirementTypeDetails;
import com.cts.idashboard.services.metricservice.data.RequirementType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * RequirementTypeDetailsDeserializer
 *
 * @author Cognizant
 */

public class RequirementTypeDetailsDeserializer extends JsonDeserializer<ALMRequirementTypeDetails> {
    @Override
    public ALMRequirementTypeDetails deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);

        ALMRequirementTypeDetails typeDetails = new ALMRequirementTypeDetails();
        List<RequirementType> types = new ArrayList<>();
        ArrayNode typesNode = (ArrayNode) jsonNode.get("types");
        typesNode.forEach(typeNode -> {
            RequirementType type = new RequirementType();
            type.setId(typeNode.get("id").asText());
            type.setName(typeNode.get("name").asText());
            type.setHasDirectCoverage(typeNode.get("has-direct-coverage").asText());
            type.setRiskAnalysisType(typeNode.get("risk-analysis-type").asText());
            type.setDefaultChildTypeId(typeNode.get("default-child-type-id").asText());
            type.setIsDocumentRoot(typeNode.get("is-document-root").asText());
            types.add(type);
        });

        typeDetails.setTypes(types);
        return typeDetails;
    }
}
