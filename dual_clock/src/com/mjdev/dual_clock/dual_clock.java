package com.mjdev.dual_clock;

import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.RemoteViews;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.appwidget.AppWidgetProvider;

public final class dual_clock extends AppWidgetProvider {
	private static Intent serviceIntent = null;
	public  static String           TAG              = "MJDev-DualClock";
	public  static Context          context          = null;
	public  static AppWidgetManager appWidgetManager = null;
	public  static PowerManager     pm               = null;
	public  static RemoteViews      views            = null;
	public  static String           pkgName          = null;
	public  static int []           cknum            = new int[10];
	public  static weather          w                = null;
	public  static int []           		dtm              = new int [4]; 
	public  static int []           		dtmo             = new int [4];
	public  static String           		wtemp            = "";
	public  static String           		wcond            = "";
	public  static String           		wloc             = "";
	public  static Bitmap           		wicon            = null;
	public  static String           		wiconstr         = null;
	public  static boolean          disabled         = true;
	public  static void LOG(String msg) { Log.i("DC",msg); }
	@SuppressWarnings("static-access")
	@Override
	public void    onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		try {
			this.context          = context;
			this.appWidgetManager = appWidgetManager;
			pkgName          = context.getPackageName();
			views            = new RemoteViews(pkgName, R.layout.dual_clock_widget);
			for(int i=0; i<10; i++) cknum[i] = context.getResources().getIdentifier("dual_clock_number_"+i, "drawable", pkgName);
			pm               = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			w           = new weather(context);
			enable();
			if(serviceIntent == null) {
				ComponentName comp = new ComponentName(pkgName, dual_clock_service.class.getName());
				serviceIntent = new Intent();
				serviceIntent.setComponent(comp);
	        	context.startService(serviceIntent);
			}
		} catch (Exception e) { LOG(e.getMessage()); }
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
	@Override
	public void    onDeleted(Context context, int[] appWidgetIds) {
		try {
			w        = null;
			context.stopService(serviceIntent);
		} catch (Exception e) { LOG(e.getMessage()); }
		super.onDeleted(context, appWidgetIds); 
	}
	@Override
	public void    onReceive(Context context, Intent intent) {
		try {
			final String action = intent.getAction();
			if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
				final int appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
				if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) this.onDeleted(context, new int[] { appWidgetId });
			}
		} catch(Exception e){ }
		super.onReceive(context, intent);
	}
	public static void    repaint_time(boolean dirty) {
		try {
			String ampm = "";
			Date dt = new Date();
			int ptm = (int)(dt.getHours());
			if((w!=null)&&(w.localise.equals(Locale.US))) {
				ampm = "am";
				if(ptm>12) { ptm -= 12; ampm="pm"; }
			}
			dtm[0]  = (int)(ptm/10);
			dtm[1]  = (int)(ptm%10);
			dtm[2]  = (int)(dt.getMinutes()/10);
			dtm[3]  = (int)(dt.getMinutes()%10);
			if((!dirty)&&(dtmo[0]==dtm[0])&&(dtmo[1]==dtm[1])&&(dtmo[2]==dtm[2])&&(dtmo[3]==dtm[3])) return;
			if((context!=null)&&(views!=null)&&(appWidgetManager!=null)) {
				views.setImageViewResource(R.id.m_1,  cknum[dtm[3]]);
				views.setImageViewResource(R.id.m_10, cknum[dtm[2]]);
				views.setImageViewResource(R.id.h_1,  cknum[dtm[1]]);
				views.setImageViewResource(R.id.h_10, cknum[dtm[0]]);
				views.setTextViewText(R.id.ampm,ampm);
				appWidgetManager.updateAppWidget(new ComponentName(context, dual_clock.class), views);
			}
			for(int i=0; i<4; i++) dtmo[i]=dtm[i];
		} catch (Exception e) { LOG(e.getMessage()); }
	}
	public static void    repaint_loc(boolean dirty) {
		try {
			if((w!=null)&&(views!=null)&&((dirty)||(wloc==""))) 
			wloc = w.getLastAddress();
			if((context!=null)&&(appWidgetManager!=null)) {
				views.setTextViewText(R.id.location, wloc);
				views.setTextViewText(R.id.date, DateFormat.format("EEEE, dd MMMM yyyy", new Date()).toString());
				appWidgetManager.updateAppWidget(new ComponentName(context, dual_clock.class), views);
			}
		} catch (Exception e) { LOG(e.getMessage()); }
	}
	public static void    repaint_weather(boolean dirty) {
		try { 
			if((w!=null)&&(views!=null)&&(((dirty)||(wloc=="")))) {
				w.updateForecasts(); 
				if(w.wc!=null) {
					wtemp = w.getTemperature();
					wcond = w.wc.getCondition();
					if(wiconstr!=w.wc.getIconURL()) {
						wiconstr=w.wc.getIconURL();
						wicon = w.getRemoteImage(wiconstr);
					}
				}
				if((context!=null)&&(appWidgetManager!=null)) {
					views.setTextViewText(R.id.temperature, wtemp);
					views.setTextViewText(R.id.wconditions, wcond);
					if(wicon!=null) views.setImageViewBitmap(R.id.wicon, wicon);
					appWidgetManager.updateAppWidget(new ComponentName(context, dual_clock.class), views);
				}
			}
		} catch (Exception e) { LOG(e.getMessage()); }
	}
	public static void    full_repaint() {
		repaint_time(true);
		repaint_loc(true);
		repaint_weather(true);
	}
	public static void    onTick() {
		if(pm!=null) {
    		if(pm.isScreenOn()) enable(); else disable();
    	} else disable();
    	if(!disabled) repaint_time(false);
	}
	public static void    disable() { 
		if(!disabled) disabled = true; 
	}
    public static void    enable()  {
    	if(disabled) {
    		disabled = false; 
    		full_repaint();
    	} else repaint_time(false);
    }
    public static class dual_clock_service extends Service {
    	private Timer      timer = new Timer();
    	public void onCreate() {
    		timer.scheduleAtFixedRate( new TimerTask() { public void run() { onTick(); } }, 0, 1000);
    		super.onCreate();
    	}
    	@SuppressWarnings("unused")
		private void stop_service() { if (timer != null) timer.cancel(); }
    	@Override
    	public IBinder onBind(Intent intent) { return null; }
    }
}