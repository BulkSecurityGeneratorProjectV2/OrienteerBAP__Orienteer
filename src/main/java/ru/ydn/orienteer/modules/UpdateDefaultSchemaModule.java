package ru.ydn.orienteer.modules;

import javax.inject.Singleton;

import ru.ydn.orienteer.CustomAttributes;
import ru.ydn.orienteer.OrienteerWebApplication;
import ru.ydn.orienteer.components.properties.visualizers.PasswordVisualizer;
import ru.ydn.orienteer.utils.OSchemaHelper;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;

@Singleton
public class UpdateDefaultSchemaModule extends AbstractOrienteerModule
{
	private static final String OCLASS_FUNCTION="OFunction";
	private static final String OCLASS_RESTRICTED="ORestricted";
	private static final String OCLASS_ROLE="ORole";
	private static final String OCLASS_USER="OUser";
	private static final String OCLASS_SCHEDULE="OSchedule";
	
	public UpdateDefaultSchemaModule()
	{
		super("update-default-schema", 1);
	}

	@Override
	public void onInstall(OrienteerWebApplication app, ODatabaseDocument db) {
		onUpdate(app, db, 0, getVersion());
	}

	@Override
	public void onUpdate(OrienteerWebApplication app, ODatabaseDocument db,
			int oldVersion, int newVersion) {
		if(oldVersion>=newVersion) return;
		switch (oldVersion+1)
		{
			case 1:
				onUpdateToFirstVesion(app, db);
				break;
			default:
				break;
		}
		if(oldVersion+1<newVersion) onUpdate(app, db, oldVersion+1, newVersion);
	}
	
	public void onUpdateToFirstVesion(OrienteerWebApplication app, ODatabaseDocument db)
	{
		OSchemaHelper helper = OSchemaHelper.bind(db);
		if(helper.existsClass(OCLASS_FUNCTION))
		{
			helper.oClass(OCLASS_FUNCTION)
				.assignVisualization("textarea", "code")
				.orderProperties("name", "language", "idempotent", "parameters", "code")
				.switchDisplayable(true, "name", "language", "parameters")
				.assignNameAndParent("name", null);
		}
		if(helper.existsClass(OCLASS_RESTRICTED))
		{
			String[] fields = {"_allow", "_allowRead", "_allowUpdate", "_allowDelete"};
			helper.oClass(OCLASS_RESTRICTED)
				.assignTab("security", fields)
				.assignVisualization("table", fields)
				.orderProperties(fields); 
		}
		if(helper.existsClass(OCLASS_ROLE))
		{
			helper.oClass(OCLASS_ROLE)
				.orderProperties("name", "mode", "inheritedRole", "rules")
				.assignNameAndParent("name", "inheritedRole")
				.switchDisplayable(true, "name", "model", "inheritedRole");
		}
		if(helper.existsClass(OCLASS_USER))
		{
			helper.oClass(OCLASS_USER)
				.orderProperties("name", "status", "password", "roles")
				.assignVisualization("table", "roles")
				.assignVisualization(PasswordVisualizer.NAME, "password")
				.assignNameAndParent("name", null)
				.switchDisplayable(true, "name", "status");
		}
		if(helper.existsClass(OCLASS_SCHEDULE))
		{
			helper.oClass(OCLASS_SCHEDULE)
				.orderProperties("name", "rule", "status", "start", "starttime", "arguments", "function")
				.assignNameAndParent("name", null)
				.switchDisplayable(true, "name", "status", "rule");
		}
	}
	
}
