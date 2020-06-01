package nl.peercode.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.appcelerator.titanium.TiApplication
import java.util.*
import java.lang.Math
import kotlin.math.ceil


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val info = NlPeercodeNotificationModule.NotificationInfo()

        info.requestCode = intent.getIntExtra(NlPeercodeNotificationModule.NOTIFICATION_REQUEST_CODE, 0)
        info.content = intent.getStringExtra(NlPeercodeNotificationModule.NOTIFICATION_CONTENT)
        info.title = intent.getStringExtra(NlPeercodeNotificationModule.NOTIFICATION_TITLE)
        // Icon must be white on transparent background
        info.icon = intent.getIntExtra(NlPeercodeNotificationModule.NOTIFICATION_ICON, R.drawable.ic_stat_onesignal_default)
        info.exact = intent.getBooleanExtra(NlPeercodeNotificationModule.NOTIFICATION_EXACT, false)
        info.repeatInSeconds = intent.getIntExtra(NlPeercodeNotificationModule.NOTIFICATION_REPEAT_SEC, 0)
        info.date = intent.getSerializableExtra(NlPeercodeNotificationModule.NOTIFICATION_DATE) as Date

        if (intent.hasExtra(NlPeercodeNotificationModule.NOTIFICATION_REPEAT)) {
            info.repeat = intent.getStringExtra(NlPeercodeNotificationModule.NOTIFICATION_REPEAT)
        }

        // Start or bring to front main activity:
        // https://stackoverflow.com/a/5502950/1294832
        val intent = getLaunchIntent()

        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, info.requestCode, intent, 0)

        // https://developer.android.com/training/notify-user/build-notification
        val builder = NotificationCompat.Builder(context, MY_CHANNEL_ID)
                .setSmallIcon(info.icon)
                .setContentTitle(info.title)
                .setContentText(info.content)
                //.setStyle(NotificationCompat.BigTextStyle()
                //        .bigText("Much longer text that cannot fit one line..."))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // For Android <= 7.1
                .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(info.requestCode, builder.build())
        Log.d(LCAT, "Notification displayed for requestCode ${info.requestCode}")

        if (info.repeatInSeconds > 0 && info.exact) {

            val repeatInMs = info.repeatInSeconds * 1000;

            // TODO: if repeat is set to monthly or yearly, use Calendar and set()
            // https://stackoverflow.com/a/25784137/1294832

            // Make sure we schedule a date in the future, because maybe the device has been off for a long time
            val now = Date()

            // We always have a previous date when we are in the receiver so check for < now is not necessary
            // var x = if (info.date < now) ceil(((now.time - info.date.time) / repeatInMs).toDouble()).toInt() else 1

            // Add 1 because info.date was the previous date and we now have to schedule the next one
            var x = ceil(((now.time - info.date.time) / repeatInMs).toDouble()).toInt() + 1

            info.date = Date(info.date.time + (x * repeatInMs))

            Log.d(LCAT, "Scheduling next exact repeating notification for ${info.date} for requestCode ${info.requestCode}")

            NlPeercodeNotificationModule.schedule(info)
        }
    }

    private fun getLaunchIntent(): Intent {
        val startActivity = TiApplication.getInstance().applicationContext.packageManager
                .getLaunchIntentForPackage(TiApplication.getInstance().applicationContext.packageName)
        if (startActivity != null) {
            startActivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity.addCategory(Intent.CATEGORY_LAUNCHER)
            startActivity.action = Intent.ACTION_MAIN
        }
        return startActivity!!
    }
}