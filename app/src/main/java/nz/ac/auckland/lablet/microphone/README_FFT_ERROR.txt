If you have problems to build Lablet because of render script errors, make sure you have a file that points to your build tools:

sudo nano /etc/ld.so.conf.d/renderscript.conf
~/adt-bundle-linux-x86_64-20131030/sdk/build-tools/20.0.0

afterwards run:

sudo ldconfig