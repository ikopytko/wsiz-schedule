package pl.wsiz.schedule.model;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import pl.wsiz.schedule.R;
import pl.wsiz.schedule.Utils;
import android.content.Context;

public class Day {
	public String id;
	public String label;
	public String title;
	public int image;
	public String time;
	public String form;
	public String date;
	public String info;
	public String teacher;

	public Day() {

	}

	public Day(Context context, String id, String subject, String room,
			String type, String time_s, String time_e, String date, int img) {

		this.id = id;
		title = subject;
		image = img;
		time = time_s + "-" + time_e;
		this.date = date;
		info = room + ", " + type;

		String tuday = Utils.sdf.format(new Date(System.currentTimeMillis()));
		String tomorrw = Utils.sdf.format(new Date(System.currentTimeMillis()
				+ 24 * 60 * 60 * 1000));

		Calendar mydate = new GregorianCalendar();

		try {
			Date thedate = Utils.sdf.parse(date);
			mydate.setTime(thedate);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		String[] days = context.getResources().getStringArray(R.array.days);
		String wt = days[mydate.get(Calendar.DAY_OF_WEEK) - 2];
		if (date.equalsIgnoreCase(tuday))
			wt = days[7]; // today
		if (date.equalsIgnoreCase(tomorrw))
			wt = days[8]; // tomorrow

		label = wt + ", " + date;
	}

	public Day(Context context, String id, String subject, String teacher,
			String room, String type, String form, String time_s, String time_e,
			String date, int img) {
		this(context, id, subject, room, type, time_s, time_e, date, img);
		this.teacher = teacher;
		this.form = form;
	}

	public String getLocale() {
		String locale;
		switch (info.charAt(0)) {
		case 'R':
			locale = "Rzeszóіw, ";
			break;
		case 'K':
			locale = "Kielnarowa, ";
			break;
		case 'T':
			locale = "Tyczyn, ";
			break;
		default:
			locale = "";
			break;
		}
		return locale + info;
	}
}
