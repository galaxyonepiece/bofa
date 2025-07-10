package com.adobe.aem.guides.wknd.core.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Calendar;
import java.util.Collections;

@Component(service = TimelineEventLogger.class)
public class TimelineEventLogger {

    private static final Logger log = LoggerFactory.getLogger(TimelineEventLogger.class);
    //private static final String SUBSERVICE_NAME = "datawrite";

    @Reference
    private ResourceResolverFactory resolverFactory;

    /**
     * Logs an event to the AEM DAM Timeline using a service user.
     */
    public static void logTimelineEvent(Session session, String assetPath, String userId, String activityName, String message) {
        try {

            log.debug("TimelineEventLogger: Logging timeline event for assetPath: {}", assetPath);
            //String assetPath = StringUtils.substringBefore(assetPath, "/jcr:content");
            String timelinePath = "/var/audit/com.day.cq.dam" + assetPath;

            Node eventRoot = JcrUtils.getOrCreateByPath(timelinePath, "nt:unstructured", session);
            String nodeName = "activity-" + System.currentTimeMillis();

            Node eventNode = eventRoot.addNode(nodeName, "cq:AuditEvent");
            eventNode.setProperty("cq:category", "com/day/cq/dam");
            eventNode.setProperty("cq:time", Calendar.getInstance());
            eventNode.setProperty("cq:userid", userId);
            eventNode.setProperty("cq:type", activityName);
            eventNode.setProperty("cq:path", assetPath);
            eventNode.setProperty("cq:message", message);

            session.save();
            log.debug("Timeline event logged successfully at {}", timelinePath);

        } catch (RepositoryException e) {
            log.error("Failed to write timeline event", e);
        }
    }
}
