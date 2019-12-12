/*
* iShell 2.0
*
* Copyright (c) 2010, Redleaf Solutions Ltd. All rights reserved.
*
* This library is proprietary software; you can not redistribute
* without an explicit consent from Releaf Solutions Ltd.
* The consent will detail the distribution and sale rights.
*/

package ca.redleafsolutions.ishell2;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface XMLWritable {
	public Element toXML (Document doc);
}
