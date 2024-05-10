
package com.cts.idashboard.services.metricservice.data;

import com.cts.idashboard.services.metricservice.util.DomainDetailsDeserializer;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "Domain"
})
@Data
@JsonDeserialize(using = DomainDetailsDeserializer.class)
public class ALMDomainDetails {

    @JsonProperty("Domain")
    private List<Domain> domains = null;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
