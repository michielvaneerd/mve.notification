# mve.notification

This Titanium mve.notification module makes it possible to schedule local notifications on Android.

## Getting Started

1. Require the module when needed.
2. Call `setChannel` once.
3. Call `scheduleNotification` for each notification you want to schedule.

## API

### setChannel

* Optional String `channelId` - Must be unique in your app.
* Optional String `channelName` - Displayed in the Android settings,
* Optional String `importance` - Possible values: `default`, `high`, `low`.
* Optional String `customSound` - Must be mp3 file in `app/platform/android/res/raw/myfile.mp3` and set with `Ti.Filesystem.getResRawDirectory() + "myfile"`. Note *without* extension!
* Optional Boolean `lights` - If lights should be on or off.
* Optional Boolean `vibrate` - If device should vibrate.
* Optional Boolean `sound` - If default sound should be played. Note that `customSound` overrides `sound`.

### scheduleNotification

* Required Int `requestCode` - ID of this notification.
* Required String `title`
* Required String `content`
* Required Date `date`
* Required Int `icon` - Place icon in the `app/platform/android/res/drawable[-xxx]` directories and call with `Ti.App.Android.R.drawable`. For example the file `app/platform/android/res/drawable/myicon.png` should be set with `Ti.App.Android.R.drawable.myicon`. Note *without* extension!
* Optional Boolean `lights` - See `setChannel`
* Optional Boolean `vibrate` - See `setChannel`
* Optional Boolean `sound` - See `setChannel`
* Optional String `customSound` - See `setChannel`
* Optional String `channelId` - If you set a `channelId` in `setChannel` you must use this here as well.
* Optional String `importance` - See `setChannel`
* Optional String `exact` - Possible values: `exact`, `inexact`, `repeat`. See below for more information.
* Optional String `extra` - Some extra data you want to send to the activity. See below for more information.
* Optional Int `repeatInSeconds` - If you want to have a repeating notification, you can specify the interval in seconds.
* Optional String `repeat` - Possible values: `hourly`, `daily`, `weekly`, `monthly`, `yearly`. Note that this will override `repeatInSeconds`.

### Installing

A step by step series of examples that tell you how to get a development env running

Say what the step will be

```
Give the example
```

And repeat

```
until finished
```

End with an example of getting some data out of the system or using it for a little demo

## Running the tests

Explain how to run the automated tests for this system

### Break down into end to end tests

Explain what these tests test and why

```
Give an example
```

### And coding style tests

Explain what these tests test and why

```
Give an example
```

## Deployment

Add additional notes about how to deploy this on a live system

## Built With

* [Dropwizard](http://www.dropwizard.io/1.0.2/docs/) - The web framework used
* [Maven](https://maven.apache.org/) - Dependency Management
* [ROME](https://rometools.github.io/rome/) - Used to generate RSS Feeds

## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags). 

## Authors

* **Billie Thompson** - *Initial work* - [PurpleBooth](https://github.com/PurpleBooth)

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Hat tip to anyone whose code was used
* Inspiration
* etc
