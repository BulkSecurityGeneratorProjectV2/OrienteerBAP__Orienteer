package org.orienteer.logger.server.repository;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.logger.OLoggerEvent;
import org.orienteer.logger.server.model.*;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository for working with {@link OLoggerEventModel}
 */
public final class OLoggerRepository {

    private OLoggerRepository() {}

    public static OLoggerEventModel storeOLoggerEvent(OLoggerEvent event) {
        return DBClosure.sudo(db -> storeOLoggerEvent(db, event));
    }

    public static OLoggerEventModel storeOLoggerEvent(ODatabaseDocument db, OLoggerEvent event) {
        return storeOLoggerEvent(db, event.toJson());
    }

    public static OLoggerEventModel storeOLoggerEvent(String eventJson) {
        return DBClosure.sudo(db -> storeOLoggerEvent(db, eventJson));
    }

    public static OLoggerEventModel storeOLoggerEvent(ODatabaseDocument db, String eventJson) {
        ODocument doc = new ODocument();
        doc.fromJSON(eventJson);
        Long dateTime = doc.field(OLoggerEventModel.PROP_DATE_TIME, Long.class);
        doc.field(OLoggerEventModel.PROP_DATE_TIME, new Date(dateTime));
        doc.setClassName(OLoggerEventModel.CLASS_NAME);
        doc.save();
        doc.reload();
        return new OLoggerEventModel(doc);
    }

    public static List<OLoggerEventModel> getEventsByCorrelationId(String correlationId) {
        return DBClosure.sudo(db -> OLoggerRepository.getEventsByCorrelationId(db, correlationId));
    }

    public static List<OLoggerEventModel> getEventsByCorrelationId(ODatabaseDocument db, String correlationId) {
        String sql = String.format("select from %s where %s = ?", OLoggerEventModel.CLASS_NAME,
                OLoggerEventModel.PROP_CORRELATION_ID);

        return db.query(sql, correlationId).elementStream()
                .map(e -> new OLoggerEventModel((ODocument) e))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public static Optional<OLoggerEventFilteredDispatcherModel> getOLoggerEventFilteredDispatcher(String alias) {
        return DBClosure.sudo(db -> OLoggerRepository.getOLoggerEventFilteredDispatcher(db, alias));
    }

    public static Optional<OLoggerEventFilteredDispatcherModel> getOLoggerEventFilteredDispatcher(ODatabaseDocument db, String alias) {
        return getOLoggerEventDispatcherAsDocument(db, alias)
                .map(OLoggerEventFilteredDispatcherModel::new);
    }

    public static Optional<OLoggerEventMailDispatcherModel> getOLoggerEventMailDispatcher(String alias) {
        return DBClosure.sudo(db -> getOLoggerEventMailDispatcher(db, alias));
    }

    public static Optional<OLoggerEventMailDispatcherModel> getOLoggerEventMailDispatcher(ODatabaseDocument db, String alias) {
        return getOLoggerEventDispatcherAsDocument(db, alias)
                .map(OLoggerEventMailDispatcherModel::new);
    }

    public static Optional<OLoggerEventDispatcherModel> getOLoggerEventDispatcher(String alias) {
        return DBClosure.sudo(db -> getOLoggerEventDispatcher(db, alias));
    }

    public static Optional<OLoggerEventDispatcherModel> getOLoggerEventDispatcher(ODatabaseDocument db, String alias) {
        return getOLoggerEventDispatcherAsDocument(db, alias).map(OLoggerEventDispatcherModel::new);
    }

    public static Optional<ODocument> getOLoggerEventDispatcherAsDocument(String alias) {
        return DBClosure.sudo(db -> OLoggerRepository.getOLoggerEventDispatcherAsDocument(db, alias));
    }

    public static Optional<ODocument> getOLoggerEventDispatcherAsDocument(ODatabaseDocument db, String alias) {
        String sql = String.format("select from %s where %s = ?", OLoggerEventDispatcherModel.CLASS_NAME,
                OLoggerEventDispatcherModel.PROP_ALIAS);
        return db.query(sql, alias).elementStream()
                .map(e -> (ODocument) e)
                .findFirst();
    }

    public static Optional<OCorrelationIdGeneratorModel> getOCorrelationIdGenerator(String alias) {
        return DBClosure.sudo(db -> getOCorrelationIdGenerator(db, alias));
    }

    public static Optional<OCorrelationIdGeneratorModel> getOCorrelationIdGenerator(ODatabaseDocument db, String alias) {
        return getOCorrelationIdGeneratorAsDocument(db, alias)
                .map(OCorrelationIdGeneratorModel::new);
    }

    public static Optional<ODocument> getOCorrelationIdGeneratorAsDocument(ODatabaseDocument db, String alias) {
        String sql = String.format("select from %s where %s = ?", OCorrelationIdGeneratorModel.CLASS_NAME,
                OCorrelationIdGeneratorModel.PROP_ALIAS);
        return db.query(sql, alias).elementStream()
                .map(e -> (ODocument) e)
                .findFirst();
    }
}
