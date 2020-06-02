package nl.tinotification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import org.appcelerator.titanium.TiApplication

// https://ti-qa-archive.github.io/question/129747/how-to-launch-android-app-on-boot.html

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        Log.d(LCAT, "NotificationBootService started")
        val serviceIntent: Intent = Intent(context, Class.forName(TiApplication.getInstance().appInfo.id + ".NotificationBootServiceService"))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }

    }
}