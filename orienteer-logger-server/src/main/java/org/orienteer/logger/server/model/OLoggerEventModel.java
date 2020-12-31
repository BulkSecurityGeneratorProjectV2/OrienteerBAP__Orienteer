package org.orienteer.logger.server.model;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.type.ODocumentWrapper;

import java.time.Instant;
import java.util.Date;

/**
 * Wrapper for logger event
 */
public class OLoggerEventModel extends ODocumentWrapper {

    public static final String CLASS_NAME = "OLoggerEvent";

    public static final String PROP_EVENT_ID        = "eventId";
    public static final String PROP_APPLICATION     = "application";
    public static final String PROP_NODE_ID         = "nodeId";
    public static final String PROP_CORRELATION_ID  = "correlationId";
    public static final String PROP_DATE_TIME       = "dateTime";
    public static final String PROP_REMOTE_ADDRESS  = "remoteAddress";
    public static final String PROP_HOST_NAME       = "hostName";
    public static final String PROP_USERNAME        = "username";
    public static final String PROP_CLIENT_URL      = "clientUrl";
    public static final String PROP_SUMMARY         = "summary";
    public static final String PROP_MESSAGE         = "message";
    public static final String PROP_SEED_CLASS      = "seedClass";
    public static final String PROP_SOURCE      	= "source";
    public static final String PROP_SOURCE_DOC      = "sourceDoc";
    

    public OLoggerEventModel() {
        this(CLASS_NAME);
    }

    public OLoggerEventModel(ODocument doc) {
        super(doc);
        checkForSourceDoc();
    }

    public OLoggerEventModel(String iClassName) {
        super(iClassName);
    }

    public String getEventId() {
        return document.field(PROP_EVENT_ID);
    }

    public OLoggerEventModel setEventId(String id) {
        document.field(PROP_EVENT_ID, id);
        return this;
    }

    public String getApplication() {
        return document.field(PROP_APPLICATION);
    }

    public OLoggerEventModel setApplication(String application) {
        document.field(PROP_APPLICATION, application);
        return this;
    }

    public String getNodeId() {
        return document.field(PROP_NODE_ID);
    }

    public OLoggerEventModel setNodeId(String nodeId) {
        document.field(PROP_NODE_ID, nodeId);
        return this;
    }

    public String getCorrelationId() {
        return document.field(PROP_CORRELATION_ID);
    }

    public OLoggerEventModel setCorrelationId(String correlationId) {
        document.field(PROP_CORRELATION_ID, correlationId);
        return this;
    }

    public Instant getDatetime() {
        Date date = getDatetimeAsDate();
        return date != null ? date.toInstant() : null;
    }

    public Date getDatetimeAsDate() {
        return document.field(PROP_DATE_TIME);
    }

    public OLoggerEventModel setDatetime(Instant datetime) {
        return setDatetimeAsDate(datetime != null ? Date.from(datetime) : null);
    }

    public OLoggerEventModel setDatetimeAsDate(Date date) {
        document.field(PROP_DATE_TIME, date);
        return this;
    }

    public String getRemoteAddress() {
        return document.field(PROP_REMOTE_ADDRESS);
    }

    public OLoggerEventModel setRemoteAddress(String address) {
        document.field(PROP_REMOTE_ADDRESS, address);
        return this;
    }

    public String getHostName() {
        return document.field(PROP_HOST_NAME);
    }

    public OLoggerEventModel setHostName(String hostName) {
        document.field(PROP_HOST_NAME, hostName);
        return this;
    }

    public String getUsername() {
        return document.field(PROP_USERNAME);
    }

    public OLoggerEventModel setUsername(String username) {
        document.field(PROP_USERNAME, username);
        return this;
    }

    public String getClientUrl() {
        return document.field(PROP_CLIENT_URL);
    }

    public OLoggerEventModel setClientUrl(String clientUrl) {
        document.field(PROP_CLIENT_URL, clientUrl);
        return this;
    }

    public String getSummary() {
        return document.field(PROP_SUMMARY);
    }

    public String getMessage() {
        return document.field(PROP_MESSAGE);
    }

    public OLoggerEventModel setMessage(String message) {
        document.field(PROP_MESSAGE, message);
        return this;
    }

    public String getSeedClass() {
        return document.field(PROP_SEED_CLASS);
    }

    public OLoggerEventModel setSeedClass(String seedClass) {
        document.field(PROP_SEED_CLASS, seedClass);
        return this;
    }
    
    public String getSource() {
    	return document.field(PROP_SOURCE);
    }
    
    public OLoggerEventModel setSource(String source) {
    	document.field(PROP_SOURCE, source);
    	checkForSourceDoc();
        return this;
    }
    
    public ODocument getSourceDoc() {
    	return document.field(PROP_SOURCE_DOC);
    }
    
    public OLoggerEventModel setSourceDoc(OIdentifiable source) {
    	document.field(PROP_SOURCE_DOC, source);
        return this;
    }
    
    protected void checkForSourceDoc() {
    	String source = getSource();
    	if(source!=null && ORecordId.isA(source)) {
    		setSourceDoc(new ORecordId(source));
    	}
    }
}
