package ca.redleafsolutions.encrypt;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.StringUtils;

import ca.redleafsolutions.ishell2.annotations.IShellInvisible;
import ca.redleafsolutions.ishell2.annotations.MethodDescription;
import ca.redleafsolutions.ishell2.annotations.ParameterDescriptions;
import ca.redleafsolutions.ishell2.annotations.ParameterNames;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;

public class AES implements XCoderBase {
	private static SecretKeySpec secretKey = null;
	private static String key;

	@IShellInvisible
	public static AES getInstance () throws EncryptionException {
		if (secretKey == null)
			throw new EncryptionException ("Encryption key is not set yet");
		return new AES ();
	}

	@MethodDescription ("Set encryption key")
	@ParameterNames ({ "key" })
	@ParameterDescriptions ({ "Encryption key" })
	public static void setKey (String myKey) throws EncryptionException {
		try {
			MessageDigest sha = null;
			key = myKey + "<->" + StringUtils.reverse (myKey);
			byte[] bkey = myKey.getBytes ("UTF-8");
			sha = MessageDigest.getInstance ("SHA-1");
			bkey = sha.digest (bkey);
			bkey = Arrays.copyOf (bkey, 16); // use only first 128 bit
			secretKey = new SecretKeySpec (bkey, "AES");
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
			throw new EncryptionException (e);
		}
	}

	@IShellInvisible
	public static Cipher getCypher (int mode) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		Cipher cipher = Cipher.getInstance ("AES/ECB/PKCS5Padding");
		cipher.init (mode, secretKey);
		return cipher;
	}

	private AES () {}

	public AES (JSONItem json) throws JSONValidationException, EncryptionException {
		try {
			setKey (json.getString ("key"));
		} catch (JSONValidationException.MissingKey e) {
		}
	}

	@Override
	@MethodDescription ("Name of encryption module")
	public String name () {
		return "AES";
	}
	
	@Override
	@IShellInvisible
	public byte[] encrypt (String strToEncrypt) throws EncryptionException {
		try {
			Cipher cipher = getCypher (Cipher.ENCRYPT_MODE);
			return cipher.doFinal (strToEncrypt.getBytes ("UTF-8"));
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | UnsupportedEncodingException | IllegalBlockSizeException
				| BadPaddingException e) {
			throw new EncryptionException (e);
		}
	}

	@MethodDescription ("Encrypt a string using 128 bit AES and Base64")
	@ParameterNames ({ "str", "key" })
	@ParameterDescriptions ({ "String to be encrypted", "key" })
	public byte[] encrypt (String strToEncrypt, String key) throws EncryptionException {
		if (!key.equals (AES.key)) {
			setKey (key);
		}
		return encrypt (strToEncrypt);
	}

	@Override
	@MethodDescription ("Encrypt a string using 128 bit AES and Base64")
	@ParameterNames ({ "str" })
	@ParameterDescriptions ({ "String to be encrypted" })
	public String encryptToString (String strToEncrypt) throws EncryptionException {
		return Base64.getEncoder ().encodeToString (encrypt (strToEncrypt));
	}

	@MethodDescription ("Encrypt a string using 128 bit AES and Base64")
	@ParameterNames ({ "str", "key" })
	@ParameterDescriptions ({ "String to be encrypted", "key" })
	public String encryptToString (String strToEncrypt, String key) throws EncryptionException {
		return Base64.getEncoder ().encodeToString (encrypt (strToEncrypt, key));
	}

	@Override
	@MethodDescription ("Decrypt an Base64 encoded string using 128 bit AES")
	@ParameterNames ({ "str" })
	@ParameterDescriptions ({ "Encrypted string" })
	public String decrypt (String strToDecrypt) throws EncryptionException {
		return decrypt (Base64.getDecoder ().decode (strToDecrypt));
	}

	@MethodDescription ("Decrypt an Base64 encoded string using 128 bit AES")
	@ParameterNames ({ "str", "key" })
	@ParameterDescriptions ({ "Encrypted string", "AES key" })
	public String decrypt (String strToDecrypt, String key) throws EncryptionException {
		return decrypt (Base64.getDecoder ().decode (strToDecrypt), key);
	}

	@Override
	@IShellInvisible
	public String decrypt (byte[] encrypted) throws EncryptionException {
		try {
			Cipher cipher = getCypher (Cipher.DECRYPT_MODE);
			return new String (cipher.doFinal (encrypted));
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
			throw new EncryptionException (e);
		}
	}

	@IShellInvisible
	public String decrypt (byte[] encrypted, String key) throws EncryptionException {
		if (!key.equals (AES.key)) {
			setKey (key);
		}
		return decrypt (encrypted);
	}

	@Override
	public void fromJSON (JSONItem json) throws JSONValidationException {
		try {
			setKey (json.getString ("key"));
		} catch (EncryptionException e) {
			throw new JSONValidationException (e.getCause ());
		}
	}
}
