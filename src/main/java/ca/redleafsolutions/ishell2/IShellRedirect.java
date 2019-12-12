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

public class IShellRedirect implements HTMLWritable, IShellRedirectable {
	protected String url;

	public IShellRedirect (String url) {
		this.url = url;
	}

	@Override
	public String getUrl () {
		return url;
	}

	@Override
	public String toString () {
		return "Redirecting to " + url;
	}

	@Override
	public String toHTML () {
		return "Redirecting to <a href='" + url + "'>" + url + "</a>";
	}
}
