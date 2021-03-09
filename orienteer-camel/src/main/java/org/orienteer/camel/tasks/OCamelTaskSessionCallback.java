package org.orienteer.camel.tasks;

import org.apache.camel.CamelContext;
import org.orienteer.core.tasks.ITaskSessionCallback;

/**
 * Callback for {@link CamelContext}  
 *
 */
public class OCamelTaskSessionCallback implements ITaskSessionCallback{

	private volatile OCamelContext context;
	
	public OCamelTaskSessionCallback(OCamelContext context) {
		this.context=context;
	}

	@Override
	public void interrupt() throws Exception{
		if(context!=null){
			context.stop();
		}
	}

}
