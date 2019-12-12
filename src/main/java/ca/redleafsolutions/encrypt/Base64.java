package ca.redleafsolutions.encrypt;

import ca.redleafsolutions.ishell2.annotations.IShellInvisible;
import ca.redleafsolutions.ishell2.annotations.MethodDescription;
import ca.redleafsolutions.ishell2.annotations.ParameterDescriptions;
import ca.redleafsolutions.ishell2.annotations.ParameterNames;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;

public class Base64 implements XCoderBase {
	@Override
	@MethodDescription ("Name of encoding module")
	public String name () {
		return "Base64";
	}
	
	@Override
	public void fromJSON (JSONItem json) throws JSONValidationException {
	}

	@Override
	@MethodDescription ("Encode string to Base64")
	@ParameterNames ({ "str" })
	@ParameterDescriptions ({ "String to be encoded" })
	public byte[] encrypt (String str) throws EncryptionException {
		return java.util.Base64.getEncoder ().encode (str.getBytes ());
	}

	@Override
	@MethodDescription ("Encode string to Base64")
	@ParameterNames ({ "str" })
	@ParameterDescriptions ({ "String to be encoded" })
	public String encryptToString (String str) throws EncryptionException {
		return new String (encrypt (str));
	}

	@Override
	@IShellInvisible
	public String decrypt (byte[] encrypted) throws EncryptionException {
		return new String (java.util.Base64.getDecoder ().decode (encrypted));
	}

	@Override
	@MethodDescription ("Decode string from Base64")
	@ParameterNames ({ "str" })
	@ParameterDescriptions ({ "String to be decoded" })
	public String decrypt (String str) throws EncryptionException {
		return new String (decrypt (str.getBytes ()));
	}
}
