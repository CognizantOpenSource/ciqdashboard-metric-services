
package com.cts.idashboard.services.metricservice.data;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@Data
public class IDItemConfig {
    private String id;
    @NotBlank(message = "Name should not be empty/null")
    @Size(min = 4, message = "Name minimum characters should be '4' ")
    //private String name;
    private String description;
    @NotBlank(message = "Type should not be empty/null")
    @Size(min = 3, message = "Type minimum characters should be '3' ")
    private String type;
    private String itemGroup;
    private Map<String, Object> options;
    private String metricCategory;
    private String metricName;
    private Integer rows;
    private Integer cols;
    private Integer x;
    private Integer y;

    private List<FilterConfig> filters;
}
