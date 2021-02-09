package org.orienteer.logger.server.resource;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.util.io.IOUtils;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.logger.server.OLoggerModule;
import org.orienteer.logger.server.model.IOLoggerDAO;
import org.orienteer.logger.server.model.IOLoggerEventModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * REST entry point of OLogger events 
 */
public class OLoggerReceiverResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	public static final String MOUNT_PATH = "/resource/ologger";
	public static final String REGISTRATION_RES_KEY=OLoggerReceiverResource.class.getSimpleName();
	
	private static final Logger LOG = LoggerFactory.getLogger(OLoggerReceiverResource.class);
	
	@Inject
	private IOLoggerDAO oLoggerDao;
	
	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		final WebRequest request = (WebRequest) attributes.getRequest();
		final HttpServletRequest httpRequest = (HttpServletRequest) request.getContainerRequest();
		final ResourceResponse response = new ResourceResponse();
		response.setContentType("text/plain");
		if(response.dataNeedsToBeWritten(attributes))
		{
			String out="OK";
			try
			{
				if(httpRequest.getMethod().equalsIgnoreCase("GET") //for debug 
						|| httpRequest.getMethod().equalsIgnoreCase("POST") )
				{

					String content = IOUtils.toString(httpRequest.getInputStream());
					IOLoggerEventModel log = oLoggerDao.storeOLoggerEvent(content);
					out = log.getDocument().toJSON();
				}
			} catch (Throwable e)
			{
				LOG.error("Error", e);
				String message = e.getMessage();
				if(message==null) message = "Error";
				out = message;
			}
			final String finalOut = out;

			response.setWriteCallback(new WriteCallback() {
				@Override
				public void writeData(Attributes attributes) throws IOException {
					attributes.getResponse().write(finalOut);
				}
			});
		}
		return response;
	}
	
	public static void mount(WebApplication app)
	{
		OLoggerReceiverResource resource = ((OrienteerWebApplication) app).getServiceInstance(OLoggerReceiverResource.class);
		app.getSharedResources().add(REGISTRATION_RES_KEY, resource);
		app.mountResource(MOUNT_PATH, new SharedResourceReference(REGISTRATION_RES_KEY));
	}
	
	public static void unmount(WebApplication app)
	{
		app.unmount(MOUNT_PATH);
	}
}

