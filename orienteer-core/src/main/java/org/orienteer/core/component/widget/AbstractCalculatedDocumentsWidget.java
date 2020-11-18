package org.orienteer.core.component.widget;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.string.Strings;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.command.Command;
import org.orienteer.core.component.command.CreateODocumentCommand;
import org.orienteer.core.component.property.DisplayMode;
import org.orienteer.core.component.table.OrienteerDataTable;
import org.orienteer.core.component.table.component.GenericTablePanel;
import org.orienteer.core.service.impl.OClassIntrospector;
import org.orienteer.core.widget.AbstractWidget;
import ru.ydn.wicket.wicketorientdb.model.OClassModel;
import ru.ydn.wicket.wicketorientdb.model.OQueryDataProvider;

import java.util.List;
import java.util.Map;

/**
 * Widget for calculated document
 * @param <T> the type of main data object linked to this widget
 */
public class AbstractCalculatedDocumentsWidget<T> extends AbstractWidget<T> {

    public static final String WIDGET_OCLASS_NAME = "CalculatedDocumentsWidget";

    @Inject
    protected OClassIntrospector oClassIntrospector;

    @SuppressWarnings("unchecked")
	public AbstractCalculatedDocumentsWidget(String id, IModel<T> model,
                                    IModel<ODocument> widgetDocumentModel) {
		super(id, model, widgetDocumentModel);

	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		IModel<DisplayMode> modeModel = DisplayMode.VIEW.asModel();
		final String sql = getSql();

		GenericTablePanel<ODocument> tablePanel;

		if (!Strings.isEmpty(sql)) {
			OQueryDataProvider<ODocument> provider = newDataProvider(sql);
			OClass expectedClass = getExpectedClass(provider);
			if (expectedClass != null) {
				oClassIntrospector.defineDefaultSorting(provider, expectedClass);
				List<? extends IColumn<ODocument, String>> columns = oClassIntrospector.getColumnsFor(expectedClass, true, modeModel);
				tablePanel = new GenericTablePanel<>("tablePanel", columns, provider, 20);
				customizeDataTable(tablePanel.getDataTable(), modeModel, new OClassModel(expectedClass));
			} else {
				tablePanel = new GenericTablePanel<>("tablePanel",  new ResourceModel("error.class.not.defined"));
			}
		} else {
			tablePanel = new GenericTablePanel<>("tablePanel",  new ResourceModel("error.query.not.defined"));
		}

		add(tablePanel);
	}

	protected void customizeDataTable(OrienteerDataTable<ODocument, String> table, IModel<DisplayMode> modeModel, IModel<OClass> expectedClass) {
		Map<String, Command<ODocument>> commands = oClassIntrospector.getCommandsForDocumentsTable(table, modeModel, expectedClass);
		commands.remove(CreateODocumentCommand.class.getName());
		commands.forEach((key, command) -> table.addCommand(command));
	}
	
	protected String getSql() {
		return getWidgetDocument().field("query");
	}
	
	protected OClass getExpectedClass(OQueryDataProvider<ODocument> provider) {
		String expectedClass =  getWidgetDocument().field("class");
		OClass ret = expectedClass!=null?getSchema().getClass(expectedClass):null;
		if(ret==null) {
			ret = provider.probeOClass(20);
		}
		return ret;
	}
	
	protected OQueryDataProvider<ODocument> newDataProvider(String sql) {
		return new OQueryDataProvider<>(sql);
	}
	
	@Override
	protected String getWidgetStyleClass() {
		return "strict";
	}

    @Override
    protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.arrows_h);
    }

    @Override
    protected IModel<String> getDefaultTitleModel() {
        return new ResourceModel("widget.document.calculated");
    }

}