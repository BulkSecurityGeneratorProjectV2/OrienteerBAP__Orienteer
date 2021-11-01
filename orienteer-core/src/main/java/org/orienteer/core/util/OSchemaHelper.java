package org.orienteer.core.util;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.orienteer.core.OClassDomain;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.CustomAttribute;

import ru.ydn.wicket.wicketorientdb.OrientDbWebSession;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.core.CustomAttribute;
import org.orienteer.core.OClassDomain;
import ru.ydn.wicket.wicketorientdb.OrientDbWebSession;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Enhanced {@link ru.ydn.wicket.wicketorientdb.utils.OSchemaHelper} from wicket-orientdb library to allow Orienteer specific things
 */
public class OSchemaHelper extends ru.ydn.wicket.wicketorientdb.utils.OSchemaHelper
{
	protected OSchemaHelper(ODatabaseSession db)
	{
		super(db);
	}
	
	public static OSchemaHelper bind()
	{
		return new OSchemaHelper(OrientDbWebSession.get().getDatabaseSession());
	}
	
	public static OSchemaHelper bind(ODatabaseSession db)
	{
		return new OSchemaHelper(db);
	}
	
	@Override
	public OSchemaHelper oClass(
			String className, String... superClasses) {
		return (OSchemaHelper) super.oClass(className, superClasses);
	}
	
	@Override
	public OSchemaHelper oAbstractClass(
			String className, String... superClasses) {
		return (OSchemaHelper) super.oAbstractClass(className, superClasses);
	}
	
	public OSchemaHelper oProperty(
			String propertyName, OType type, int order) {
		super.oProperty(propertyName, type);
		return order(order);
	}

	@Override
	public OSchemaHelper oProperty(
			String propertyName, OType type) {
		return (OSchemaHelper) super.oProperty(propertyName, type);
	}
	
	@Override
	public OSchemaHelper oIndex(INDEX_TYPE type)
	{
		return (OSchemaHelper) super.oIndex(type);
	}

	@Override
	public OSchemaHelper oIndex(String name,
			INDEX_TYPE type) {
		return (OSchemaHelper) super.oIndex(name, type);
	}

	@Override
	public OSchemaHelper oIndex(String name,
			INDEX_TYPE type, String... fields) {
		return (OSchemaHelper) super.oIndex(name, type, fields);
	}
	
	public OSchemaHelper domain(OClassDomain domain) {
		checkOClass();
		CustomAttribute.DOMAIN.setValue(lastClass, domain, false);
		return this;
	}
	
	
	@Override
	public OSchemaHelper set(OClass.ATTRIBUTES attr, Object value) 
	{
		super.set(attr, value);
		return this;
	}
	
	@Override
	public OSchemaHelper set(OProperty.ATTRIBUTES attr, Object value) 
	{
		super.set(attr, value);
		return this;
	}
	
	@Override
	public OSchemaHelper defaultValue(String defaultValue)
	{
		super.defaultValue(defaultValue);
		return this;
	}
	
	@Override
	public OSchemaHelper min(String min)
	{
		super.min(min);
		return this;
	}
	
	@Override
	public OSchemaHelper max(String max)
	{
		super.max(max);
		return this;
	}
	
	@Override
	public OSchemaHelper notNull()
	{
		super.notNull();
		return this;
	}
	
	@Override
	public OSchemaHelper notNull(boolean value)
	{
		super.notNull(value);
		return this;
	}
	
	@Override
	public OSchemaHelper linkedClass(String className) {
		return (OSchemaHelper) super.linkedClass(className);
	}
	
	@Override
	public OSchemaHelper linkedType(
			OType linkedType) {
		return (OSchemaHelper) super.linkedType(linkedType);
	}
	
	public OSchemaHelper order(int order)
	{
		checkOProperty();
		CustomAttribute.ORDER.setValue(lastProperty, order);
		return this;
	}

	public OSchemaHelper orderProperties(String... fields)
	{
		checkOClass();
		for(int i=0; i<fields.length; i++)
		{
			String field = fields[i];
			OProperty oProperty = lastClass.getProperty(field);
			if(oProperty!=null)
			{
				CustomAttribute.ORDER.setValue(oProperty, i*10);
			}
		}
		return this;
	}
	
	public OSchemaHelper assignTab(String tab)
	{
		return updateCustomAttribute(CustomAttribute.TAB, tab);
	}
	
	public OSchemaHelper assignVisualization(String visualization)
	{
		return updateCustomAttribute(CustomAttribute.VISUALIZATION_TYPE, visualization);
	}
	
	public OSchemaHelper switchDisplayable(boolean displayable)
	{
		return updateCustomAttribute(CustomAttribute.DISPLAYABLE, displayable);
	}
	
	public OSchemaHelper assignTab(String tab, String... fields)
	{
		return updateCustomAttribute(CustomAttribute.TAB, tab, fields);
	}
	
	public OSchemaHelper assignVisualization(String visualization, String... fields)
	{
		return updateCustomAttribute(CustomAttribute.VISUALIZATION_TYPE, visualization, fields);
	}
	
	public OSchemaHelper switchDisplayable(boolean displayable, String... fields)
	{
		return updateCustomAttribute(CustomAttribute.DISPLAYABLE, displayable, fields);
	}
	
	public <V> OSchemaHelper updateCustomAttribute(CustomAttribute attr, V value)
	{
		checkOProperty();
		attr.setValue(lastProperty, value);
		return this;
	}
	
