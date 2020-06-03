package nl.tinotification

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.appcelerator.titanium.TiApplication
import java.util.*
import kotlin.math.ceil


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val tiContext = TiApplication.getInstance().applicationContext

        val info = NlTinotificationModule.NotificationInfo()

        info.requestCode = intent.getIntExtra(NlTinotificationModule.NOTIFICATION_REQUEST_CODE, 0)
        info.content = intent.getStringExtra(NlTinotificationModule.NOTIFICATION_CONTENT)
        info.title = intent.getStringExtra(NlTinotificationModule.NOTIFICATION_TITLE)
        // Icon must be white on transparent background
        info.icon = intent.getIntExtra(NlTinotificationModule.NOTIFICATION_ICON, R.drawable.ic_stat_onesignal_default)
        info.exact = intent.getStringExtra(NlTinotificationModule.NOTIFICATION_EXACT)
        info.repeatInSeconds = intent.getIntExtra(NlTinotificationModule.NOTIFICATION_REPEAT_SEC, 0)
        info.date = intent.getSerializableExtra(NlTinotificationModule.NOTIFICATION_DATE) as Date
        info.sound = intent.getBooleanExtra(NlTinotificationModule.NOTIFICATION_SOUND, true)
        info.lights = intent.getBooleanExtra(NlTinotificationModule.CHANNEL_LIGHTS, true)
        info.vibrate = intent.getBooleanExtra(NlTinotificationModule.CHANNEL_VIBRATE, true)

        if (intent.hasExtra(NlTinotificationModule.NOTIFICATION_REPEAT)) {
            info.repeat = intent.getStringExtra(NlTinotificationModule.NOTIFICATION_REPEAT)
        }

        info.customSound = intent.getStringExtra(NlTinotificationModule.CHANNEL_CUSTOM_SOUND)
        info.importance = intent.getStringExtra(NlTinotificationModule.CHANNEL_IMPORTANCE)
        info.channelId = intent.getStringExtra(NlTinotificationModule.CHANNEL_ID)

        // Start or bring to front main activity:
        // https://stackoverflow.com/a/5502950/1294832
        val intent = getLaunchIntent()

        val pendingIntent: PendingIntent = PendingIntent.getActivity(tiContext, info.requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT )

        // https://developer.android.com/training/notify-user/build-notification
        val builder = NotificationCompat.Builder(tiContext, info.channelId)
                .setSmallIcon(info.icon)
                .setContentTitle(info.title)
                .setContentText(info.content)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(info.content))

        // Note: for >= Build.VERSION_CODES.O these values are set on the channel
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.priority = when (info.importance) {
                NlTinotificationModule.IMPORTANCE_HIGH -> NotificationCompat.PRIORITY_HIGH
                NlTinotificationModule.IMPORTANCE_LOW -> NotificationCompat.PRIORITY_LOW
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

        NotificationManagerCompat.from(tiContext).notify(info.requestCode, builder.build())

        Log.d(LCAT, "Notification displayed for #${info.requestCode}")

        if (info.repeatInSeconds > 0 && arrayOf(NlTinotificationModule.EXACT, NlTinotificationModule.INEXACT).contains(info.exact)) {

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

            NlTinotificationModule.schedule(info)

        }

    }


    private fun getLaunchIntent(): Intent {

        // Dit werkt niet als ik vanuit service de notifacties inplan...
        // Ik weet nog niet waarom.
        //val startActivity = Intent(TiApplication.getInstance().applicationContext, Class.forName("nl.peercode.testapp.PeercodetestappActivity"))

        val startActivity = TiApplication.getInstance().applicationContext.packageManager
                .getLaunchIntentForPackage(TiApplication.getInstance().applicationContext.packageName)
        if (startActivity != null) {
            startActivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity.addCategory(Intent.CATEGORY_LAUNCHER)
            startActivity.action = Intent.ACTION_MAIN
        }
        return startActivity!!
        //return startActivity
    }
}