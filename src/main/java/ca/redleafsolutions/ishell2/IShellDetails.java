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


public class IShellDetails extends IShellObject.RawObject {
	public IShellDetails (IShellObject ishellobj) {
		super (ishellobj.getObject (), ishellobj.getRequest ());
	}
	
	@Override
	public String toString () {
		return super.toString ();
	}
}
