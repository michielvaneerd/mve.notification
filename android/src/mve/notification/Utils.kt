package mve.notification

import android.util.Log

class Utils {

    companion object {
        fun log(message: String) {
            if (DBG) {
                Log.d(LCAT, message)
            }
        }
    }


}