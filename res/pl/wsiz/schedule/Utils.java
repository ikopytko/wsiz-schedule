package pl.wsiz.schedule;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class Utils {
	public final static String ID = "id";
	public final static String IMGKEY = "img";
	public final static String SUBJECT = "subject";
	public final static String TIME_S = "time_s";
	public final static String TIME_E = "time_e";
	public final static String ROOM = "room";
	public final static String FORM = "form";
	public final static String COURSE = "course";
	public final static String TEACHER = "teacher";
	public final static String TYPE = "type";
	public final static String EXAM = "examination";
	public final static String HOURS = "cl_per_sem";
	public final static String NAME = "name";
	public final static String GRADE = "grade";
	public final static String GROUPS = "groups";
	
	public final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	public static SharedPreferences getPrefs(Context context) {
		return context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
	}
	
	public static int getDrawableIcon(String s) {
		int f;
		if (s.equalsIgnoreCase("JD"))
			f = R.drawable.jd;
		else switch (s.toUpperCase().charAt(0)) {
		case 'W':
			f = R.drawable.w;
			break;
		case 'C':
			f = R.drawable.c;
			break;
		case 'L':
			f = R.drawable.l;
			break;
		case 'K':
			f = R.drawable.k;
			break;
		case 'J':
			f = R.drawable.j;
			break;
		case 'S':
			f = R.drawable.sp;
			break;
		case 'F':
			f = R.drawable.f;
			break;
		default:
			f = R.drawable.i;
		}
		return f;
	}
	
	/**
	 * Return the date of start and end of the week
	 * @param enterWeek Number of the week in the year
	 * @param enterYear year
	 * @return Return string in format "yyyy.mm.dd - yyyy.mm.dd"
	 */
	public static String getStartEndOFWeek(int enterWeek, int enterYear) {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(Calendar.WEEK_OF_YEAR, enterWeek);
		calendar.set(Calendar.YEAR, enterYear);

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd"); // PST`
		Date startDate = calendar.getTime();
		String startDateInStr = formatter.format(startDate);

		calendar.add(Calendar.DATE, 6);
		Date enddate = calendar.getTime();
		String endDaString = formatter.format(enddate);

		return startDateInStr + " - " + endDaString;
	}
	
	
	public static boolean hasInternetConnection(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
			return false;
		}
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		if (netInfo == null) {
			return false;
		}
		for (NetworkInfo ni : netInfo) {
			if (ni.getTypeName().equalsIgnoreCase("WIFI"))
				if (ni.isConnected()) {
					return true;
				}
			if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
				if (ni.isConnected()) {
					return true;
				}
		}
		return false;
	}
	
	public static String replaceSpec(String str) {
		str = str.replaceAll("№", "ą");
		str = str.replaceAll("к", "ę");
		str = str.replaceAll("Ј", "Ł");
		str = str.replaceAll("і", "ł");
		str = str.replaceAll("с", "ń");
		str = str.replaceAll("у", "ó");
		str = str.replaceAll("њ", "ś");
		str = str.replaceAll("џ", "ź");
		str = str.replaceAll("ї", "ż");
		return str;
	}
}
