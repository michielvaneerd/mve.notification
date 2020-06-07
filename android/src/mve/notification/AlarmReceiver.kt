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
import org.appcelerator.kroll.KrollDict
import org.appcelerator.titanium.TiApplication
import java.util.*
import kotlin.math.ceil

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val info = MveNotificationModule.NotificationInfo()

        // All fields are set on the intent
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
        info.repeat = intent.getStringExtra(MveNotificationModule.NOTIFICATION_REPEAT)!!
        info.customSound = intent.getStringExtra(MveNotificationModule.CHANNEL_CUSTOM_SOUND)!!
        info.extra = intent.getStringExtra(MveNotificationModule.NOTIFICATION_EXTRA) ?: ""
        info.importance = intent.getStringExtra(MveNotificationModule.CHANNEL_IMPORTANCE)!!
        info.channelId = intent.getStringExtra(MveNotificationModule.CHANNEL_ID)!!

        if (intent.hasExtra(MveNotificationModule.NOTIFICATION_ACTION1)) {
            @Suppress("UNCHECKED_CAST")
            info.action1 = intent.getSerializableExtra(MveNotificationModule.NOTIFICATION_ACTION1) as HashMap<String, Any>
        }
        if (intent.hasExtra(MveNotificationModule.NOTIFICATION_ACTION2)) {
            @Suppress("UNCHECKED_CAST")
            info.action2 = intent.getSerializableExtra(MveNotificationModule.NOTIFICATION_ACTION2) as HashMap<String, Any>
        }
        if (intent.hasExtra(MveNotificationModule.NOTIFICATION_ACTION3)) {
            @Suppress("UNCHECKED_CAST")
            info.action3 = intent.getSerializableExtra(MveNotificationModule.NOTIFICATION_ACTION3) as HashMap<String, Any>
        }

        //info.startActivityName = intent.getStringExtra(MveNotificationModule.NOTIFICATION_START_ACTIVITY_NAME)!!

        // https://developer.android.com/training/notify-user/build-notification
        val builder = NotificationCompat.Builder(TiApplication.getInstance().applicationContext, info.channelId)
                .setSmallIcon(info.icon)
                .setContentTitle(info.title)
                .setContentText(info.content)
                .setContentIntent(createPendingIntent(info.requestCode,
                        null, null, info.extra))
                .setAutoCancel(true)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(info.content))

        if (info.action1 != null) {
            builder.addAction(R.drawable.ic_stat_onesignal_default, info.action1!!["title"].toString(),
                    createPendingIntent(info.requestCode, MveNotificationModule.NOTIFICATION_ACTION1,
                            info.action1!!["extra"].toString(), info.extra))
        }
        if (info.action2 != null) {
            builder.addAction(R.drawable.btn_check_buttonless_on_144, info.action2!!["title"].toString(),
                    createPendingIntent(info.requestCode, MveNotificationModule.NOTIFICATION_ACTION2,
                            info.action2!!["extra"].toString(), info.extra))
        }
        if (info.action3 != null) {
            builder.addAction(R.drawable.btn_more_144, info.action3!!["title"].toString(),
                    createPendingIntent(info.requestCode, MveNotificationModule.NOTIFICATION_ACTION3,
                            info.action3!!["extra"].toString(), info.extra))
        }

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

            // Add 1 because info.date was the previous date and we now have to schedule the next one
            val x = ceil(((now.time - info.date.time) / repeatInMs).toDouble()).toInt() + 1

            info.date = Date(info.date.time + (x * repeatInMs))

            Utils.log("Scheduling next exact repeating notification for ${info.date} for requestCode ${info.requestCode}")

            MveNotificationModule.schedule(info)

        }

    }

    private fun createPendingIntent(requestCode: Int, extraKey: String?, extraValue: String?, extra: String?) : PendingIntent {

        val launchIntent = TiApplication.getInstance().applicationContext.packageManager
                .getLaunchIntentForPackage(TiApplication.getInstance().applicationContext.packageName)

        if (launchIntent != null) {
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            launchIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            launchIntent.action = Intent.ACTION_MAIN
            if (extra != null && extra != "") {
                launchIntent.putExtra(MveNotificationModule.NOTIFICATION_EXTRA, extra)
            }
            if (extraKey != null && extraValue != null && extraValue != "") {
                launchIntent.putExtra(extraKey, extraValue)
                launchIntent.action = extraKey // If we don't add this, the extra fields will be the same for all intents.
            }

        }
        return PendingIntent.getActivity(TiApplication.getInstance().applicationContext, requestCode,
                launchIntent!!, PendingIntent.FLAG_UPDATE_CURRENT) as PendingIntent
    }

}