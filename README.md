Lablet: Physics Experiments on the Tablet
====

Lablet leverages tablets (and smart phones) as experimental tools to perform real physics experiments. Experiments can be conducted and analyzed entirely on the tablet. Lablet is designed for use in lab classes, but is not limited to such. Lablet can run fully customizable lab activities to guide students through an experiment.

Build
----
Lablet is developed using Intellij IDEA / Android Studio. If you want to build Lablet on your own best use one of these IDEs. Lablet uses Gradle as a build system. Please make sure that you installed the Android SDK and configured it correctly.

Dependencies
---
Lablet depends on OpenCV for Android to provide object tracking functionality. Assuming you have used git to clone Lablet:

1. Download and unzip [OpenCV for Android 2.4.11](http://sourceforge.net/projects/opencvlibrary/files/opencv-android/2.4.11/OpenCV-2.4.11-android-sdk.zip/download).
2. Copy the directory OpenCV-android-sdk-2.4.11/sdk/native/libs into Lablet/app/src/main
3. Rename the libs folder (that you just copied) to jniLibs

Links
----

* [Web Page](http://lablet.auckland.ac.nz/)
* [Google Play Store](https://play.google.com/store/apps/details?id=nz.ac.auckland.lablet)

