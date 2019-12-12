package ca.redleafsolutions.ishell2.displays;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import ca.redleafsolutions.ishell2.IShellFactory;
import ca.redleafsolutions.ishell2.annotations.MethodDescription;
import ca.redleafsolutions.ishell2.annotations.ParameterDescriptions;
import ca.redleafsolutions.ishell2.annotations.ParameterNames;
import ca.redleafsolutions.json.JSONValidationException;

public class IShellDisplayWrapper {
	private ArrayList<IShellDisplay> displays;

	public IShellDisplayWrapper (ArrayList<IShellDisplay> displays) {
		this.displays = displays;
	}
	
	@MethodDescription ("List all displays")
	public ArrayList<IShellDisplay> list () {
		return displays;
	}
	
	@MethodDescription ("Open a display to a URL")
	@ParameterNames ({"type", "url"})
	@ParameterDescriptions ({"Type of display", "URL to open"})
	public void open (String type, String url) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException, JSONValidationException {
		Class<? extends IShellDisplay> cls = IShellFactory.getInstance ().getDisplayClass (type);
		IShellFactory.getInstance ().createDisplay (cls, url);
	}
}
