package mve.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.appcelerator.titanium.TiApplication
import java.util.*
import kotlin.math.ceil

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val info = MveNotificationModule.NotificationInfo()

        info.requestCode = intent.getIntExtra(MveNotificationModule.NOTIFICATION_REQUEST_CODE, 0)
        info.content = intent.getStringExtra(MveNotificationModule.NOTIFICATION_CONTENT)!!
        info.title = intent.getStringExtra(MveNotificationModule.NOTIFICATION_TITLE)!!
        // Icon must be white on transparent background
        info.icon = intent.getIntExtra(MveNotificationModule.NOTIFICATION_ICON, R.drawable.ic_stat_onesignal_default)
        info.exact = intent.getStringExtra(MveNotificationModule.NOTIFICATION_EXACT)!!
        info.repeatInSeconds = intent.getIntExtra(MveNotificationModule.NOTIFICATION_REPEAT_SEC, 0)
        info.date = intent.getSerializableExtra(MveNotificationModule.NOTIFICATION_DATE) as Date
        info.sound = intent.getBooleanExtra(MveNotificationModule.NOTIFICATION_SOUND, true)
        info.lights = intent.getBooleanExtra(MveNotificationModule.CHANNEL_LIGHTS, true)
        info.vibrate = intent.getBooleanExtra(MveNotificationModule.CHANNEL_VIBRATE, true)
        info.startActivityName = intent.getStringExtra(MveNotificationModule.NOTIFICATION_START_ACTIVITY_NAME)!!

        info.extra = intent.getStringExtra(MveNotificationModule.NOTIFICATION_EXTRA) ?: ""

        if (intent.hasExtra(MveNotificationModule.NOTIFICATION_REPEAT) && intent.getStringArrayExtra(MveNotificationModule.NOTIFICATION_REPEAT) != null) {
            info.repeat = intent.getStringExtra(MveNotificationModule.NOTIFICATION_REPEAT)!!
        }

        info.customSound = intent.getStringExtra(MveNotificationModule.CHANNEL_CUSTOM_SOUND) ?: ""

        if (intent.hasExtra(MveNotificationModule.CHANNEL_IMPORTANCE) && intent.getStringArrayExtra(MveNotificationModule.CHANNEL_IMPORTANCE) != null) {
            info.importance = intent.getStringExtra(MveNotificationModule.CHANNEL_IMPORTANCE)!!
        }

        if (intent.hasExtra(MveNotificationModule.CHANNEL_ID) && intent.getStringArrayExtra(MveNotificationModule.CHANNEL_ID) != null) {
            info.channelId = intent.getStringExtra(MveNotificationModule.CHANNEL_ID)!!
        }

        val launchIntent = Intent(TiApplication.getInstance().applicationContext, Class.forName("nl.peercode.testapp.PeercodetestappActivity"))
        //val launchIntent = Intent(TiApplication.getInstance().applicationContext, Class.forName(info.startActivityName))
        //val launchIntent = TiApplication.getInstance().applicationContext.packageManager
        //        .getLaunchIntentForPackage(TiApplication.getInstance().applicationContext.packageName)
        Utils.log("Create launchIntent for ${info.startActivityName}")

            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            launchIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            launchIntent.action = Intent.ACTION_MAIN
            if (info.extra != "") {
                launchIntent.putExtra(MveNotificationModule.NOTIFICATION_EXTRA, info.extra)
            }


        val pendingIntent: PendingIntent = PendingIntent.getActivity(TiApplication.getInstance().applicationContext, info.requestCode,
                launchIntent, PendingIntent.FLAG_UPDATE_CURRENT )

        // https://developer.android.com/training/notify-user/build-notification
        val builder = NotificationCompat.Builder(TiApplication.getInstance().applicationContext, info.channelId)
                .setSmallIcon(info.icon)
                .setContentTitle(info.title)
                .setContentText(info.content)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(info.content))

        // Note: for >= Build.VERSION_CODES.O these values are set on the channel instead
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.priority = when (info.importance) {
                MveNotificationModule.IMPORTANCE_HIGH -> NotificationCompat.PRIORITY_HIGH
                MveNotificationModule.IMPORTANCE_LOW -> NotificationCompat.PRIORITY_LOW
                else -> NotificationCompat.PRIORITY_DEFAULT
            }

            var defaults = 0

            // If defaults is set to DEFAULT_SOUND, then custom sound will be ignored
            // So first check for customSound
            if (info.customSound != "") {
                builder.setSound(Uri.parse(info.customSound))
            } else if (info.sound) {
                defaults = defaults or Notification.DEFAULT_SOUND
            }

            if (info.lights) {
                defaults = defaults or Notification.DEFAULT_LIGHTS
            }
            if (info.vibrate) {
                defaults = defaults or Notification.DEFAULT_VIBRATE
            }

            builder.setDefaults(defaults)

        }

        NotificationManagerCompat.from(TiApplication.getInstance()).notify(info.requestCode, builder.build())

        Utils.log("Notification displayed for #${info.requestCode}")

        if (info.repeatInSeconds > 0 && arrayOf(MveNotificationModule.EXACT, MveNotificationModule.INEXACT).contains(info.exact)) {

            val repeatInMs = info.repeatInSeconds * 1000;

            // TODO: if repeat is set to monthly or yearly, use Calendar and set()
            // https://stackoverflow.com/a/25784137/1294832

            // Make sure we schedule a date in the future, because maybe the device has been off for a long time
            val now = Date()

            // We always have a previous date when we are in the receiver so check for < now is not necessary
            // var x = if (info.date < now) ceil(((now.time - info.date.time) / repeatInMs).toDouble()).toInt() else 1

            // Add 1 because info.date was the previous date and we now have to schedule the next one
            val x = ceil(((now.time - info.date.time) / repeatInMs).toDouble()).toInt() + 1

            info.date = Date(info.date.time + (x * repeatInMs))

            Utils.log("Scheduling next exact repeating notification for ${info.date} for requestCode ${info.requestCode}")

            MveNotificationModule.schedule(info)

        }

    }


//    private fun getLaunchIntent(extra: String): Intent {
//
//        // We could also get a classname instead, but below seems to work.
//        val launchIntent = Intent(TiApplication.getInstance().applicationContext, Class.forName("nl.peercode.testapp.PeercodetestappActivity"))
//
//        // context.getPackageManager().getLaunchIntentForPackage(context.getApplicationContext().getPackageName());
//        //val launchIntent = TiApplication.getInstance().applicationContext.packageManager
//          //      .getLaunchIntentForPackage(TiApplication.getInstance().applicationContext.packageName)
//        if (launchIntent != null) {
//            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
//            launchIntent.addCategory(Intent.CATEGORY_LAUNCHER)
//            launchIntent.action = Intent.ACTION_MAIN
//            if (extra != "") {
//                launchIntent.putExtra(MveNotificationModule.NOTIFICATION_EXTRA, extra)
//            }
//        }
//        return launchIntent
//    }
}