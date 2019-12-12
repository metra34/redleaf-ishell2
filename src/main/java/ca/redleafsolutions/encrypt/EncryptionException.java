package ca.redleafsolutions.encrypt;

@SuppressWarnings ("serial")
public class EncryptionException extends Exception {
	public EncryptionException (Exception e) {
		super (e);
	}

	public EncryptionException (String msg) {
		super (msg);
	}
}
