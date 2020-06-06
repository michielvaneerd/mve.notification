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

            //val info = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            val info = TiApplication.getInstance().applicationContext.packageManager
                    .getReceiverInfo(ComponentName(TiApplication.getInstance().applicationContext,
                            "mve.notification.BootReceiver"), PackageManager.GET_META_DATA)
//            if (info == null) {
//                Utils.log("App info voor ${context.packageName} info is null!!!!")
//                return
//            } else {
//                Utils.log("Heb een app info!")
//            }

            if (info.metaData == null) {
                Utils.log("Meta data bundle voor ${context.packageName} info is null!!!!")
                return
            } else {
                Utils.log("Heb een data bundle meta data!")
            }
            var serviceName = info.metaData.getString("serviceName")
            if (serviceName != null) {

                Utils.log("Service name = $serviceName")

                if (serviceName.startsWith(".")) {
                    serviceName = TiApplication.getInstance().applicationContext.packageName + serviceName
                }
                Utils.log("Try to start service ${serviceName}")

                //val serviceIntent: Intent = Intent(context, Class.forName(TiApplication.getInstance().appInfo.id + ".NotificationBootServiceService"))
                val serviceIntent: Intent = Intent(TiApplication.getInstance().applicationContext, Class.forName(serviceName))

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    TiApplication.getInstance().applicationContext.startForegroundService(serviceIntent);
                } else {
                    TiApplication.getInstance().applicationContext.startService(serviceIntent);
                }
            } else {
                Utils.log("service name is null!")
            }



        } catch (e: ClassNotFoundException) {
            // User doesn't have this service set
            Utils.log("No NotificationBootService, so nothing to do")
        } catch (e: Exception) {
            // What happened?
            Utils.log("Some exception in trying to start boot service...")
            e.printStackTrace()
        }


    }
}