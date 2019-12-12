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

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import ca.redleafsolutions.ishell2.annotations.MethodDescription;
import ca.redleafsolutions.ishell2.annotations.ParameterDescriptions;
import ca.redleafsolutions.ishell2.annotations.ParameterNames;

public class DisplayWrapper {
	private ArrayList<IShellDisplay> displays;
	private int _default;

	public DisplayWrapper (ArrayList<IShellDisplay> displays) {
		this.displays = displays;
		setdefault (0);
	}

	@MethodDescription ("List all available displays")
	public Collection<IShellDisplay> list () {
		return displays;
	}

	@MethodDescription ("Get display from list of available displays")
	@ParameterNames ("index")
	@ParameterDescriptions ("Index of display")
	public IShellDisplay get (int index) {
		return displays.get (index);
	}

	@MethodDescription ("Set the default display index for other commands to apply")
	@ParameterNames ("index")
	@ParameterDescriptions ("Index of default display")
	public void setdefault (int index) {
		if (displays.size () < index) {
			throw new IndexOutOfBoundsException ();
		}
		this._default = index;
	}
	
	@MethodDescription ("Close display")
	@ParameterNames ("index")
	@ParameterDescriptions ("Index of display to be removed")
	public void close (int index) {
		displays.get (index).close ();
		displays.remove (index);
	}

	@MethodDescription ("Open a URL")
	@ParameterNames ("url")
	@ParameterDescriptions ("The URL of resource to open")
	public void open (String url) throws MalformedURLException {
		if (displays.isEmpty ())
			throw new RuntimeException ("No displayes are set");

		try {
			displays.get (_default).open (URI.create (url));
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace ();
		}
	}

	@MethodDescription ("Get some infomation about the default display")
	public String info () throws Exception {
		IShellDisplay display = get (_default);
		return display.info ();
	}
	
	@Override
	public String toString () {
		return displays.toString ();
	}
}
