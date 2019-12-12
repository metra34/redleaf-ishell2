package ca.redleafsolutions.ishell2;

import java.util.LinkedList;

public class iShellTemplateList extends LinkedList<iShellTemplate> implements HTMLWritable {
	private static final long serialVersionUID = 2311972043432373780L;

	@Override
	public String toHTML () {
		String s = "";
		for (iShellTemplate template:this) {
			s += template.toHTML ();
		}
		return s;
	}

}
