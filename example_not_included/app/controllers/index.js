const moment = require("/alloy/moment");
const notification = require("mve.notification");

// const channelId = "michiel";

const channelArg = {
	//channelId: channelId,
	//channelName: "Michieltje",
	//importance: "high",
	customSound: Ti.Filesystem.getResRawDirectory() + "got_it_done",
	lights: true,
	vibrate: false,
	//sound: Ti.Filesystem.getResRawDirectory() + "percussion_sound"
};
console.log(channelArg);
notification.setChannel(channelArg);

function onStart() {
	let arg = {
		extra: "Extra from the onStart function",
		title: $.titleField.value || "The title",
		content: $.contentField.value || "The content",
		date: $.dateField.value ? (moment().add(parseInt($.dateField.value, 10), "s").toDate()) : new Date(),
		icon: Ti.App.Android.R.drawable.ic_stat_ac_unit,
		requestCode: parseInt($.requestCodeField.value, 10) || 1
	};
	if ($.exactField.value) {
		arg.exact =  $.exactField.value;
	}
	if ($.repeatField.value) {
		arg.repeat = $.repeatField.value;
	}
	if ($.repeatInSecondsField.value) {
		arg.repeatInSeconds = parseInt($.repeatInSecondsField.value, 10);
	}
	console.log(arg);
	notification.scheduleNotification(arg);
}

function onCancel() {
	notification.cancel(parseInt($.requestCodeField.value, 10));
}

Ti.Android.rootActivity.addEventListener('newintent', function (e) {
	var data = e.intent.getStringExtra("extra");
    alert("newintent: " + data);
});

function onWindowOpen() {
	var extra = Ti.Android.currentActivity.intent.getStringExtra("extra");
	alert("open: " + extra)
}

$.index.open();
