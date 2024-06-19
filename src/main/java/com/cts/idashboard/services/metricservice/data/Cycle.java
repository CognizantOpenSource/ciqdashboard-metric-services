

package com.cts.idashboard.services.metricservice.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "source_alm_cycle")
@CompoundIndex(name = "proj_cyc_index",
        def = "{'projectName': 1, 'cycleId': 1}",
        unique = true)
public class Cycle {
    @Id
    private String id;
    private String cycleId;
    private String cycleName;
    private String description;
    private String domainName;
    private String projectName;
    /*parent-id*/
    private String releaseId;
    /*req-count*/
    private int reqCount;
    /*ver-stamp*/
    private String verStamp;
    /*cf-count*/
    private int cfCount;
    /*has-attachments*/
    private String hasAttachments;
    /*start-date*/
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date startDate;
    /*end-date*/
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date endDate;
    /*last-modified*/
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastModified;

    private String environmentName;
}
