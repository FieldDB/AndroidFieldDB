[![Build Status][travis-image]][travis-url]

# AndroidFieldDB

An Android app which lets the user build a custom visual and auditory vocabulary, useful for guided anomia treatment and self designed language lessons by heritage speakers.

Plugs into FieldDB databases to create language learning apps.

[![Video shows how heritage speakers can use field methods techniques and a Learn X app to practice with their friends and family.](http://img.youtube.com/vi/nULRWUMUc-I/0.jpg)](https://www.youtube.com/watch?v=nULRWUMUc-I)


## Install

You can see what this codebase is for by installing this app (which was customized for Kartuli/Georgian heritage speakers).
https://play.google.com/store/apps/details?id=com.github.opensourcefieldlinguistics.fielddb.lessons.georgian


Tablet uses fragments side by side:
![learn_x_tablet](https://f.cloud.github.com/assets/196199/2483261/6c4e6442-b0fe-11e3-93df-e74309100571.png)

Phone uses list:
![learn_x_phone_list](https://f.cloud.github.com/assets/196199/2483266/7cb070b4-b0fe-11e3-9a42-de24f7e1be3f.png)

And a separate detail screen (here with speech recognition showing)
![learn_x_phone_speech_recognition](https://f.cloud.github.com/assets/196199/2483269/837d01f0-b0fe-11e3-8707-748ab9b02022.png)


## Android Library

As an Android developer, you can reuse this library in your app.

### Install

You can manually download the `.aar` and include it in your project from this url: https://bintray.com/fielddb/maven/com.github.fielddb

To install it in your project via maven, add the maven repository to your Android build.gradle:

```groovy
repositories {
    maven {
        url  "https://dl.bintray.com/fielddb/maven"
    }
}
```


### Development

Copy and optionally modify the sample private constants to customize the library:

```bash
$ sed 's/PrivateConstantsSample/PrivateConstants/' fielddb/src/main/java/com/github/fielddb/PrivateConstantsSample.java >  fielddb/src/main/java/com/github/fielddb/PrivateConstants.java
```


#### Tests

Copy the sample data to the device:

```bash
$ cd sample-data
$ adb push * /sdcard/
```

To run all the tests

```bash
./gradlew fielddb:connectedDebugAndroidTest
```

To run the tests click on the > next to the method you want to test.


### Release

To publish a new release of this library, edit the `version` in `fielddb/build.gradle' and set the ENV variables for `BINTRAY_USER` and `BINTRAY_API_KEY`

```bash
./gradlew tasks
./gradlew install
./gradlew clean
./gradlew build
./gradlew assembleRelease
ls -alt fielddb/build/outputs/aar/
./gradlew generateSourcesJar
./gradlew generateJavadocs
./gradlew generateJavadocsJar
./gradlew bintrayUpload
```


[travis-url]: https://travis-ci.org/FieldDB/AndroidFieldDB
[travis-image]: https://travis-ci.org/FieldDB/AndroidFieldDB.svg?branch=master
