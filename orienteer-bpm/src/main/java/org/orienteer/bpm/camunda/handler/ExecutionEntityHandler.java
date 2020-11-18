package org.orienteer.bpm.camunda.handler;

import com.github.raymanrt.orientqb.query.Clause;
import com.github.raymanrt.orientqb.query.Operator;
import com.github.raymanrt.orientqb.query.Query;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.camunda.bpm.engine.impl.*;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.orienteer.bpm.camunda.OPersistenceSession;
import org.orienteer.core.util.OSchemaHelper;
import ru.ydn.wicket.wicketorientdb.utils.GetODocumentFieldValueFunction;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * {@link IEntityHandler} for {@link ExecutionEntity} 
 */
public class ExecutionEntityHandler extends AbstractEntityHandler<ExecutionEntity> {
	
	public static final String OCLASS_NAME = "BPMExecution";

	public ExecutionEntityHandler() {
		super(OCLASS_NAME);
	}
	
	@Override
	public void applySchema(OSchemaHelper helper) {
		super.applySchema(helper);
		helper.oProperty("processInstanceId", OType.STRING, 10)
			  .oProperty("parentId", OType.STRING, 20)
			  .oProperty("processDefinition", OType.LINK, 30).assignVisualization("listbox")
			  												 .markAsLinkToParent()
			  												 .markDisplayable()
			  												 .markAsDocumentName()
			  .oProperty("created", OType.DATETIME, 30).defaultValue("sysdate()").markDisplayable()
			  .oProperty("businessKey", OType.STRING, 30).markDisplayable()
			  .oProperty("superExecution", OType.LINK, 40).assignVisualization("listbox")
			  .oProperty("superCaseExecutionId", OType.STRING, 50)
			  .oProperty("caseInstanceId", OType.STRING, 60)
			  .oProperty("activityInstanceId", OType.STRING, 70)
			  .oProperty("activityId", OType.STRING, 80)
			  .oProperty("active", OType.BOOLEAN, 90)
			  .oProperty("concurrent", OType.BOOLEAN, 100)
			  .oProperty("scope", OType.BOOLEAN, 120)
			  .oProperty("eventScope", OType.BOOLEAN, 120)
			  .oProperty("suspensionState", OType.INTEGER, 140)
			  .oProperty("cachedEntityState", OType.INTEGER, 150)
			  .oProperty("sequenceCounter", OType.LONG, 160)
			  .oProperty("childExecutions", OType.LINKLIST, 170).assignVisualization("table")
		      .oProperty("tasks", OType.LINKLIST, 180).assignVisualization("table")
		      .oProperty("eventSubscriptions", OType.LINKLIST, 190).assignVisualization("table")
			  .oProperty("variables", OType.LINKLIST, 200).assignVisualization("table")
			  .oProperty("historyEvents", OType.LINKLIST, 210).assignTab("history").assignVisualization("table")
		      .oProperty("historyVariableInstances", OType.LINKLIST, 220).assignVisualization("table");
	}

	@Override
	public void applyRelationships(OSchemaHelper helper) {
		super.applyRelationships(helper);
		helper.setupRelationship(ExecutionEntityHandler.OCLASS_NAME, "processDefinition", ProcessDefinitionEntityHandler.OCLASS_NAME, "executions");
		helper.setupRelationship(ExecutionEntityHandler.OCLASS_NAME, "superExecution", ExecutionEntityHandler.OCLASS_NAME, "childExecutions");
		helper.setupRelationship(ExecutionEntityHandler.OCLASS_NAME, "childExecutions", ExecutionEntityHandler.OCLASS_NAME, "superExecution");
		helper.setupRelationship(ExecutionEntityHandler.OCLASS_NAME, "tasks", TaskEntityHandler.OCLASS_NAME, "execution");
		helper.setupRelationship(ExecutionEntityHandler.OCLASS_NAME, "eventSubscriptions", EventSubscriptionEntityHandler.OCLASS_NAME, "execution");
		helper.setupRelationship(ExecutionEntityHandler.OCLASS_NAME, "variables", VariableInstanceEntityHandler.OCLASS_NAME, "execution");
	}
	
	@Statement
	public List<ExecutionEntity> selectProcessInstanceByQueryCriteria(OPersistenceSession session, ProcessInstanceQueryImpl query) {
		return  query(session, query, new IQueryMangler() {
			
			@Override
			public Query apply(Query input) {
				input.where(Clause.clause("parentId", Operator.NULL, ""));
				return input;
			}
		});
	}
	
