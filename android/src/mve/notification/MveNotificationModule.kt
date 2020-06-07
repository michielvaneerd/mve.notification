// Inspired by and sometimes copied from https://github.com/benbahrenburg/benCoding.AlarmManager

package mve.notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import org.appcelerator.kroll.KrollDict
import org.appcelerator.kroll.KrollModule
import org.appcelerator.kroll.annotations.Kroll
import org.appcelerator.kroll.common.TiConfig
import org.appcelerator.titanium.TiApplication
import org.appcelerator.titanium.util.TiConvert
import java.util.*

const val LCAT = "MveNotification"
const val MY_CHANNEL_ID = "MveNotification"
const val MY_CHANNEL_NAME = "notifications"

// See https://docs.appcelerator.com/module-apidoc/latest/android/org/appcelerator/kroll/common/TiConfig.html
// Takes value from tiap.xml:
// <property name="ti.android.debug" type="bool">false</property>
val DBG = TiConfig.LOGD

@Kroll.module(name = "MveNotification", id = "mve.notification")
class MveNotificationModule : KrollModule() {

    companion object {

        const val NOTIFICATION_REQUEST_CODE = "requestCode"
        const val NOTIFICATION_TITLE = "title"
        const val NOTIFICATION_CONTENT = "content"
        const val NOTIFICATION_DATE = "date"
        const val NOTIFICATION_ICON = "icon"
        const val NOTIFICATION_REPEAT_SEC = "repeatInSeconds"
        const val NOTIFICATION_REPEAT = "repeat"
        const val NOTIFICATION_EXACT = "exact"
        const val NOTIFICATION_SOUND = "sound"
        const val NOTIFICATION_EXTRA = "extra"
        // https://github.com/benbahrenburg/benCoding.AlarmManager/issues/13
        // https://github.com/benbahrenburg/benCoding.AlarmManager/pull/59
        //const val NOTIFICATION_START_ACTIVITY_NAME = "startActivityName"

        const val NOTIFICATION_ACTION1 = "action1"
        const val NOTIFICATION_ACTION2 = "action2"
        const val NOTIFICATION_ACTION3 = "action3"

        const val CHANNEL_NAME = "channelName"
        const val CHANNEL_ID = "channelId"
        const val CHANNEL_CUSTOM_SOUND = "customSound"
        const val CHANNEL_IMPORTANCE = "importance"
        const val CHANNEL_LIGHTS = "lights"
        const val CHANNEL_VIBRATE = "vibrate"

        @Kroll.constant
        const val YEARLY_SECONDS = 31536000
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

        @Kroll.constant
        const val IMPORTANCE_DEFAULT = "default"
        @Kroll.constant
        const val IMPORTANCE_HIGH = "high"
        @Kroll.constant
        const val IMPORTANCE_LOW = "low"

        @JvmStatic
        @Kroll.onAppCreate
        fun onAppCreate(app: TiApplication) {

        }

        fun schedule(info: NotificationInfo) {

            val intent = Intent(TiApplication.getInstance().applicationContext, AlarmReceiver::class.java)
            intent.putExtra(NOTIFICATION_TITLE, info.title)
            intent.putExtra(NOTIFICATION_CONTENT, info.content)
            intent.putExtra(NOTIFICATION_ICON, info.icon)
            intent.putExtra(NOTIFICATION_REQUEST_CODE, info.requestCode)
            intent.putExtra(NOTIFICATION_EXACT, info.exact)
            intent.putExtra(NOTIFICATION_REPEAT_SEC, info.repeatInSeconds)
            intent.putExtra(NOTIFICATION_DATE, info.date)
            intent.putExtra(CHANNEL_ID, info.channelId)
            intent.putExtra(CHANNEL_CUSTOM_SOUND, info.customSound)
            intent.putExtra(CHANNEL_IMPORTANCE, info.importance)
            intent.putExtra(NOTIFICATION_SOUND, info.sound)
            intent.putExtra(CHANNEL_LIGHTS, info.lights)
            intent.putExtra(CHANNEL_VIBRATE, info.vibrate)
            intent.putExtra(NOTIFICATION_EXTRA, info.extra)
            //intent.putExtra(NOTIFICATION_START_ACTIVITY_NAME, info.startActivityName)
            intent.putExtra(NOTIFICATION_REPEAT, info.repeat)

            if (info.action1 != null) {
                intent.putExtra(NOTIFICATION_ACTION1, info.action1)
            }
            if (info.action2 != null) {
                intent.putExtra(NOTIFICATION_ACTION2, info.action2)
            }
            if (info.action3 != null) {
                intent.putExtra(NOTIFICATION_ACTION3, info.action3)
            }

            val pendingIntent = PendingIntent.getBroadcast(TiApplication.getInstance().applicationContext,
                    info.requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmManager: AlarmManager = TiApplication.getInstance().applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (info.repeatInSeconds > 0) {
                when (info.exact) {
                    REPEAT -> {
                        Utils.log("Scheduling inexact repeating notification for ${info.date} #${info.requestCode}")
                        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, info.date.time, (info.repeatInSeconds * 1000).toLong(), pendingIntent)
                    }
                    INEXACT -> {
                        Utils.log("Scheduling one inexact repeating notification for ${info.date} #${info.requestCode}")
                        alarmManager.set(AlarmManager.RTC_WAKEUP, info.date.time, pendingIntent)
                    }
                    else -> {
                        Utils.log("Scheduling one exact repeating notification for ${info.date} #${info.requestCode}")
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, info.date.time, pendingIntent)
                    }
                }
            } else {
                when (info.exact) {
                    INEXACT -> {
                        Utils.log("Scheduling one inexact notification for ${info.date} #${info.requestCode}")
                        alarmManager.set(AlarmManager.RTC_WAKEUP, info.date.time, pendingIntent)
                    }
                    else -> {
                        Utils.log("Scheduling one exact notification for ${info.date} #${info.requestCode}")
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, info.date.time, pendingIntent)
                    }
                }
            }

        }

    }

    class NotificationInfo {
        var repeatInSeconds: Int = 0
        var repeat: String = ""
        var title: String = ""
        var content: String = ""
        var date: Date = Date()
        var icon: Int = R.drawable.ic_stat_onesignal_default
        var exact: String = EXACT
        var requestCode: Int = 0
        var sound: Boolean = true
        var extra: String = ""
        //var startActivityName: String = ""

        var action1: HashMap<String, Any>? = null
        var action2: HashMap<String, Any>? = null
        var action3: HashMap<String, Any>? = null

        // Required for >= Build.VERSION_CODES.O
        var channelId: String = MY_CHANNEL_ID

        // Not active for >= Build.VERSION_CODES.O (these must be set on the channel instead)
        var customSound: String = ""
        var importance: String = IMPORTANCE_DEFAULT
        var lights: Boolean = true
        var vibrate: Boolean = true
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
    fun setChannel(arg: KrollDict) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val id = if (arg.containsKeyAndNotNull(CHANNEL_ID)) arg.getString(CHANNEL_ID) else MY_CHANNEL_ID
            val name = if (arg.containsKeyAndNotNull(CHANNEL_NAME)) arg.getString(CHANNEL_NAME) else MY_CHANNEL_NAME
            var importance = NotificationManager.IMPORTANCE_DEFAULT
            if (arg.containsKeyAndNotNull(CHANNEL_IMPORTANCE)) {
                importance = when (arg.getString(CHANNEL_IMPORTANCE)) {
                    IMPORTANCE_HIGH -> NotificationManager.IMPORTANCE_HIGH
                    IMPORTANCE_LOW -> NotificationManager.IMPORTANCE_LOW
                    else -> NotificationManager.IMPORTANCE_DEFAULT
                }
            }

            val channel = NotificationChannel(id, name, importance)

            // Note: setting a customSound overrides setting sound
            if (arg.containsKeyAndNotNull(CHANNEL_CUSTOM_SOUND)) {
                val customSound = arg.getString(CHANNEL_CUSTOM_SOUND)
                Utils.log("Setting custom sound to ${customSound}")
                channel.setSound(Uri.parse(customSound),
                        AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION).build())
            } else if (arg.containsKeyAndNotNull(NOTIFICATION_SOUND) && !arg.getBoolean(NOTIFICATION_SOUND)) {
                channel.setSound(null, null)
            }

            if (arg.containsKeyAndNotNull(CHANNEL_LIGHTS)) {
                channel.enableLights(arg.getBoolean(CHANNEL_LIGHTS))
            }

            if (arg.containsKeyAndNotNull(CHANNEL_VIBRATE)) {
                channel.enableVibration(arg.getBoolean(CHANNEL_VIBRATE))
            }

            val notificationManager: NotificationManager = TiApplication.getInstance().applicationContext
                    .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

        }
    }

    // https://stackoverflow.com/a/11682008/1294832
    // https://stackoverflow.com/a/61455067/1294832
    @Kroll.method
    fun cancelScheduledNotification(requestCode: Int) {
        val context = TiApplication.getInstance().applicationContext
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        Utils.log("Notification $requestCode cancelled")
    }

    @Kroll.method
    fun cancelActiveNotification(requestCode: Int) {
        NotificationManagerCompat.from(TiApplication.getInstance()).cancel(requestCode)
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
        //info.startActivityName = arg.getString(NOTIFICATION_START_ACTIVITY_NAME)

        if (arg.containsKeyAndNotNull(NOTIFICATION_ACTION1)) {
            info.action1 = arg.getKrollDict(NOTIFICATION_ACTION1) as HashMap<String, Any>
        }

        if (arg.containsKeyAndNotNull(NOTIFICATION_ACTION2)) {
            info.action2 = arg.getKrollDict(NOTIFICATION_ACTION2) as HashMap<String, Any>
        }

        if (arg.containsKeyAndNotNull(NOTIFICATION_ACTION3)) {
            info.action3 = arg.getKrollDict(NOTIFICATION_ACTION3) as HashMap<String, Any>
        }

        if (arg.containsKeyAndNotNull(CHANNEL_LIGHTS)) {
            info.lights = arg.getBoolean(CHANNEL_LIGHTS)
        }

        if (arg.containsKeyAndNotNull(CHANNEL_VIBRATE)) {
            info.vibrate = arg.getBoolean(CHANNEL_VIBRATE)
        }

        if (arg.containsKeyAndNotNull(NOTIFICATION_SOUND)) {
            info.sound = arg.getBoolean(NOTIFICATION_SOUND)
        }

        if (arg.containsKeyAndNotNull(CHANNEL_CUSTOM_SOUND)) {
            info.customSound = arg.getString(CHANNEL_CUSTOM_SOUND)
        }

        if (arg.containsKeyAndNotNull(CHANNEL_ID)) {
            info.channelId = arg.getString(CHANNEL_ID)
        }

        if (arg.containsKeyAndNotNull(CHANNEL_IMPORTANCE)) {
            info.importance = arg.getString(CHANNEL_IMPORTANCE)
        }

        if (arg.containsKeyAndNotNull(NOTIFICATION_EXACT)) {
            info.exact = arg.getString(NOTIFICATION_EXACT)
        }

        if (arg.containsKeyAndNotNull(NOTIFICATION_EXTRA)) {
            info.extra = arg.getString(NOTIFICATION_EXTRA)
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