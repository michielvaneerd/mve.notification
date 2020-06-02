package nl.tinotification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import org.appcelerator.kroll.KrollDict
import org.appcelerator.kroll.KrollModule
import org.appcelerator.kroll.annotations.Kroll
import org.appcelerator.kroll.common.Log
import org.appcelerator.kroll.common.TiConfig
import org.appcelerator.titanium.TiApplication
import org.appcelerator.titanium.util.TiConvert
import java.util.*

// https://github.com/hansemannn/titanium-qrcode/blob/master/android/src/ti/qrcode/TitaniumQrcodeModule.kt
// https://dev.to/miga/using-android-studio-to-develop-titanium-modules-2jc4
// https://jira.appcelerator.org/browse/TIMOB-25896 (Hierin een ZIP file met Kotlin module)

const val LCAT = "NlTinotification"
const val MY_CHANNEL_ID = "NlTinotification"
const val MY_CHANNEL_NAME = "notifications"

@Kroll.module(name = "NlTinotification", id = "nl.tinotification")
class NlTinotificationModule : KrollModule() {

    companion object {

        private val DBG = TiConfig.LOGD

        const val NOTIFICATION_REQUEST_CODE = "requestCode"
        const val NOTIFICATION_TITLE = "title"
        const val NOTIFICATION_CONTENT = "content"
        const val NOTIFICATION_DATE = "date"
        const val NOTIFICATION_ICON = "icon"
        const val NOTIFICATION_REPEAT_SEC = "repeatInSeconds"
        const val NOTIFICATION_REPEAT = "repeat"
        const val NOTIFICATION_EXACT = "exact"

        // TODO: yearly and monthly alarms should ALWAYS be one time alarms and in receiver schedule next one!
        @Kroll.constant
        const val YEARLY_SECONDS = 31536000
        // TODO: yearly and monthly alarms should ALWAYS be one time alarms and in receiver schedule next one!
        @Kroll.constant
        const val MONTHLY_SECONDS = 2628000
        @Kroll.constant
        const val WEEKLY_SECONDS = 604800
        @Kroll.constant
        const val DAILY_SECONDS = 86400
        @Kroll.constant
        const val HOURLY_SECONDS = 3600

        @Kroll.constant
        const val HOURLY = "hourly"
        @Kroll.constant
        const val DAILY = "daily"
        @Kroll.constant
        const val WEEKLY = "weekly"
        @Kroll.constant
        const val MONTHLY = "monthly"
        @Kroll.constant
        const val YEARLY = "yearly"

        @Kroll.constant
        const val INEXACT = "inexact"
        @Kroll.constant
        const val EXACT = "exact"
        @Kroll.constant
        const val REPEAT = "repeat"


        @JvmStatic
        @Kroll.onAppCreate
        fun onAppCreate(app: TiApplication) {
            // https://developer.android.com/training/notify-user/build-notification
            // Note:
            // Because you must create the notification channel before posting any notifications on Android 8.0 and higher,
            // you should execute this code as soon as your app starts.
            // It's safe to call this repeatedly because creating an existing notification channel performs no operation.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(MY_CHANNEL_ID, MY_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
                val notificationManager: NotificationManager = app
                        .applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

        fun schedule(info: NotificationInfo) {

            val context = TiApplication.getInstance().applicationContext

            val intent = Intent(context, AlarmReceiver::class.java)
            intent.putExtra(NOTIFICATION_TITLE, info.title)
            intent.putExtra(NOTIFICATION_CONTENT, info.content)
            intent.putExtra(NOTIFICATION_ICON, info.icon)
            intent.putExtra(NOTIFICATION_REQUEST_CODE, info.requestCode)
            intent.putExtra(NOTIFICATION_EXACT, info.exact)
            intent.putExtra(NOTIFICATION_REPEAT_SEC, info.repeatInSeconds)
            intent.putExtra(NOTIFICATION_DATE, info.date)

            if (info.repeat != "") {
                intent.putExtra(NOTIFICATION_REPEAT, info.repeat)
            }

            val pendingIntent = PendingIntent.getBroadcast(context, info.requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (info.repeatInSeconds > 0) {
                when (info.exact) {
                    REPEAT -> {
                        Log.d(LCAT, "Scheduling inexact repeating notification for ${info.date} #${info.requestCode}")
                        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, info.date.time, (info.repeatInSeconds * 1000).toLong(), pendingIntent)
                    }
                    INEXACT -> {
                        Log.d(LCAT, "Scheduling one inexact repeating notification for ${info.date} #${info.requestCode}")
                        alarmManager.set(AlarmManager.RTC_WAKEUP, info.date.time, pendingIntent)
                    }
                    else -> {
                        Log.d(LCAT, "Scheduling one exact repeating notification for ${info.date} #${info.requestCode}")
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, info.date.time, pendingIntent)
                    }
                }
            } else {
                when (info.exact) {
                    INEXACT -> {
                        Log.d(LCAT, "Scheduling one inexact notification for ${info.date} #${info.requestCode}")
                        alarmManager.set(AlarmManager.RTC_WAKEUP, info.date.time, pendingIntent)
                    }
                    else -> {
                        Log.d(LCAT, "Scheduling one exact notification for ${info.date} #${info.requestCode}")
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, info.date.time, pendingIntent)
                    }
                }
            }

        }
    }

    // HIer intent extras + krolldict values naar toe mappen en dan doorgeven aan schedile
    // TODO: repeat of repeat_ms (repeat is dan een string, zoals daily, weekly)
    // omdat je soms dailight saving time hebt, en dan klopt de ms variant niet!
    class NotificationInfo {
        var repeatInSeconds: Int = 0
        var repeat: String = ""
        var title: String = ""
        var content: String = ""
        var date: Date = Date()
        var icon: Int = R.drawable.ic_stat_onesignal_default
        var exact: String = EXACT
        var requestCode: Int = 0
    }

    // Om deze te cancellen moet je dezelfde pendingintent "terughalen", dwz dezelfde requestCode en deze FLAG.
    // Dus de intent die je als arg opstuurt hoeft niet...
    // https://stackoverflow.com/a/11682008/1294832
    // Ook goede uitleg over hoe systeem pendingintents checkt of ze gelijk zijn of niet.
    // https://stackoverflow.com/a/61455067/1294832
    // Dus zolang ik dezelfde requestcode meegeef, en dezelfde intent (excl extra_data) dan zou dat goed moeten zijn
    // Maar zie ook: https://developer.android.com/training/scheduling/alarms
    @Kroll.method
    fun cancel(requestCode: Int) {
        val context = TiApplication.getInstance().applicationContext
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        Log.d(LCAT, "Notification $requestCode cancelled")
    }

    private fun repeatToSeconds(repeat: String) : Int {
        return when (repeat) {
            HOURLY -> HOURLY_SECONDS
            DAILY -> DAILY_SECONDS
            WEEKLY -> WEEKLY_SECONDS
            MONTHLY -> MONTHLY_SECONDS
            YEARLY -> YEARLY_SECONDS
            else -> 0
        }
    }

    @Kroll.method
    fun scheduleNotification(arg: KrollDict) {

        val info = NotificationInfo()
        info.title = arg.getString(NOTIFICATION_TITLE)
        info.content = arg.getString(NOTIFICATION_CONTENT)
        info.date = TiConvert.toDate(arg.getValue(NOTIFICATION_DATE))
        // https://romannurik.github.io/AndroidAssetStudio/icons-notification
        info.icon = arg.getInt(NOTIFICATION_ICON)
        info.requestCode = arg.getInt(NOTIFICATION_REQUEST_CODE)

        if (arg.containsKeyAndNotNull(NOTIFICATION_EXACT)) {
            info.exact = arg.getString(NOTIFICATION_EXACT)
        }
        if (arg.containsKeyAndNotNull(NOTIFICATION_REPEAT_SEC)) {
            info.repeatInSeconds = arg.getInt(NOTIFICATION_REPEAT_SEC)
        }
        if (arg.containsKeyAndNotNull(NOTIFICATION_REPEAT)) {
            info.repeat = arg.getString(NOTIFICATION_REPEAT)
            // Note: if you specify both repeat and repeatInSeconds, then repeat will override repeatInSeconds!
            info.repeatInSeconds = repeatToSeconds(info.repeat)
        }

        schedule(info)

    }
}