Lablet: Physics Experiments on the Tablet
====

Lablet leverages tablets (and smart phones) as experimental tools to perform real physics experiments. Experiments can be conducted and analyzed entirely on the tablet. Lablet is designed for use in lab classes, but is not limited to such. Lablet can run fully customizable lab activities to guide students through an experiment.

Camera Experiment
----

Lablet enables students to use the tablet camera to perform an experiment. For example, students can record a video of the trajectory of a thrown ball. To analyze the trajectory, students can step through the video and tag the ball in each frame. Using this data, the velocity and the acceleration of the ball can be calculated and even the gravitational constant can be derived. Students learn the relation between kinetic and potential energy. Furthermore, they see how the movement of the ball differs in the x and y directions.
Lab Activities (“electronic” lab handouts)
Question sheet in a lab activity.

Question sheet within a lab activity.
---

Lablet supports “electronic” lab handouts with information and instructions to guide the students through a complete lab activity. Teachers can widely customize a lab activity by editing a simple lab activity script file. Here, a rich set of components and layout options are provided, for example, text questions, check boxes or special components such as a sheet where students learn to calculate velocity and acceleration from the raw data.


Build
----
Lablet is developed using Intellij IDEA / Android Studio. If you want to build Lablet on your own best use one of these IDEs since no other IDEs or build systems has been tested so far.

Dependencies
---
Lablet depends on OpenCV for Android to provide object tracking functionality. Assuming you have used git to clone Lablet:

1. Download and unzip [OpenCV for Android 2.4.11](http://sourceforge.net/projects/opencvlibrary/files/opencv-android/2.4.11/OpenCV-2.4.11-android-sdk.zip/download).
2. Copy the directory OpenCV-android-sdk-2.4.11/sdk/native/libs into Lablet/app/src/main
3. Rename the libs folder (that you just copied) to jniLibs
4. From Android Studio or Intellij IDEA, click: File -> New -> Import Module -> set the source directory to C:/path to OpenCV for android/sdk/java -> Next -> Finish
6. Right click on Lablet in the Project panel -> Open Module Settings -> Dependencies -> Add (plus) -> 3 Module Dependency -> select :openCVLibrary2411 -> OK -> OK

Links
----

* [Web Page](http://lablet.auckland.ac.nz/)
* [Google Play Store](https://play.google.com/store/apps/details?id=nz.ac.auckland.lablet)

