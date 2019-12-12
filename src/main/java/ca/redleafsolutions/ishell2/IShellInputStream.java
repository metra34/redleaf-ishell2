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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * This class is intended to pass an input stream of a resource all the way back
 * to the renderer for efficient streaming out to the output stream
 */
public class IShellInputStream {
	protected InputStream is;
	private long count;
	protected java.lang.String mimeType;

	public IShellInputStream (InputStream is, long length) {
		this.is = is;
		this.count = length;
		this.mimeType = null;
	}

	public InputStream getInputStream () {
		return is;
	}

	public long length () {
		return count;
	}

	public void tunnelTo (OutputStream os) {
		count = 0;
		byte[] buff = new byte[1024];
		int len;
		try {
			while ((len = is.read (buff)) > 0) {
				os.write (buff, 0, len);
				count += len;
			}
		} catch (IOException e) {
			// read stream to the end
		}
	}

	public void close () throws IOException {
		is.close ();
	}

	public java.lang.String getMimeType () {
		return mimeType;
	}

	public java.lang.String toString () {
		StringWriter os = new StringWriter ();
		byte[] buff = new byte[1024];
		int len;
		try {
			while ((len = is.read (buff)) > 0) {
				os.write (new java.lang.String (buff, 0, len));
			}
		} catch (IOException e) {
			// end of input stream
		} finally {
			try {
				is.close ();
			} catch (IOException e) {
				// nothing to do
			}
			try {
				os.close ();
			} catch (IOException e) {
				// nothing to do
			}
		}
		return os.toString ();
	}

	static public class LeaveOpen extends IShellInputStream {
		public LeaveOpen (InputStream is, long length) {
			super (is, length);
		}

		@Override
		public void close () {
			// do nothing. Leave input stream open
		}
	}

	static public class String extends IShellInputStream {
		public String (java.lang.String s) {
			super (new ByteArrayInputStream (s.getBytes (StandardCharsets.UTF_8)), s.length ());
		}
	}
}