	public <V> OSchemaHelper updateCustomAttribute(CustomAttribute attr, V value, String... fields)
	{
		checkOClass();
		for(String field: fields)
		{
			OProperty oProperty = lastClass.getProperty(field);
			if(oProperty!=null)
			{
				attr.setValue(oProperty, value);
			}
		}
		return this;
	}
	
	public OSchemaHelper markDisplayable()
	{
		checkOProperty();
		CustomAttribute.DISPLAYABLE.setValue(lastProperty, true);
		return this;
	}
	
	public OSchemaHelper markAsDocumentName()
	{
		checkOProperty();
		CustomAttribute.PROP_NAME.setValue(lastClass, lastProperty);
		return this;
	}
	
	public OSchemaHelper markAsLinkToParent()
	{
		checkOProperty();
		CustomAttribute.PROP_PARENT.setValue(lastClass, lastProperty);
		return this;
	}
	
	public OSchemaHelper calculateBy(String script)
	{
		checkOProperty();
		CustomAttribute.CALCULABLE.setValue(lastProperty, true);
		CustomAttribute.CALC_SCRIPT.setValue(lastProperty, script);
		return this;
	}
	
	public OSchemaHelper defaultTab(String tab)
	{
		checkOClass();
		CustomAttribute.TAB.setValue(lastClass, tab);
		return this;
	}
	
	public OSchemaHelper assignNameAndParent(String nameField, String parentField)
	{
		checkOClass();
		OProperty name = nameField!=null?lastClass.getProperty(nameField):null;
		OProperty parent = parentField!=null?lastClass.getProperty(parentField):null;
		if(name!=null)
		{
			CustomAttribute.PROP_NAME.setValue(lastClass, name);
		}
		if(parent!=null)
		{
			CustomAttribute.PROP_PARENT.setValue(lastClass, parent);
		}
		return this;
	}
	
	public OSchemaHelper setupRelationship(String class1Name, String property1Name, String class2Name, String property2Name)
	{
		OClass class1 = schema.getClass(class1Name);
		if(class1==null) throw new IllegalStateException("Class '"+class1Name+"' was not found for setting up relationships");
		OProperty property1 = class1.getProperty(property1Name);
		if(property1==null) throw new IllegalStateException("Property '"+class1Name+"."+property1Name+"' was not found for setting up relationships");
		OClass class2 = schema.getClass(class2Name);
		if(class2==null) throw new IllegalStateException("Class '"+class2Name+"' was not found for setting up relationships");
		OProperty property2 = class2.getProperty(property2Name);
		if(property2==null) throw new IllegalStateException("Property '"+class2Name+"."+property2Name+"' was not found for setting up relationships");
		if(!Objects.equals(property1.getLinkedClass(), class2)) property1.setLinkedClass(class2);
		if(!Objects.equals(property2.getLinkedClass(), class1)) property2.setLinkedClass(class1);
		CustomAttribute.PROP_INVERSE.setValue(property1, property2);
		CustomAttribute.PROP_INVERSE.setValue(property2, property1);
		return this;
	}

	public OSchemaHelper setupRelationship(String class1Name, String propertyName, String class2Name) {
		OClass class1 = schema.getClass(class1Name);
		OProperty property = class1.getProperty(propertyName);
		OClass class2 = schema.getClass(class2Name);

		if (!Objects.equals(property.getLinkedClass(), class2)) property.setLinkedClass(class2);

		return this;
	}

	@Override
	public OSchemaHelper oDocument() {
		super.oDocument();
		return this;
	}

	@Override
	public OSchemaHelper oDocument(String pkField, Object pkValue) {
		super.oDocument(pkField, pkValue);
		return this;
	}
	
	@Override
	public OSchemaHelper saveDocument() {
		super.saveDocument();
		return this;
	}
	
	@Override
	public OSchemaHelper field(String field, Object value) {
		super.field(field, value);
		return this;
	}
	
	@Override
	public OSchemaHelper doOnOClass(Consumer<OClass> consumer) {
		super.doOnOClass(consumer);
		return this;
	}
	
	@Override
	public OSchemaHelper doOnOProperty(Consumer<OProperty> consumer) {
		super.doOnOProperty(consumer);
		return this;
	}
	
	@Override
	public OSchemaHelper doOnOIndex(Consumer<OIndex> consumer) {
		super.doOnOIndex(consumer);
		return this;
	}
	
	@Override
	public OSchemaHelper doOnODocument(Consumer<ODocument> consumer) {
		super.doOnODocument(consumer);
		return this;
	}
	
	/**
	 * Do operation on wrapped ODocument
	 * @param <T> type of a wrapper
	 * @param daoClass class of a wrapper
	 * @param consumer consumer for performing operation
	 * @return this
	 */
	public <T> OSchemaHelper doOnODocument(Class<? extends T> daoClass, Consumer<T> consumer) {
		checkODocument();
		consumer.accept(getODocument(daoClass));
		return this;
	}
	
	/**
	 * Get ODocument as wrapped in DAO
	 * @param <T> type of a wrapper
	 * @param daoClass class of a wrapper
	 * @return wrapped ODocument
	 */
	public <T> T getODocument(Class<? extends T> daoClass) {
		ODocument doc = getODocument();
		return doc!=null?DAO.provide(daoClass, doc):null;
	}
}
