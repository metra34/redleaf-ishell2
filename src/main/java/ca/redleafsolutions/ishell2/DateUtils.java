package ca.redleafsolutions.ishell2;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ca.redleafsolutions.SingletonException;
import ca.redleafsolutions.ishell2.logs.iLogger;

public class DateUtils {
	private static DateUtils instance;

	public static DateUtils getInstance () {
		if (instance == null)
			try {
				instance = new DateUtils ();
			} catch (SingletonException e) {
				iLogger.severe (e);
			}
		return instance;
	}

	private DateUtils () throws SingletonException {
		if (instance != null)
			throw new SingletonException (this);
	}

	public String date1 (Date date) {
		DateFormat df = new SimpleDateFormat ("MMM d, yyyy");
		return df.format (date);
	}

	public String date2 (Date date) {
		DateFormat df = new SimpleDateFormat ("M d, yyyy");
		return df.format (date);
	}

	public Object timeDiffString (Date date) {
		Date now = new Date ();
		long diff = now.getTime () - date.getTime ();
		
		diff /= 1000;
		
		if (diff < 30) {
			return "just now";
		}
		if (diff < 600) {
			return "few minutes ago";
		}
		diff /= 60;
		if (diff < 60) {
			return diff + " minutes ago";
		}
		if (diff < 105) {
			return "an hour ago";
		}
		diff /= 60;
		if (diff < 24) {
			return diff + " hours ago";
		}
		
		return DateUtils.getInstance ().date1 (date);
	}
}
