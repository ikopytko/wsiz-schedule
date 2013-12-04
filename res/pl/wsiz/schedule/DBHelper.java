package pl.wsiz.schedule;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	String name;
	public DBHelper(Context context, String db_name) {
		super(context, db_name, null, 1);
		name = db_name;
	}

	public void onCreate(SQLiteDatabase db) {
		if (name.equalsIgnoreCase("wsiz_sch"))
		{
		db.execSQL("create table timetable ("
				+ "id integer primary key autoincrement," + "subject text,"
				+ "teacher text," + "date text," + "time_s text,"
				+ "time_e text," + "room text," + "form text," + "type text,"
				+ "week text," + "mounth text" + ");");
		db.execSQL("create table grades ("
				+ "id integer primary key autoincrement," + "name text,"
				+ "type text," + "grade text," + "one text," + "two text,"
				+ "conditional text," + "advance text," + "commission text,"
				+ "deep text" + ");");
		db.execSQL("create table academic_plan ("
				+ "id integer primary key autoincrement," + "course text,"
				+ "teacher text," + "type text," + "cl_per_sem text,"
				+ "examination text," + "deep text" + ");");
		db.execSQL("create table modules ("
				+ "id integer primary key autoincrement," + "groups text,"
				+ "type text," + "deep text" + ");");
		} else {
			db.execSQL("create table timetable ("
					+ "id integer primary key autoincrement," + "date text,"
					+ "k1 integer," + "k2 integer," + "k3 integer," + "k4 integer," + "k5 integer," 
					+ "r1 integer," + "r2 integer," + "r3 integer" + ");");
		}
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
