package org.orienteer.core.service;

import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.server.OServer;
import de.agilecoders.wicket.webjars.settings.IWebjarsSettings;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.Localizer;
import org.apache.wicket.extensions.markup.html.repeater.data.table.export.CSVDataExporter;
import org.apache.wicket.extensions.markup.html.repeater.data.table.export.IDataExporter;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.service.impl.GuiceOrientDbSettings;
import org.orienteer.core.service.impl.OClassIntrospector;
import org.orienteer.core.service.impl.OrienteerWebjarsSettings;
import org.orienteer.core.tasks.OTaskManager;
import ru.ydn.wicket.wicketorientdb.IOrientDbSettings;
import ru.ydn.wicket.wicketorientdb.security.IResourceCheckingStrategy;

/**
 * Main module to load Orienteer stuff to Guice
 * 
 * <h1>Properties</h1>
 * Properties can be retrieved from both files from the local filesystem and
 * files on the Java classpath. 
 * Highlevel lookup:
 * <ol>
 * <li>If there is a qualifier - lookup by this qualifier</li>
 * <li>If there is no a qualifier - lookup by default qualifier 'orienteer'</li>
 * <li>If nothing was found - use embedded configuration</li> 
 * </ol>
 * Order of lookup for a specific qualifier (for example 'myapplication'):
 * <ol>
 * <li>lookup of file specified by system property 'myapplication.properties'</li>
 * <li>lookup of URL specified by system property 'myapplication.properties'</li>
 * <li>lookup of file 'myapplication.properties' up from current directory</li>
 * <li>lookup of file 'myapplication.properties' in '~/orienteer/' directory</li>
 * <li>lookup of resource 'myapplication.properties' in a classpath</li>
 * </ol>
 */
public class OrienteerModule extends AbstractModule {

	public OrienteerModule() {
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void configure() {
		
		bind(IOrientDbSettings.class).to(GuiceOrientDbSettings.class).asEagerSingleton();
		bind(IOClassIntrospector.class).to(OClassIntrospector.class);
		bind(UIVisualizersRegistry.class).asEagerSingleton();
		bind(IWebjarsSettings.class).to(OrienteerWebjarsSettings.class).asEagerSingleton();
		bind(IDataExporter.class).to(CSVDataExporter.class);
		Provider<ODatabaseDocumentInternal> dbProvider = binder().getProvider(ODatabaseDocumentInternal.class);
		bind(ODatabaseSession.class).toProvider(dbProvider);
		bind(ODatabaseDocument.class).toProvider(dbProvider);
	}
	
	@Provides
    @Named("orientdb.server.config")
    public String provideOrientDBConfig(@Named("orientdb.distributed") boolean distributed, @Orienteer Properties properties) throws IOException {
		String dbConfigName = distributed?"distributed.db.config.xml":"db.config.xml";
		URL resourceURL = OrienteerWebApplication.class.getResource(dbConfigName);
		return new MapVariableInterpolator(IOUtils.toString(resourceURL, Charset.forName("UTF8")), 
										   Maps.fromProperties(properties), 
										   true).toString();
		
    }

	@Provides
	public ODatabaseDocumentInternal getDatabaseDocumentInternal() {
		return ODatabaseRecordThreadLocal.instance().get();
	}
	
	@Provides
	public ODatabasePool getCachedDatabasePool(IOrientDbSettings settings) {
		OrienteerWebSession session = OrienteerWebSession.get();
		return settings.getContext().cachedPool(settings.getDbName(), session.getUsername(), session.getPassword());
	}

	@Provides
	public OSchema getSchema(ODatabaseSession db)
	{
		return db.getMetadata().getSchema();
	}

	@Provides
	public OServer getOServer(WebApplication application)
	{

		OrienteerWebApplication app = (OrienteerWebApplication)application;
		return app.getServer();
	}

	@Provides
	public Localizer getLocalizer(WebApplication application)
	{
		return application.getResourceSettings().getLocalizer();
	}
	
	@Provides
	public OTaskManager getTaskManager() {
		return OTaskManager.get();
	}

	@Provides
	public IResourceCheckingStrategy getResourceCheckingStrategy()
	{
		return OrienteerWebApplication.lookupApplication();
	}

}
