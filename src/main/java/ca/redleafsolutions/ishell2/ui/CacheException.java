package ca.redleafsolutions.ishell2.ui;

import ca.redleafsolutions.ishell2.IShellException;

@SuppressWarnings ("serial")
public class CacheException extends IShellException {
	protected CacheException () {
		super ();
	}

	public CacheException (String msg) {
		super (msg);
	}

	static public class NotFound extends CacheException {}
}
