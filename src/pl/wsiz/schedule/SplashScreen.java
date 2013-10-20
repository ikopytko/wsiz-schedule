package pl.wsiz.schedule;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

public class SplashScreen extends Activity {
	private boolean mIsBackButtonPressed;
	private int SPLASH_DURATION;
	private SharedPreferences spf;
	private SharedPreferences sp;
	private Intent intent;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
		
		spf = getSharedPreferences("AppPreferences", MODE_PRIVATE);
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		
		Locale locale = new Locale(sp.getString("entry_language", Locale.getDefault().getLanguage())); //Language code
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		SplashScreen.this.getResources().updateConfiguration(config, null);

		SPLASH_DURATION = Integer.parseInt(sp.getString("splash_screen_time", "3"))*1000;
		String usr_log_check = spf.getString(Prefs.USER_LOGIN, "");
		boolean db_check = spf.getBoolean(Prefs.DB_STATE, false);
		// if db not exist or login field is empty show login_page
		if (usr_log_check.length() == 0 || (db_check==false && usr_log_check.length() == 0))
			intent = new Intent(SplashScreen.this, Page_login.class);
		else intent = new Intent(SplashScreen.this, Page_menu.class);
     
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				finish();
				if (!mIsBackButtonPressed) {
					SplashScreen.this.startActivity(intent);
				}
			}
		}, SPLASH_DURATION);
	}

	@Override
	public void onBackPressed() {
		// set the flag to true so the next activity won't start up
		mIsBackButtonPressed = true;
		super.onBackPressed();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		spf = null;
		sp = null;
		intent = null;
	}
}