	@Statement
	public List<ExecutionEntity> selectExecutionsByQueryCriteria(final OPersistenceSession session, final ExecutionQueryImpl query) {
		List<ExecutionEntity> ret = query(session, query, input -> {
			SuspensionState state = query.getSuspensionState();
			if(state!=null) input.where(Clause.clause("suspensionState", Operator.EQ, state.getStateCode()));

			String businessKey = query.getBusinessKey();
			if(businessKey!=null) {
				String sql = String.format("select processInstanceId from %s where businessKey=? limit 1", getSchemaClass());
				List<ODocument> proc = session.getDatabase().query(sql, businessKey).elementStream()
						.map(e -> (ODocument) e)
						.collect(Collectors.toCollection(LinkedList::new));
				if(!proc.isEmpty()) {
					String processInstanceId = proc.get(0).field("processInstanceId");
					input.where(Clause.clause("processInstanceId", Operator.EQ,  processInstanceId));
				}
			}
			return input;
		},"suspensionState", "businessKey");
		
		List<EventSubscriptionQueryValue> subscriptionsQueries = query.getEventSubscriptions();
		
		if(subscriptionsQueries!=null && !subscriptionsQueries.isEmpty()) {
			ret = new ArrayList<>(ret);
			for(EventSubscriptionQueryValue sub : subscriptionsQueries) {
				ListIterator<ExecutionEntity> it = ret.listIterator();
				while(it.hasNext()) {
					ExecutionEntity entity = it.next();
					List<EventSubscriptionEntity> subscriptions = entity.getEventSubscriptions();
					boolean hasMatch = false;
					for(EventSubscriptionEntity subscription: subscriptions) {
						if((sub.getEventName()==null || sub.getEventName().equals(subscription.getEventName())) 
								&& (sub.getEventType()==null || sub.getEventType().equals(subscription.getEventType()))) {
							hasMatch = true;
						}
					}
					if(!hasMatch) it.remove();
				}
			}
		}
		
		List<QueryVariableValue> queryVariableValues = query.getQueryVariableValues();
		if(queryVariableValues!=null && !queryVariableValues.isEmpty()) {
			ret = new ArrayList<>(ret);
			for(QueryVariableValue queryValue : queryVariableValues) {
				ListIterator<ExecutionEntity> it = ret.listIterator();
				while(it.hasNext()) {
					ExecutionEntity entity = it.next();
					QueryOperator operator = queryValue.getOperator();
					Object value = entity.getVariable(queryValue.getName());
					Object refValue = queryValue.getValue();
					Comparable<Object> comparable = (value instanceof Comparable && refValue instanceof Comparable)?(Comparable<Object>)value:null;
					boolean hasMatch;
					switch (operator) {
						case GREATER_THAN:
							hasMatch = comparable!=null && comparable.compareTo(refValue)>0;
						case GREATER_THAN_OR_EQUAL:
							hasMatch = comparable!=null && comparable.compareTo(refValue)>=0;
						case LESS_THAN:
							hasMatch = comparable!=null && comparable.compareTo(refValue)<0;
						case LESS_THAN_OR_EQUAL:
							hasMatch = comparable!=null && comparable.compareTo(refValue)<=0;
						case LIKE:
							hasMatch = value!=null && Pattern.matches(refValue.toString(), value.toString());
							break;
						case NOT_EQUALS:
							hasMatch = !Objects.equals(value, refValue);
							break;
						case EQUALS:
						default:
							hasMatch = Objects.equals(value, refValue);
							break;
					}
					if(!hasMatch) it.remove();
				}
			}
		}
		
		return ret;
	}
	
	@Override
	public boolean hasNeedInCache() {
		return true;
	}
	
	@Statement
	public List<ExecutionEntity> selectExecutionsByProcessInstanceId(OPersistenceSession session, ListQueryParameterObject obj) {
		return queryList(session, "select from "+getSchemaClass()+" where processInstanceId = ?", obj.getParameter());
	}
	
	@Statement
	public List<ExecutionEntity> selectExecutionsByParentExecutionId(OPersistenceSession session, ListQueryParameterObject parameter) {
		return queryList(session, "select from "+getSchemaClass()+" where parentId = ?", parameter.getParameter());
	}
	
	@Statement
	public List<String> selectProcessInstanceIdsByProcessDefinitionId(OPersistenceSession session, ListQueryParameterObject parameter) {
		ODatabaseDocument db = session.getDatabase();
		String sql = String.format("select id from %s where processDefinition.id = ?", getSchemaClass());

		return db.query(sql, parameter.getParameter()).stream()
				.map(r -> (String) r.getProperty("id"))
				.collect(Collectors.toCollection(LinkedList::new));
	}
	
}
