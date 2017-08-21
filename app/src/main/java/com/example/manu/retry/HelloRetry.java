package com.example.manu.retry;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class HelloRetry extends IntentService {

    public HelloRetry() {
        super("HelloRetry");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            try {

            } catch (Exception e) {
                // Restore interrupt status.
                //Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, startId, startId);

        return START_REDELIVER_INTENT;
    }
    void notification(int notifid){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.example_appwidget_preview)
                        .setContentTitle("Retry getting rid of me!")
                        .setContentText("Tired doing it?");
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
        mNotifyMgr.notify(notifid, mBuilder.build());
    }
}
