package org.orienteer.core.dao;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.wicket.util.string.Strings;
import static org.orienteer.core.dao.handler.AbstractMethodHandler.typeToRequiredClass;
import org.orienteer.core.util.CommonUtils;
import org.orienteer.core.util.OSchemaHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.type.ODocumentWrapper;

/**
 * Utility class for creating implementations for required interfaces
 */
public final class DAO {
	
	private static final Logger LOG = LoggerFactory.getLogger(DAO.class);
	
	private static final Class<?>[] NO_CLASSES = new Class<?>[0];
	
	private DAO() {
		
	}
	
	public static <T> T create(Class<T> interfaceClass, Class<?>... additionalInterfaces) {
		DAOOClass daoOClass = interfaceClass.getAnnotation(DAOOClass.class);
		ODocumentWrapper docWrapper = daoOClass!=null && !Strings.isEmpty(daoOClass.value())
											? new ODocumentWrapper(daoOClass.value())
											: new ODocumentWrapper();
		return provide(interfaceClass, docWrapper, additionalInterfaces);
	}
	
	public static <T> T create(Class<T> interfaceClass, String className, Class<?>... additionalInterfaces) {
		return provide(interfaceClass, new ODocumentWrapper(className), additionalInterfaces);
	}
	
	public static <T> T provide(Class<T> interfaceClass, ORID iRID, Class<?>... additionalInterfaces) {
		return provide(interfaceClass, new ODocumentWrapper(iRID), additionalInterfaces);
	}
	
	public static <T> T provide(Class<T> interfaceClass, ODocument doc, Class<?>... additionalInterfaces) {
		return provide(interfaceClass, new ODocumentWrapper(doc), additionalInterfaces);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T provide(Class<T> interfaceClass, ODocumentWrapper docWrapper, Class<?>... additionalInterfaces) {
		if(additionalInterfaces == null) additionalInterfaces = NO_CLASSES;
		Class<?>[] builtInInterfaces = docWrapper.getClass().getInterfaces();
		Class<?>[] interfaces = new Class[2+builtInInterfaces.length+additionalInterfaces.length];
		interfaces[0] = interfaceClass;
		interfaces[1] = IODocumentWrapper.class;
		if(builtInInterfaces.length>0) System.arraycopy(builtInInterfaces, 0, interfaces, 2, builtInInterfaces.length);
		if(additionalInterfaces.length>0) System.arraycopy(additionalInterfaces, 0, interfaces, 2+builtInInterfaces.length, additionalInterfaces.length);
		return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), interfaces,  new ODocumentWrapperInvocationHandler(docWrapper));
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T dao(Class<T> interfaceClass, Class<?>... additionalInterfaces) {
		if(additionalInterfaces == null) additionalInterfaces = NO_CLASSES;
		Class<?>[] interfaces = new Class[1+additionalInterfaces.length];
		interfaces[0] = interfaceClass;
		if(additionalInterfaces.length>0) System.arraycopy(additionalInterfaces, 0, interfaces, 1, additionalInterfaces.length);
		return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), interfaces ,  new DAOInvocationHandler());
	}
	
	
	public static OSchemaHelper describe(OSchemaHelper helper, Class<?>... classes) {
		describe(helper, Arrays.asList(classes), new HashMap<Class<?>, String>());
		return helper;
	}
	
	private static Set<String> describe(OSchemaHelper helper, List<Class<?>> classes, Map<Class<?>, String> describedClasses) {
		Set<String> oClassNames = new HashSet<String>();
		for (Class<?> clazz : classes) {
			String className = describe(helper, clazz, describedClasses);
			if(className!=null) oClassNames.add(className);
		}
		return oClassNames;
	}
	
	private static String describe(OSchemaHelper helper, Class<?> clazz, Map<Class<?>, String> describedClasses) {
		if(clazz==null || !clazz.isInterface()) return null;	
		DAOOClass daooClass = clazz.getAnnotation(DAOOClass.class);
		if(daooClass==null) return null;
		if(describedClasses.containsKey(clazz)) return describedClasses.get(clazz);
		List<Class<?>> interfaces = Arrays.asList(clazz.getInterfaces());
		Set<String> superClasses = describe(helper, interfaces, describedClasses);
		superClasses.addAll(Arrays.asList(daooClass.superClasses()));
		
		List<Supplier<OProperty>> creators = new ArrayList<>();
		int currentOrder=0;
		
		for(Method method : clazz.getDeclaredMethods()) {
			String methodName = method.getName();
			Parameter[] params =  method.getParameters();
			String fieldNameCandidate = null;
			Class<?> javaType = null;
			Class<?> subJavaType = null;
			if(methodName.startsWith("set") && params.length==1) {
				fieldNameCandidate = CommonUtils.decapitalize(methodName.substring(3));
				javaType = params[0].getType();
				subJavaType = typeToRequiredClass(params[0].getParameterizedType());
			} else if(methodName.startsWith("get") && params.length==0) {
				fieldNameCandidate = CommonUtils.decapitalize(methodName.substring(3));
				javaType = method.getReturnType();
				subJavaType = typeToRequiredClass(method.getGenericReturnType());
			} else if(methodName.startsWith("is") && params.length==0) {
				fieldNameCandidate = CommonUtils.decapitalize(methodName.substring(2));
				javaType = method.getReturnType();
				subJavaType = typeToRequiredClass(method.getGenericReturnType());
			}
			if(fieldNameCandidate==null) continue;
			if(subJavaType!=null && subJavaType.equals(javaType)) subJavaType = null;
			final DAOField daoField = method.getAnnotation(DAOField.class);
			if(daoField!=null) fieldNameCandidate = daoField.value();
			OType oTypeCandidate = daoField!=null && !OType.ANY.equals(daoField.type())
											?daoField.type()
											:OType.getTypeByClass(javaType);
			final OType linkedType = daoField!=null && !OType.ANY.equals(daoField.linkedType())
											?daoField.linkedType()
											:(subJavaType!=null?OType.getTypeByClass(subJavaType):null);
			final int order = daoField!=null && daoField.order()>=0
									?daoField.order()
									:10*currentOrder++;
			String linkedClassCandidate = describe(helper, subJavaType, describedClasses);
			if(linkedClassCandidate==null) linkedClassCandidate = describe(helper, javaType, describedClasses);
			if(oTypeCandidate==null && linkedClassCandidate!=null) {
				oTypeCandidate = OType.LINK;
			}
			
			final String fieldName = fieldNameCandidate;
			final OType oType = oTypeCandidate;
			final String linkedClass = linkedClassCandidate;
			LOG.info("Method: "+methodName+" type: "+oType + "LinkedType: "+linkedType+" LinkedClass: "+linkedClass);
			creators.add(() -> {
				helper.oProperty(fieldName, oType, order);
				if(linkedType!=null) helper.linkedType(linkedType);
				if(linkedClass!=null) helper.linkedClass(linkedClass);
				return null;
			});
		}
		
		if(daooClass.isAbstract()) helper.oAbstractClass(daooClass.value(), superClasses.toArray(new String[superClasses.size()]));
		else helper.oClass(daooClass.value(), superClasses.toArray(new String[superClasses.size()]));
		
		for (Supplier<OProperty> supplier : creators) {
			supplier.get();
		}
		
		describedClasses.put(clazz, daooClass.value());
		return daooClass.value();
	}
}
