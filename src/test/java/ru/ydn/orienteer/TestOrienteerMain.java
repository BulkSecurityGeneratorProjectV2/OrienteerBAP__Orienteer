package ru.ydn.orienteer;

import java.util.Collection;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.ydn.orienteer.junit.OrienteerTestRunner;
import ru.ydn.orienteer.web.BrowseClassPage;
import ru.ydn.orienteer.web.LoginPage;
import ru.ydn.orienteer.web.schema.ListOClassesPage;
import ru.ydn.orienteer.web.schema.OClassPage;
import ru.ydn.orienteer.web.schema.OIndexPage;
import ru.ydn.orienteer.web.schema.OPropertyPage;
import ru.ydn.wicket.wicketorientdb.IOrientDbSettings;
import ru.ydn.wicket.wicketorientdb.OrientDbWebSession;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;

@RunWith(OrienteerTestRunner.class)
@Singleton
public class TestOrienteerMain
{
	private static final Logger LOG = LoggerFactory.getLogger(TestOrienteerMain.class);
	@Inject
	private WicketTester tester;
	
	@Before
	public void performLogin()
	{
		tester.startPage(ListOClassesPage.class);
		if(tester.getLastRenderedPage() instanceof LoginPage)
		{
			FormTester formTester = tester.newFormTester("signInPanel:signInForm");
			IOrientDbSettings settings = ((OrienteerWebApplication)tester.getApplication()).getOrientDbSettings();
            formTester.setValue("username", settings.getDBInstallatorUserName());
            formTester.setValue("password", settings.getDBInstallatorUserPassword());
            formTester.submit();
		}
		tester.assertRenderedPage(ListOClassesPage.class);
	}
	
	@Test
	public void testMainPages() throws Exception
	{
		tester.startPage(ListOClassesPage.class);
		tester.assertRenderedPage(ListOClassesPage.class);
	}
	
	@Test
	public void testBrowsePages() throws Exception
	{
		ODatabaseRecord db = getDatabase();
		Collection<OClass> classes = db.getMetadata().getSchema().getClasses();
		PageParameters parameters = new PageParameters();
		for (OClass oClass : classes)
		{
			parameters.set("className", oClass.getName());
			LOG.info("Rendering browse page for class '"+oClass.getName()+"'");
			tester.startPage(BrowseClassPage.class, parameters);
			tester.assertRenderedPage(BrowseClassPage.class);
		}
	}
	
	@Test
	public void testViewClassesAndPropertiesPages() throws Exception
	{
		ODatabaseRecord db = getDatabase();
		Collection<OClass> classes = db.getMetadata().getSchema().getClasses();
		PageParameters parameters = new PageParameters();
		for (OClass oClass : classes)
		{
			parameters.clearNamed();
			parameters.set("className", oClass.getName());
			LOG.info("Rendering page for class '"+oClass.getName()+"'");
			tester.startPage(OClassPage.class, parameters);
			tester.assertRenderedPage(OClassPage.class);
			Collection<OProperty> properties = oClass.properties();
			for (OProperty oProperty : properties)
			{
				parameters.set("propertyName", oProperty.getName());
				LOG.info("Rendering page for property '"+oProperty.getFullName()+"'");
				tester.startPage(OPropertyPage.class, parameters);
				tester.assertRenderedPage(OPropertyPage.class);
			}
			Collection<OIndex<?>> indexes = oClass.getIndexes();
			for (OIndex<?> oIndex : indexes)
			{
				parameters.set("indexName", oIndex.getName());
				LOG.info("Rendering page for index '"+oIndex.getName()+"'");
				tester.startPage(OIndexPage.class, parameters);
				tester.assertRenderedPage(OIndexPage.class);
			}
		}
	}
	
	private ODatabaseRecord getDatabase()
	{
		return ((OrientDbWebSession)tester.getSession()).getDatabase();
	}
}
