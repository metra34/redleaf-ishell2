package ca.redleafsolutions.ishell2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ImageFile extends IShellInputStream {
	private File file;

	public ImageFile (File file) throws FileNotFoundException {
		super (new FileInputStream (file), file.length ());
		this.file = file;
		super.mimeType = "image/png";
	}

	@Override
	public java.lang.String toString () {
		return file + " (" + (file.exists ()? file.length () + " bytes": "not found") + ")";
	}
}
