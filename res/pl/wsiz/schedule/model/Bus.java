package pl.wsiz.schedule.model;

import android.util.Log;

public class Bus {
	public String date;
	public String[] k = new String[5];
	public String[] r = new String[3];
	
	public void log() {
		Log.i("BUS", k[0] + " " + k[1] + " " + k[2] + " " + k[3] + " " + k[4]);
	}
}
