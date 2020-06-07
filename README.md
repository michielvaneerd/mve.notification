# mve.notification

This Titanium mve.notification module makes it possible to schedule local notifications on Android.

## Getting Started

1. Build this project or download the module from the `dist` directory.
2. Require the module.
3. Call `setChannel` once.
4. Call `scheduleNotification` for each notification you want to schedule.

## API

### setChannel(arg)

Notifications should be scheduled inside a channel. So first call this once before scheduling notifications.

* Optional String `channelId` - Must be unique inside your app.
* Optional String `channelName` - Human readable name.
* Optional String `importance` - Possible values: `default` (default), `high`, `low`.
* Optional String `customSound` - Must be mp3 file in `app/platform/android/res/raw/myfile.mp3` and set with `Ti.Filesystem.getResRawDirectory() + "myfile"`. Note *without* extension!
* Optional Boolean `lights` - If lights should be on or off. Default `true`.
* Optional Boolean `vibrate` - If device should vibrate. Default `true`.
* Optional Boolean `sound` - If default sound should be played. Note that `customSound` overrides `sound`. Default `true`.

### scheduleNotification(arg)

* Required Int `requestCode` - ID of notification.
* Required String `title` - Title of notification.
* Required String `content` - Content of notification.
* Required Date `date` - Date of notification.
* Required Int `icon` - Place icon in the `app/platform/android/res/drawable[-xxx]` directories and call with `Ti.App.Android.R.drawable`. For example the file `app/platform/android/res/drawable/myicon.png` should be set with `Ti.App.Android.R.drawable.myicon`. Note *without* extension!
* Optional Boolean `lights` - See `setChannel`
* Optional Boolean `vibrate` - See `setChannel`
* Optional Boolean `sound` - See `setChannel`
* Optional String `customSound` - See `setChannel`
* Optional String `channelId` - If you set a `channelId` in `setChannel` you must use this here as well.
* Optional String `importance` - See `setChannel`
* Optional String `exact` - Possible values: `exact` (default), `inexact`, `repeat`. See below for more information.
* Optional String `extra` - Some extra data you want to send to the activity. See below for more information.
* Optional Int `repeatInSeconds` - If you want to have a repeating notification, you can specify the interval in seconds. Default is `0` meaning no repeat.
* Optional String `repeat` - Possible values: `hourly`, `daily`, `weekly`, `monthly`, `yearly`. Note that this will override `repeatInSeconds`. Default is an empty string, meaning no repeat.
* Option Object `action1` - Action button 1. See below.
* Option Object `action2` - Action button 2. See below.
* Option Object `action3` - Action button 3. See below.

#### arg.action1, arg.action2 and arg.action3

You can add up to 3 action buttons by specifying the title and some extra data.

```
{
    title: "Snooze",
    extra: ""
}
```

See example for how to get this information inside your app.


#### arg.exact

* `exact` = `exact` - Notification is delivered exactly on time. This has more impact on the battery life than inexact notifications. Can be used for one time and repeated notifications.
* `exact` = `inexact` - Notification is batched up with other notifications. This preserves the battery life. Can be used for one time and repeated notifications.
* `exact` = `repeat` - Can only be used for repeated notifications. This preserves the battery life.

#### arg.extra

You can set the `extra` field to some string. This will be available in the Intent of your activity. See example.

### cancelScheduledNotification(requestCode)

Cancels scheduled notifications. Use the same `requestCode` as you used to schedule this notification.

* Required Int `requestCode` - ID of notification.

### cancelActiveNotification(requestCode)

Cancels active notification. Use the same `requestCode` as you used to schedule this notification.

* Required Int `requestCode` - ID of notification.

## Building this module

Clone this project. Go inside the android directory and do:

```
ti build -p android --build-only
```

## License

This project is licensed under the GNU GPLv3 License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

Heavily inspired by [benCoding.AlarmManager](https://github.com/benbahrenburg/benCoding.AlarmManager).