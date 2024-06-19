
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

import com.cts.idashboard.services.metricservice.data.ALMTestDetails;
import com.cts.idashboard.services.metricservice.data.Test;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * TestDetailsDeserializer
 *
 * @author Cognizant
 */

public class TestDetailsDeserializer extends JsonDeserializer<ALMTestDetails> {
    @Override
    public ALMTestDetails deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
        ALMTestDetails almTestDetails = new ALMTestDetails();
        List<Test> tests = new ArrayList<>();

        ArrayNode entities = (ArrayNode) jsonNode.get("entities");
        int totalResults = jsonNode.get("TotalResults").asInt();
        if (entities != null && entities.size() > 0) {
            entities.forEach(entityNode -> {
                ArrayNode fields = (ArrayNode) entityNode.get("Fields");
                if (fields != null && fields.size() > 0) {
                    Test test = getTest(fields);
                    tests.add(test);
                }
            });
        }

        almTestDetails.setTests(tests);
        almTestDetails.setTotalResults(totalResults);
        return almTestDetails;
    }

    private Test getTest(ArrayNode fields) {
        Test test = new Test();
        fields.forEach(fieldNode -> {
            String name = fieldNode.get("Name").asText();
            ArrayNode values = (ArrayNode) fieldNode.get("values");
            if (values != null && values.size() > 0) {
                JsonNode valueNode = values.get(0);
                if (valueNode.has("value")) {
                    String value = valueNode.get("value").asText();
                    switch (name) {
                        case "id":
                            test.setTestId(value);
                            break;
                        case "name":
                            test.setTestName(value);
                            break;
                        case "description":
                            test.setDescription(value);
                            break;
                        case "status":
                            test.setStatus(value);
                            break;
                        case "template":
                            test.setTemplate(value);
                            break;
                        case "timeout":
                            test.setTimeout(value);
                            break;
                        case "attachment":
                            test.setAttachment(value);
                            break;
                        case "owner":
                            test.setDesigner(value);
                            break;
                        case "steps":
                            test.setSteps(value);
                            break;
                        case "subtype-id":
                            test.setType(value);
                            break;
                        case "exec-status":
                            test.setExecutionStatus(value);
                            break;
                        case "dev-comments":
                            test.setDevComments(value);
                            break;
                        case "base-test-id":
                            test.setBaseTestId(value);
                            break;
                        case "has-linkage":
                            test.setHasLinkage(value);
                            break;
                        case "order-id":
                            test.setOrderId(value);
                            break;
                        case "parent-id":
                            test.setParentId(value);
                            break;
                        case "step-param":
                            test.setStepParam(value);
                            break;
                        case "creation-time":
                            test.setCreationDate(Util.getDateFromString(value));
                            break;
                        case "last-modified":
                            test.setLastModified(Util.getDateFromString(value));
                            break;
                        default:
                            break;
                    }
                }
            }
        });
        return test;
    }
}
