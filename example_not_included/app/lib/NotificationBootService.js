// Make sure to first call foregroundNotify and then everything else!


var channel = Ti.Android.NotificationManager.createNotificationChannel({
    id: 'my_channel',
    name: 'TEST CHANNEL',
    importance: Ti.Android.IMPORTANCE_DEFAULT
}),
notification1 = Ti.Android.createNotification({
    icon: Ti.App.Android.R.drawable.ic_stat_ac_unit,
    contentTitle: 'TITLE',
    contentText : 'This is a test',
    channelId: channel.id
});

Ti.Android.currentService.foregroundNotify(1000000, notification1);

Titanium.API.info('*** Hello World, I am a Service 2 ***');

const notification = require("mve.notification");
const channelArg = {
	//channelId: channelId,
	//channelName: "Michieltje",
	//importance: "high",
	customSound: Ti.Filesystem.getResRawDirectory() + "got_it_done",
	lights: true,
	vibrate: false,
	//sound: Ti.Filesystem.getResRawDirectory() + "percussion_sound"
};
notification.setChannel(channelArg);

notification.scheduleNotification({
    title: "Title from boot",
    content: "Content from boot",
    date: new Date(Date.now() + 1000 * 10),
    requestCode: 110,
    icon: Ti.App.Android.R.drawable.ic_stat_beach_access,
    exact: "exact",
    extra: "Extra from the boot service!"
});

Titanium.API.info('*** Notified! ***');

//Ti.Android.stopService(serviceIntent);

var t = setTimeout(function() {
    clearTimeout(t);
    Titanium.API.info('*** Stop service! ***');
    Ti.Android.currentService.stop();
    //Ti.Android.stopService(serviceIntent);
}, 0);