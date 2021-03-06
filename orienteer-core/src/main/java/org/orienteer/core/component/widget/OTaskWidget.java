package org.orienteer.core.component.widget;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.string.Strings;
import org.orienteer.core.behavior.UpdateOnActionPerformedEventBehavior;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.widget.document.CalculatedDocumentsWidget;
import org.orienteer.core.tasks.ITaskSession;
import org.orienteer.core.tasks.IOTask;
import org.orienteer.core.widget.Widget;
import ru.ydn.wicket.wicketorientdb.model.OQueryDataProvider;

/**
 * Widget for {@link IOTask}
 *
 */
@Widget(domain="document",selector=IOTask.CLASS_NAME, id=OTaskWidget.WIDGET_TYPE_ID, order=20, autoEnable=true)
public class OTaskWidget extends CalculatedDocumentsWidget{

	public static final String WIDGET_TYPE_ID = "task";
	private static final long serialVersionUID = 1L;

	public OTaskWidget(String id, IModel<ODocument> model, IModel<ODocument> widgetDocumentModel) {
		super(id, model, widgetDocumentModel);
		add(UpdateOnActionPerformedEventBehavior.INSTANCE_ALL_CONTINUE);
	}
	
	@Override
	protected String getSql() {
		String sql = super.getSql();
		if(!Strings.isEmpty(sql)) return sql;
		else {
			return "select from "+ITaskSession.TASK_SESSION_CLASS+" where "+ ITaskSession.Field.TASK_LINK.fieldName()+"=:doc";

		}
	}

	@Override
	protected OClass getExpectedClass(OQueryDataProvider<ODocument> provider) {
		return getSchema().getClass(ITaskSession.TASK_SESSION_CLASS);
	}
	
	@Override
	protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.bars);
	}

	@Override
	protected IModel<String> getDefaultTitleModel() {
		return new ResourceModel("task.title");
	}

}