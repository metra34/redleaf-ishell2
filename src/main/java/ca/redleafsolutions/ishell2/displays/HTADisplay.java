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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Iterator;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;

public class HTADisplay implements IShellDisplay {
	private URI uri;
	private JSONItem params;
	private transient File htafile;

	public HTADisplay () {
		params = null;
	}

	public HTADisplay (JSONItem params) {
		this ();
		this.params = params;
	}

	@Override
	public String getName () {
		return "HTA display";
	}

	@Override
	public Object getID () {
		return null;
	}

	@Override
	public void close () {
	}

	@Override
	public String info () {
		return toString ();
	}

	@Override
	public void setTitle (String title) {
		// TODO Auto-generated method stub

	}

	@Override
	public void open (URI uri) throws IOException {
		VelocityContext map = new VelocityContext ();
		map.put ("launchuri", uri);
		if (params != null) {
			for (Iterator<?> it = params.keys (); it.hasNext ();) {
				String key = (String)it.next ();
				try {
					Object value = params.get (key);
					map.put (key, value);
				} catch (JSONValidationException e) {
				}
			}
		}

		Writer os = new StringWriter ();
		InputStream inputstream = this.getClass ().getResourceAsStream ("hta.vm");
		if (inputstream == null) {
			throw new IOException ("HTA file template not found");
		}
		Reader is = new InputStreamReader (inputstream);
		try {
			htafile = File.createTempFile ("launch.", ".hta");
			os = new FileWriter (htafile);
			if (Velocity.evaluate (map, os, "HTA", is)) {
				is.close ();
				os.close ();

				Runtime.getRuntime ().exec ("cmd /c " + htafile.getAbsolutePath ());
				this.uri = uri;

				// after 10 seconds delete the temp file
				new Thread ("HTA File delete timer") {
					@Override
					public void run () {
						try {
							Thread.sleep (10000);
							htafile.delete ();
						} catch (InterruptedException e) {
						}
					}
				}.start ();
			}
		} catch (ParseErrorException e) {
			throw new IOException (e);
		} catch (MethodInvocationException e) {
			throw new IOException (e);
		} catch (ResourceNotFoundException e) {
			throw new IOException ("HTA file template not found");
		}
	}

	@Override
	public String toString () {
		return "HTA Display of " + uri;
	}
}