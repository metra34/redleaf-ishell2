package ca.redleafsolutions.encrypt;

import ca.redleafsolutions.json.JSONReadable;

public interface XCoderBase extends JSONReadable {
	String name ();
	byte[] encrypt (String strToEncrypt) throws EncryptionException;
	String encryptToString (String strToEncrypt) throws EncryptionException;
	String decrypt (byte[] encrypted) throws EncryptionException;
	String decrypt (String strToDecrypt) throws EncryptionException;
}
