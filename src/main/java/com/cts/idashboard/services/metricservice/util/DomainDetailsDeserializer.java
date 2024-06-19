
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

import com.cts.idashboard.services.metricservice.data.ALMDomainDetails;
import com.cts.idashboard.services.metricservice.data.Domain;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*
 * DomainDetailsDeserializer
 *
 * @author Cognizant
 */

public class DomainDetailsDeserializer extends JsonDeserializer<ALMDomainDetails> {
    @Override
    public ALMDomainDetails deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);

        ALMDomainDetails details = new ALMDomainDetails();
        List<Domain> domains = new ArrayList<>();
        JsonNode domainNodes = jsonNode.get("Domains");
        if (domainNodes.has("Domain")) {
            JsonNode domain = domainNodes.get("Domain");
            if (domain instanceof ArrayNode)
                domains = (List<Domain>) JsonUtil.getJsonClassList(domainNodes, "Domain", Domain.class);
            else
                domains = Collections.singletonList((Domain) JsonUtil.getJsonClassObject(domainNodes, "Domain", Domain.class));
        }
        details.setDomains(domains);

        return details;
    }
}
