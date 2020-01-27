package ca.redleafsolutions.ishell2.api;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import ca.redleafsolutions.ishell2.annotations.IShellInvisible;
import ca.redleafsolutions.ishell2.annotations.MethodDescription;
import ca.redleafsolutions.ishell2.annotations.ParameterDescriptions;
import ca.redleafsolutions.ishell2.annotations.ParameterNames;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;
import ca.redleafsolutions.json.JSONWritable;

public class APIInterface extends APIResponse<JSONWritable> implements JSONWritable {
	public APIInterface (APIInterface parent) {
		super (parent);
		super.o = this;
	}

	public APIInterface (Exception e) {
		super (e);
	}

	private static JSONItem toJSON (Class<?> cls) throws JSONValidationException {
		JSONItem json = JSONItem.newObject ();
		for (Method method:cls.getMethods ()) {
			Class<?> declaringclass = method.getDeclaringClass ();
			String name = method.getName ();

			if (declaringclass.equals (cls)) {
				JSONItem mj = JSONItem.newObject ();
				Class<?> rettype = method.getReturnType ();
				if (APIInterface.class.isAssignableFrom (rettype)) {
					json.put (name, toJSON (rettype));
				} else {
					json.put ("$type", rettype.getSimpleName ());
					if (APIResponse.class.isAssignableFrom (rettype)) {
						Type genrettype = method.getGenericReturnType ();
						String s = genrettype.getTypeName ();
						String gen = s.split ("<")[1];
						gen = gen.replaceAll (">", "");
						try {
							rettype = Class.forName (gen);
						} catch (ClassNotFoundException e) {
						}
					}
					mj.put ("return-type", rettype.getSimpleName ());
					mj.put ("return-pacakge", rettype.getPackage().getName ());

					MethodDescription description = method.getAnnotation (MethodDescription.class);
					if (description != null) {
						mj.put ("description", description.value ());
					}

					ParameterNames pnames = method.getAnnotation (ParameterNames.class);
					ParameterDescriptions pdescr = method.getAnnotation (ParameterDescriptions.class);

					int index = 0;
					Parameter[] params = method.getParameters ();
					JSONItem paramsj = JSONItem.newArray ();
					mj.put ("parameters", paramsj);
					for (Parameter param:params) {
						JSONItem pj = JSONItem.newObject ();
						paramsj.put (pj);
						pj.put ("pname", param.getName ());
						pj.put ("class", param.getType ().getSimpleName ());
						pj.put ("package", param.getType ().getPackage().getName ());
						if (pnames != null) {
							if (index < pnames.value ().length) {
								pj.put ("name", pnames.value ()[index]);
							}
						}
						if (pdescr != null) {
							if (index < pdescr.value ().length) {
								pj.put ("description", pdescr.value ()[index]);
							}
						}
						++index;
					}
					json.put (name, mj);
				}
			}
		}

		return json;
	}

	@Override
	@IShellInvisible
	public JSONItem toJSON () throws JSONValidationException {
		return toJSON (this.getClass ());
	}
/*
	@Override
	@IShellInvisible
	public String toHTML () {
		return toHTML (this.getClass (), 0);
	}

	private String toHTML (Class<?> cls, int depth) {
		StringBuffer html = new StringBuffer ();
		html.append ("\n<ul>");

		for (Method method:cls.getMethods ()) {
			Class<?> declaringclass = method.getDeclaringClass ();
			String name = method.getName ();

			html.append ("\n<li>").append (name);
			
			if (declaringclass.equals (cls)) {
				JSONItem mj = JSONItem.newObject ();
				Class<?> rettype = method.getReturnType ();
				if (APIInterface.class.isAssignableFrom (rettype)) {
					html.append ("\n<br>").append (toHTML (rettype, depth+1));
				} else {
					if (APIResponse.class.isAssignableFrom (rettype)) {
						Type genrettype = method.getGenericReturnType ();
						String s = genrettype.getTypeName ();
						String gen = s.split ("<")[1];
						gen = gen.replaceAll (">", "");
						try {
							rettype = Class.forName (gen);
						} catch (ClassNotFoundException e) {
						}
					}
					html.append (" RET ").append (rettype.getSimpleName ());

					MethodDescription description = method.getAnnotation (MethodDescription.class);
					if (description != null) {
						html.append (" DSCR ").append (description.value ());
					}

//					ParameterNames pnames = method.getAnnotation (ParameterNames.class);
//					ParameterDescriptions pdescr = method.getAnnotation (ParameterDescriptions.class);
//
//					int index = 0;
//					Parameter[] params = method.getParameters ();
//					JSONItem paramsj = JSONItem.newArray ();
//					mj.put ("parameters", paramsj);
//					for (Parameter param:params) {
//						JSONItem pj = JSONItem.newObject ();
//						paramsj.put (pj);
//						pj.put ("pname", param.getName ());
//						pj.put ("class", param.getType ().getSimpleName ());
//						pj.put ("package", param.getType ().getPackageName ());
//						if (pnames != null) {
//							if (index < pnames.value ().length) {
//								pj.put ("name", pnames.value ()[index]);
//							}
//						}
//						if (pdescr != null) {
//							if (index < pdescr.value ().length) {
//								pj.put ("description", pdescr.value ()[index]);
//							}
//						}
//						++index;
//					}
//					json.put (name, mj);
				}
			}
		}
		html.append ("\n</ul>");
		return html.toString ();
	}
	*/
}
