/*
 * Copyright 2013 Thomas Hoffmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.j4velin.wifiAutoOff;

import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 
 * Utility class to set all necessary timers / start the background service
 * 
 */
public class Start {

	/**
	 * Sets all necessary timers / starts the background service depending on
	 * the user settings
	 * 
	 * @param c
	 *            the context
	 */
	@SuppressWarnings("deprecation")
	static void start(Context c) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		if (prefs.getBoolean("off_screen_off", true) || prefs.getBoolean("on_unlock", true)) {
			c.startService(new Intent(c, ScreenChangeDetector.class));
		} else {
			c.stopService(new Intent(c, ScreenChangeDetector.class));
		}

		AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());

		if (prefs.getBoolean("on_at", false)) {
			String[] time = prefs.getString("on_at_time", Receiver.ON_AT_TIME).split(":");

			cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time[0]));
			cal.set(Calendar.MINUTE, Integer.valueOf(time[1]));
			cal.set(Calendar.SECOND, 0);

			if (cal.getTimeInMillis() < System.currentTimeMillis())
				cal.add(Calendar.DAY_OF_MONTH, 1);

			am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), PendingIntent.getBroadcast(c, Receiver.TIMER_ON_AT,
					new Intent(c, Receiver.class).putExtra("changeWiFi", true).setAction("ON_AT"), 0));
			if (Logger.LOG)
				Logger.log("ON_AT alarm set at " + new Date(cal.getTimeInMillis()).toLocaleString());

		} else { // stop timer
			am.cancel(PendingIntent.getBroadcast(c, Receiver.TIMER_ON_AT,
					new Intent(c, Receiver.class).putExtra("changeWiFi", true).setAction("ON_AT"), 0));
		}
		if (prefs.getBoolean("off_at", false)) {
			String[] time = prefs.getString("off_at_time", Receiver.OFF_AT_TIME).split(":");

			cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time[0]));
			cal.set(Calendar.MINUTE, Integer.valueOf(time[1]));
			cal.set(Calendar.SECOND, 0);

			if (cal.getTimeInMillis() < System.currentTimeMillis())
				cal.add(Calendar.DAY_OF_MONTH, 1);

			am.set(AlarmManager.RTC_WAKEUP,
					cal.getTimeInMillis(),
					PendingIntent.getBroadcast(c, Receiver.TIMER_OFF_AT,
							new Intent(c, Receiver.class).putExtra("changeWiFi", false).setAction("OFF_AT"), 0));

			if (Logger.LOG)
				Logger.log("OFF_AT alarm set at " + new Date(cal.getTimeInMillis()).toLocaleString());
		} else { // stop timer
			am.cancel(PendingIntent.getBroadcast(c, Receiver.TIMER_OFF_AT,
					new Intent(c, Receiver.class).putExtra("changeWiFi", false).setAction("OFF_AT"), 0));
		}
		if (prefs.getBoolean("on_every", false)) {
			am.setInexactRepeating(
					AlarmManager.RTC_WAKEUP,
					System.currentTimeMillis(),
					AlarmManager.INTERVAL_HOUR * prefs.getInt("on_every_time", Receiver.ON_EVERY_TIME),
					PendingIntent.getBroadcast(c, Receiver.TIMER_ON_EVERY,
							new Intent(c, Receiver.class).putExtra("changeWiFi", true).setAction("ON_EVERY"), 0));
		} else { // stop timer
			am.cancel(PendingIntent.getBroadcast(c, Receiver.TIMER_ON_EVERY,
					new Intent(c, Receiver.class).putExtra("changeWiFi", true).setAction("ON_EVERY"), 0));
		}

		if (Logger.LOG)
			Logger.log("all timers set/cleared");
	}
}
