package ${package};

import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.module.AbstractOrienteerModule;
import org.orienteer.core.module.IOrienteerModule;
import org.orienteer.core.util.OSchemaHelper;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * {@link IOrienteerModule} for '${artifactId}' module
 */
public class Module extends AbstractOrienteerModule{

	protected Module() {
		super("${artifactId}", 1);
	}
	
	@Override
	public ODocument onInstall(OrienteerWebApplication app, ODatabaseSession db) {
		super.onInstall(app, db);
		OSchemaHelper helper = OSchemaHelper.bind(db);
		//Install data model
		//Return null of default OModule is enough
		return null;
	}
	
	@Override
	public void onInitialize(OrienteerWebApplication app, ODatabaseSession db) {
		super.onInitialize(app, db);
		app.mountPackage("${package}.web");
	}
	
	@Override
	public void onDestroy(OrienteerWebApplication app, ODatabaseSession db) {
		super.onDestroy(app, db);
		app.unmountPackage("${package}.web");
	}
	
}
