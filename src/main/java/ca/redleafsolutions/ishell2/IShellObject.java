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

abstract public class IShellObject {
	protected Object object;
	private double duration = -1;
	private boolean details = false;
	private IShellRequest request;

	protected IShellObject (Object object, IShellRequest request) {
		this.object = object;
		this.request = request;
	}

	public Object getObject () {
		return object;
	}
	
	public IShellRequest getRequest () {
		return request;
	}
	
	public void setDuration (long duration) {
		this.duration = duration/1000000.;
		if (this.duration >= 1000) {
			this.duration = Math.round (this.duration);
		} else if (this.duration >= 100) {
			this.duration = Math.round (this.duration*10)/10.;
		} else if (this.duration >= 10) {
			this.duration = Math.round (this.duration*100)/100.;
		} else {
			this.duration = Math.round (this.duration*1000)/1000.;
		}
	}
	
	public double getDuration () {
		return duration;
	}

	public boolean isExecuted () {
		return (this instanceof ExecutedObject);
	}
	public boolean isRaw () {
		return (this instanceof RawObject);
	}
	public boolean isException () {
		return (this instanceof ExceptionObject);
	}

	static public class ExecutedObject extends IShellObject {
		public ExecutedObject (Object object, IShellRequest request) {
			super (object, request);
		}
	}

	static public class RawObject extends IShellObject {
		public RawObject (Object object, IShellRequest request) {
			super (object, request);
		}
	}

	static public class ExceptionObject extends ExecutedObject {
		public ExceptionObject (Throwable exception, IShellRequest request) {
			super (exception, request);
		}
	}
	static public class Cached extends IShellObject {
		public Cached (Object object, IShellRequest request) {
			super (object, request);
		}
	}

	public void showDetails () {
		this.details = true;
	}
	public boolean isDetails () {
		return details;
	}
}
