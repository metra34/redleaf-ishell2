package ca.redleafsolutions.ishell2.interfaces.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import ca.redleafsolutions.ishell2.IShellRequest;

public class StreamAndLength {
	private long length;
	private InputStream is;
	private IShellRequest request;

	public StreamAndLength (File file, IShellRequest request) throws FileNotFoundException {
		this.request = request;
		length = file.length ();
		is = new FileInputStream (file);
	}

	public StreamAndLength (String str) {
		length = str.length ();
		is = new StringInputStream (str);
	}

	public long length () {
		return length;
	}

	public InputStream getInputStream () {
		return is;
	}
	
	public IShellRequest getRequest () {
		return request;
	}

	private class StringInputStream extends InputStream {
		private String str;
		private int ptr = 0;

		public StringInputStream (String str) {
			this.str = str;
		}

		@Override
		public int read () throws IOException {
			if (ptr >= str.length ())
				return -1;
			return str.charAt (ptr++);
		}

		@Override
		public int read (byte[] b, int off, int len) throws IOException {
			if (b == null)
				throw new NullPointerException ();
			if ((off < 0) || (len < 0) || (len > b.length - off))
				throw new IndexOutOfBoundsException ();
			if (len == 0)
				return 0;

			byte[] strbuff = str.getBytes ();
			int i;
			for (i=0; i<len; ++i) {
				if (ptr >= str.length ())
					break;
				b[i + off] = strbuff[ptr++];
			}
			return i;
		}
	}
}
