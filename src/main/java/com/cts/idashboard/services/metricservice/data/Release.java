
package com.cts.idashboard.services.metricservice.data;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "source_alm_release")
@CompoundIndex(name = "proj_rel_index", def = "{'projectName': 1, 'releaseId': 1}", unique = true)
public class Release {
    @Id
    private String id;
    private String releaseId;
    private String releaseName;
    private String domainId;
    private String domainName;
    private String projectId;
    private String projectName;
    private String description;
    private String parentId;
    private Date startDate;
    private Date endDate;
    private Date lastModified;
    private int reqCount;
    private String verStamp;
    private int scopeItemsCount;
    private int milestonesCount;
    private Object hasAttachments;
    private String environmentName;
}
