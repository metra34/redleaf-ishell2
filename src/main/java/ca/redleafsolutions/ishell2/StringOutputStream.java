package ca.redleafsolutions.ishell2;

import java.io.IOException;
import java.io.OutputStream;

public class StringOutputStream extends OutputStream {
	private StringBuffer sb = new StringBuffer ();

	@Override
	public void write (int b) throws IOException {
		sb.append (b);
	}

	@Override
	public void write (byte[] b, int off, int len) throws IOException {
		sb.append (new String (b, off, len));
	}
	
	@Override
	public void write (byte[] b) throws IOException {
		write (b, 0, b.length);
	}
	
	@Override
	public String toString () {
		return sb.toString ();
	}
}
