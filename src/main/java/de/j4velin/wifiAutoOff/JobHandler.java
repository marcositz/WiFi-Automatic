package de.j4velin.wifiAutoOff;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;

/**
 * Job to call {@link Log#deleteOldLogs(Context, long)} every {@link Log#KEEP_DURATION} ms
 */
public class JobHandler extends JobService {

    final static int JOB_ID_DELETE_LOGS = 1;
    final static int JOB_ID_WIFI_AVAILABLE = 2;
    final static int JOB_ID_CHARGING = 3;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        if (BuildConfig.DEBUG) Logger.log("onStartJob " + jobParameters.getJobId());
        switch (jobParameters.getJobId()) {
            case JOB_ID_DELETE_LOGS:
                Log.deleteOldLogs(this, Log.KEEP_DURATION);
                return false;
            case JOB_ID_WIFI_AVAILABLE:
                sendBroadcast(
                        new Intent(this, Receiver.class).setAction(Receiver.WIFI_CHANGED_ACTION));
                return false;
            case JOB_ID_CHARGING:
                sendBroadcast(new Intent(this, Receiver.class)
                        .setAction(Receiver.POWER_CONNECTED_ACTION));
                return false;
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (BuildConfig.DEBUG) Logger.log("onStopJob " + jobParameters.getJobId());
        switch (jobParameters.getJobId()) {
            case JOB_ID_WIFI_AVAILABLE:
                sendBroadcast(
                        new Intent(this, Receiver.class).setAction(Receiver.WIFI_CHANGED_ACTION));
                return false;
            case JOB_ID_CHARGING:
                sendBroadcast(new Intent(this, Receiver.class)
                        .setAction(Receiver.POWER_DISCONNECTED_ACTION));
                return false;
        }
        return false;
    }
}
