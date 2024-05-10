
package com.cts.idashboard.services.metricservice.data;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;


@Document(collection = "source_alm_test")
@CompoundIndex(name = "proj_test_index",
        def = "{'projectName': 1, 'testId': 1}",
        unique = true)
@Data
public class Test {
    @Id
    private String id;
    private String testId;
    private String testName;
    private String description;
    private String domainName;
    private String projectName;
    private String status;
    private String template;
    private String timeout;
    private String attachment;
    /*owner*/
    private String designer;
    private String steps;
    /*subtype-id*/
    private String type;
    /*exec-status*/
    private String executionStatus;
    /*dev-comments*/
    private String devComments;

    /*base-test-id*/
    private String baseTestId;
    /*has-linkage*/
    private String hasLinkage;

    /*order-id*/
    private String orderId;
    /*parent-id*/
    private String parentId;
    /*step-param*/
    private String stepParam;

    /*creation-time*/
    private Date creationDate;
    /*last-modified*/
    private Date lastModified;

    private String environmentName;
}
