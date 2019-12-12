package ca.redleafsolutions.ishell2.interfaces.http;

public class IShellDownloadStream implements IShellDownloadable {
	private String mimeType;
	private String filename;
	private byte[] buffer;

	public IShellDownloadStream (byte[] buffer, String filename) {
		this (buffer, "application/octet-stream", filename);
	}

	public IShellDownloadStream (byte[] buffer, String mimeType, String filename) {
		this.buffer = buffer;
		this.mimeType = mimeType;
		this.filename = filename;
	}

	public String getMimeType () {
		return mimeType;
	}

	public String getFilename () {
		return filename;
	}

	public long length () {
		return buffer.length;
	}

	@Override
	public byte[] getBuffer () {
		return buffer;
	}

	@Override
	public void close () {
		buffer = null;
	}
}
