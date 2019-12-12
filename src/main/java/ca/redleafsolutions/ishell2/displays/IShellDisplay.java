/*
* iShell 2.0
*
* Copyright (c) 2010, Redleaf Solutions Ltd. All rights reserved.
*
* This library is proprietary software; you can not redistribute
* without an explicit consent from Releaf Solutions Ltd.
* The consent will detail the distribution and sale rights.
*/

package ca.redleafsolutions.ishell2.displays;

import java.io.IOException;
import java.net.URI;

public interface IShellDisplay {
	String getName ();
	Object getID ();
	void close ();
	String info ();
	void setTitle (String title);
	void open(URI uri) throws IOException;
}
