package ca.redleafsolutions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings ("serial")
public class IShellDate extends Date {
	public IShellDate () {
		super ();
	}

	public IShellDate (long millis) {
		super (millis);
	}

	public IShellDate (Date date) {
		this (date.getTime ());
	}

	public String getMmmdyyyy () {
		DateFormat df = new SimpleDateFormat ("MMM d, yyyy");
		return df.format (this);
	}

	public String getMdyyyy () {
		DateFormat df = new SimpleDateFormat ("MMMMM d, yyyy");
		return df.format (this);
	}

	public String getYyyymmdd () {
		DateFormat df = new SimpleDateFormat ("yyyy-MM-dd");
		return df.format (this);
	}

	public String getShort () {
		DateFormat df = new SimpleDateFormat ("yyyyMMdd");
		return df.format (this);
	}

	public String getDatetime () {
		DateFormat df = new SimpleDateFormat ("yyyy-MM-dd HH:mm");
		return df.format (this);
	}

	public String getElapsed () {
		long different = new Date ().getTime () - getTime ();

		long secondsInMilli = 1000;
		long minutesInMilli = secondsInMilli * 60;
		long hoursInMilli = minutesInMilli * 60;
		long daysInMilli = hoursInMilli * 24;

		long elapsedDays = different / daysInMilli;
		different = different % daysInMilli;

		long elapsedHours = different / hoursInMilli;
		different = different % hoursInMilli;

		long elapsedMinutes = different / minutesInMilli;
		different = different % minutesInMilli;

		long elapsedSeconds = different / secondsInMilli;

		return elapsedDays + " days, " + elapsedHours + " hours, " + elapsedMinutes + " minutes, " + elapsedSeconds
				+ " seconds";
	}

	public String getAgo () {
		long diff = new Date ().getTime () - getTime ();

		// diff now in seconds
		diff /= 1000;
		if (diff < 60) {
			return "just now";
		}
		
		// diff now in minutes
		diff /= 60;
		if (diff < 60) {
			return diff + " minutes ago";
		}
		if (diff < 60*24) {
			int hours = (int)(diff/60);
			int minutes = (int)(diff % 60);
			return hours + "h " + minutes + "m ago";
		}
		// diff now in days
		diff /= 60*24;
		++diff;
		if (diff < 30) {
			return diff + " days ago";
		}

		// diff now in months
		diff /= 30;
		if (diff == 1) {
			return "Last month";
		}
		if (diff < 12) {
			return diff + " months ago";
		}

		diff /= 12;
		if (diff == 1) {
			return "Last year";
		}
		return diff + " years ago";
	}

	public int getDaysAgo (IShellDate date) {
		Date now = new Date ();
		long sincemidnight = now.getTime () % (1000*60*60*24);
		long diff = Math.abs (date.getTime () - getTime ());
		long wholedays = (diff - sincemidnight)/(1000*60*60*24);
		return (int)wholedays;
	}
}
