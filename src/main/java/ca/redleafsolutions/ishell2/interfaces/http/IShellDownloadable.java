package ca.redleafsolutions.ishell2.interfaces.http;

public interface IShellDownloadable {
	String getMimeType ();
	String getFilename ();
	long length ();
	byte[] getBuffer ();
	void close ();
}
