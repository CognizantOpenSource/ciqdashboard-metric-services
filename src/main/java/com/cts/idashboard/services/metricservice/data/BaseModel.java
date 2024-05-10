
package com.cts.idashboard.services.metricservice.data;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

/**
 * BaseModel - BaseModel for all the collection in mongodb
 * @author Cognizant
 */

@Data
public class BaseModel {
    @JsonIgnore
    @CreatedBy
    private String createdUser;
    @JsonIgnore
    @CreatedDate
    private Instant createdDate;
    @JsonIgnore
    @LastModifiedBy
    private String lastModifiedUser;
    @JsonIgnore
    @LastModifiedDate
    private Instant lastModifiedDate;
}
