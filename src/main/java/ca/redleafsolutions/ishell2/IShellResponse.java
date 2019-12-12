/*
* iShell 2.0
*
* Copyright (c) 2010, Redleaf Solutions Ltd. All rights reserved.
*
* This library is proprietary software; you can not redistribute
* without an explicit consent from Releaf Solutions Ltd.
* The consent will detail the distribution and sale rights.
*/

package ca.redleafsolutions.ishell2;

import java.lang.reflect.InvocationTargetException;

import ca.redleafsolutions.ishell2.IShellException.ResourceNotFound;
import ca.redleafsolutions.ishell2.IShellObject.ExecutedObject;
import ca.redleafsolutions.ishell2.IShellObject.RawObject;
import ca.redleafsolutions.ishell2.logs.iLogger;
import ca.redleafsolutions.ishell2.renderers.RendererFactory;
import ca.redleafsolutions.ishell2.renderers.ResponseRendererBase;

public class IShellResponse {
	private IShellObject o;
	private ResponseRendererBase renderer;

	public IShellResponse (IShellObject o, String format, double timing) {
		this.o = o;
		RendererFactory factory = RendererFactory.getInstance ();
		try {
			renderer = factory.getRendererByExtention (format, timing);
		} catch (ResourceNotFound | IShellException.InvocationTargetException e) {
			try {
				renderer = factory.getDefaultRenderer (timing);
			} catch (Throwable e2) {
				iLogger.severe (e2);
			}
		}
	}

	public Object getResponse () {
		return o;
	}

	@Override
	public String toString () {
		if (o.isException ()) {
			Throwable throwable = (Throwable)o.getObject ();
			if (throwable instanceof IShellException.InvocationTargetException) {
				throwable = ((IShellException.InvocationTargetException)throwable).getException ();
			}
			if (throwable instanceof InvocationTargetException) {
				throwable = ((InvocationTargetException)throwable).getTargetException ();
			}
			return renderer.toExceptionString (throwable, o.getRequest ());
		}
		
		Object obj = o;
		if (o instanceof RawObject) {
			obj = ((RawObject)o).getObject();
		} else if (o instanceof ExecutedObject) {
			obj = ((ExecutedObject)o).getObject();
		}
		
		if (o.getRequest ().isDetails ()) {
			return renderer.toDetails (obj, o.getRequest ());
		} else {
			return renderer.toString (obj, o.getRequest ());
		}
	}
}
