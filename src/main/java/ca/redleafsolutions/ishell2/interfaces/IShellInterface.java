/*
* iShell 2.0
*
* Copyright (c) 2010, Redleaf Solutions Ltd. All rights reserved.
*
* This library is proprietary software; you can not redistribute
* without an explicit consent from Releaf Solutions Ltd.
* The consent will detail the distribution and sale rights.
*/

package ca.redleafsolutions.ishell2.interfaces;

import java.util.Set;

public interface IShellInterface {
	Set<IShellInterfaceHandler> getHandlers ();
	void addHandler (IShellInterfaceHandler handler);
	void removeHandler (IShellInterfaceHandler handler);
	boolean isBrowserInterface ();
}
