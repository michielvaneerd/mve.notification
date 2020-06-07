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

function getRequestCode() {
	return parseInt($.requestCodeField.value, 10) || 1;
}

function onStart() {
	let arg = {
		extra: "Extra from the onStart function",
		title: $.titleField.value || "The title",
		content: $.contentField.value || "The content",
		date: $.dateField.value ? (moment().add(parseInt($.dateField.value, 10), "s").toDate()) : new Date(),
		icon: Ti.App.Android.R.drawable.ic_stat_ac_unit,
		requestCode: getRequestCode(),
		// Idee is: voeg actie buttons toe
		// met als pendingintent de service met dit als extra
		action1: {
			title: "Snooze",
			extra: "snooze"
		},
		action2: {
			title: "Cancel",
			extra: "cancel"
		},
		action3: {
			title: "Remove",
			extra: "remove"
		},
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
	notification.cancelScheduledNotification(getRequestCode());
}

function handleIntent(intent) {
	var extra = intent.getStringExtra("extra")
	var extra1 = intent.getStringExtra("action1");
	var extra2 = intent.getStringExtra("action2");
	var extra3 = intent.getStringExtra("action3");
	var action = intent.action;
	switch (action) {
		case "action1":
			alert("You clicked " + extra1);
			notification.cancelActiveNotification(getRequestCode());
			break;
		case "action2":
			alert("You clicked " + extra2);
			notification.cancelActiveNotification(getRequestCode());
			break;
		case "action3":
			alert("You clicked " + extra3);
			notification.cancelActiveNotification(getRequestCode());
			break;
	}
	console.log("action = " + action + ", extra = " + extra + ", extra1 = " + extra1 + ", extra2 = " + extra2 + ", extra3 = " + extra3);
}

Ti.Android.rootActivity.addEventListener('newintent', function (e) {
	handleIntent(e.intent);
});

function onWindowOpen() {
	handleIntent(Ti.Android.currentActivity.intent);
}

$.index.open();
