package mve.notification

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import org.appcelerator.titanium.TiApplication
import java.lang.Exception

// https://ti-qa-archive.github.io/question/129747/how-to-launch-android-app-on-boot.html
// Note that this will fail when testing boot_completed broadcast event with adb (at least on an emulator)
// But on a real device with a real restart it works.
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        Utils.log("Boot Completed received")

        try {

            val info = TiApplication.getInstance().applicationContext.packageManager
                    .getReceiverInfo(ComponentName(TiApplication.getInstance().applicationContext,
                            "mve.notification.BootReceiver"), PackageManager.GET_META_DATA)

            if (info.metaData == null) {
                Utils.log("Meta data bundle for ${context.packageName} is null, exiting")
                return
            }

            var serviceName = info.metaData.getString("serviceName")
            if (serviceName != null) {

                if (serviceName.startsWith(".")) {
                    serviceName = TiApplication.getInstance().applicationContext.packageName + serviceName
                }
                Utils.log("Trying to start service $serviceName")

                //val serviceIntent: Intent = Intent(context, Class.forName(TiApplication.getInstance().appInfo.id + ".NotificationBootServiceService"))
                val serviceIntent: Intent = Intent(TiApplication.getInstance().applicationContext, Class.forName(serviceName))

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    TiApplication.getInstance().applicationContext.startForegroundService(serviceIntent);
                } else {
                    TiApplication.getInstance().applicationContext.startService(serviceIntent);
                }
            }

        } catch (e: ClassNotFoundException) {
            Utils.log("No NotificationBootService, nothing to do")
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }
}