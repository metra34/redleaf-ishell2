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

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

public class Browser implements IShellDisplay {
	@Override
	public String getName () {
		return "default browser";
	}

	@Override
	public Object getID () {
		return null;
	}

	@Override
	public void close () {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String info () {
		return "default browser";
	}

	@Override
	public void setTitle (String title) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void open (URI uri) {
		try {
			Desktop.getDesktop ().browse (uri);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString () {
		return "Default Browser Display";
	}
}